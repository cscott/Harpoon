#include <assert.h>
#include <fcntl.h>
#include <sys/mman.h>
#include "jni-types.h"
#include "jni-private.h"
#include "precise_gc.h"
#include "deflate_objs.h"
#include "cp_heap.h"
#include "free_list.h"
#include "ms_heap.h"
#include "system_page_size.h"

#define INITIAL_PAGES_TO_MAP_PER_SPACE 16384
#define INITIAL_PAGES_TO_MAP_TOTAL     (3*INITIAL_PAGES_TO_MAP_PER_SPACE)
#define INITIAL_PAGES_PER_HEAP  16
#define MINOR                 0
#define MAJOR                 1

FLEX_MUTEX_DECLARE_STATIC(generational_gc_mutex);

#ifndef WITH_THREADED_GC
# define ENTER_GENERATIONAL_GC()
#else
extern jint halt_for_GC_flag;

# define ENTER_GENERATIONAL_GC() \
({ while (pthread_mutex_trylock(&generational_gc_mutex)) \
if (halt_for_GC_flag) halt_for_GC(); })
#endif

#define EXIT_GENERATIONAL_GC() FLEX_MUTEX_UNLOCK(&generational_gc_mutex)

static struct copying_heap young_gen;
static struct marksweep_heap old_gen;

static int fd = 0;

static collection_type = MAJOR;

/* function declarations */
void generational_marksweep_reference(jobject_unwrapped obj);

void move_to_to_space(jobject_unwrapped *ref);

int promote_to_old_gen(jobject_unwrapped *ref);


/* effects: verifies the integrity of the given object w/ simple checks */
void debug_verify_object(jobject_unwrapped obj)
{
  // check that this is probably a pointer
  assert(!((ptroff_t)obj & 1));

  // check that the claz ptr is correct
  assert(CLAZ_OKAY(obj));
}


/* returns: 1 if we can afford a major collection, 0 otherwise */
int major_collection_makes_sense(size_t bytes_since_last_GC)
{
  size_t young_cost = young_gen.avg_occupancy * young_gen.heap_size;
  size_t old_cost = old_gen.avg_occupancy * old_gen.heap_size;
  return (bytes_since_last_GC > young_cost + old_cost);
}


/* returns: 1 if we can afford a minor collection, 0 otherwise */
int minor_collection_makes_sense(size_t bytes_since_last_GC)
{
  size_t cost = young_gen.avg_occupancy * young_gen.heap_size;
  printf("bytes %d vs cost %d\n", bytes_since_last_GC, cost);
  return (bytes_since_last_GC > cost);
}


/* effects: garbage collects either just the young generation
   or the entire heap depending on the collection_type flag
*/
void generational_collect()
{
  static int num_collections = 0;
  float young_occupancy;
  void *scan;

  setup_for_threaded_GC();

  scan = young_gen.to_begin;

  find_root_set();

  while(scan < young_gen.to_free)
    {
      trace((jobject_unwrapped)scan);
      scan += align(FNI_ObjectSize(scan));
    }

  assert(scan == young_gen.to_free);

  // set the pointer that marks the end of the
  // objects that have survived a garbage collection
  young_gen.survived = scan;

  // if we are running a major collection,
  // may need to free memory in old generation
  if (collection_type == MAJOR)
    free_unreachable_blocks(&old_gen);

  // free up any resources from inflated objs 
  // in the young generation that have been GC'd
  deflate_freed_objs(&young_gen);

  // flip semi-spaces
  flip_semispaces(&young_gen);

  // calculate heap occupancy
  young_occupancy = ((float) (young_gen.from_free - 
			      young_gen.from_begin))/((float) young_gen.heap_size);

  young_gen.avg_occupancy = (young_gen.avg_occupancy*num_collections + 
			     young_occupancy)/(num_collections + 1);
  num_collections++;

  //printf("Minor GC number %d\n", num_collections);

  cleanup_after_threaded_GC();
}


/* effects: initializes heap */
void generational_gc_init()
{
  size_t bytes_to_map, heap_size, mapped_per_space;
  void *mapped;
  int i;
  struct block *new_block;

  SYSTEM_PAGE_SIZE = getpagesize();
  PAGE_MASK = SYSTEM_PAGE_SIZE - 1;

  printf("%d\n", sizeof(void *));

  bytes_to_map = INITIAL_PAGES_TO_MAP_TOTAL*SYSTEM_PAGE_SIZE;

  // reserve part of the virtual address space
  fd = open("/dev/zero", O_RDONLY);
  mapped = mmap(0, bytes_to_map, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);

  // could not allocate memory. we really should throw an exception,  
  // but there are various problems with this. even if we pre-allocated
  // the exception object, throwing the exception would involve
  // allocating more memory.
  assert(mapped != MAP_FAILED);

  // calculate heap and page size
  heap_size = INITIAL_PAGES_PER_HEAP*SYSTEM_PAGE_SIZE;
  mapped_per_space = INITIAL_PAGES_TO_MAP_PER_SPACE*SYSTEM_PAGE_SIZE;

  // allocate copying generation at the bottom of the mapped space
  init_copying_heap(mapped, heap_size, 2*mapped_per_space, &young_gen);

  // allocate marksweep generation in the last 1/3 of the mapped space
  mapped += 2*bytes_to_map/3;
  init_marksweep_heap(mapped, heap_size, mapped_per_space, &old_gen);
}


