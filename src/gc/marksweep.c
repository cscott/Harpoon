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
#define INITIAL_HEAP_SIZE 65536
#define SMALL_OBJ_SIZE    72
#define NUM_SMALL_OBJ_SIZES (SMALL_OBJ_SIZE/ALIGN_TO - 1)

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

/* we keep a singly-linked list of free blocks of memory */
static struct block *free_list;
/* we keep a singly-linked list of allocated pages */
static struct page *page_list;
/* we keep an array of singly-linked list of memory blocks of frequently-requested sizes */
static struct block *small_blocks[NUM_SMALL_OBJ_SIZES];

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


/* effects: adds the given block to the small blocks table */
void add_to_small_blocks(struct block *small_block)
{
  size_t bl_size;
  int index;
  
  bl_size = small_block->bheader.size_of_block;
  assert(bl_size <= SMALL_OBJ_SIZE + BLOCK_HEADER_SIZE);

  error_gc("Adding %d bytes to small blocks table.\n", bl_size);
  
  // "small" blocks are stored separately for fast allocation
  index = (bl_size - BLOCK_HEADER_SIZE - OBJ_HEADER_SIZE)/ALIGN_TO;
  assert(0 <= index && index < NUM_SMALL_OBJ_SIZES);

  small_block->bheader.markunion.next = small_blocks[index];
  small_blocks[index] = small_block;
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
  add_to_free_list(new_block);
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
    add_to_free_list(new_block);
  }
  // clear mark bit
  curr->bheader.markunion.mark = 0;
  return curr;
}

