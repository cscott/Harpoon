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
#include "deflate_objs.h"

#ifndef WITH_PRECISE_C_BACKEND
# error unimplemented
#endif

#define BOTTOM 0
#define TOP    1


/* data structure for semi-spaces */
struct semi_space {
  void *begin;
  void *free;
  void *end;
};

/* variables locked by copying_gc_mutex */
static struct semi_space heap[2];
/* indices into copying_heap */
static int from;
static int to;
/* file descriptor for mmap */
static int fd;
/* current size of heap */
static size_t heap_size;
/* end of locked variables */

/* list of objects that need to be deflated after being GC'd, locked by inflated_objs_mutex */
static struct obj_list *inflated_objs = NULL;

/* convenience macros */
#define IN_HEAP(void_p) ((void_p) >= heap[from].begin && (void_p) < heap[from].free)

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
  result = (jlong)(heap[from].end - heap[from].free);
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
  memset(heap[to].begin, 7, heap_size/2);
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
      if (((void *)(obj->claz) >= heap[to].begin) && 
	  ((void *)(obj->claz) <  heap[to].free))
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
  assert(heap[to].free + obj_size <= heap[to].end);
  /* copy over to to-space */
  forwarding_address = TAG_HEAP_PTR(memcpy(heap[to].free, obj, obj_size));
  /* write forwarding address to previous location;
     the order of the following two operations are critical */
  obj->claz = /* not really */(struct claz *)forwarding_address;
  (*ref) = forwarding_address;
  /* increment free */
  heap[to].free += obj_size;
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
      if (((void *)(infl_obj->claz) >= heap[to].begin) && 
	  ((void *)(infl_obj->claz) <  heap[to].free))
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

/* initial number of pages to reserve */
#define INITIAL_PAGES_TO_MAP 65536

/* beginning and end of reserved space */
static void *mapped_begin;
static void *mapped_end;

/* initial number of pages in the heap */
#define INITIAL_PAGES_IN_HEAP 32

/* more magic numbers specifying the target heap occupancy */
#define HEAP_DIVISOR          4

static size_t SYSTEM_PAGE_SIZE = 0;
static size_t PAGE_MASK = 0;
#define ROUND_TO_NEXT_PAGE(x) (((x) + PAGE_MASK) & (~PAGE_MASK))

/* effects: initializes heap */
void copying_gc_init()
{
  size_t bytes_to_map;

  // find out the system page size and pre-calculate some constants
  SYSTEM_PAGE_SIZE = getpagesize();
  PAGE_MASK = SYSTEM_PAGE_SIZE - 1;

  printf("SYSTEM_PAGE_SIZE %d\n", SYSTEM_PAGE_SIZE);

  bytes_to_map = INITIAL_PAGES_TO_MAP*SYSTEM_PAGE_SIZE;

  // reserve a large amount of memory in which to grow the heap
  fd = open("/dev/zero", O_RDONLY);
  mapped_begin = mmap(0, bytes_to_map, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);

  // could not allocate memory. we really should throw an exception,  
  // but there are various problems with this. even if we pre-allocated
  // the exception object, throwing the exception would involve
  // allocating more memory.
  assert(mapped_begin != MAP_FAILED);

  mapped_end = mapped_begin + bytes_to_map;

  // allocate heap
  heap_size = INITIAL_PAGES_IN_HEAP*SYSTEM_PAGE_SIZE;

  // initialized to- and from- spaces
  from = BOTTOM;
  to = TOP;
  
  // from-space is initially allocated at the beginning of the mapped space
  heap[from].begin = mapped_begin;
  heap[from].free  = mapped_begin;
  heap[from].end   = mapped_begin + heap_size/2;

  // to-space is initially allocated at the midpoint of the mapped space
  heap[to].begin = mapped_begin + bytes_to_map/2;
  heap[to].free  = heap[to].begin;
  heap[to].end   = heap[to].begin + heap_size/2;
}


