#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <assert.h>
#include "config.h"
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"
#include "gc-data.h"
#include "precise_gc.h"
#ifdef WITH_THREADED_GC
#include "flexthread.h"
#endif

#define MAGIC_RATIO            14

static void *free;
static void *to_space;
static void *from_space;
static void *top_of_space;
static void *top_of_to_space;
/* end of locked variables */

#ifdef DEBUG_GC
static int num_gcs = 0;
static int num_mallocs = 0;
static size_t total_memory_requested = 0;
static jobject_unwrapped *special_ptr = (jobject_unwrapped *)0;
static jobject_unwrapped special;
#endif

#ifdef WITH_THREADED_GC
/* mutex for the GC variables */
FLEX_MUTEX_DECLARE_STATIC(copying_gc_mutex);

/* barriers for synchronizing threads */
extern pthread_barrier_t before;
extern pthread_barrier_t after;

extern pthread_cond_t done_cond;
extern pthread_mutex_t done_mutex;
extern int done_count;
extern jint halt_for_GC_flag; 

void halt_for_GC();

/* halt threads and acquire necessary locks */
void setup_for_threaded_GC();

/* release locks and allow threads to continue */
void cleanup_after_threaded_GC();
#endif /* WITH_THREADED_GC */

size_t copying_get_size_of_obj(jobject_unwrapped ptr_to_obj);

void relocate(jobject_unwrapped *obj);

void copying_handle_nonroot(jobject_unwrapped *nonroot) {
  if ((*nonroot) >= (jobject_unwrapped)from_space && 
      (*nonroot) <  (jobject_unwrapped)top_of_space) {
    if (((void *)((*nonroot)->claz) >= to_space) && 
	((void *)((*nonroot)->claz) < top_of_to_space)) {
      /* already moved, needs forwarding */
      error_gc(" already moved from %p to ", (*nonroot));
      (*nonroot) = (jobject_unwrapped)((*nonroot)->claz);
      error_gc("%p\n", (*nonroot));
    } else {
      /* needs moving */
      error_gc(" relocated from %p to ", (*nonroot));
      relocate(nonroot);
      error_gc("%p\n", (*nonroot));
    }
  } else {
    error_gc("!!!    tracing nonroot at %p\n", (*nonroot));
    //trace((*field));
  }
}

void relocate(jobject_unwrapped *obj) {
  void *forwarding_address;
  /* figure out how big the object is */
  size_t obj_size = copying_get_size_of_obj(*obj);
  /* relocated objects should not exceed size of heap */
  assert(free + obj_size <= top_of_to_space);
  /* copy over to to_space */
  forwarding_address = memcpy(free, (*obj), obj_size);
  /* write forwarding address to previous location;
     the order of the following two operations are critical */
  (*obj)->claz = /* not really */(struct claz *)forwarding_address;
  (*obj) = (jobject_unwrapped)forwarding_address;
  /* increment free */
  free += obj_size;
}


void copying_add_to_root_set(jobject_unwrapped *obj) {
  if ((*obj) >= (jobject_unwrapped)from_space && 
      (*obj) <  (jobject_unwrapped)top_of_space) {
    void *forwarding_address = (*obj)->claz;
    if(forwarding_address >= to_space && 
       forwarding_address < top_of_to_space) {
      /* relocated, needs forwarding */
      error_gc("    already moved from %p to ", (*obj));
      (*obj) = (jobject_unwrapped)forwarding_address;
      error_gc("%p\n", (*obj));
    } else {
      /* needs relocation */
      error_gc("    relocated from %p to ", (*obj));
      relocate(obj);
      error_gc("%p\n", (*obj));
    }
  } else {
    error_gc("    tracing object at %p\n", (*obj));
    trace(*obj);
  }
}


