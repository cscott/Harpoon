#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <assert.h>
#include "config.h"
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"

#define kDEBUG                 0
#define MAGIC_RATIO            14
#define COMPACT_ENCODING_SIZE (SIZEOF_VOID_P*SIZEOF_VOID_P*8)
#define HEADERSZ               3 /* size of array header */
#define ALIGN                  7
#define BITMASK               (~ALIGN)

#define align(_unaligned_size_) (((_unaligned_size_) + ALIGN) & BITMASK)
#define aligned_size_of_np_array(_np_arr_) \
(align((HEADERSZ * WORDSZ_IN_BYTES + \
	(_np_arr_)->length * (_np_arr_)->obj.claz->component_claz->size)))
#define aligned_size_of_p_array(_p_arr_) \
(align((HEADERSZ + (_p_arr_)->length) * WORDSZ_IN_BYTES))

static void *free;
static void *to_space;
static void *from_space;
static void *top_of_space;
static void *top_of_to_space;

void relocate(jobject_unwrapped *obj) {
  void *forwarding_address;
  size_t obj_size;
  assert((*obj) >= (jobject_unwrapped)from_space && 
	 (*obj) <  (jobject_unwrapped)top_of_space);
  if ((*obj)->claz->component_claz == NULL) {
    /* non-array */
    obj_size = align((*obj)->claz->size);
  } else {
    struct aarray *arr = (struct aarray *)(*obj);
    ptroff_t containsPointers = arr->obj.claz->gc_info.bitmap;
    assert(containsPointers == 0 || containsPointers == 1);
    if (!containsPointers) {
      /* array of non-pointers */
      obj_size = aligned_size_of_np_array(arr);
    } else {
      obj_size = aligned_size_of_p_array(arr);
    }
  }
  /* copy over to to_space */
  assert(free + obj_size <= top_of_to_space);

  forwarding_address = memcpy(free, (*obj), obj_size);
  /*  printf("%p to %p. Size: %d ", (*obj), forwarding_address, obj_size);
  printf("claz: %p\n", ((jobject_unwrapped)forwarding_address)->claz);
  fflush(0); */
  /* write forwarding address to previous location;
     the order of the following two operations are critical */
  (*obj)->claz = /* not really */(struct claz *)forwarding_address;
  (*obj) = (jobject_unwrapped)forwarding_address;
  /* increment free */
  free += obj_size;
}

size_t trace_object(jobject_unwrapped obj) {
  size_t obj_size = obj->claz->size;
  ptroff_t bitmap;
  ptroff_t mask = 0;
  int bitmap_position = 0;
  int num_bitmaps =
    (obj_size + COMPACT_ENCODING_SIZE - 1)/COMPACT_ENCODING_SIZE;
  assert(num_bitmaps > 0);
  if (num_bitmaps > 1)
    bitmap = *(obj->claz->gc_info.ptr);
  else
    bitmap = obj->claz->gc_info.bitmap;  
  while(num_bitmaps > 0) { /* continue until done w/ all masks */
    while(bitmap & (~mask)) { /* continue until done w/ this mask */
      /* update mask */
      mask |= 1 << bitmap_position;
      /* check if the current bit is set */
      if (bitmap & (1 << bitmap_position)) {
	/* field is pointer */
	jobject_unwrapped *field = (jobject_unwrapped *)obj + bitmap_position;
	if ((*field) != NULL &&
	    (*field) >= (jobject_unwrapped)from_space && 
	    (*field) <  (jobject_unwrapped)top_of_space) {
	  if (((void *)((*field)->claz) >= to_space) && 
	      ((void *)((*field)->claz) < top_of_to_space)) {
	    /* already moved, needs forwarding */
	    /* printf("FAIL: Can't happen.\n"); /* for testing only */
	    (*field) = (jobject_unwrapped)((*field)->claz);
	  } else {
	    /* needs moving */
	    relocate(field);
	  }
	}
      } /* current bit is set */
      bitmap_position++;
    }
    bitmap = *((&bitmap)+1); /* go to next bitmap */
    num_bitmaps--;
    mask = 0;  
  }
  return align(obj_size); /* done */
}

size_t trace_array(struct aarray *arr) {
  ptroff_t containsPointers = arr->obj.claz->gc_info.bitmap;
  assert(containsPointers == 0 || containsPointers == 1);
  if (!containsPointers)
    /* array of non-pointers */
    return aligned_size_of_np_array(arr);
  { /* contains pointers */
    size_t arr_length = arr->length;
    size_t element_size = align(arr->obj.claz->size);
    jobject_unwrapped *element = ((jobject_unwrapped *)arr)+HEADERSZ;
    while(arr_length > 0) { /* continue until done w/ entire array */
      if ((*element) != NULL &&
	  (jobject_unwrapped)(*element) >= (jobject_unwrapped)from_space && 
	  (jobject_unwrapped)(*element) <  (jobject_unwrapped)top_of_space) {
	/* needs moving/forwarding */
	if (((void *)((*element)->claz) >= to_space) &&
	    ((void *)((*element)->claz) < top_of_to_space)) {
	  /* already moved, needs forwarding */
	  /* printf("FAIL: Can't happen.\n"); */
	  (*element) = (jobject_unwrapped)((*element)->claz);
	} else {
	  /* needs moving */
	  relocate(element);
	}
      }
      arr_length--;
      element++;
    }
    return aligned_size_of_p_array(arr); /* done */
  }
}

