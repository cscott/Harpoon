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

/* data structures for list of inflated objects */
struct obj_list {
  jobject_unwrapped obj;
  struct obj_list *next;
};

/* variables locked by copying_gc_mutex */
static void *free_ptr;
static void *to_space;
static void *from_space;
static void *top_of_space;
static void *top_of_to_space;
/* file descriptor for mmap */
static int fd;
/* current size of heap */
static size_t heap_size;
/* end of locked variables */

/* list of objects that need to be deflated after being GC'd, locked by inflated_objs_mutex */
static struct obj_list *inflated_objs = NULL;

/* convenience macros */
#define IN_HEAP(void_p) ((void_p) >= from_space && (void_p) < top_of_space)

#ifdef DEBUG_GC
static int num_gcs = 0;
static int num_mallocs = 0;
static size_t total_memory_requested = 0;
#endif

#ifndef WITH_THREADED_GC
#define ENTER_COPYING_GC()
#define EXIT_COPYING_GC()
#else
/* mutex for the GC variables */
FLEX_MUTEX_DECLARE_STATIC(copying_gc_mutex);
FLEX_MUTEX_DECLARE_STATIC(inflated_objs_mutex);

/* barriers for synchronizing threads */
extern pthread_barrier_t before;
extern pthread_barrier_t after;

extern pthread_cond_t done_cond;
extern pthread_mutex_t done_mutex;
extern int done_count;
extern jint halt_for_GC_flag;

/* effects: acquire locks for copying GC in "safe" manner (non-blocking) */
#define ENTER_COPYING_GC() \
({ while (pthread_mutex_trylock(&copying_gc_mutex)) \
if (halt_for_GC_flag) halt_for_GC(); })

/* effects: release locks for copying GC */
#define EXIT_COPYING_GC() FLEX_MUTEX_UNLOCK(&copying_gc_mutex)

#endif /* WITH_THREADED_GC */

void relocate(jobject_unwrapped *obj);


/* effects: adds obj to the list of inflated objects that need to be deflated
   after object has been garbage collected. it is not immediately obvious
   why this function works. basically, while the thread calling this function
   has not halted, gc cannot occur, so we are safe to add to the inflated
   obj list. when gc is occurring, all other threads have stopped, so really,
   the gc thread does not need to grab the lock to go through the list, but
   since the mutex protects the list head, it seems good practice to go
   through the exercise.
*/
void copying_register_inflated_obj(jobject_unwrapped obj)
{
  struct obj_list *unit = (struct obj_list *)malloc(sizeof(struct obj_list));
  FLEX_MUTEX_LOCK(&inflated_objs_mutex);
  if (IN_HEAP((void *)obj))
    {
      unit->obj = obj;
      unit->next = inflated_objs;
      inflated_objs = unit;
    }
  FLEX_MUTEX_UNLOCK(&inflated_objs_mutex); 
}


/* returns: amt of free memory available */
jlong copying_free_memory()
{
  jlong result;
  ENTER_COPYING_GC();
  result = (jlong)(top_of_space - free_ptr);
  EXIT_COPYING_GC();
  return result;
}


/* returns: heap size */
jlong copying_get_heap_size()
{
  jlong result;
  ENTER_COPYING_GC();
  result = (jlong) heap_size;
  EXIT_COPYING_GC();
  return result;
}


#ifndef DEBUG_GC
#define debug_overwrite_to_space()
#define debug_verify_object(obj)
#else
/* effects: overwrites to-space with recognizable garbage for easy debugging */
void debug_overwrite_to_space()
{
  memset(to_space, 7, heap_size/2);
}


/* effects: verifies the integrity of the given object with simple checks */
void debug_verify_object(jobject_unwrapped obj)
{
  // check that this is probably a pointer
  assert(!((ptroff_t)obj & 1));
  
  // check that the claz ptr is correct
  assert(&claz_start <= obj->claz && obj->claz < &claz_end);
}
#endif


/* copying_handle_references handles refereneces to objects. objects in the
   heap that need to be copied to the new semispace are copied. if the 
   object has already been copied, the pointer is updated. */
void copying_handle_reference(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = PTRMASK((*ref));
  // we only have to do something if we are given a reference in the heap.
  // we cannot trace through non-root references outside the heap because 
  // we may end up in an undetectable infinite loop.
  if (IN_HEAP((void *)obj))
    {
      // handle objects that have already been moved
      if (((void *)(obj->claz) >= to_space) && 
	  ((void *)(obj->claz) < top_of_to_space))
	{
	  // forward pointer appropriately
	  (*ref) = (jobject_unwrapped)(obj->claz);
	  error_gc("already moved to %p.\n", obj);
	}
      else
	{
	  debug_verify_object(obj);
	  // move to new semispace
	  relocate(ref);
	  error_gc("relocated to %p.\n", PTRMASK((*ref)));
	}
    }
  else
    error_gc("not in heap, not traced.\n", "");
}


