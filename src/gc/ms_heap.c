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

  result = find_free_block(aligned_size, &(h->free_list), h->small_blocks);

  // if no block available, expand the heap and try again
  if (result == NULL)
    {
      int retval;
      //printf("&");
      retval = expand_marksweep_heap(aligned_size, h);

      if (retval == -1)
	return NULL;

      result = find_free_block(aligned_size, 
			       &(h->free_list), 
			       h->small_blocks);
    }

  if (result != NULL)
    {
      h->free_memory -= result->size;

      // increment past the block header
      return result->object;
    }
  else
    return NULL;
}


/* effects: expands heap to accommodate size bytes
   returns: 0 on success, -1 on failure
*/
int expand_marksweep_heap(size_t size, struct marksweep_heap *h)
{
  size_t bytes;
  struct block *b;

  bytes = ROUND_TO_NEXT_PAGE(size + h->heap_size/MARKSWEEP_HEAP_DIVISOR);

  // no memory to be had, fail for now
  if (h->heap_end == h->mapped_end)
    return -1;

  // if we can't expand the heap as much as we want,
  // expand it as much as possible
  if (h->heap_end + bytes > h->mapped_end)
    bytes = h->mapped_end - h->heap_end;

  // the new block is at the end of the old heap
  b = h->heap_end;
  b->size = bytes;

  h->heap_end += bytes;
  h->heap_size += bytes;
  h->free_memory += bytes;

  add_to_free_blocks(b, &h->free_list, h->small_blocks);

  return 0;
}


/* effects: frees any blocks in the given heap that have
   become unreachable, updating the average occupancy
   of the heap with data obtained from this round of freeing
*/
void free_unreachable_blocks(struct marksweep_heap *h)
{
  static num_collections = 0;
  size_t free_bytes = 0;
  float occupancy;
  struct block *curr_block = h->heap_begin;

  while ((void *)curr_block < h->heap_end)
    {
      if (MARKED_AS_REACHABLE(curr_block))
	// marked, cannot be freed; unmark
	CLEAR_MARK(curr_block);
      else if (NOT_MARKED(curr_block))
	{
	  // if inflated, need to free associated resources
	  if ((curr_block->object[0].hashunion.hashcode & 1) == 0)
	    {
	      //printf("@");
	      INFLATED_MASK(curr_block->object[0].hashunion.inflated)->
		precise_deflate_obj(curr_block->object, (ptroff_t)0);
	    }
	  
	  //printf("!");
	  free_bytes += curr_block->size;
	  add_to_free_blocks(curr_block, &h->free_list, h->small_blocks);
	}
      
      // go to next block
      curr_block = (void *)curr_block + curr_block->size;
    }
  
  // update free_memory information
  h->free_memory += free_bytes;

  // calculate new avg_occupancy for heap
  occupancy = ((float)(h->heap_size - free_bytes))/((float) h->heap_size);
  h->avg_occupancy = (num_collections*h->avg_occupancy +
		      occupancy)/(num_collections + 1);
  num_collections++;
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
  h->free_memory = heap_size;
  h->avg_occupancy = 0;

  // initialize the allocated memory
  h->heap_begin = mapped;
  h->heap_end = mapped + heap_size;
  h->mapped_end = mapped + mapped_size;

  // initialize the free list
  // we'll add the block to it later
  h->free_list = NULL;

  // initialize the small blocks array
  for (i = 0; i < NUM_SMALL_BLOCK_SIZES; i++)
    h->small_blocks[i] = NULL;

  // initialize new block
  new_block = mapped;
  new_block->size = heap_size;

  // add to the free list
  add_to_free_blocks(new_block, &(h->free_list), h->small_blocks);  
}


/* effects: moves object to the given mark-and-sweep heap, updating
   the given pointer and creating a forwarding reference.
   returns: 0 if success, -1 if fail
*/
int move_to_marksweep_heap(jobject_unwrapped *ref, struct marksweep_heap *h)
{
  jobject_unwrapped obj = PTRMASK((*ref));
  void *forwarding_address;
  size_t obj_size = align(FNI_ObjectSize(obj));

  // get a block of memory in the heap
  forwarding_address = allocate_in_marksweep_heap(obj_size, h);

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