void add_to_root_set(jobject_unwrapped *obj) {
  if ((*obj) >= (jobject_unwrapped)from_space && 
      (*obj) <  (jobject_unwrapped)top_of_space) {
    void *forwarding_address = (*obj)->claz;
    if(forwarding_address >= to_space && 
       forwarding_address < top_of_to_space) {
      /* relocated, needs forwarding */
      if (kDEBUG) printf("    already moved from %p to ", (*obj));
      (*obj) = (jobject_unwrapped)forwarding_address;
    } else {
      /* needs relocation */
      if (kDEBUG) printf("    relocated from %p to ", (*obj));
      relocate(obj);
    }
  } else if ((*obj)->claz->component_claz == NULL) {
    if (kDEBUG) printf("    tracing object at %p = ", (*obj));
    trace_object(*obj);
  } else {
    if (kDEBUG) printf("    tracing object at %p = ", (*obj));
    trace_array((struct aarray *)(*obj));
  }
}

#ifdef WITH_PRECISE_C_BACKEND
void *collect(int heap_expanded)
#else
void *collect(void *saved_registers[], int heap_expanded)
#endif
{
  void *tmp, *scan;
  
  if (kDEBUG) {
    printf("\n");
    printf("from_space: %p\n", from_space);
    printf("free: %p\n", free);
    printf("top_of_space: %p\n", top_of_space);
    printf("to_space: %p\n", to_space);
    printf("top_of_to_space: %p\n", top_of_to_space);
    fflush(0);
  }

  free = to_space;
  scan = free;

  if (kDEBUG) {
    printf("scan: %p\n", scan);
    printf("free: %p\n", free);
    fflush(0);
  }

#ifdef WITH_PRECISE_C_BACKEND
  find_root_set();
#else
  find_root_set(saved_registers);
#endif
  if (kDEBUG) { printf("Found roots.\n"); fflush(0); }
  /* trace roots */
  while(scan < free) {
    assert(scan != NULL && ((jobject_unwrapped)scan)->claz != NULL);
    if (((jobject_unwrapped)scan)->claz->component_claz == NULL) {
      report("object: scan: %p free: %p\n", scan, free); fflush(NULL);
      scan += trace_object((jobject_unwrapped)scan); /* object */
    } else {
      report("array*: scan: %p free: %p\n", scan, free); fflush(NULL);
      scan += trace_array((struct aarray *)scan); /* array */
    }
  }
  assert(scan == free);
  if (heap_expanded) {
    from_space = to_space;
    to_space = top_of_to_space;
    top_of_space = top_of_to_space;
    top_of_to_space = to_space + (top_of_space - from_space);
  } else {
    /* swap from_space and to_space */
    tmp = from_space;
    from_space = to_space;
    to_space = tmp;
    /* swap top_of_space and top_of_to_space */
    tmp = top_of_space;
    top_of_space = top_of_to_space;
    top_of_to_space = tmp;
  }
}

void *copying_malloc(size_t size_in_bytes)
{
  static int initialized = 0; /* FALSE */
  static int fd;
  static size_t heap_size;
  size_t aligned_size_in_bytes;
  void *result;
  if (!initialized) { /* allocate heap */
    heap_size = MAGIC_RATIO*size_in_bytes;
    fd = open("/dev/zero", O_RDONLY);
    from_space = mmap
      (0, heap_size, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
    assert(from_space != MAP_FAILED);
    if (kDEBUG) printf("heap size = %d\n", heap_size);
    free = from_space;
    to_space = from_space + heap_size/2;
    top_of_space = to_space;
    top_of_to_space = to_space + heap_size/2;
    initialized = 1; /* TRUE */
  }
  aligned_size_in_bytes = align(size_in_bytes);
  if (free + aligned_size_in_bytes > top_of_space) {
#ifdef WITH_PRECISE_C_BACKEND
    collect(/* heap not expanded */0);
#else
    collect(saved_registers, /* heap not expanded */0);
#endif
    if (free + aligned_size_in_bytes > top_of_space) {
      /* expand heap */
      int result;
      void *to_free = (to_space < from_space) ? to_space : from_space;
      size_t occupied = free - from_space;
      size_t old_heap_size = heap_size;
      heap_size = 
	((MAGIC_RATIO*occupied) > (2*(occupied + aligned_size_in_bytes))) ?
	(MAGIC_RATIO*occupied) : (4*(occupied + aligned_size_in_bytes));
      to_space = mmap
	(0, heap_size, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
      assert(to_space != MAP_FAILED);
      if (kDEBUG) printf("heap size = %d\n", heap_size);
      top_of_to_space = to_space + heap_size/2;
#ifdef WITH_PRECISE_C_BACKEND
      collect(/* heap expanded */1);
#else
      collect(saved_registers, /* heap expanded */1);
#endif
      result = munmap(to_free, old_heap_size);
      assert(result == 0); /* success */
      assert(free + aligned_size_in_bytes < top_of_space);
    }
  }
  result = free;
  free += aligned_size_in_bytes;
  if (kDEBUG) 
    printf("Allocated: %p size: %d\n", result, aligned_size_in_bytes);
  return result;
}







