#include "ms_heap.h"
#include "system_page_size.h"
#include <assert.h>

/* tuning value for adjusting how fast to grow heap */
#define MARKSWEEP_HEAP_DIVISOR 4

#define PAGE_HEADER_SIZE (sizeof(struct page))


/* effects: may expand heap if necessary
   returns: a block of memory of size bytes if successful, NULL otherwise */
void *allocate_in_marksweep_heap(size_t size, struct marksweep_heap *h)
{
  size_t aligned_size = align(size + BLOCK_HEADER_SIZE);
  struct block *result;

  result = faster_find_free_block(aligned_size, 
				  &(h->free_list), 
				  h->small_blocks);

  // if no block available, expand the heap and try again
  if (result == NULL)
    {
      printf("&");
      expand_marksweep_heap(aligned_size, h);
      result = faster_find_free_block(aligned_size, 
				      &(h->free_list), 
				      h->small_blocks);
    }

  if (result != NULL)
    // increment past the block header
    return result->objects;
  else
    return NULL;
}


/* effects: expands heap to accommodate size bytes */
int expand_marksweep_heap(size_t size, struct marksweep_heap *h)
{
  struct page *p = h->allocated_pages;
  size_t bytes;
  struct block *b;

  bytes = ROUND_TO_NEXT_PAGE(size + h->heap_size/MARKSWEEP_HEAP_DIVISOR);

  // expand the last page
  while (p->next != NULL)
    p = p->next;

  // no memory to be had, fail for now
  assert (p->page_end != p->mapped_end);

  // if we can't expand the heap as much as we want,
  // expand it as much as possible
  if (p->page_end + bytes > p->mapped_end)
    bytes = p->mapped_end - p->page_end;

  // new block is at the end of the old heap
  b = p->page_end;
  b->size = bytes;

  p->page_end += bytes;
  h->heap_size += bytes;

  faster_add_to_free_list(b, &h->free_list, h->small_blocks);
}


/* effects: frees any blocks in the given heap that have
   become unreachable, updating the average occupancy
   of the heap with data obtained from this round of freeing
*/
void free_unreachable_blocks(struct marksweep_heap *h)
{
  static num_collections = 0;
  struct page *curr_page = h->allocated_pages;
  size_t free_bytes = 0;
  float occupancy;

  // go through all the pages
  while (curr_page != NULL)
    {
      struct block *curr_block = curr_page->blocks;

      // scan each page
      while ((void *)curr_block < curr_page->page_end)
	{
	  if (MARKED_AS_REACHABLE(curr_block))
	    // marked, cannot be freed; unmark
	    CLEAR_MARK(curr_block);
	  else if (NOT_MARKED(curr_block))
	    {
	      // if inflated, need to free associated resources
	      if ((curr_block->objects[0].hashunion.hashcode & 1) == 0)
		{
		  printf("@");
		  curr_block->objects[0].hashunion.inflated->
		    precise_deflate_obj(curr_block->objects, (ptroff_t)0);
		}

	      printf("!");
	      free_bytes += curr_block->size;
	      faster_add_to_free_list(curr_block, 
				      &h->free_list, 
				      h->small_blocks);
	    }

	  // go to next block
	  curr_block = (void *)curr_block + curr_block->size;
	}

      // go to next page
      curr_page = curr_page->next;
    }

  // calculate new avg_occupancy for heap
  occupancy = ((float)(h->heap_size - free_bytes))/((float) h->heap_size);
  h->avg_occupancy = (num_collections*h->avg_occupancy +
		      occupancy)/(num_collections + 1);
  num_collections++;
}


/* returns: 1 if the specified object was allocated in the given heap */
int in_marksweep_heap(jobject_unwrapped obj, struct marksweep_heap *h)
{
  struct page *curr_page = h->allocated_pages;

  // check through list of allocated pages
  while(curr_page != NULL)
    {
      if ((void *)obj > (void *)&(curr_page->blocks) && 
	  (void *)obj < curr_page->page_end)
	return 1;
      else
	curr_page = curr_page->next;
    }

  // if we got this far, it's not there
  return 0;  
}


/* effects: initializes the heap at the given memory with the given sizes */
void init_marksweep_heap(void *mapped, 
			 size_t heap_size, 
			 size_t mapped_size, 
			 struct marksweep_heap *h)
{
  int i;
  struct block *new_block;

  h->heap_size = heap_size;
  h->avg_occupancy = 0;

  // initialize the allocated page
  h->allocated_pages = mapped;
  h->allocated_pages->page_end = mapped + heap_size;
  h->allocated_pages->mapped_end = mapped + mapped_size;
  h->allocated_pages->next = NULL;

  // initialize the free list
  // we'll add the block to it later
  h->free_list = NULL;

  // initialize the small blocks array
  for (i = 0; i < NUM_SMALL_BLOCK_SIZES; i++)
    h->small_blocks[i] = NULL;

  // initialize new block
  new_block = h->allocated_pages->blocks;
  new_block->size = heap_size - PAGE_HEADER_SIZE;

  // add to the free list
  faster_add_to_free_list(new_block, &(h->free_list), h->small_blocks);  
}

