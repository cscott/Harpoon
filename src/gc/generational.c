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
#include "obj_list.h"
#include "omit_gc_timer.h"
#include "system_page_size.h"
#include "write_barrier.h"

//#define GC_EVERY_TIME
#define WITH_WRITE_BARRIER_REMOVAL
#define INITIAL_PAGES_TO_MAP_PER_SPACE 16384
#define INITIAL_PAGES_TO_MAP_TOTAL     (3*INITIAL_PAGES_TO_MAP_PER_SPACE)
#define INITIAL_PAGES_PER_HEAP  16
#define MINOR                 0
#define MAJOR                 1

#ifndef JOLDEN_WRITE_BARRIER
FLEX_MUTEX_DECLARE_STATIC(intergenerational_roots_mutex);
FLEX_MUTEX_DECLARE_STATIC(generational_gc_mutex);
#endif

#ifndef WITH_THREADED_GC
# define ENTER_GENERATIONAL_GC()
#else
extern jint halt_for_GC_flag;

# define ENTER_GENERATIONAL_GC() \
({ while (pthread_mutex_trylock(&generational_gc_mutex)) \
if (halt_for_GC_flag) halt_for_GC(); })
#endif

#define EXIT_GENERATIONAL_GC() FLEX_MUTEX_UNLOCK(&generational_gc_mutex)

#ifdef JOLDEN_WRITE_BARRIER

// array of intergenerational references
#define INTERGEN_LENGTH 3700000
jobject_unwrapped **intergen;
int intergen_next;
#else

// linked list of intergenerational references
struct ref_list {
  jobject_unwrapped *ref;
  struct ref_list *next;
};
static struct ref_list *roots = NULL;
#endif

// intergenerational pointers by promotion
static struct obj_list *objs_curr = NULL;
static struct obj_list *objs_next = NULL;

#ifdef JOLDEN_WRITE_BARRIER
struct marksweep_heap old_gen;
static struct copying_heap young_gen;
#else
static struct marksweep_heap old_gen;
static struct copying_heap young_gen;
#endif

static int fd = 0;

static collection_type = MINOR;

/* function declarations */

int major_collection_makes_sense(size_t bytes_since_last_GC);

int minor_collection_makes_sense(size_t bytes_since_last_GC);

void generational_print_heap();

/* effects: adds inter-generational pointers to the root set
   for minor collections.
*/
#ifdef JOLDEN_WRITE_BARRIER
void find_generational_refs()
{
  if (collection_type == MINOR)
    {
      int intergen_saved = 0;
      int intergen_curr;
      struct obj_list *obj = objs_curr;

      //FLEX_MUTEX_LOCK(&intergenerational_roots_mutex);

      for(intergen_curr = 0; intergen_curr < intergen_next; intergen_curr++)
	{
	  jobject_unwrapped *ref = intergen[intergen_curr];

	  //if (IN_FROM_SPACE(*ref, young_gen) || IN_TO_SPACE(*ref, young_gen))
	  if (IN_MARKSWEEP_HEAP(ref, old_gen) &&
	      (IN_FROM_SPACE(*ref, young_gen) || IN_TO_SPACE(*ref, young_gen)))
	    {
	      add_to_root_set(ref);
	      intergen[intergen_saved++] = intergen[intergen_curr];
	    }
	}
      // array of references compacted
      intergen_next = intergen_saved;
      //FLEX_MUTEX_UNLOCK(&intergenerational_roots_mutex);
      
      while (obj != NULL)
	{
	  trace(obj->obj);
	  obj = obj->next;
	}
    }
}
#else
void find_generational_refs()
{
  if (collection_type == MINOR)
    {
      struct ref_list *ref = roots, *prev = NULL;
      struct obj_list *obj = objs_curr;

      FLEX_MUTEX_LOCK(&intergenerational_roots_mutex);

      while (ref != NULL)
	{
	  if (IN_MARKSWEEP_HEAP(ref->ref, old_gen) &&
	      (IN_FROM_SPACE(*(ref->ref), young_gen) ||
	       IN_TO_SPACE(*(ref->ref), young_gen)))
	    {
	      add_to_root_set(ref->ref);
	      prev = ref;
	      ref = ref->next;
	    }
	  else
	    {
	      // remove from list
	      if (prev != NULL)
		{
		  prev->next = ref->next;
		  free(ref);
		  ref = prev->next;
		}
	      else
		{
		  roots = ref->next;
		  free(ref);
		  ref = roots;
		}
	    }
	}

      FLEX_MUTEX_UNLOCK(&intergenerational_roots_mutex);

      while (obj != NULL)
	{
	  trace(obj->obj);
	  obj = obj->next;
	}
    }
}
#endif


