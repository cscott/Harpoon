#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <assert.h>
#include "config.h"
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"
#include "gc-data.h"
#ifdef WITH_THREADED_GC
#include "flexthread.h"
#include "jni-gcthreads.h"
#endif

//#define GC_EVERY_TIME
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
/* end of locked variables */

#ifdef DEBUG_GC
static int num_gcs = 0;
static int num_mallocs = 0;
static size_t total_memory_requested = 0;
static jobject_unwrapped *special_ptr = (jobject_unwrapped *)0;
static jobject_unwrapped special;
#endif

size_t trace_array(struct aarray *arr);
size_t trace_object(jobject_unwrapped obj);

/* x: jobject_unwrapped */
#define trace(x) ({ ((x)->claz->component_claz == NULL) ? \
                    trace_object(x) : trace_array((struct aarray *)(x)); })

#ifdef WITH_THREADED_GC

/* mutex for the GC variables */
FLEX_MUTEX_DECLARE_STATIC(gc_mutex);

/* barriers for synchronizing threads */
static pthread_barrier_t before;
static pthread_barrier_t after;

static pthread_cond_t done_cond = PTHREAD_COND_INITIALIZER;
static pthread_mutex_t done_mutex = PTHREAD_MUTEX_INITIALIZER;
static int done_count;

jint halt_for_GC_flag = 0;
void halt_for_GC() {
  error_gc("Thread %p ready for GC\n", FNI_GetJNIEnv());
  // ready for GC
  pthread_barrier_wait(&before);
  // wait for GC to finish
  pthread_barrier_wait(&after);
  // let GC know that we're done
  pthread_mutex_lock(&done_mutex);
  done_count++;
  pthread_cond_signal(&done_cond);
  pthread_mutex_unlock(&done_mutex);
}
#endif /* WITH_THREADED_GC */

void precise_gc_init() {
#ifdef WITH_HEAVY_THREADS
    error_gc("Heavy threads\n", "");
#endif
#ifdef WITH_PTH_THREADS
    error_gc("Pth threads\n", "");
#endif
#ifdef WITH_USER_THREADS
    error_gc("User threads\n", "");
#endif
#ifdef WITH_THREADED_GC
  gc_data_init();
#endif
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
	if ((*field) != NULL) {
	  if ((*field) >= (jobject_unwrapped)from_space && 
	      (*field) <  (jobject_unwrapped)top_of_space) {
	    if (((void *)((*field)->claz) >= to_space) && 
		((void *)((*field)->claz) < top_of_to_space)) {
	      /* already moved, needs forwarding */
	      error_gc("    field already moved from %p to ", (*field));
	      (*field) = (jobject_unwrapped)((*field)->claz);
	      error_gc("%p\n", (*field));
	    } else {
	      /* needs moving */
	      error_gc("    field relocated from %p to ", (*field));
	      relocate(field);
	      error_gc("%p\n", (*field));
	    }
	  } else {
	    error_gc("!!!    tracing field at %p\n", (*field));
	    //trace((*field));
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
      if ((*element) != NULL) {
	if ((jobject_unwrapped)(*element) >= (jobject_unwrapped)from_space && 
	    (jobject_unwrapped)(*element) <  (jobject_unwrapped)top_of_space) {
	  /* needs moving/forwarding */
	  if (((void *)((*element)->claz) >= to_space) &&
	      ((void *)((*element)->claz) < top_of_to_space)) {
	    /* already moved, needs forwarding */
	    error_gc("    array element already moved from %p to ", (*element));
	    (*element) = (jobject_unwrapped)((*element)->claz);
	    error_gc("%p\n", (*element));
	  } else {
	    /* needs moving */
	    error_gc("    array element relocated from %p to ", (*element));
	    relocate(element);
	    error_gc("%p\n", (*element));
	  }
	} else {
	  error_gc("!!!    tracing field at %p\n", (*element));
	  //trace(*element);
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
  while (pthread_mutex_trylock(&gc_mutex))
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
#ifdef WITH_THREADED_GC
      struct thread_list *nlist;
      /* get count of the number of threads */
      pthread_mutex_lock(&gc_thread_mutex);
      pthread_mutex_lock(&running_threads_mutex);
      error_gc("%d threads are running.\n", num_running_threads);
      if (num_running_threads > 1) {
	pthread_barrier_init(&before, 0, num_running_threads);
	pthread_barrier_init(&after, 0, num_running_threads);
	halt_for_GC_flag = 1;
	// grab done_mutex before any thread gets past the first barruer
	pthread_mutex_lock(&done_mutex);
	pthread_barrier_wait(&before);
	halt_for_GC_flag = 0;
      }
#endif /* WITH_THREADED_GC */
      error_gc("\nx%x bytes needed ", aligned_size_in_bytes);
      error_gc("but only x%x bytes available\n", top_of_space - free);
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
      if (num_running_threads > 1) {
	pthread_barrier_wait(&after);
	// make sure all threads have restarted before continuing
	done_count = 1;
	while(done_count < num_running_threads)
	  pthread_cond_wait(&done_cond, &done_mutex);;
	pthread_mutex_unlock(&done_mutex);
      }
      pthread_mutex_unlock(&gc_thread_mutex);
      pthread_mutex_unlock(&running_threads_mutex);
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
  FLEX_MUTEX_UNLOCK(&gc_mutex);
#endif
  return result;
}