void relocate(jobject_unwrapped *ref) {
  jobject_unwrapped obj = (jobject_unwrapped)PTRMASK((*ref));
  void *forwarding_address;
  /* figure out how big the object is */
  size_t obj_size = align(FNI_ObjectSize(obj));
  /* relocated objects should not exceed size of heap */
  assert(free_ptr + obj_size <= top_of_to_space);
  /* copy over to to_space */
  forwarding_address = TAG_HEAP_PTR(memcpy(free_ptr, obj, obj_size));
  /* write forwarding address to previous location;
     the order of the following two operations are critical */
  obj->claz = /* not really */(struct claz *)forwarding_address;
  (*ref) = forwarding_address;
  /* increment free */
  free_ptr += obj_size;
}


/* effects: updates list of inflated objects with new locations,
   deflating any that have been garbage-collected */
void deflate_freed_objs ()
{
  struct obj_list *prev = NULL;
  struct obj_list *unit;

  FLEX_MUTEX_LOCK(&inflated_objs_mutex);

  unit = inflated_objs;

  while(unit != NULL)
    {
      jobject_unwrapped infl_obj = unit->obj;
      // printf("Inspecting %p: ", infl_obj);
      
      // update objects that have been moved
      if (((void *)(infl_obj->claz) >= to_space) && 
	  ((void *)(infl_obj->claz) < top_of_to_space))
	{
	  // forward pointer appropriately
	  unit->obj = (jobject_unwrapped)PTRMASK(infl_obj->claz);
	  // printf("updating to %p\n", unit->obj);
	  // go to next
	  prev = unit;
	  unit = unit->next;
	}
      else
	{
	  struct obj_list *to_free = unit;
	  debug_verify_object(infl_obj);
	  // invoke deflate fcn
	  infl_obj->hashunion.inflated->precise_deflate_obj(infl_obj, (ptroff_t)0);
	  // printf("Invoking deflate function on %p.\n", infl_obj); fflush(stdout);	  
	  // update links
	  if (prev == NULL)
	    inflated_objs = unit->next;
	  else
	    prev->next = unit->next;
	  // go to next
	  unit = unit->next;
	  // free unit
	  free(to_free);
	}
    }

  // printf("\n"); fflush(stdout);
  FLEX_MUTEX_UNLOCK(&inflated_objs_mutex);
}


/* magic number (quite arbitrary) used to determine start size of heap */
#define INITIAL_HEAP_SIZE     131072
/* more magic numbers specifying the target heap occupancy */
#define TARGET_OCCUPANCY      0.25
#define HEAP_DIVISOR          2

/* statistics for determining when to grow heap. */
// start with a conservative guess for the occupancy
static float avg_occupancy = 0.4; // 0 <= avg_occupancy <= 0.5
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
  free_ptr = from_space;
  to_space = from_space + heap_size/2;
  top_of_space = to_space;
  top_of_to_space = to_space + heap_size/2;
  error_gc("Free space from %p ", from_space);
  error_gc("to %p\n\n", top_of_space);
}


void copying_print_stats ()
{
    printf("Maximum heap size = %d bytes.\n", max_heap_size);
}


#define GC_PAGE_SIZE 65536
#define GC_PAGE_MASK (GC_PAGE_SIZE - 1)
#define ROUND_TO_NEXT_PAGE(x) (((x) + GC_PAGE_MASK) & (~GC_PAGE_MASK))
#define GC_BLK_SIZE  4096
#define GC_BLK_MASK  (GC_BLK_SIZE - 1)
#define ROUND_TO_NEXT_BLK(x) (((x) + GC_BLK_MASK) & (~GC_BLK_MASK))


