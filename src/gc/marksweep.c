#include <fcntl.h>
#include <sys/mman.h>
#include <assert.h>
#include "jni-types.h"
#include "jni-private.h"
#include "jni-gc.h"
#include "precise_gc.h"
#include "free_list.h"
#include "ms_heap.h"
#include "system_page_size.h"
#ifdef WITH_THREADED_GC
#include "flexthread.h"
#endif

#ifndef WITH_PRECISE_C_BACKEND
# error unimplemented
#else

//#define GC_EVERY_TIME
#define INITIAL_PAGES_IN_HEAP 32
#define INITIAL_PAGES_TO_MAP  65536

FLEX_MUTEX_DECLARE_STATIC(marksweep_gc_mutex);

#ifndef WITH_THREADED_GC
# define ENTER_MARKSWEEP_GC()
#else
extern jint halt_for_GC_flag;

/* effects: acquire locks for marksweep GC in "safe" manner (non-blocking) */
# define ENTER_MARKSWEEP_GC() \
({ while (pthread_mutex_trylock(&marksweep_gc_mutex)) \
if (halt_for_GC_flag) halt_for_GC(); })
#endif /* WITH_THREADED_GC */

/* effects: release locks for marksweep GC */
#define EXIT_MARKSWEEP_GC() FLEX_MUTEX_UNLOCK(&marksweep_gc_mutex)

static struct marksweep_heap heap;

static int fd = 0;


/* returns: 1 if we can afford to garbage-collect, 0 otherwise */
int collection_makes_sense(size_t bytes_since_last_GC)
{
  size_t cost = heap.avg_occupancy*heap.heap_size;
  return (bytes_since_last_GC > cost);
}


/* effects: performs a garbage-collection */
void marksweep_collect()
{
  static int num_collections = 0;
  num_collections++;
  find_root_set();
  free_unreachable_blocks(&heap);
}


/* returns: amt of free memory available */
jlong marksweep_free_memory()
{
  jlong result;
  ENTER_MARKSWEEP_GC();
  result = heap.free_memory;
  EXIT_MARKSWEEP_GC();
  return result;
}


/* effects: creates and initializes the heap */
void marksweep_gc_init()
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
  init_marksweep_heap(mapped,
		      INITIAL_PAGES_IN_HEAP*SYSTEM_PAGE_SIZE,
		      bytes_to_map,
		      &heap);
}


/* returns: heap size */
jlong marksweep_get_heap_size()
{
  jlong result;
  ENTER_MARKSWEEP_GC();
  result = heap.heap_size;
  EXIT_MARKSWEEP_GC();
  return result;
}


/* effects: marks object as well as any objects pointed to by it */
void marksweep_handle_reference(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = (jobject_unwrapped)PTRMASK((*ref));
  if (IN_MARKSWEEP_HEAP(obj, heap))
    {
      // get a pointer to the block
      struct block *bl = (void *)obj - BLOCK_HEADER_SIZE;
      // mark or check for mark
      if (NOT_MARKED(bl))
	{
	  // if not already marked, mark and trace
	  error_gc("being marked.\n", "");
	  MARK_AS_REACHABLE(bl);
	  trace(obj);
	}
      else
	// if already marked, done
	assert(MARKED_AS_REACHABLE(bl));
    }
}


/* returns: a block of allocated memory of the specified size */
void* marksweep_malloc(size_t size_in_bytes)
{
  static size_t bytes_allocated = 0;
  size_t aligned_size_in_bytes = 0;
  void *result = NULL;
  aligned_size_in_bytes = align(size_in_bytes + BLOCK_HEADER_SIZE);

  ENTER_MARKSWEEP_GC();

#ifdef GC_EVERY_TIME
  marksweep_collect();
#endif

  result = find_free_block(aligned_size_in_bytes, 
			   &(heap.free_list), 
			   heap.small_blocks);

  if (result != NULL)
    {
      // get past block header
      result = ((struct block *)result)->object;
    }
  else
    {
      // no free blocks, allocate or expand heap
      if (collection_makes_sense(bytes_allocated))
	{
	  setup_for_threaded_GC();

	  marksweep_collect();
	  bytes_allocated = 0;
	  
	  cleanup_after_threaded_GC();
	}

      // see if we have the necessary memory,
      // either with or without collection
      result = allocate_in_marksweep_heap(aligned_size_in_bytes, &heap);

      // if we didn't collect already and we
      // ran out of memory, run a collection
      if (result == NULL && bytes_allocated != 0) {
	  setup_for_threaded_GC();

	  marksweep_collect();
	  bytes_allocated = 0;
	  
	  cleanup_after_threaded_GC();
      }
    }

  // if no memory at this point, just fail
  assert(result != NULL);

  bytes_allocated += aligned_size_in_bytes;

  EXIT_MARKSWEEP_GC();

  return result;
}