/* effects: garbage collects either just the young generation
   or the entire heap depending on the collection_type flag
*/
void generational_collect()
{
  static int num_collections = 0;
  float young_occupancy;
  void *scan;

  pause_timer();
  
  setup_for_threaded_GC();

  //generational_print_heap();

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

  // add the new inter-generational pointers
  objs_curr = objs_next;
  objs_next = objs_curr;

  // calculate heap occupancy
  young_occupancy = ((float) (young_gen.from_free - 
			      young_gen.from_begin))/((float) young_gen.heap_size);

  young_gen.avg_occupancy = (young_gen.avg_occupancy*num_collections + 
			     young_occupancy)/(num_collections + 1);
  num_collections++;

  //fprintf(stderr, "Minor GC number %d\n", num_collections);

  cleanup_after_threaded_GC();

  start_timer();
}


/* returns: amt of free memory available */
jlong generational_free_memory()
{
  jlong result;
  ENTER_GENERATIONAL_GC();
  result = (jlong)(young_gen.from_end - young_gen.from_free +
		   old_gen.free_memory);
  EXIT_GENERATIONAL_GC();
  return result;
}


/* returns: heap size */
jlong generational_get_heap_size()
{
  jlong result;
  ENTER_GENERATIONAL_GC();
  result = (jlong) (young_gen.heap_size + old_gen.heap_size);
  EXIT_GENERATIONAL_GC();
  return result;
}


/* effects: initializes heap */
void generational_gc_init()
{
  size_t bytes_to_map, heap_size, mapped_per_space;
  void *mapped;
  int i;
  struct block *new_block;

  // find out the system page size and pre-calculate some constants
  SYSTEM_PAGE_SIZE = getpagesize();
  PAGE_MASK = SYSTEM_PAGE_SIZE - 1;

  bytes_to_map = INITIAL_PAGES_TO_MAP_TOTAL*SYSTEM_PAGE_SIZE;

  assert(bytes_to_map != 0);

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

#ifdef JOLDEN_WRITE_BARRIER
  // allocate space for intergenerational pointers
  intergen = (jobject_unwrapped **) 
    malloc(INTERGEN_LENGTH*sizeof(jobject_unwrapped *));
  intergen_next = 0;
#endif
}


/* effects: handles references to objects. objects in from-space are copied
   to to-space. if the object is already in to-space, the pointer is updated */
void generational_handle_reference(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = PTRMASK(*ref);

  // check if the object is in from-space
  if (IN_FROM_SPACE(obj, young_gen))
    {
      // if the claz pointer is unmodified,
      // the object has not yet been moved
      if (CLAZ_OKAY(obj))
	{
	  debug_verify_object(obj);

	  // BIT EXPERIMENT: pretend we are stealing bit from
	  // claz pointer. in reality we would use hashcode bit.
	  //if ((void *)obj < young_gen.survived && 
	  //(((ptroff_t)(obj->claz) & 2) == 0))

	  // if the object has survived a collection, promote it
	  if ((void *)obj < young_gen.survived)
	    {
	      int retval;

	      retval = move_to_marksweep_heap(ref, &old_gen);

	      if (collection_type == MAJOR)
		// mark block as reachable
		MARK_AS_REACHABLE((struct block *)(ref - BLOCK_HEADER_SIZE));

	      // if success, done
	      if (retval == 0)
		{
		  struct obj_list *obj;
		  obj = (struct obj_list *)malloc(sizeof(struct obj_list));
		
		  obj->obj = PTRMASK(*ref);
		  obj->next = objs_next;
		  objs_next = obj;

		  //printf("Adding %p to roots\n", *ref);
		  trace(PTRMASK(*ref));
		  return;
		}
	    }
	  
	  // if still here, then move to to-space
	  relocate_to_to_space(ref, &young_gen);
	}
      else
	// handle moved objects
	(*ref) = (jobject_unwrapped) obj->claz;
    }
  else if (collection_type == MAJOR && IN_MARKSWEEP_HEAP(obj, old_gen))
    {
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
}


/* returns: a pointer to the requested amt of memory allocated
   by the generational gc */
void *generational_malloc(size_t size)
{
  static size_t bytes_since_last_GC = 0;
  size_t aligned_size;
  void *result = NULL;
  int collected_minor = 0;
  int collected_major = 0;
  int grew_young_gen = 0;
  int grew_old_gen = 0;

  ENTER_GENERATIONAL_GC();

#ifdef GC_EVERY_TIME
  generational_collect();
#endif

  // for large objects, try to allocate in the old
  // generation so we don't have to move them around
  
#ifndef WITH_WRITE_BARRIER_REMOVAL
  if (!size > SMALL_OBJ_SIZE)
    {
      result = allocate_in_marksweep_heap(size, &old_gen);

      if (result != NULL)
	{
	  //fprintf(stderr, ":");
	  EXIT_GENERATIONAL_GC();
	  return result;
	}
    }
#endif

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
      if (!(collected_minor || collected_major) &&
	  minor_collection_makes_sense(bytes_since_last_GC))
	{
	  // run a minor collection and clear statistics
	  generational_collect();
	  collected_minor = 1;
	  bytes_since_last_GC = 0;
	} 
      else if (!(collected_minor || collected_major) &&
	       major_collection_makes_sense(bytes_since_last_GC))
	{
	  // run a major collection and clear statistics
	  collection_type = MAJOR;
	  generational_collect();
	  collected_major = 1;
	  bytes_since_last_GC = 0;
	  // re-set collection_type
	  collection_type = MINOR;
	} 
      else if (!grew_young_gen)
	{
	  // doesn't make sense to collect, expand the heap
	  grow_copying_heap(aligned_size, &young_gen);
	  grew_young_gen = 1;
	} 
#ifndef WITH_WRITE_BARRIER_REMOVAL
      else if (!grew_old_gen)
	{
	  // already grew the young generation, and may
	  // have collected, but still no space
	  expand_marksweep_heap(size, &old_gen);
	  grew_old_gen = 1;
	  result = allocate_in_marksweep_heap(size, &old_gen);

	  if (result != NULL)
	    {
	      EXIT_GENERATIONAL_GC();
	      return result;
	    }
	}
#endif
      else if (!collected_minor)
	{
	  // already tried to expand both heaps
	  generational_collect();
	  collected_minor = 1;
	  bytes_since_last_GC = 0;
	}
      else if (!collected_major)
	{
	  // already tried to expand both heaps
	  // and run a minor collection
	  collection_type = MAJOR;
	  generational_collect();
	  collected_major = 1;
	  bytes_since_last_GC = 0;
	  collection_type = MINOR;
	}
      else
	{
	  fprintf(stderr, "OUT OF MEMORY ERROR\n");
	  return (void *)malloc(aligned_size);
	}

      /*
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
	  //printf("- HELP -");
	  return (void *)malloc(aligned_size);
	}
	*/
    }

  result = young_gen.from_free;
  young_gen.from_free += aligned_size;
  bytes_since_last_GC += aligned_size;
  //fprintf(stderr, ".");

  EXIT_GENERATIONAL_GC();

  return result;
}

