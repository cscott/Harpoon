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

#define INITIAL_HEAP_SIZE 1024
#define HEAP_INCREMENT    1024

#ifdef DEBUG_GC
static int num_mallocs = 0;
static size_t total_memory_requested = 0;
#endif

struct block_header {
  size_t size_of_block;
  // when a block of memory is in use, the mark bit should be clear
  // except during collection, when it may be set. when a block of
  // memory is not in use, the mark bit is the link to the next free block.
  union { int mark; struct block *next; } markunion;
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

#ifdef WITH_THREADED_GC
/* mutex for the GC variables */
FLEX_MUTEX_DECLARE_STATIC(marksweep_gc_mutex);

/* barriers for synchronizing threads */
extern pthread_barrier_t before;
extern pthread_barrier_t after;

extern pthread_cond_t done_cond;
extern pthread_mutex_t done_mutex;
extern int done_count;
extern jint halt_for_GC_flag; 
#endif /* WITH_THREADED_GC */

/* function declarations */
void free_unreachable_blocks();

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
void add_to_free_list(struct block *new_block) {
  struct block *curr = free_list;
  struct block *prev = NULL;
#ifdef DEBUG_GC
  int *here, *end;
  error_gc("add_to_free_list: clearing freed memory at %p\n", here);
  here = (int *)((void *)(&(new_block->object)));
  end = (int *)((void *)(&(new_block->object)) + 
		new_block->bheader.size_of_block - 
		sizeof(struct block_header));
  while (here < end) {
    (*here) = 0x77777777;
    here++;
  }
#endif
  error_gc("Adding %d bytes to free list\n", new_block->bheader.size_of_block);
  while (curr != NULL) {
    if (curr->bheader.size_of_block < new_block->bheader.size_of_block) {
      prev = curr;
      curr = curr->bheader.markunion.next;
    } else {
      break;
    }
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
  new_block->bheader.size_of_block = size_of_page - sizeof(struct page_header);
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
  if (curr->bheader.size_of_block > (size + sizeof(struct block))) {
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
void marksweep_add_to_root_set(jobject_unwrapped *obj) {
  struct block *root;
  if (allocated_by_marksweep((void *)(*obj))) {
    // mark or check for mark
    root = (void *)(*obj) - sizeof(struct block_header);
    if (root->bheader.markunion.mark)
      // if already marked, done
      return;
    else
      // mark
      root->bheader.markunion.mark = 1;
  }
  trace(*obj);
}

void marksweep_collect() {
  static int count = 1;
  error_gc("\nGC number %d\n", count++);
  find_root_set();
  free_unreachable_blocks();
}

void marksweep_gc_init() {
  create_new_page(INITIAL_HEAP_SIZE);
}

void marksweep_handle_nonroot(jobject_unwrapped *obj) {
  struct block *root;
  if (allocated_by_marksweep((void *)(*obj))) {
    // mark or check for mark
    root = (void *)(*obj) - sizeof(struct block_header);
    if (root->bheader.markunion.mark)
      // if already marked, done
      return;
    else
      // mark
      root->bheader.markunion.mark = 1;
    // for nonroot, only trace if allocated by us
    // otherwise, no way to identify loops
    trace(*obj);
  }
}

void* marksweep_malloc(size_t size_in_bytes) {
  size_t aligned_size_in_bytes = 0;
  struct block_header *result = NULL;
  aligned_size_in_bytes = align(sizeof(struct block_header)+size_in_bytes);

#ifdef WITH_THREADED_GC
  /* only one thread can get memory at a time */
  while (pthread_mutex_trylock(&marksweep_gc_mutex))
    if (halt_for_GC_flag) halt_for_GC();
#endif

#ifdef GC_EVERY_TIME
  result = NULL;
#else
  result = (struct block_header *)find_free_block(aligned_size_in_bytes);
#endif
  if (result == NULL)
    {
      error_gc("\nx%x bytes needed\n", aligned_size_in_bytes);
#ifdef WITH_THREADED_GC
      setup_for_threaded_GC();
#endif
      // don't have any free blocks handy, allocate new
      marksweep_collect();
      // see if we've managed to free the necessary memory
      result = (struct block_header *)find_free_block(aligned_size_in_bytes);
      // okay, no, then allocate a new page
      if (result == NULL) {
	size_t size_of_new_page = 
	  aligned_size_in_bytes + sizeof(struct page_header);
	// allocate new page
	create_new_page((size_of_new_page > HEAP_INCREMENT) ? 
			size_of_new_page : HEAP_INCREMENT);
	result = (struct block_header *)find_free_block(aligned_size_in_bytes);
      }
#ifdef WITH_THREADED_GC
      cleanup_after_threaded_GC();
#endif
    }
  assert(result != NULL);

  error_gc("%d\t", ++num_mallocs);
  error_gc("Allocated %d bytes ", size_in_bytes);
  error_gc("at %p (for a total of ", result+1);
  error_gc("%d bytes)\n", (total_memory_requested += size_in_bytes));
  // increment past header
#ifdef WITH_THREADED_GC
  FLEX_MUTEX_UNLOCK(&marksweep_gc_mutex);
#endif
  return (result+1);
}

void free_unreachable_blocks() {
  struct page *curr_page = page_list;
#ifdef DEBUG_GC
  size_t free_bytes = 0;
#endif
  free_list = NULL;
  while (curr_page != NULL) {
    struct block *curr_block = &(curr_page->page_contents);
    struct block *to_be_merged = NULL; // for merging purposes
    size_t to_be_scanned = 
      curr_page->pheader.page_size - sizeof(struct page_header);
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
	if (to_be_merged != NULL) add_to_free_list(to_be_merged);
	to_be_merged = NULL; // reset
      }
      // go to next block
      to_be_scanned -= size_of_curr_block;
      curr_block = (struct block *)((void *)curr_block + size_of_curr_block);
    }
    if (to_be_merged != NULL) add_to_free_list(to_be_merged);
    // go to next page
    curr_page = curr_page->pheader.next;
  }
  error_gc("%d bytes free.\n", free_bytes);
}

/* returns 1 if the given ptr points to an object
   in the garbage-collected heap, returns 0 otherwise. */
int marksweep_in_heap(void *ptr)
{
  int result = 0;
#ifdef WITH_THREADED_GC
  /* only one thread can touch the GC variables at a time */
  while (pthread_mutex_trylock(&marksweep_gc_mutex))
    if (halt_for_GC_flag) halt_for_GC();
#endif
  result = allocated_by_marksweep(ptr);
  FLEX_MUTEX_UNLOCK(&marksweep_gc_mutex);
  return result;
}

#endif