#ifdef WITH_PRECISE_C_BACKEND
void collect(int heap_expanded)
#else
void collect(void *saved_registers[], int heap_expanded)
#endif
{
  void *tmp, *scan;

  free = to_space;
  scan = free;

  error_gc("GC number %d\n", ++num_gcs);
  error_gc("New free space from %p to ", free);
  error_gc("%p\n\n", top_of_to_space);

#ifdef WITH_PRECISE_C_BACKEND
  find_root_set();
#else
  find_root_set(saved_registers);
#endif
  /* trace roots */
  while(scan < free) {
    assert(scan != NULL && ((jobject_unwrapped)scan)->claz != NULL);
    error_gc("Object at %p being scanned\n", scan);
    scan += trace((jobject_unwrapped)scan);
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
#ifdef DEBUG_GC
  {
    int *curr;

    if (special_ptr != (jobject_unwrapped *)0) {
      special = *special_ptr;
      special_ptr = (jobject_unwrapped *)0;
    }

    /* wipe out old memory */
    for(curr = (int *)to_space; curr < (int *)top_of_to_space; curr++)
      (*curr) = 0x77777777;
  }
#endif
}


#ifdef WITH_PRECISE_C_BACKEND
void *copying_malloc (size_t size_in_bytes)
#else
void *copying_malloc (size_t size_in_bytes, void *saved_registers[])
#endif
{
  static int initialized = 0; /* FALSE */
  static int fd;
  static size_t heap_size;
  size_t aligned_size_in_bytes;
  void *result;

#ifdef WITH_THREADED_GC
  /* only one thread can get memory at a time */
  while (pthread_mutex_trylock(&copying_gc_mutex))
    if (halt_for_GC_flag) halt_for_GC();
#endif
    
  if (!initialized) { /* allocate heap */
    heap_size = MAGIC_RATIO*size_in_bytes;
#ifdef DEBUG_GC
    //    heap_size = 2*0x848;
#endif
    fd = open("/dev/zero", O_RDONLY);
    from_space = mmap
      (0, heap_size, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
    assert(from_space != MAP_FAILED);
    error_gc("Initializing heap of size x%x\n", heap_size);
    free = from_space;
    to_space = from_space + heap_size/2;
    top_of_space = to_space;
    top_of_to_space = to_space + heap_size/2;
    initialized = 1; /* TRUE */
    error_gc("Free space from %p ", from_space);
    error_gc("to %p\n\n", top_of_space);
  }

  aligned_size_in_bytes = align(size_in_bytes);

#ifndef GC_EVERY_TIME
  if (free + aligned_size_in_bytes > top_of_space)
#endif
    {
      error_gc("\nx%x bytes needed ", aligned_size_in_bytes);
      error_gc("but only x%x bytes available\n", top_of_space - free);
#ifdef WITH_THREADED_GC
      setup_for_threaded_GC();
#endif /* WITH_THREADED_GC */
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
	error_gc("\nAvailable: %d ", top_of_space - free);
	error_gc("Need: %d\n", aligned_size_in_bytes);
	heap_size = 
	  ((MAGIC_RATIO*occupied) > (2*(occupied + aligned_size_in_bytes))) ?
	  (MAGIC_RATIO*occupied) : (4*(occupied + aligned_size_in_bytes));
	to_space = mmap
	  (0, heap_size, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
	assert(to_space != MAP_FAILED);
	error_gc("Expanding heap to size x%x\n", heap_size);
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
#ifdef WITH_THREADED_GC
      cleanup_after_threaded_GC();
#endif
    }
  
  result = free;
  free += aligned_size_in_bytes;

  error_gc("%d\t", ++num_mallocs);
  error_gc("Allocated x%x bytes ", aligned_size_in_bytes);
  error_gc("at %p (for a total of ", result);
  error_gc("x%x bytes)\n", (total_memory_requested += aligned_size_in_bytes));

#ifdef DEBUG_GC
  if (total_memory_requested == 0x418)
    special_ptr = (jobject_unwrapped *)result;
#endif

#ifdef WITH_THREADED_GC
  FLEX_MUTEX_UNLOCK(&copying_gc_mutex);
#endif
  return result;
}

/* copying_get_size_of_obj returns the size (in bytes) of
   the object to which the argument points. requires that
   the given object be in the currently occupied heap. */
size_t copying_get_size_of_obj(jobject_unwrapped ptr_to_obj)
{
  /* assert that the object is in the currently occupied heap */
  assert(ptr_to_obj >= (jobject_unwrapped)from_space && 
	 ptr_to_obj <  (jobject_unwrapped)top_of_space);
  if (ptr_to_obj->claz->component_claz == NULL) {
    /* non-array, simply returned aligned size */
    return align(ptr_to_obj->claz->size);
  } else {
    struct aarray *ptr_to_arr = (struct aarray *)(ptr_to_obj);
    ptroff_t containsPointers = ptr_to_arr->obj.claz->gc_info.bitmap;
    /* only two valid values: 0 or 1 */
    assert(containsPointers == 0 || containsPointers == 1);
    if (!containsPointers) {
      /* array of non-pointers */
      return aligned_size_of_np_array(ptr_to_arr);
    } else {
      /* array of pointers */
      return aligned_size_of_p_array(ptr_to_arr);
    }
  }
}

/* copying_free allows explicit freeing of memory allocated by the
   copying collector. note that you can only free memory to which
   there are no more pointers! */
void copying_free(void *ptr_to_memory_to_be_freed)
{
  /* to be written! */
}