#ifdef WITH_PRECISE_C_BACKEND
void *copying_malloc (size_t size_in_bytes)
#else
void *copying_malloc (size_t size_in_bytes, void *saved_registers[])
#endif
{
  size_t aligned_size_in_bytes;
  void *result;

  ENTER_COPYING_GC();

  /* calculate the actual number of bytes we need */
  aligned_size_in_bytes = align(size_in_bytes);

#ifndef GC_EVERY_TIME
  if (free_ptr + aligned_size_in_bytes > top_of_space)
#endif
    {
      float heap_occupancy;
      size_t expected_free_space;
      size_t expand_heap = 0; // minimum amt by which we want to expand the heap
      // if not enough memory to allocate, run a collection
      error_gc("\nx%x bytes needed ", aligned_size_in_bytes);
      error_gc("but only x%x bytes available\n", top_of_space - free_ptr);

      // printf("avg_occupancy %f\n", avg_occupancy);

      if (aligned_size_in_bytes > GC_PAGE_SIZE || 
	  avg_occupancy > TARGET_OCCUPANCY)
	expand_heap = aligned_size_in_bytes;
      else
	{
	  // guess if we want to grow heap based on statistics
	  expected_free_space = (0.5 - avg_occupancy)*heap_size;
	  error_gc("Expected free space after collection is %d bytes.\n", expected_free_space);

	  // guess if we have enough space. also try to maintain a low occupancy
	  if ((expected_free_space < ROUND_TO_NEXT_BLK(aligned_size_in_bytes)))
	    // if we are going to expand the heap, make sure we do it by enough
	    expand_heap = aligned_size_in_bytes;
	}

      // setup_for_threaded_GC is a nop if not using threads
      setup_for_threaded_GC();
#ifdef WITH_PRECISE_C_BACKEND
      copying_collect(expand_heap);
#else
      copying_collect(saved_registers, expand_heap);
#endif
      // done with collection, calculate statistics
      heap_occupancy = ((float)(free_ptr - from_space)) / ((float)heap_size);
      // avg_occupancy = (heap_occupancy + num_collections*avg_occupancy)/(num_collections+1);
      avg_occupancy = (heap_occupancy + avg_occupancy)/2;
      assert(avg_occupancy >= 0 && avg_occupancy <= 0.5);
      num_collections++;
      error_gc("Heap occupancy %.4f", avg_occupancy);
      error_gc(" after %d collections\n", num_collections);

      error_gc("Actual free space after collection is %d bytes\n", (top_of_space - free_ptr));

      // if we are still out of space then we have to expand the heap
      if (free_ptr + aligned_size_in_bytes > top_of_space)
	{
	  error_gc("ALERT -- second collection needed for allocating %d bytes!\n", aligned_size_in_bytes);
	  // we should never have to expand the heap twice in the same allocation
	  assert(expand_heap == 0);
#ifdef WITH_PRECISE_C_BACKEND
	  copying_collect(aligned_size_in_bytes);
#else
	  copying_collect(saved_registers, aligned_size_in_bytes);
#endif
	}
      // cleanup_after_threaded_GC is a nop if not using threads
      cleanup_after_threaded_GC();
    }
  
  result = free_ptr;
  free_ptr += aligned_size_in_bytes;

  //calculate statistics for max_heap_size
  max_heap_size = (max_heap_size > (free_ptr - from_space)) ? max_heap_size : (free_ptr - from_space);

  error_gc("%d\t", ++num_mallocs);
  error_gc("Allocated x%x bytes ", aligned_size_in_bytes);
  error_gc("at %p (for a total of ", result);
  error_gc("x%x bytes)\n", (total_memory_requested += aligned_size_in_bytes));

  EXIT_COPYING_GC();

  return result;
}


/* collect performs the actual tracing and copying collection */
#ifdef WITH_PRECISE_C_BACKEND
void copying_collect(int expand_amt)
#else
void copying_collect(void *saved_registers[], int expand_amt)
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

      // printf("heapsize %d -> ", heap_size);
      // we are going to expand the heap by enough to fit at 
      // least expand_amt; the doubling takes into account
      // the fact that half of the heap is empty at any point
      // since we're expanding, grow the heap a reasonable amt
      // (1/HEAP_DIVISOR of the current space) for good measure
      heap_size += ROUND_TO_NEXT_PAGE(2*expand_amt + heap_size/HEAP_DIVISOR);
      // printf("%d\n", heap_size);
      to_space = mmap(0, heap_size, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);

      // NOTE TO SELF: turn this assert into an exception!!!
      assert(to_space != MAP_FAILED);
      error_gc("Expanding heap from %d", old_heap_size);
      error_gc(" to %d bytes\n", heap_size);

      top_of_to_space = to_space + heap_size/2;
      next_to_space = top_of_to_space;
    }

  free_ptr = to_space;
  scan = free_ptr;

  error_gc("FLIP -- New free space from %p to ", free_ptr);
  error_gc("%p\n\n", top_of_to_space);

#ifdef WITH_PRECISE_C_BACKEND
  find_root_set();
#else
  find_root_set(saved_registers);
#endif
  while(scan < free_ptr)
    {
      // trace object at scan for references,
      // then increment scan by size of object
      assert(scan != NULL && ((jobject_unwrapped)scan)->claz != NULL);
      trace((jobject_unwrapped)scan);
      scan += align(FNI_ObjectSize(scan));
    }
  assert(scan == free_ptr);

  // free up any resources from inflated objs that have been GC'd
  deflate_freed_objs();

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
    }
  
  // this function is a nop if not debug
  debug_overwrite_to_space();
}
