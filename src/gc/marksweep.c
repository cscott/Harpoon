#include <fcntl.h>
#include <sys/mman.h>
#include <assert.h>
#include "jni-types.h"
#include "jni-private.h"
#include "jni-gc.h"
#include "precise_gc.h"
#ifdef WITH_THREADED_GC
#include "flexthread.h"
#endif

#ifndef WITH_PRECISE_C_BACKEND
# error /* unimplemented */
#else

#define BLOCK_SIZE        (sizeof(struct block))
#define BLOCK_HEADER_SIZE (sizeof(struct block_header))
#define PAGE_HEADER_SIZE  (sizeof(struct page_header))
#define INITIAL_HEAP_SIZE 131072
#define SMALL_BLOCK_SIZE  (72 + BLOCK_HEADER_SIZE)
#define NUM_SMALL_BLOCK_SIZES ((SMALL_BLOCK_SIZE - BLOCK_SIZE)/ALIGN_TO + 1)

#ifdef DEBUG_GC
static int num_mallocs = 0;
static size_t total_memory_requested = 0;
#endif

struct block_header {
  size_t size_of_block;
  // when a block of memory is in use, the mark bit should be clear
  // except during collection, when it may be set. when a block of
  // memory is not in use, markunion is the link to the next free block.
  // during collection, if the block of memory is in use, (markunion - 2)
  // is the index of the field being examined.
  union { ptroff_t mark; struct block *next; } markunion;
};
/* a block of memory contains a header with information about the block */
struct block {
  struct block_header bheader;
  struct oobj object;
};

struct page_header {
  size_t page_size;
  struct page *next;
};

struct page {
  struct page_header pheader;
  struct block page_contents;
};

#define UNREACHABLE 1
#define REACHABLE   2
#define MARK_OFFSET 3

#define GET_INDEX(bl) (bl->bheader.markunion.mark - MARK_OFFSET);

#define SET_INDEX(bl,index) \
({ (bl)->bheader.markunion.mark = (index) + MARK_OFFSET; })

#define MARK_AS_REACHABLE(bl) \
({ (bl)->bheader.markunion.mark = REACHABLE; })

#define CLEAR_MARK(bl) \
({ (bl)->bheader.markunion.mark = UNREACHABLE; })

#define MARKED_AS_REACHABLE(bl) ((bl)->bheader.markunion.mark == REACHABLE)
#define NOT_MARKED(bl) ((bl)->bheader.markunion.mark == UNREACHABLE)

/* we keep a singly-linked list of free blocks of memory */
static struct block *free_list;
/* we keep a singly-linked list of allocated pages */
static struct page *page_list;
/* we keep an array of singly-linked list of memory blocks of frequently-requested sizes */
static struct block *small_blocks[NUM_SMALL_BLOCK_SIZES];

#ifndef WITH_THREADED_GC
# define ENTER_MARKSWEEP_GC()
# define EXIT_MARKSWEEP_GC()
#else
/* mutex for the GC variables */
FLEX_MUTEX_DECLARE_STATIC(marksweep_gc_mutex);

/* barriers for synchronizing threads */
extern pthread_barrier_t before;
extern pthread_barrier_t after;

extern pthread_cond_t done_cond;
extern pthread_mutex_t done_mutex;
extern int done_count;
extern jint halt_for_GC_flag;

/* effects: acquire locks for marksweep GC in "safe" manner (non-blocking) */
# define ENTER_MARKSWEEP_GC() \
({ while (pthread_mutex_trylock(&marksweep_gc_mutex)) \
if (halt_for_GC_flag) halt_for_GC(); })

/* effects: release locks for marksweep GC */
#define EXIT_MARKSWEEP_GC() FLEX_MUTEX_UNLOCK(&marksweep_gc_mutex)

#endif /* WITH_THREADED_GC */

/* function declarations */
void free_unreachable_blocks();

static size_t free_memory;
static size_t heap_size;

/* returns: amt of free memory available */
jlong marksweep_free_memory()
{
  jlong result;
  ENTER_MARKSWEEP_GC();
  result = free_memory;
  EXIT_MARKSWEEP_GC();
  return result;
}


/* returns: heap size */
jlong marksweep_get_heap_size()
{
  jlong result;
  ENTER_MARKSWEEP_GC();
  result = heap_size;
  EXIT_MARKSWEEP_GC();
  return result;
}


/* returns: 1 if the specified object was allocated by us, 0 otherwise */
int allocated_by_marksweep(void *ptr_to_oobj) {
  struct page *curr_page = page_list;
  while(curr_page != NULL) {
    if (ptr_to_oobj > (void *)&(curr_page->page_contents) && 
	ptr_to_oobj < (void *)curr_page + curr_page->pheader.page_size)
      return 1;
    else
      curr_page = curr_page->pheader.next;
  }
  return 0;
}