/* requires: needed_space > 0
   effects: grows heap by enough to accomodate needed_space, which is given in bytes
   returns: 0 on success, -1 on failure
*/
int grow_heap(size_t needed_space)
{
  size_t bytes;

  // we are going to expand the heap by enough to fit at 
  // least needed_space; at the same time, we might as
  // well grow the heap a reasonable amt (1/HEAP_DIVISOR 
  // of the current space) for good measure.
  bytes = ROUND_TO_NEXT_PAGE(needed_space + heap_size/HEAP_DIVISOR);

  // if we don't have enough space reserved, just fail for now
  if (heap[TOP].end + bytes > mapped_end)
    return -1;
  
  // update the size of the heap
  heap_size += 2*bytes;
  
  // update the bounds of the semispaces
  heap[to].end   += bytes;
  heap[from].end += bytes;

  assert(((heap[from].end - heap[from].begin) +
	 (heap[to].end - heap[to].begin)) == heap_size);
  printf("heap_size %d\n", heap_size);

  return 0;
}


void copying_print_stats ()
{
  printf("heap_size %d\n", heap_size);
}


/* returns: 1 if we can afford to garbage collect, 0 otherwise */
int collection_makes_sense(size_t bytes_since_last_GC, float occupancy_estimate)
{
  size_t cost = occupancy_estimate * heap_size;
  return (bytes_since_last_GC > cost);
}


/* returns: on success, a pointer to an amt (size_in_bytes) of memory; on failure, NULL */
void *copying_malloc (size_t size_in_bytes)
{
  static size_t bytes_allocated = 0; // bytes allocated since last collection
  static float avg_occupancy = 0;

  size_t aligned_size_in_bytes;
  void *result;

  ENTER_COPYING_GC();

  /* calculate the actual number of bytes we need */
  aligned_size_in_bytes = align(size_in_bytes);

  // if not enough memory, need to expand heap or run GC
  if (heap[from].free + aligned_size_in_bytes > heap[from].end)
    // if we can afford the cost of a collection, go ahead; else expand heap
    if (!collection_makes_sense(bytes_allocated, avg_occupancy))
      {
	int retval = grow_heap(aligned_size_in_bytes);

	// if we couldn't grow the heap, try collecting
	if (retval != 0)
	  {
	    avg_occupancy = copying_collect();
	    // reset bytes_allocated
	    bytes_allocated = 0;
	  }
      }
    else
      {
	avg_occupancy = copying_collect();

	// reset bytes_allocated
	bytes_allocated = 0;
	
	// if still not enough memory, grow heap
	if (heap[from].free + aligned_size_in_bytes > heap[from].end)
	  {
	    int retval = grow_heap(aligned_size_in_bytes);
	   
	    // if we couldn't collect and couldn't expand heap, just die
	    assert(retval == 0);
	  }
      }
  
  result = heap[from].free;
  heap[from].free += aligned_size_in_bytes;
  bytes_allocated += aligned_size_in_bytes;

  assert(heap[from].free <= heap[from].end);

  EXIT_COPYING_GC();

  return result;
}


/* effects: performs compacting garbage collection on heap and flips semi-spaces
   returns: average heap occupancy, a number between 0 and 0.5, inclusive */
float copying_collect()
{
  static int num_collections = 0;
  static float avg_occupancy = 0;
  void *scan;
  float occupancy;

  setup_for_threaded_GC();

  scan = heap[to].free;

  find_root_set();

  while(scan < heap[to].free)
    {
      // trace object at scan for references,
      // then increment scan by size of object
      assert(scan != NULL && ((jobject_unwrapped)scan)->claz != NULL);
      trace((jobject_unwrapped)scan);
      scan += align(FNI_ObjectSize(scan));
    }
  assert(scan == heap[to].free);

  // free up any resources from inflated objs that have been GC'd
  deflate_freed_objs();

  // flip semi-spaces and re-set new to-space
  from = to;
  to = (to == BOTTOM) ? TOP : BOTTOM;
  heap[to].free = heap[to].begin;

  // this function is a nop if not debug
  debug_overwrite_to_space();

  // calculate heap occupancy after compacting garbage-collection
  occupancy = ((float) (heap[from].free - 
			heap[from].begin))/((float) heap_size);
  avg_occupancy = (avg_occupancy*num_collections +
		   occupancy)/(num_collections + 1);
  num_collections++;

  error_gc("GC number %d\n", num_collections);
  printf("GC number %d\n", num_collections);

  cleanup_after_threaded_GC();

  return avg_occupancy;
}
