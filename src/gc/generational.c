#include <assert.h>
#include <fcntl.h>
#include <stdlib.h> /* for malloc */
#include <sys/mman.h>
#include <unistd.h> /* for getpagesize */
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

#ifdef WITH_PRECISE_GC_STATISTICS
extern int num_promoted;
#endif

//#define GC_EVERY_TIME
#define WITH_WRITE_BARRIER_REMOVAL
#define INITIAL_PAGES_TO_MAP_PER_SPACE 16384
#define INITIAL_PAGES_TO_MAP_TOTAL     (3*INITIAL_PAGES_TO_MAP_PER_SPACE)
#define INITIAL_PAGES_PER_HEAP  16
#define MINOR                 0
#define MAJOR                 1

#ifndef JOLDEN_WRITE_BARRIER
FLEX_MUTEX_DECLARE_STATIC(intergenerational_roots_mutex);
#endif
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

#ifdef JOLDEN_WRITE_BARRIER
// array of intergenerational references
//#define INTERGEN_LENGTH 3700000
#define INTERGEN_LENGTH 20000000
jobject_unwrapped **intergen;
#ifdef ARRAY_WB 
int intergen_next = 0;
#else // !ARRAY_WB
jobject_unwrapped **intergen_next;
#endif // !ARRAY_WB
#else // !JOLDEN_WRITE_BARRIER
// linked list of intergenerational references
struct ref_list {
  jobject_unwrapped *ref;
  struct ref_list *next;
};
static struct ref_list *roots = NULL;
#endif // !JOLDEN_WRITE_BARRIER

// intergenerational pointers by promotion
static struct obj_list *objs_curr = NULL;
static struct obj_list *objs_last = NULL;
static struct obj_list *objs_next = NULL;
static struct obj_list *objs_free = NULL;

#ifdef JOLDEN_WRITE_BARRIER
struct marksweep_heap old_gen;
static struct copying_heap young_gen;
#else
static struct marksweep_heap old_gen;
static struct copying_heap young_gen;
#endif

static int fd = 0;

static int collection_type = MINOR;

/* function declarations */

void find_roots_in_obj_list();

struct obj_list *get_new_list_element();

int major_collection_makes_sense(size_t bytes_since_last_GC);

int minor_collection_makes_sense(size_t bytes_since_last_GC);

void generational_print_heap();

void remove_from_list(struct obj_list *to_free);

/* add object reference to list of references to objects that contain roots.
 */
void add_to_curr_obj_list(jobject_unwrapped aligned)
{
  struct obj_list *obj = get_new_list_element();
  obj->obj = aligned;
  obj->next = objs_curr;
  objs_curr = obj;
}


/* add object reference to list of references to objects that contain roots */
void add_to_next_obj_list(jobject_unwrapped aligned)
{
  struct obj_list *obj = get_new_list_element();
  obj->obj = aligned;
  obj->next = objs_next;
  objs_next = obj;
}