/* effects: adds the given block in the appropriate place in the free list */
void add_to_free_list(struct block *new_block)
{
  struct block *curr = free_list;
  struct block *prev = NULL;
  size_t bl_size;
  
  bl_size = new_block->bheader.size_of_block;
  
  //assert(bl_size > SMALL_OBJ_SIZE + BLOCK_HEADER_SIZE);

  error_gc("Adding %d bytes to free list.\n", bl_size);

  while (curr != NULL)
    {
      if (curr->bheader.size_of_block < bl_size)
	{
	  prev = curr;
	  curr = curr->bheader.markunion.next;
	}
      else
	break;
    }
  new_block->bheader.markunion.next = curr;
  if (prev == NULL)
    // result is first element in list
    free_list = new_block;
  else
    prev->bheader.markunion.next = new_block;
}


/* effects: adds the given block to the small blocks table, if applicable or, if not, to the free list */
void faster_add_to_free_list(struct block *new_block)
{
  size_t nb_size = new_block->bheader.size_of_block;

  // "small" blocks are stored separately for fast allocation
 if (nb_size <= SMALL_BLOCK_SIZE)
    {
      int index = (nb_size - BLOCK_SIZE)/ALIGN_TO;
      
      error_gc("Adding %d bytes to small blocks table.\n", nb_size);

      assert(index >= 0 && index < NUM_SMALL_BLOCK_SIZES);
      
      new_block->bheader.markunion.next = small_blocks[index];
      small_blocks[index] = new_block;
    }
 else
   add_to_free_list(new_block);
}


