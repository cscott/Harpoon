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

/* data structures for free list */
struct free_block {
  size_t size_of_block;
  struct free_block *next;
  /* rest of free memory here */
};

/* variables locked by copying_gc_mutex */
static void *free;
static void *to_space;
static void *from_space;
static void *top_of_space;
static void *top_of_to_space;
/* file descriptor for mmap */
static int fd;
/* current size of heap */
static size_t heap_size;
/* end of locked variables */

/* convenience macros */
#define IN_HEAP(void_p) ((void_p) >= from_space && (void_p) < top_of_space)

#ifdef DEBUG_GC
static int num_gcs = 0;
static int num_mallocs = 0;
static size_t total_memory_requested = 0;
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
#endif /* WITH_THREADED_GC */

#ifdef WITH_PRECISE_C_BACKEND
void collect(int expand_amt);
#else
void collect(void *saved_registers[], int expand_amt);
#endif

size_t copying_get_size_of_obj(jobject_unwrapped ptr_to_obj);

void overwrite_to_space();

void relocate(jobject_unwrapped *obj);

/* copying_handle_references handles refereneces to objects. objects in the
   heap that need to be copied to the new semispace are copied. if the 
   object has already been copied, the pointer is updated. the second
   argument tells the function whether the given reference is a part of
   the root set. if not, references outside the heap are ignored, since
   tracing them can put the GC in an infinite loop. if so, even references
   outside the heap are traced. */
void copying_handle_reference(jobject_unwrapped *ref)
{
  // we only have to do something if we are given a reference in the heap.
  // we cannot trace through non-root references outside the heap because 
  // we may end up in an undetectable infinite loop.
  if (IN_HEAP((void *)(*ref)))
    {
      // handle objects that have already been moved
      if (((void *)((*ref)->claz) >= to_space) && 
	  ((void *)((*ref)->claz) < top_of_to_space))
	{
	  // forward pointer appropriately
	  (*ref) = (jobject_unwrapped)((*ref)->claz);
	  error_gc("already moved to %p.\n", (*ref));
	}
      else
	{
	  // move to new semispace
	  relocate(ref);
	  error_gc("relocated to %p.\n", (*ref));
	}
    }
  else
    error_gc("not in heap, not traced.\n", "");
}

/* overwrite_to_space writes over to_space so that, when debugging, it is
   clear when there is a memory reference to the heap that is bad. */

#ifndef DEBUG_GC
#define debug_overwrite_to_space()
#else
void debug_overwrite_to_space()
{
  int *curr;
  // write over to_space
  for(curr = (int *)to_space; curr < (int *)top_of_to_space; curr++)
    (*curr) = 0x77777777;
}
#endif

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

/* magic number (quite arbitrary) used to determine start size of heap */
#define INITIAL_HEAP_SIZE     1024

/* statistics for determining when to grow heap. */
static float avg_occupancy = 0; // 0 <= avg_occupancy <= 0.5
static int num_collections = 0;
static size_t max_heap_size = 0;

/* initializatiion: set up heap */
void copying_gc_init()
{
  heap_size = INITIAL_HEAP_SIZE;
  fd = open("/dev/zero", O_RDONLY);
  from_space = mmap(0, heap_size, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);

  // could not allocate memory. we really should throw an exception,  
  // but there are various problems with this. even if we pre-allocated
  // the exception object, throwing the exception would involve
  // allocating more memory.
  assert(from_space != MAP_FAILED);

  error_gc("Initializing copying heap of size x%x\n", heap_size);
  free = from_space;
  to_space = from_space + heap_size/2;
  top_of_space = to_space;
  top_of_to_space = to_space + heap_size/2;
  error_gc("Free space from %p ", from_space);
  error_gc("to %p\n\n", top_of_space);
} 

