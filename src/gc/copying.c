#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <assert.h>
#include "config.h"
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"

#define HEAPSZ                16384 /* 16 MB */
#define COMPACT_ENCODING_SIZE (SIZEOF_VOID_P*SIZEOF_VOID_P*8)
#define HEADERSZ              3 /* size of array header */
#define ALIGN                 7
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

static int _continue = 1;

void *copying_malloc(size_t size_in_bytes)
{
  static int initialized = 0; /* FALSE */
  size_t aligned_size_in_bytes;
  void *result;
  if (!initialized) { /* allocate heap */
    int fd = open("/dev/zero", O_RDONLY);
    free = from_space = mmap
       (0, HEAPSZ, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
    top_of_space = to_space = from_space + HEAPSZ/2;
    initialized = 1; /* TRUE */
  }
  aligned_size_in_bytes = align(size_in_bytes);
  if (free + aligned_size_in_bytes > top_of_space)
    return NULL; /* need to garbage collect */
  result = free;
  free += aligned_size_in_bytes;
  report("Allocated: %p size: %d\n", result, aligned_size_in_bytes);
  return result;
}

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
    if (!containsPointers)
      /* array of non-pointers */
      obj_size = aligned_size_of_np_array(arr);
    else
      obj_size = aligned_size_of_p_array(arr);
  }
  /* copy over to to_space */
  /*
    assert(free + obj_size <= to_space + HEAPSZ/2); */
  if (free + obj_size > to_space + HEAPSZ/2) {
    _continue = 0;
    return;
  }
  forwarding_address = memcpy(free, (*obj), obj_size);
  report("Copied %p to %p. Size: %d\n", (*obj), forwarding_address, obj_size);
  /* write forwarding address to previous location */
  /* (*obj) = (**obj) = forwarding_address; /* order is important */
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
	      ((void *)((*field)->claz) < (to_space + HEAPSZ/2))) {
	    /* already moved, needs forwarding */
	    printf("FAIL: Can't happen.\n"); /* for testing only */
	    /* (*field) = (jobject_unwrapped)(**field); */
	  } else {
	    /* needs moving */
	    relocate(field);
	    if (!_continue) return align(obj_size);
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
	    ((void *)((*element)->claz) < (to_space + HEAPSZ/2))) {
	  /* already moved, needs forwarding */
	  printf("FAIL: Can't happen.\n");
	  /* (*element) = (jobject_unwrapped)(**element); */
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
      (*obj) <  (jobject_unwrapped)top_of_space)
    relocate(obj);
  else if ((*obj)->claz->component_claz == NULL)
    trace_object(*obj);
  else
    trace_array((struct aarray *)(*obj));
}

void *collect(void *saved_registers[]) {
  void *tmp = from_space;
  void *scan;
  void *old_free;

  report("\n");
  report("from_space: %p\n", from_space);
  report("free: %p\n", free);
  report("top_of_space: %p\n", top_of_space);
  report("to_space: %p\n", to_space);
  report("to_space + HEAPSZ/2: %p\n", to_space + HEAPSZ/2);

  old_free = free;
  scan = free = to_space;

  report("scan: %p\n", scan);
  report("free: %p\n", free);

  find_root_set(saved_registers);
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
    if (!_continue) {
      _continue = 1;
      scan = free;
      break;
    }
  }
  report("scan: %p free: %p\n", scan, free);
  assert(scan == free);
  /* swap from_space and to_space */
  /* don't do swap while testing
  free = from_space = to_space;
  to_space = tmp;
  top_of_space = from_space + HEAPSZ/2;
  */
  free = old_free; /* for testing only */
}