void create_new_page(size_t size_of_page) {
  static int fd = 0;
  struct page *curr_page = page_list;
  struct page *prev_page = NULL;
  struct page **new_page_ptr;
  struct block *new_block;
#ifdef DEBUG_GC
  static size_t num_pages = 1;
  static size_t total_size = 0;
#endif
  if (fd == 0) fd = open("/dev/zero", O_RDONLY);
  while (curr_page != NULL) {
    prev_page = curr_page;
    curr_page = curr_page->pheader.next;
  }
  new_page_ptr = (prev_page == NULL) ? &page_list : &(prev_page->pheader.next);
  (*new_page_ptr) = mmap
    (0, size_of_page, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
  assert((*new_page_ptr) != MAP_FAILED);
  // debug messages
  error_gc("create_new_page no.%d ", num_pages++);
  error_gc("at %p ", (*new_page_ptr));
  error_gc("of size %d ", size_of_page);
  error_gc("for a total af %d bytes\n", (total_size += size_of_page));
  // initializing allocated page
  (*new_page_ptr)->pheader.page_size = size_of_page;
  (*new_page_ptr)->pheader.next = NULL;
  // initializing free block
  new_block = &((*new_page_ptr)->page_contents);
  new_block->bheader.size_of_block = size_of_page - PAGE_HEADER_SIZE;
  new_block->bheader.markunion.next = NULL;
  faster_add_to_free_list(new_block);
}


/* effects: marks object as well as any objects pointed to by it */
void marksweep_handle_reference(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = (jobject_unwrapped)PTRMASK((*ref));
  if (allocated_by_marksweep(obj))
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

void marksweep_collect()
{
  static int count = 1;
  error_gc("\nGC number %d\n", count++);
  find_root_set();
  free_unreachable_blocks();
  error_gc("done!\n", "");
}

void marksweep_gc_init() {
  int i;
  free_memory = heap_size = INITIAL_HEAP_SIZE;

  // initialize small blocks lists
  for(i = 0; i < NUM_SMALL_BLOCK_SIZES; i++)
    small_blocks[i] = NULL;

  create_new_page(INITIAL_HEAP_SIZE);
}


/* returns: smallest block that is greater than or equal in size to request
            block is initialized before being returned
 */
struct block *find_free_block(size_t size) {
  struct block *curr = free_list;
  struct block *prev = NULL;
  while (curr != NULL) {
    if (curr->bheader.size_of_block < size) {
      prev = curr;
      curr = curr->bheader.markunion.next;
    } else {
      // done!
      break;
    }
  }
  // no free blocks
  if (curr == NULL) return NULL;
  // remove result from free list
  if (prev == NULL) 
    // result is first element in list
    free_list = curr->bheader.markunion.next;
  else
    prev->bheader.markunion.next = curr->bheader.markunion.next;
  if (curr->bheader.size_of_block > (size + BLOCK_SIZE)) {
    // this block is big enough to split
    struct block *new_block = (struct block *)((void *)curr + size);
    // split block up
    new_block->bheader.size_of_block = curr->bheader.size_of_block - size;
    curr->bheader.size_of_block = size;
    // insert in free list
    faster_add_to_free_list(new_block);
  }
  // clear mark bit
  CLEAR_MARK(curr);
  return curr;
}


/* returns: a block that is greater than or equal in size to request
            block is initialized before being returned
 */
struct block *faster_find_free_block(size_t size) {
  if (size <= SMALL_BLOCK_SIZE)
    {
      int index = (size - BLOCK_SIZE)/ALIGN_TO;
      struct block *result;

      assert(index >= 0 && index < NUM_SMALL_BLOCK_SIZES);

      result = small_blocks[index];

      if (result != NULL)
	{
	  // remove block from list
	  small_blocks[index] = result->bheader.markunion.next;
	  // initialize block
	  CLEAR_MARK(result);
	  // return
	  return result;
	}
    }
  return find_free_block(size);
}


#ifndef DEBUG_GC
# define debug_clear_memory(bl)
# define debug_verify_block(bl)
#else
void debug_clear_memory(struct block *bl)
{
  memset(&(bl->object), 7, (bl->bheader.size_of_block - BLOCK_HEADER_SIZE));
}


void debug_verify_block(struct block *bl)
{
  struct block *begin = free_list;

  while (begin != NULL)
    {
      struct block *end = ((void *)begin) + begin->bheader.size_of_block;
      struct claz *claz_ptr;

      assert(!((ptroff_t)bl & 1));

      // check that this is not something from the free list
      assert(!(begin <= bl && bl < end));

      // check that the claz ptr is correct
      claz_ptr = bl->object.claz;
      assert(&claz_start <= claz_ptr && claz_ptr < &claz_end);
      
      begin = begin->bheader.markunion.next;
    }
}
#endif


void free_unreachable_blocks() {
  struct page *curr_page = page_list;
  while (curr_page != NULL) {
    struct block *curr_block = &(curr_page->page_contents);
    size_t to_be_scanned = curr_page->pheader.page_size - PAGE_HEADER_SIZE;
    // scan page for free blocks
    while (to_be_scanned > 0) {
      size_t size_of_curr_block = curr_block->bheader.size_of_block;

      if (MARKED_AS_REACHABLE(curr_block))
	// marked, cannot be freed; unmark
	CLEAR_MARK(curr_block);
      else if (NOT_MARKED(curr_block))
	{
	  // not marked, can be freed
	  free_memory += size_of_curr_block;
	  debug_clear_memory(curr_block);
	  faster_add_to_free_list(curr_block);
	} 

      // go to next block
      to_be_scanned -= size_of_curr_block;
      curr_block = (struct block *)((void *)curr_block + size_of_curr_block);
    }
    // go to next page
    curr_page = curr_page->pheader.next;
  }
}


void* marksweep_malloc(size_t size_in_bytes) {
  size_t aligned_size_in_bytes = 0;
  struct block_header *result = NULL;
  aligned_size_in_bytes = align(size_in_bytes + BLOCK_HEADER_SIZE);

  ENTER_MARKSWEEP_GC();

#ifndef GC_EVERY_TIME
  result = (struct block_header *)faster_find_free_block(aligned_size_in_bytes);
#endif

  if (result == NULL)
    {
      error_gc("\nx%x bytes needed\n", aligned_size_in_bytes);
      setup_for_threaded_GC();
      // don't have any free blocks handy, allocate new
      marksweep_collect();
      // see if we've managed to free the necessary memory
      result = (struct block_header *)faster_find_free_block(aligned_size_in_bytes);
      // okay, no, then allocate a new page
      if (result == NULL) {
	size_t size_of_new_page = aligned_size_in_bytes + PAGE_HEADER_SIZE;
	size_of_new_page = (size_of_new_page > heap_size) ? size_of_new_page : heap_size;
	heap_size += size_of_new_page;
	free_memory += size_of_new_page;
	// allocate new page
	create_new_page(size_of_new_page);
	result = (struct block_header *)faster_find_free_block(aligned_size_in_bytes);
      }
      cleanup_after_threaded_GC();
    }
  assert(result != NULL);

  /*
  printf("%d\t", ++num_mallocs);
  printf("Allocated %d bytes ", size_in_bytes);
  printf("at %p (for a total of ", result+1);
  printf("%d bytes)\n", (total_memory_requested += size_in_bytes));
  */
  error_gc("%d\t", ++num_mallocs);
  error_gc("Allocated %d bytes ", size_in_bytes);
  error_gc("at %p (for a total of ", result+1);
  error_gc("%d bytes)\n", (total_memory_requested += size_in_bytes));

  free_memory -= aligned_size_in_bytes;
  EXIT_MARKSWEEP_GC();

  return (result+1);
}


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

	  if (!allocated_by_marksweep(obj))
	    {
	      error_gc("%p not allocated by marksweep.\n", obj);
	      if (prev == NULL)
		done = 1;
	      break;
	    }
	  
	  debug_verify_block(bl);

	  // these objects are untouched
	  if (NOT_MARKED(bl))
	    {
	      ptroff_t next_index;
	      jobject_unwrapped *elements_and_fields;

	      error_gc("%p is unmarked.\n", obj);

	      assert(&claz_start <= obj->claz && obj->claz < &claz_end);

	      if (obj->claz->component_claz != NULL)
		{
		  // we have an array
		  struct aarray *arr = (struct aarray *) obj;
		  elements_and_fields = (jobject_unwrapped *)(arr->_padding_);
		}
	      else
		// we have a non-array object
		elements_and_fields = (jobject_unwrapped *)(obj->field_start);

	      next_index = get_next_index(obj, 0);

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

	  if (obj->claz->component_claz != NULL)
	    {
	      // prev is an array
	      struct aarray *arr = (struct aarray *)obj;
	      elements_and_fields = (jobject_unwrapped *)(arr->_padding_);
	    }
	  else
	      elements_and_fields = (jobject_unwrapped *)(obj->field_start);

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
