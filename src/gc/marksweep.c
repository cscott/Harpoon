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
void marksweep_handle_reference(jobject_unwrapped *obj)
{
  struct block_header *root;
  if (allocated_by_marksweep((void *)(*obj)))
    {
      // move back to top of block
      root = (struct block_header *)(*obj) - 1;
      // mark or check for mark
      if (root->markunion.mark == 0)
	{
	  // if not already marked, mark and trace
	  error_gc("being marked.\n", "");
	  root->markunion.mark = 1;
	  trace(*obj);
	}
      else
	// if already marked, done
	assert(root->markunion.mark == 1);
    }
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
      setup_for_threaded_GC();
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
      cleanup_after_threaded_GC();
    }
  assert(result != NULL);

  error_gc("%d\t", ++num_mallocs);
  error_gc("Allocated %d bytes ", size_in_bytes);
  error_gc("at %p (for a total of ", result+1);
  error_gc("%d bytes)\n", (total_memory_requested += size_in_bytes));
  FLEX_MUTEX_UNLOCK(&marksweep_gc_mutex);
  // increment past header
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

/* GC bitmaps for objects whose size (minus the object header) is 
   less than COMPACT_ENCODING_SIZE fits inside the claz object. */
#define COMPACT_ENCODING_SIZE (SIZEOF_VOID_P*SIZEOF_VOID_P*8)

void print_bitmap(ptroff_t bitmap)
{
  int i, j;

  printf("BITMAP ");

  // start from the high bit
  for (i = SIZEOF_VOID_P*8 - 1; i >= 0; i--)
    {
      // print a 1 for every set bit,
      // a 0 for every cleared bit
      printf("%d", ((bitmap & (1 << i)) != 0));
    }
  printf("\n");
}

/* pointerreversed_handle_reference marks the given object as
   well as any objects pointed to by it using pointer-reversal. */