/* effects: adds inter-generational pointers to the root set
   for minor collections.
*/
void find_generational_refs()
#ifdef JOLDEN_WRITE_BARRIER
{
  if (collection_type == MINOR)
    {
      //FLEX_MUTEX_LOCK(&intergenerational_roots_mutex);

#ifdef ARRAY_WB
      {
	int intergen_saved = 0;
	int intergen_curr, intergen_end;

	intergen_end = intergen_next;

	// scan intergenerational pointers, compacting list as we go
	for(intergen_curr = 0; intergen_curr < intergen_end; intergen_curr++) {
	  jobject_unwrapped *ref = intergen[intergen_curr];

#ifndef WITH_GENERATION_CHECK
	  if ((IN_MARKSWEEP_HEAP(ref, old_gen)) &&
	      (IN_FROM_SPACE(*ref, young_gen) || 
	       IN_TO_SPACE(*ref, young_gen)))
#else
	  if (IN_FROM_SPACE(*ref, young_gen) || 
	      IN_TO_SPACE(*ref, young_gen))
#endif
	    {
	      add_to_root_set(ref, 0);
	      intergen[intergen_saved++] = ref;
	    }
	}
	// add to compacted list any new intergenerational references
	for( ; intergen_curr < intergen_next; intergen_curr++)
	  intergen[intergen_saved++] = intergen[intergen_curr];

	// array of references compacted
	intergen_next = intergen_saved;

      }
#else
      {
	jobject_unwrapped **intergen_saved = intergen;
	jobject_unwrapped **intergen_curr, **intergen_end;

	intergen_end = intergen_next;

	// scan intergenerational pointers, compacting list as we go
	for(intergen_curr = intergen; intergen_curr < intergen_end;
	    intergen_curr++)
	  {
	    //jobject_unwrapped *ref = intergen_curr;
	    jobject_unwrapped *ref = *intergen_curr;

#ifndef WITH_GENERATION_CHECK
	    if (IN_MARKSWEEP_HEAP(ref, old_gen) &&
		(IN_FROM_SPACE(*ref, young_gen)||IN_TO_SPACE(*ref, young_gen)))
#else
	    if (IN_FROM_SPACE(*ref, young_gen)||IN_TO_SPACE(*ref, young_gen))
#endif
	      {
		add_to_root_set(ref, 0);

		{
#ifdef PTR_WB
		  *intergen_saved++ = *intergen_curr;
#else		  
		  // manual pointer disambiguation; equivalent to above.
		  jobject_unwrapped **t = intergen_saved;
		  jobject_unwrapped *pt = *intergen_curr;
		  *t = pt;
		  t++;
		  intergen_saved = t;
#endif
		}
	      }
	  }
	// add to compacted list any new intergenerational references
	for( ; intergen_curr < intergen_next; intergen_curr++)
	  {
#ifdef PTR_WB
	    *intergen_saved++ = *intergen_curr;
#else
	    // manual pointer disambiguation; equivalent to above.
	    jobject_unwrapped **t = intergen_saved;
	    jobject_unwrapped *pt = *intergen_curr;
	    *t = pt;
	    t++;
	    intergen_saved = t;
#endif
	  }

	// array of references compacted
	intergen_next = intergen_saved;
      }
#endif
      //FLEX_MUTEX_UNLOCK(&intergenerational_roots_mutex);

      find_roots_in_obj_list();
    }
}
#else
{
  if (collection_type == MINOR)
    {
      //struct ref_list *ref = roots, *prev = NULL;
      struct ref_list *old_roots, *ref, *prev = NULL;

      FLEX_MUTEX_LOCK(&intergenerational_roots_mutex);
      old_roots = roots;
      roots = NULL;
      FLEX_MUTEX_UNLOCK(&intergenerational_roots_mutex);

      ref = old_roots;
      
      while (ref != NULL)
	{
#ifndef WITH_GENERATION_CHECK
	  if (IN_MARKSWEEP_HEAP(ref->ref, old_gen) &&
	      (IN_FROM_SPACE(*(ref->ref), young_gen) ||
	       IN_TO_SPACE(*(ref->ref), young_gen)))
#else
	  if (IN_FROM_SPACE(*(ref->ref), young_gen) ||
	       IN_TO_SPACE(*(ref->ref), young_gen))
#endif
	    {
	      add_to_root_set(ref->ref, 0);
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
		  old_roots = ref->next;
		  free(ref);
		  ref = old_roots;
		}
	    }
	}

      if (prev != NULL) {
	FLEX_MUTEX_LOCK(&intergenerational_roots_mutex);
	prev->next = roots;
	roots = old_roots;
	FLEX_MUTEX_UNLOCK(&intergenerational_roots_mutex);
      }

      find_roots_in_obj_list();
    }
}
#endif


/* traverses objs_curr list and handles objects that contain intergenerational
   pointers. */
void find_roots_in_obj_list()
{
  struct obj_list *obj = objs_curr;
  struct obj_list *prev = NULL;
  
  while (obj != NULL)
    {
      jobject_unwrapped aligned = obj->obj;
      
#ifdef WITH_DYNAMIC_WB
      if ((DYNAMIC_WB_ON((jobject_unwrapped) PTRMASK(aligned))))
	{
	  // do not remove object from list
	  trace(aligned, 0);
	  
	  prev = obj;
	  obj = obj->next;
	}
      else
#endif   
	{
	  // remove object from list, but first,
	  // use as roots and add to remembered
	  // set, if necessary

	  if (IN_MARKSWEEP_HEAP(aligned, old_gen)) {
	    // add to remembered set if necessary
	    trace(aligned, 1);
	  }
	  
	  // remove from list
	  if (prev == NULL) {
	    objs_curr = obj->next;
	    remove_from_list(obj);
	    obj = objs_curr;
	  } else {
	    prev->next = obj->next;
	    remove_from_list(obj);
	    obj = prev->next;
	  }
	}
    }
  // note end of list so new objects can be added on
  objs_last = prev;
}


/* returns a free element off the objs_free list, or, if none available,
   return newly-allocated element. */
