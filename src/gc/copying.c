#include <fcntl.h>
#include <sys/mman.h>
#include <assert.h>
#include "config.h"
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"
#include "gc-data.h"
#include "precise_gc.h"
#include "deflate_objs.h"
#include "cp_heap.h"
#include "system_page_size.h"
#ifdef WITH_THREADED_GC
#include "flexthread.h"
#endif

#ifndef WITH_PRECISE_C_BACKEND
# error unimplemented
#endif

//#define GC_EVERY_TIME
#define INITIAL_PAGES_IN_HEAP 32
#define INITIAL_PAGES_TO_MAP  65536

FLEX_MUTEX_DECLARE_STATIC(copying_gc_mutex);

#ifndef WITH_THREADED_GC
#define ENTER_COPYING_GC()
#else
extern jint halt_for_GC_flag;

/* effects: acquire locks for copying GC in "safe" manner (non-blocking) */
#define ENTER_COPYING_GC() \
({ while (pthread_mutex_trylock(&copying_gc_mutex)) \
if (halt_for_GC_flag) halt_for_GC(); })
#endif

/* effects: release locks for copying GC */
#define EXIT_COPYING_GC() FLEX_MUTEX_UNLOCK(&copying_gc_mutex)

static struct copying_heap heap;

static int fd = 0;


/* returns: 1 if we can afford to garbage collect, 0 otherwise */
int collection_makes_sense(size_t bytes_since_last_GC)
{
  size_t cost = heap.avg_occupancy * heap.heap_size;
  return (bytes_since_last_GC > cost);
}


/* effects: performs compacting garbage collection on heap,
   flips semi-spaces, and updates the heap occupancy statistics.
*/
void copying_collect()
{
  static int num_collections = 0;
  void *scan;
  float occupancy;

  setup_for_threaded_GC();

  scan = heap.to_free;

  find_root_set();

  while(scan < heap.to_free)
    {
      // trace object at scan for references,
      // then increment scan by size of object
      assert(scan != NULL && ((jobject_unwrapped)scan)->claz != NULL);
      trace((jobject_unwrapped)scan);
      scan += align(FNI_ObjectSize(scan));
    }
  assert(scan == heap.to_free);

  // free up any resources from inflated objs that have been GC'd
  deflate_freed_objs(&heap);

  // flip semi-spaces
  flip_semispaces(&heap);

  // this function is a nop if not debug
  debug_overwrite_to_space(&heap);

  // calculate heap occupancy after compacting garbage-collection
  occupancy = ((float) (heap.from_free - 
			heap.from_begin))/((float) heap.heap_size);
  heap.avg_occupancy = (heap.avg_occupancy*num_collections +
			occupancy)/(num_collections + 1);
  num_collections++;

  error_gc("GC number %d\n", num_collections);

  cleanup_after_threaded_GC();
}


/* returns: amt of free memory available */
jlong copying_free_memory()
{
  jlong result;
  ENTER_COPYING_GC();
  result = (jlong)(heap.from_end - heap.from_free);
  EXIT_COPYING_GC();
  return result;
}


/* effects: initializes heap */
void copying_gc_init()
{
  size_t bytes_to_map;
  void *mapped;

  bytes_to_map = INITIAL_PAGES_TO_MAP*SYSTEM_PAGE_SIZE;

  // make sure we are allocating some amount of memory
  assert(bytes_to_map != 0);

  // reserve a large amount of memory in which to grow the heap
  fd = open("/dev/zero", O_RDONLY);
  mapped = mmap(0, bytes_to_map, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);

  // could not allocate memory. we really should throw an exception,  
  // but there are various problems with this. even if we pre-allocated
  // the exception object, throwing the exception would involve
  // allocating more memory.
  assert(mapped != MAP_FAILED);

  // initialize the heap data structure
  init_copying_heap(mapped, 
		    INITIAL_PAGES_IN_HEAP*SYSTEM_PAGE_SIZE, 
		    bytes_to_map, 
		    &heap);
}


/* returns: heap size */
jlong copying_get_heap_size()
{
  jlong result;
  ENTER_COPYING_GC();
  result = (jlong) heap.heap_size;
  EXIT_COPYING_GC();
  return result;
}


/* copying_handle_references handles refereneces to objects. objects in the
   heap that need to be copied to the new semispace are copied. if the 
   object has already been copied, the pointer is updated. */
void copying_handle_reference(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = PTRMASK((*ref));
  // we only have to do something if we are given a reference in the heap.
  // we cannot trace through non-root references outside the heap because 
  // we may end up in an undetectable infinite loop.
  if (IN_FROM_SPACE(obj, heap))
    {
      // handle objects that have already been moved
      if (IN_TO_SPACE(obj->claz, heap))
	{
	  // forward pointer appropriately
	  (*ref) = (jobject_unwrapped)(obj->claz);
	  error_gc("already moved to %p.\n", obj);
	}
      else
	{
	  debug_verify_object(obj);
	  // move to new semispace
	  relocate_to_to_space(ref, &heap);
	  error_gc("relocated to %p.\n", PTRMASK((*ref)));
	}
    }
  else
    error_gc("not in heap, not traced.\n", "");
}


/* returns: on success, a pointer to an amt (size_in_bytes) of memory; 
   on failure, NULL */
void *copying_malloc (size_t size_in_bytes)
{
  static size_t bytes_allocated = 0; // bytes allocated since last collection

  size_t aligned_size_in_bytes;
  void *result;

  ENTER_COPYING_GC();

#ifdef GC_EVERY_TIME
  copying_collect();
#endif

  /* calculate the actual number of bytes we need */
  aligned_size_in_bytes = align(size_in_bytes);

  // if not enough memory, need to expand heap or run GC
  if (heap.from_free + aligned_size_in_bytes > heap.from_end) {
    // if we can afford the cost of a collection, go ahead; else expand heap
    if (collection_makes_sense(bytes_allocated))
      {
	copying_collect();

	// reset bytes_allocated
	bytes_allocated = 0;
	
	// if still not enough memory, grow heap
	if (heap.from_free + aligned_size_in_bytes > heap.from_end)
	  grow_copying_heap(aligned_size_in_bytes, &heap);
      }
    else
      {
	grow_copying_heap(aligned_size_in_bytes, &heap);

	// if still not enough space, try collecting
	if (heap.from_free + aligned_size_in_bytes > heap.from_end)
	  {
	    copying_collect();
	    // reset bytes_allocated
	    bytes_allocated = 0;
	  }
      }
  }

  // if still no space, then just fail
  assert(heap.from_free + aligned_size_in_bytes <= heap.from_end);
  
  result = heap.from_free;
  heap.from_free += aligned_size_in_bytes;
  bytes_allocated += aligned_size_in_bytes;

  EXIT_COPYING_GC();

  return result;
}


void copying_print_stats ()
{
  printf("heap_size %d\n", heap.heap_size);
}


/* effects: adds obj to the list of inflated objects that need to be deflated
   after object has been garbage collected.
*/
void copying_register_inflated_obj(jobject_unwrapped obj)
{
  register_inflated_obj(obj, &heap);
}