#ifdef WITH_PRECISE_C_BACKEND
void *copying_malloc (size_t size_in_bytes)
#else
void *copying_malloc (size_t size_in_bytes, void *saved_registers[])
#endif
{
  size_t aligned_size_in_bytes;
  void *result;

#ifdef WITH_THREADED_GC
  /* only one thread can get memory at a time */
  while (pthread_mutex_trylock(&copying_gc_mutex))
    if (halt_for_GC_flag) halt_for_GC();
#endif
  /* calculate the actual number of bytes we need */
  aligned_size_in_bytes = align(size_in_bytes);

#ifndef GC_EVERY_TIME
  if (free + aligned_size_in_bytes > top_of_space)
#endif
    {
      float heap_occupancy;
      size_t expected_free_space;
      size_t expand_heap = 0; // minimum amt by which we want to expand the heap
      // if not enough memory to allocate, run a collection
      error_gc("\nx%x bytes needed ", aligned_size_in_bytes);
      error_gc("but only x%x bytes available\n", top_of_space - free);

      // guess if we want to grow heap based on statistics
      expected_free_space = (0.5 - avg_occupancy)*heap_size;
      error_gc("Expected free space after collection is %d bytes.\n", expected_free_space);
      if (expected_free_space < 2*aligned_size_in_bytes)
	{
	  // if we are going to expand the heap, make sure we do it by enough
	  expand_heap = aligned_size_in_bytes;
	  error_gc("Force heap expansion of %d bytes.\n", expand_heap);
	}

      // setup_for_threaded_GC is a nop if not using threads
      setup_for_threaded_GC();
#ifdef WITH_PRECISE_C_BACKEND
      collect(expand_heap);
#else
      collect(saved_registers, expand_heap);
#endif
      // done with collection, calculate statistics
      heap_occupancy = ((float)(free - from_space)) / ((float)heap_size);
      avg_occupancy = (heap_occupancy + num_collections*avg_occupancy)/(num_collections+1);
      assert(avg_occupancy <= 0.5);
      num_collections++;
      error_gc("Heap occupancy %.4f", avg_occupancy);
      error_gc(" after %d collections\n", num_collections);

      error_gc("Actual free space after collection is %d bytes\n", (top_of_space - free));

      // if we are still out of space then we have to expand the heap
      if (free + aligned_size_in_bytes > top_of_space)
	{
	  error_gc("ALERT -- second collection needed for allocating %d bytes!\n", aligned_size_in_bytes);
	  // we should never have to expand the heap twice in the same allocation
	  assert(expand_heap == 0);
#ifdef WITH_PRECISE_C_BACKEND
	  collect(aligned_size_in_bytes);
#else
	  collect(saved_registers, aligned_size_in_bytes);
#endif
	}
      // cleanup_after_threaded_GC is a nop if not using threads
      cleanup_after_threaded_GC();
    }
  
  result = free;
  free += aligned_size_in_bytes;

  // calculate statistics for max_heap_size
  max_heap_size = (max_heap_size > (free - from_space)) ? max_heap_size : (free - from_space);
  error_gc("Maximum heap size = %d bytes\n", max_heap_size);

  error_gc("%d\t", ++num_mallocs);
  error_gc("Allocated x%x bytes ", aligned_size_in_bytes);
  error_gc("at %p (for a total of ", result);
  error_gc("x%x bytes)\n", (total_memory_requested += aligned_size_in_bytes));

  FLEX_MUTEX_UNLOCK(&copying_gc_mutex);
  return result;
}