struct obj_list *get_new_list_element()
{
  struct obj_list *result;
  if (objs_free == NULL) {
    result = (struct obj_list *) malloc(sizeof(struct obj_list));
  } else {
    result = objs_free;
    objs_free = objs_free->next;
  }
  return result;
}


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

  /*
  printf("YOUNG TO:\t%p to %p\n", young_gen.to_begin, young_gen.to_end);
  printf("YOUNG FROM:\t%p to %p\n", young_gen.from_begin, young_gen.from_free);
  printf("OLD     :\t%p to %p\n", old_gen.heap_begin, old_gen.heap_end);
  */

  scan = young_gen.to_begin;

  find_root_set();

  while(scan < young_gen.to_free)
    {
      trace((jobject_unwrapped)scan, 0);
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
  if (objs_last == NULL) {
    objs_curr = objs_next;
  } else {
    objs_last->next = objs_next;
  }
  objs_next = NULL;

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

#if defined(WITH_DYNAMIC_WB) && defined(WITH_PRECISE_GC_STATISTICS)
  init_statistics();
#endif

#ifdef JOLDEN_WRITE_BARRIER
  // allocate space for intergenerational pointers
  intergen = (jobject_unwrapped **) 
    malloc(INTERGEN_LENGTH*sizeof(jobject_unwrapped *));
#ifndef ARRAY_WB
  intergen_next = intergen;
#endif // ARRAY_WB
#endif // JOLDEN_WRITE_BARRIER
}

#ifndef JOLDEN_WRITE_BARRIER
void generational_write_barrier(jobject_unwrapped *ref)
{
  struct ref_list *root;

  root = (struct ref_list *)malloc(sizeof(struct ref_list));
  root->ref = ref;
  
  FLEX_MUTEX_LOCK(&intergenerational_roots_mutex);
  root->next = roots;
  roots = root;
  FLEX_MUTEX_UNLOCK(&intergenerational_roots_mutex);
}
#else
static inline void no_lock_generational_write_barrier(jobject_unwrapped *ref)
#ifdef ARRAY_WB
{
  intergen[intergen_next++] = ref;
}
#elif defined(PTR_WB)
{
  *intergen_next++ = ref;
}
#else
{
  jobject_unwrapped **t;
  t = intergen_next;
  *t = ref;
  t++;
  intergen_next = t;
}
#endif
#endif

/* requires: that wbtype be one of 0 (do not add this location to the
   remembered set), or 1 (add this location to the remembered set if it 
   contains a pointer to an object in the young generation).
   effects: handles references to objects. objects in from-space are copied
   to to-space. if the object is already in to-space, the pointer is updated */
void generational_handle_reference(jobject_unwrapped *ref, int wbtype)
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
		  jobject_unwrapped aligned = PTRMASK(*ref);

#ifdef WITH_DYNAMIC_WB
		  if (DYNAMIC_WB_ON(aligned))
		    {
#ifdef WITH_PRECISE_GC_STATISTICS
		      num_promoted++;
#endif
		      add_to_next_obj_list(aligned);
		      trace(aligned, 0);
		    } 
		  else
#endif
		    {
		      trace(aligned, 1);
		    }
		  return;
		}
	    }
	  
	  // if still here, then move to to-space
	  relocate_to_to_space(ref, &young_gen);

	  if (wbtype) {
	    // add this location to the remembered set
	    generational_write_barrier(ref);
	  }
	}
      else
	{
	  // handle moved objects
	  (*ref) = (jobject_unwrapped) obj->claz;

	  if (wbtype && IN_TO_SPACE(*ref, young_gen)) {
	    // add this location to the remembered set
	    generational_write_barrier(ref);
	  }
	}
    }
  else if (IN_TO_SPACE(obj, young_gen))
    {
      // reference already updated
      if (wbtype) {
	generational_write_barrier(ref);
      }
    }
  else if (collection_type == MAJOR && IN_MARKSWEEP_HEAP(obj, old_gen))
    {
      struct block *bl = (void *)obj - BLOCK_HEADER_SIZE;

      // if the claz pointer is unmodified,
      // mark or check for mark
      if (NOT_MARKED(bl))
	{
	  MARK_AS_REACHABLE(bl);
	  trace(obj, 0);
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
#ifndef WITH_WRITE_BARRIER_REMOVAL
  int grew_old_gen = 0;
#endif

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
  //char c;
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
	    printf("%10x ", *w);
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
	      printf("%10p: %10x\n", word, *word);
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

int in_young_gen(jobject_unwrapped obj)
{
  return (IN_FROM_SPACE(obj, young_gen) || IN_TO_SPACE(obj, young_gen));
}

int in_old_gen(jobject_unwrapped obj)
{
  return IN_MARKSWEEP_HEAP(obj, old_gen);
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
  return (bytes_since_last_GC > cost);
}

/* adds given obj_list element to the free list */
void remove_from_list(struct obj_list *to_free)
{
  to_free->obj = NULL;
  to_free->next = objs_free;
  objs_free = to_free;
}