void pointerreversed_handle_reference(jobject_unwrapped *obj)
{
  struct block *root;
  jobject_unwrapped prev = NULL;
  jobject_unwrapped curr = *obj;
  jobject_unwrapped next;
  int done = 0;

  printf("Entering pointerreversed_handle_reference.\n");
  fflush(stdout);

  // with pointer-reversal, the mark has 3 possible states. 
  // when an object has not yet been visited, mark = 0. 
  // when the object is being handled, (mark - 2) is the 
  // index to the field (or array element) in the object 
  // that is currently being examined. when we have 
  // completed looking at the object, mark = 1.
  while (!done)
    {
      printf("Starting done loop.\n");
      fflush(stdout);

      // follow pointers down
      while (curr != NULL)
	{
	  struct block *root = (void *)curr - sizeof(struct block_header);

	  if (!allocated_by_marksweep(curr))
	    {
	      printf("Not allocated by marksweep.\n");
	      fflush(stdout);
	      if (prev == NULL)
		done = 1;
	      break;
	    }

	  printf("Starting curr loop: %p\n", curr);
	  fflush(stdout);

	  // these objects are untouched
	  if (root->bheader.markunion.mark == 0)
	    {
	      printf("%p is unmarked.\n", curr);
	      fflush(stdout);

	      // these are references that we have not looked at
	      if (curr->claz->component_claz != NULL)
		{
		  // we have an array
		  struct aarray *arr = (struct aarray *) curr;

		  // we have never seen this array before
		  // it may or may not contain pointers
		  ptroff_t containsPointers = arr->obj.claz->gc_info.bitmap;

		  printf("%p is an array.\n", arr);		  
		  fflush(stdout);

		  assert(containsPointers == 0 || containsPointers == 1);
		  if (containsPointers && arr->length > 0)
		    {
		      // this array contains pointers
		      jobject_unwrapped *elements = (jobject_unwrapped *)(arr->element_start);
		      
		      printf("%p contains pointers.\n", arr);
		      fflush(stdout);

		      // start by looking at the first element of the array
		      root->bheader.markunion.mark = 2;
		      next = elements[0];
		      elements[0] = prev;
		      prev = curr;
		      printf("Setting prev = %p.\n", prev);
		      fflush(stdout);
		      curr = next;
		    }
		  else
		    {
		      // this array does not contain pointers, mark as done
		      printf("%p does not contain pointers.\n", arr);
		      fflush(stdout);

		      root->bheader.markunion.mark = 1;
		      if (prev == NULL)
			done = 1;
		      break;
		    }
		}
	      else
		{
		  //  we have a non-array object
		  size_t obj_size_minus_header;
		  int num_bitmaps, i, field_index;
		  ptroff_t *bitmap_ptr;
		  
		  printf("%p is an object.\n", curr);
		  fflush(stdout);

		  // if the object size minus the object header is bigger than 
		  // COMPACT_ENCODING_SIZE, then the GC bitmap is not inlined.
		  // here we use some clever integer divide roundup thing.
		  // we may want to be dumber but more efficient in the future
		  // by borrowing the low bit to indicate whether the bitmap
		  // is inline or not.
		  obj_size_minus_header = curr->claz->size - sizeof(struct oobj);
		  num_bitmaps = (obj_size_minus_header + COMPACT_ENCODING_SIZE - 1)/COMPACT_ENCODING_SIZE;
		  assert(num_bitmaps >= 0);
		  
		  if (num_bitmaps > 1)
		    bitmap_ptr = curr->claz->gc_info.ptr;
		  else
		    bitmap_ptr = &(curr->claz->gc_info.bitmap);
		  
		  field_index = -1;
		  for (i = 0; i < num_bitmaps; i++)
		    {
		      int j;
		      ptroff_t bitmap = bitmap_ptr[i];

		      print_bitmap(bitmap);
		      // as we examine each bit in the bitmap, starting
		      // from bit zero, we shift the bitmap right. since 
		      // we are interested in the first bit that is set, 
		      // we can stop when all the remaining bits are
		      // clear or when the current bit is set.
		      for (j = i*SIZEOF_VOID_P*8; (bitmap != 0); j++) {
			if (bitmap & 1)
			  {
			    field_index = j;
			    break;
			  }
			bitmap = bitmap >> 1;
		      }
		      if (field_index != -1)
			break;
		    }
		  
		  if (field_index != -1)
		    {
		      // we found a field in the object that contains a pointer
		      jobject_unwrapped *field_ptrs = (jobject_unwrapped *)(curr->field_start);

		      printf("Field %d of this object contains a ptr.\n", field_index);
		      fflush(stdout);

		      root->bheader.markunion.mark = field_index + 2;
		      next = field_ptrs[field_index];
		      field_ptrs[field_index] = prev;
		      prev = curr;
		      printf("Setting prev = %p.\n", prev);
		      fflush(stdout);
		      curr = next;
		    }
		  else
		    {
		      // none of the fields in the object contain a pointer, mark as done
		      printf("None of the fields in this object contains a ptr.\n");
		      fflush(stdout);
		      root->bheader.markunion.mark = 1;
		      if (prev == NULL)
			done = 1;
		      break;
		    }
		}
	    }
	  else
	    {
	      // these are references we're done with or that
	      // we are already looking at somewhere in the chain
	      printf("%p has mark %d.\n", curr, root->bheader.markunion.mark);
	      fflush(stdout);
	      assert(root->bheader.markunion.mark >= 1);
	      if (prev == NULL)
		done = 1;
	      break;
	    }
	}

      // retreat! curr should point to the last thing we looked at, and prev to its parent
      while (prev != NULL)
	{
	  // these objects are in the middle of being examined
	  struct block *root = (void *)prev - sizeof(struct block_header);
	  printf("Starting prev loop with prev = %p.\n", prev);
	  fflush(stdout);

	  assert(allocated_by_marksweep(prev));
	  assert(root->bheader.markunion.mark > 1);

	  if (prev->claz->component_claz != NULL)
	    {
	      // prev is an array. we know it has pointers b/c
	      // we started looking at it.
	      struct aarray *arr = (struct aarray *) prev;
	      ptroff_t index = root->bheader.markunion.mark - 1;
	      jobject_unwrapped *elements = (jobject_unwrapped *)(arr->element_start);  

	      printf("%p is an array.\n", arr);
	      fflush(stdout);

	      if (index < arr->length)
		{
		  root->bheader.markunion.mark = index + 2;
		  next = elements[index];
		  elements[index] = elements[index-1];
		  elements[index-1] = curr;
		  curr = next;
		  break;
		}
	      else
		{
		  // this array does not contain any more pointers, mark as done
		  printf("None of the rest of the fields in this object contains a ptr.\n");
		  fflush(stdout);
		  root->bheader.markunion.mark = 1;
		  // restore pointers
		  next = prev;
		  prev = elements[index-1];
		      printf("Setting prev = %p.\n", prev);
		      fflush(stdout);
		  elements[index-1] = curr;
		  curr = next;
		  if (prev == NULL)
		    done = 1;
		}
	    }
	  else
	    {
	      // prev is a non-array object
	      size_t obj_size_minus_header;
	      int num_bitmaps, i;
	      ptroff_t *bitmap_ptr;
	      ptroff_t field_index;
	      jobject_unwrapped *field_ptrs;

	      printf("%p is an object\n", prev);
	      fflush(stdout);

	      // if the object size minus the object header is bigger than 
	      // COMPACT_ENCODING_SIZE, then the GC bitmap is not inlined.
	      // here we use some clever integer divide roundup thing.
	      // we may want to be dumber but more efficient in the future
	      // by borrowing the low bit to indicate whether the bitmap
	      // is inline or not.
	      obj_size_minus_header = prev->claz->size - sizeof(struct oobj);
	      num_bitmaps = (obj_size_minus_header + COMPACT_ENCODING_SIZE - 1)/COMPACT_ENCODING_SIZE;
	      assert(num_bitmaps >= 0);
	      
	      if (num_bitmaps > 1)
		bitmap_ptr = prev->claz->gc_info.ptr;
	      else
		bitmap_ptr = &(prev->claz->gc_info.bitmap);
	      
	      field_index = root->bheader.markunion.mark - 2;

	      for (i = (field_index+1)/(SIZEOF_VOID_P*8); i < num_bitmaps; i++)
		{
		  int j;
		  ptroff_t bitmap = bitmap_ptr[i];
		  
		  // as we examine each bit in the bitmap, starting
		  // from bit zero, we shift the bitmap right. since 
		  // we are interested in the first bit that is set, 
		  // we can stop when all the remaining bits are
		  // clear or when the current bit is set.
		  bitmap = bitmap >> (field_index + 1 - i*SIZEOF_VOID_P*8);
		  for (j = field_index+1; (bitmap != 0); j++) {
		    if (bitmap & 1)
			  {
			    field_index = j;
			    break;
			  }
			bitmap = bitmap >> 1;
		  }
		  if (field_index != root->bheader.markunion.mark - 2)
		    break;
		}
	      
	      field_ptrs = (jobject_unwrapped *)(prev->field_start);
	      
	      if (field_index != root->bheader.markunion.mark - 2)
		{
		  // we found the next field in the object that contains a pointer
		  printf("Field %d of this object contains a ptr.\n", field_index);
		  fflush(stdout);
		  next = field_ptrs[field_index];
		  field_ptrs[field_index] = field_ptrs[root->bheader.markunion.mark-2];
		  field_ptrs[root->bheader.markunion.mark-2] = curr;
		  curr = next;
		  root->bheader.markunion.mark = field_index + 2;
		  break;
		}
	      else
		{
		  // none of the remaining fields in the object contain a pointer, 
		  // restore pointers and mark as done
		  printf("None of the remaining fields of this object contain a ptr.\n");
		  next = prev;
		  prev = field_ptrs[root->bheader.markunion.mark-2];
		      printf("Setting prev = %p.\n", prev);
		      fflush(stdout);
		  field_ptrs[root->bheader.markunion.mark-2] = curr;
		  curr = next;
		  root->bheader.markunion.mark = 1;
		  if (prev == NULL)
		    done = 1;
		}
	    }
	}
      printf("\n");

    }

  printf("Exiting pointerreversed_handle_reference.\n");
  printf("---------------------------------------------\n\n");
  fflush(stdout);

}

#endif