/* effects: handles references to objects. objects in from-space are copied
   to to-space. if the object is already in to-space, the pointer is updated */
void generational_handle_reference(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = PTRMASK((*ref));

  // check if the object is in from-space
  if (IN_FROM_SPACE(obj, &young_gen))
    {
      // if the claz pointer is unmodified,
      // the object has not yet been moved
      if (CLAZ_OKAY(obj))
	{
	  debug_verify_object(obj);

	  // if the object has survived a collection, promote it
	  if ((void *)obj < young_gen.survived)
	    {
	      int retval;

	      retval = promote_to_old_gen(ref);

	      // if success, done
	      if (retval == 0)
		{
		  generational_marksweep_reference(PTRMASK(*ref));
		  return;
		}
	    }
	  
	  // if still here, then move to to-space
	  move_to_to_space(ref);
	}
      else
	// handle moved objects
	(*ref) = (jobject_unwrapped) obj->claz;
    }
  else if (collection_type == MAJOR && in_marksweep_heap(obj, &old_gen))
    generational_marksweep_reference(obj);
}


/* requires: that object be in a marksweep heap
   effects: handles references to objects in the heap by
   marking untouched objects and tracing roots in them.
*/
void generational_marksweep_reference(jobject_unwrapped obj)
{
  // get a pointer to the block
  struct block *bl = (void *)obj - BLOCK_HEADER_SIZE;
  // mark or check for mark
  if (NOT_MARKED(bl))
    {
      MARK_AS_REACHABLE(bl);
      trace(obj);
    }
  else
    assert(MARKED_AS_REACHABLE(bl));
}


/* returns: a pointer to the requested amt of memory allocated
   by the generational gc */
void *generational_malloc(size_t size)
{
  static size_t bytes_since_last_GC = 0;
  size_t aligned_size;
  void *result = NULL;
  int collected = 0;
  int grew_young_gen = 0;

  ENTER_GENERATIONAL_GC();

#ifdef GC_EVERY_TIME
  generational_collect();
#endif

  assert(old_gen.free_list->size != 0);

  // for large objects, try to allocate in the old
  // generation so we don't have to move them around
  if (size > SMALL_OBJ_SIZE)
    {
      result = allocate_in_marksweep_heap(size, &old_gen);

      if (result != NULL)
	{
	  printf(":");
	  EXIT_GENERATIONAL_GC();
	  return result;
	}
    }

  aligned_size = align(size);
  
  /*
  if (young_gen.heap[young_gen.from].free + aligned_size >
      young_gen.heap[young_gen.from].end)
    generational_collect();

  if (young_gen.heap[young_gen.from].free + aligned_size >
      young_gen.heap[young_gen.from].end)
    grow_copying_heap(aligned_size, &young_gen);
  */


  while (young_gen.from_free + aligned_size > young_gen.from_end)
    {
      if ((!collected && major_collection_makes_sense(bytes_since_last_GC)) ||
	  grew_young_gen)
	{
	  generational_collect();
	  collected = 1;
	  bytes_since_last_GC = 0;
	}
      else if (!grew_young_gen)
	{
	  grow_copying_heap(aligned_size, &young_gen);
	  grew_young_gen = 1;
	}
      else
	{
	  printf("- HELP -");
	  return (void *)malloc(aligned_size);
	}
    }

  result = young_gen.from_free;
  young_gen.from_free += aligned_size;
  bytes_since_last_GC += aligned_size;
  printf(".");

  EXIT_GENERATIONAL_GC();

  return result;
}


/* effects: if the object resides in the young generation, it is added
   to the list of inflated objects that need to be deflated after the
   object has been garbage collected.
*/
void generational_register_inflated_obj(jobject_unwrapped obj)
{
  register_inflated_obj(obj, &young_gen);
}


/* effects: move object from from-space to to-space, updating
   the given pointer and creating a forwarding reference */
void move_to_to_space(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = PTRMASK((*ref));
  void *forwarding_address;
  size_t obj_size = align(FNI_ObjectSize(obj));

  // relocating objects should not exceed the size of the heap
  assert(young_gen.to_free + obj_size <= young_gen.to_end);

  // copy over to to-space
  forwarding_address = TAG_HEAP_PTR(memcpy(young_gen.to_free, 
					   obj, 
					   obj_size));
  
  // write forwarding address to previous location
  obj->claz = (struct claz *)forwarding_address;
  
  // update given reference
  (*ref) = forwarding_address;

  // increment free
  young_gen.to_free += obj_size;
}


/* effects: promotes object from from-space to the old generation, 
   updating the given pointer and creating a forwarding reference. 
   returns: 0 if success, -1 if fail (lack of space in old generation)
*/
int promote_to_old_gen(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = PTRMASK((*ref));
  void *forwarding_address;
  size_t obj_size = align(FNI_ObjectSize(obj));

  // get a block of memory in the old generation
  forwarding_address = allocate_in_marksweep_heap(obj_size, &old_gen);

  if (forwarding_address == NULL)
    return -1;

  // copy over to the newly-allocated space
  forwarding_address = TAG_HEAP_PTR(memcpy(forwarding_address, 
					   obj, 
					   obj_size));
  
  // write forwarding address to previous location
  obj->claz = (struct claz *)forwarding_address;
  
  // update given reference
  (*ref) = forwarding_address;

  return 0;
}