#define NCOLUMNS 4

/* effects: prints the contents of the heap to stdout */
void generational_print_heap()
{
  char c;
  size_t nentries = (young_gen.from_free - 
		     young_gen.from_begin)/sizeof(ptroff_t);
  size_t nrows = (nentries + NCOLUMNS - 1)/NCOLUMNS;
  int j;
  struct block *bl;

  nrows = (nrows < 16) ? 16 : nrows;

  printf("FROM SPACE %p -> %p\n", young_gen.from_begin, young_gen.from_free);

  for(j = 0; j < nrows; j++) 
    {
      ptroff_t *row_begin = (ptroff_t *)young_gen.from_begin + j;
      int i;

      if ((void *)row_begin >= young_gen.from_free)
	break;
      if ((void *)row_begin == young_gen.from_begin)
	{
	  printf("          | ");

	  for(i = 0; i < NCOLUMNS; i++)
	    printf("%10p ", (ptroff_t *)row_begin + i*nrows);

	  printf("\n-----------");
	  for(i = 0; i < NCOLUMNS; i++)
	    printf("-----------");
	  printf("\n");
	}

      printf("%10p| ", row_begin);

      for(i = 0; i < NCOLUMNS; i++)
	{
	  ptroff_t *w = row_begin + i*nrows;
	  
	  if ((void *)w < young_gen.from_free)
	    printf("%10p ", *w);
	  else
	    break;
	}
      printf("\n");
    }

  printf("\n");
  printf("OLD GENERATION %p -> %p\n", old_gen.heap_begin, old_gen.heap_end);

  for(bl = (struct block *)old_gen.heap_begin; (void *)bl < old_gen.heap_end; )
    {
      size_t size = bl->size;

      printf("BLOCK @ %p of size %d mark %d:\n", bl, size, bl->markunion.mark);

      if (NOT_MARKED(bl) || MARKED_AS_REACHABLE(bl))
	{
	  ptroff_t *word = (ptroff_t *)bl->object;
	  bl = (void *)bl + size;
	  while(word < (ptroff_t *)bl)
	    {
	      printf("%10p: %10p\n", word, *word);
	      word++;
	    }
	}
      else
	bl = (void *)bl + size;
    }

  fflush(stdout);
  //scanf("%c", &c);
}


/* effects: if the object resides in the young generation, it is added
   to the list of inflated objects that need to be deflated after the
   object has been garbage collected.
*/
void generational_register_inflated_obj(jobject_unwrapped obj)
{
  register_inflated_obj(obj, &young_gen);
}

#ifdef JOLDEN_WRITE_BARRIER
inline void generational_write_barrier(jobject_unwrapped *ref)
{
  // if (IN_MARKSWEEP_HEAP(ref, old_gen))
    intergen[intergen_next++] = ref;
  //assert(intergen_next < INTERGEN_LENGTH);
}
#else
void generational_write_barrier(jobject_unwrapped *ref)
{
  struct ref_list *root;

  //fprintf(stderr, "WRITE BARRIER %p\n", ref);

  root = (struct ref_list *)malloc(sizeof(struct ref_list));
  root->ref = ref;
  
  FLEX_MUTEX_LOCK(&intergenerational_roots_mutex);
  root->next = roots;
  roots = root;
  FLEX_MUTEX_UNLOCK(&intergenerational_roots_mutex);
}
#endif

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
  return (bytes_since_last_GC > cost);
}