#ifdef WITH_POINTER_REVERSAL
/* pointerreversed_handle_reference marks the given object as
   well as any objects pointed to by it using pointer-reversal. */
void pointerreversed_handle_reference(jobject_unwrapped *ref)
{
  jobject_unwrapped prev = NULL;
  jobject_unwrapped curr = *ref;
  jobject_unwrapped next;
  int done = 0;

  // in pointer-reversal, the mark has 3 possible states.
  // when an object has not yet been visited, it is
  // unmarked. after an object has been visited and all
  // its roots traced, it is marked as reachable. after
  // an object has been visited and before all its roots
  // have been traced, its mark encodes the field or
  // array element that is currently being examined.
  while (!done)
    {
      // follow pointers down
      while (curr != NULL)
	{
	  jobject_unwrapped obj = PTRMASK(curr);
	  struct block *bl = (void *)obj - BLOCK_HEADER_SIZE;

	  if (!IN_MARKSWEEP_HEAP(obj, heap))
	    {
	      error_gc("%p not allocated by marksweep.\n", obj);
	      if (prev == NULL)
		done = 1;
	      break;
	    }
	  
	  debug_verify_object(obj);

	  // these objects are untouched
	  if (NOT_MARKED(bl))
	    {
	      ptroff_t next_index;
	      jobject_unwrapped *elements_and_fields;

	      error_gc("%p is unmarked.\n", obj);

	      elements_and_fields = (jobject_unwrapped *)obj;

	      // we know the header does not contain heap pointers
	      next_index = get_next_index(obj, OBJ_HEADER_SIZE/SIZEOF_VOID_P);

	      if (next_index == NO_POINTERS)
		{
		  // does not contain pointers, mark as done
		  error_gc("%p does not contain pointers.\n", obj);
		  
		  MARK_AS_REACHABLE(bl);

		  if (prev == NULL)
		    done = 1;
		  break;
		}
	      else
		{
		  // the indices returned by next_index 
		  // is off by one; do fix up
		  next_index -= INDEX_OFFSET;
		  assert(next_index >= 0);

		  error_gc("next_index = %d\n", next_index); 

		  // look at the next index
		  SET_INDEX(bl, next_index);

		  next = elements_and_fields[next_index];
		  elements_and_fields[next_index] = prev;
		  prev = curr;
		  error_gc("Setting prev = %p.\n", prev);
		  curr = next;
		}
	    }
	  else
	    {
	      // these are references we're done with or that
	      // we are already looking at somewhere in the chain
	      if (prev == NULL)
		done = 1;
	      break;
	    }
	}
      
      // retreat! curr should point to the last thing we looked at, and prev to its parent
      while (prev != NULL)
	{
	  // these objects are in the middle of being examined
	  jobject_unwrapped obj = PTRMASK(prev);
	  struct block *bl = (void *)obj - BLOCK_HEADER_SIZE;
	  ptroff_t last_index;
	  ptroff_t next_index;
	  jobject_unwrapped *elements_and_fields;

	  error_gc("Starting prev loop with prev = %p.\n", prev);

	  last_index = GET_INDEX(bl);

	  elements_and_fields = (jobject_unwrapped *)obj;

	  next_index = get_next_index(obj, last_index+1);

	  if (next_index == NO_POINTERS)
	    {
	      // does not contain any more pointers, mark as done
	      error_gc("None of the rest of the fields/elements in this object contains a ptr.\n", "");
	      MARK_AS_REACHABLE(bl);

	      // restore pointers
	      next = prev;
	      prev = elements_and_fields[last_index];
	      error_gc("Setting prev = %p.\n", prev);
	      elements_and_fields[last_index] = curr;
	      curr = next;

	      if (prev == NULL)
		done = 1;
	    }
	  else
	    {
	      // the indices returned by next_array_index 
	      // and next_field_index are off by one; do fix up
	      next_index -= INDEX_OFFSET;
	      assert(next_index > 0);

	      error_gc("last_index = %d\n", last_index); 
	      error_gc("next_index = %d\n", next_index); 
	      
	      SET_INDEX(bl, next_index);

	      next = elements_and_fields[next_index];
	      elements_and_fields[next_index] = elements_and_fields[last_index];
	      elements_and_fields[last_index] = curr;
	      curr = next;

	      break;
	    }
	}
    }
}
#endif
#endif