/* collect performs the actual tracing and copying collection */
#ifdef WITH_PRECISE_C_BACKEND
void collect(int expand_amt)
#else
void collect(void *saved_registers[], int expand_amt)
#endif
{
  void *scan;
  size_t old_heap_size;
  void *old_heap = NULL; // ptr to old heap (for when we grow it)
  void *next_to_space = from_space; // unless we grow the heap

  error_gc("GC number %d\n", ++num_gcs);

  // see if we need to grow the heap
  if (expand_amt != 0)
    {
      // before doing anything we need a handle on the memory that we are going to munmap
      old_heap = (to_space < from_space) ? to_space : from_space;
      old_heap_size = heap_size;

      // we are going to expand the heap by at least expand_amt 
      // but we prefer to just double the size of the heap
      heap_size = (heap_size/2 < expand_amt) ? (4*expand_amt) : (2*heap_size);
      to_space = mmap(0, heap_size, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);

      // NOTE TO SELF: turn this assert into an exception!!!
      assert(to_space != MAP_FAILED);
      error_gc("Expanding heap from %d", old_heap_size);
      error_gc(" to %d bytes\n", heap_size);

      top_of_to_space = to_space + heap_size/2;
      next_to_space = top_of_to_space;
    }

  free = to_space;
  scan = free;

  error_gc("FLIP -- New free space from %p to ", free);
  error_gc("%p\n\n", top_of_to_space);

#ifdef WITH_PRECISE_C_BACKEND
  find_root_set();
#else
  find_root_set(saved_registers);
#endif
  while(scan < free)
    {
      // trace object at scan for references,
      // then increment scan by size of object
      assert(scan != NULL && ((jobject_unwrapped)scan)->claz != NULL);
      error_gc("Object at %p being scanned\n", scan);
      trace((jobject_unwrapped)scan);
      scan += align(FNI_ObjectSize(scan));
    }
  assert(scan == free);

  // the new from_space is what used to be to_space
  from_space = to_space;
  top_of_space = top_of_to_space;

  // set up new to_space
  to_space = next_to_space;
  top_of_to_space = to_space + heap_size/2;

  // if necessary, munmap the previous heap
  if (old_heap != NULL)
    {
      // on success, munmap returns 0, on failure -1
      int munmap_result = munmap(old_heap, old_heap_size);
      assert(munmap_result == 0);
      assert(free + expand_amt < top_of_space);
    }
  
  // this function is a nop if not debug
  debug_overwrite_to_space();
}

/* copying_get_size_of_obj returns the size (in bytes) of
   the object to which the argument points. */
size_t copying_get_size_of_obj(jobject_unwrapped ptr_to_obj)
{
  /* assert that the object is in the currently occupied heap */
  assert(IN_HEAP((void *)ptr_to_obj));;
  return align(FNI_ObjectSize(ptr_to_obj));
}

/* to free an object, we need it to be big enough to go on the free list */
#define MINIMUM_SIZE sizeof(struct free_block)

/* we keep an unsorted, singly-linked list of free blocks of memory */
static struct free_block *free_list = NULL;
/* mutex for free_list */
FLEX_MUTEX_DECLARE_STATIC(copying_free_list_mutex);

/* copying_free allows explicit freeing of memory allocated by the
   copying collector. you can only free memory to which there will
   be no more pointers. therefore, you must null out the ptr to the 
   freed memory after this call. note that using free introduces
   fragmentation. */
void copying_free(void *ptr_to_memory_to_be_freed)
{
  struct free_block *ptr_to_freed_memory;
  /* find out how big this object is */
  size_t amt_of_memory_to_be_freed = 
    copying_get_size_of_obj(ptr_to_memory_to_be_freed);
  /* we depend on the memory to be freed being big enough to go on the free list */
  assert(amt_of_memory_to_be_freed >= MINIMUM_SIZE);
  /* note the amt of memory being freed */
  ptr_to_freed_memory = (struct free_block *)ptr_to_memory_to_be_freed;
#ifdef WITH_THREADED_GC
  /* only one thread can touch free list at a time */
  while (pthread_mutex_trylock(&copying_free_list_mutex))
    if (halt_for_GC_flag) halt_for_GC();
#endif
  /* put new freed block at the beginning of the free list */
  ptr_to_freed_memory->next = free_list;
  free_list = ptr_to_freed_memory;
  /* release */
  FLEX_MUTEX_UNLOCK(&copying_free_list_mutex);
}

/* returns 1 if the given ptr points to an object
   in the garbage-collected heap, returns 0 otherwise. */
int copying_in_heap(void *ptr)
{
  int result = 0;
#ifdef WITH_THREADED_GC
  /* only one thread can touch the GC variables */
  while (pthread_mutex_trylock(&copying_gc_mutex))
    if (halt_for_GC_flag) halt_for_GC();
#endif
  result = IN_HEAP(ptr);
  FLEX_MUTEX_UNLOCK(&copying_gc_mutex);
  return result;
}