/* effects: marks object as well as any objects pointed to by it */
void marksweep_handle_reference(jobject_unwrapped *ref)
{
  jobject_unwrapped obj = (jobject_unwrapped)PTRMASK((*ref));
  struct block_header *root;
  if (allocated_by_marksweep((void *)obj))
    {
      // move back to top of block
      root = (struct block_header *)obj - 1;
      // mark or check for mark
      if (root->markunion.mark == 0)
	{
	  // if not already marked, mark and trace
	  error_gc("being marked.\n", "");
	  root->markunion.mark = 1;
	  trace(obj);
	}
      else
	// if already marked, done
	assert(root->markunion.mark == 1);
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
  free_memory = heap_size = INITIAL_HEAP_SIZE;
  create_new_page(INITIAL_HEAP_SIZE);
}


void* marksweep_malloc(size_t size_in_bytes) {
  size_t aligned_size_in_bytes = 0;
  struct block_header *result = NULL;
  aligned_size_in_bytes = align(size_in_bytes + BLOCK_HEADER_SIZE);

  ENTER_MARKSWEEP_GC();

#ifndef GC_EVERY_TIME
  result = (struct block_header *)find_free_block(aligned_size_in_bytes);
#endif

  if (result == NULL)
    {
      error_gc("\nx%x bytes needed\n", aligned_size_in_bytes);
      setup_for_threaded_GC();
      // don't have any free blocks handy, allocate new
      marksweep_collect();
      // see if we've managed to free the necessary memory
      result = (struct block_header *)find_free_block(aligned_size_in_bytes);
      // okay, no, then allocate a new page
      if (result == NULL) {
	size_t size_of_new_page = aligned_size_in_bytes + PAGE_HEADER_SIZE;
	size_of_new_page = (size_of_new_page > heap_size) ? size_of_new_page : heap_size;
	heap_size += size_of_new_page;
	free_memory += size_of_new_page;
	// allocate new page
	create_new_page(size_of_new_page);
	result = (struct block_header *)find_free_block(aligned_size_in_bytes);
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
#ifdef DEBUG_GC
  size_t free_bytes = 0;
#endif
  free_list = NULL;
  while (curr_page != NULL) {
    struct block *curr_block = &(curr_page->page_contents);
    struct block *to_be_merged = NULL; // for merging purposes
    size_t to_be_scanned = curr_page->pheader.page_size - PAGE_HEADER_SIZE;
    // scan page for free blocks
    while (to_be_scanned > 0) {
      size_t size_of_curr_block = curr_block->bheader.size_of_block;
      if (curr_block->bheader.markunion.mark != 1) {
	// not marked (includes blocks on the free list)
	curr_block->bheader.markunion.mark = 0; // clear any next ptrs
	// not marked, can be freed
#ifdef DEBUG_GC
	free_bytes += size_of_curr_block;
#endif
	free_memory += size_of_curr_block;
	if (to_be_merged == NULL) {
	  to_be_merged = curr_block;
	} else {
	  // merge with previous block
	  to_be_merged->bheader.size_of_block += size_of_curr_block;
	}
      } else {
	// marked, cannot be freed
	// unmark and add to_be_merged to free list
	curr_block->bheader.markunion.mark = 0;
	if (to_be_merged != NULL) 
	    {
		debug_clear_memory(to_be_merged);
		add_to_free_list(to_be_merged);
	    }
	to_be_merged = NULL; // reset
      }
      // go to next block
      to_be_scanned -= size_of_curr_block;
      curr_block = (struct block *)((void *)curr_block + size_of_curr_block);
    }
    if (to_be_merged != NULL)
	{
	    debug_clear_memory(to_be_merged);
	    add_to_free_list(to_be_merged);
	}
    // go to next page
    curr_page = curr_page->pheader.next;
  }
  error_gc("%d bytes free.\n", free_bytes);
}


#define make_mark(index) ((index)+2)
#define get_index_from_mark(mark) ((mark)-2)

/* pointerreversed_handle_reference marks the given object as
   well as any objects pointed to by it using pointer-reversal. */
void pointerreversed_handle_reference(jobject_unwrapped *obj)
{
  struct block *root;
  jobject_unwrapped prev = NULL;
  jobject_unwrapped curr;
  jobject_unwrapped next;
  jobject_unwrapped saved_prev;
  jobject_unwrapped saved_curr = (*obj);
  int done = 0;

  curr = PTRMASK(saved_curr);

  // with pointer-reversal, the mark has 3 possible states. 
  // when an object has not yet been visited, mark = 0. 
  // when the object is being handled, (mark - 2) is the 
  // index to the field (or array element) in the object 
  // that is currently being examined. when we have 
  // completed looking at the object, mark = 1.
  while (!done)
    {
      // follow pointers down
      while (curr != NULL)
	{
	  struct block *root = (void *)curr - BLOCK_HEADER_SIZE;

	  if (!allocated_by_marksweep(curr))
	    {
	      error_gc("%p not allocated by marksweep.\n", curr);
	      if (prev == NULL)
		done = 1;
	      break;
	    }

	  debug_verify_block(root);

	  // these objects are untouched
	  if (root->bheader.markunion.mark == 0)
	    {
	      ptroff_t next_index;
	      jobject_unwrapped *elements_and_fields;

	      error_gc("%p is unmarked.\n", curr);

	      assert(&claz_start <= curr->claz && curr->claz < &claz_end);

	      if (curr->claz->component_claz != NULL)
		{
		  // we have an array
		  struct aarray *arr = (struct aarray *) curr;
		  elements_and_fields = (jobject_unwrapped *)(arr->_padding_);
		}
	      else
		// we have a non-array object
		elements_and_fields = (jobject_unwrapped *)(curr->field_start);

	      next_index = get_next_index(curr, 0, 1);

	      if (next_index == NO_POINTERS)
		{
		  // does not contain pointers, mark as done
		  error_gc("%p does not contain pointers.\n", curr);
		  
		  root->bheader.markunion.mark = 1;
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
		  root->bheader.markunion.mark = make_mark(next_index);
		  next = elements_and_fields[next_index];
		  elements_and_fields[next_index] = prev;
		  prev = saved_curr;
		  error_gc("Setting prev = %p.\n", prev);
		  saved_curr = next;
		  curr = PTRMASK(saved_curr);
		}
	    }
	  else
	    {
	      // these are references we're done with or that
	      // we are already looking at somewhere in the chain
	      error_gc("%p has mark ", curr);
	      error_gc("%d.\n", root->bheader.markunion.mark);

	      assert(root->bheader.markunion.mark >= 1);
	      if (prev == NULL)
		done = 1;
	      break;
	    }
	}
      
      saved_prev = prev;
      prev = PTRMASK(saved_prev);

      // retreat! curr should point to the last thing we looked at, and prev to its parent
      while (prev != NULL)
	{
	  // these objects are in the middle of being examined
	  struct block *root;
	  ptroff_t last_index;
	  ptroff_t next_index;
	  jobject_unwrapped *elements_and_fields;

	  root = (void *)prev - BLOCK_HEADER_SIZE;
	  last_index = get_index_from_mark(root->bheader.markunion.mark);

	  error_gc("Starting prev loop with prev = %p.\n", prev);

	  if (prev->claz->component_claz != NULL)
	    {
	      // prev is an array
	      struct aarray *arr = (struct aarray *)prev;
	      elements_and_fields = (jobject_unwrapped *)(arr->_padding_);
	    }
	  else
	      elements_and_fields = (jobject_unwrapped *)(prev->field_start);

	  next_index = get_next_index(prev, last_index, 0);

	  if (next_index == NO_POINTERS)
	    {
	      // does not contain any more pointers, mark as done
	      error_gc("None of the rest of the fields/elements in this object contains a ptr.\n", "");
	      root->bheader.markunion.mark = 1;
	      // restore pointers
	      next = saved_prev;
	      saved_prev = elements_and_fields[last_index];
	      prev = PTRMASK(saved_prev);
	      error_gc("Setting prev = %p.\n", prev);
	      elements_and_fields[last_index] = saved_curr;
	      saved_curr = next;
	      curr = PTRMASK(saved_curr);

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
	      
	      root->bheader.markunion.mark = make_mark(next_index);
	      next = elements_and_fields[next_index];
	      elements_and_fields[next_index] = elements_and_fields[last_index];
	      elements_and_fields[last_index] = saved_curr;
	      saved_curr = next;
	      curr = PTRMASK(saved_curr);

	      break;
	    }
	}
    }
}
#endif
