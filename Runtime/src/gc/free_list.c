#include <assert.h>
#include "free_list.h"
#include "precise_gc.h"

struct block *find_block_in_free_list(size_t size,
				      struct block **free_list_ptr,
				      struct block *small_blocks[]);

/* requires: that the blocks in the free list are in order of sizeo
   from smallest to biggest
   effects: adds the given block in the appropriate place in the free list
*/
void add_to_free_list(struct block *new_block, struct block **free_list_ptr)
{
  struct block *curr_block = (*free_list_ptr);
  struct block *prev_block = NULL;

  // go through list until we find the right place
  while (curr_block != NULL)
    {
      if (curr_block->size < new_block->size)
	{
	  prev_block = curr_block;
	  curr_block = curr_block->markunion.next;
	}
      else
	break;
    }

  // set the next field of the new block
  new_block->markunion.next = curr_block;

  // set the next field of the previous block
  if (prev_block == NULL)
    // if no previous block, the new block 
    // is the new head of the list
    (*free_list_ptr) = new_block;
  else
    prev_block->markunion.next = new_block;
}


/* effects: adds small blocks to the small blocks table, and
   large blocks to the correct location in the free list
*/
void add_to_free_blocks(struct block *new_block,
			struct block **free_list,
			struct block *small_blocks[])
{
  // "small" blocks are stored separately for fast allocation
  if (new_block->size <= SMALL_BLOCK_SIZE)
    {
      // calculate the array index
      int index = new_block->size/ALIGN_TO;

      // check that the array index is in-bounds
      assert(index >= 0 && index < NUM_SMALL_BLOCK_SIZES);
      
      new_block->markunion.next = small_blocks[index];
      small_blocks[index] = new_block;
    }
  else
    add_to_free_list(new_block, free_list);
}


/* effects: overwrites the non-header portion of the given block
   with recognizable garbage for easy debugging.
*/
#ifdef DEBUG_GC
void debug_clear_memory(struct block *bl)
{
  memset(bl->object, 7, (bl->size - BLOCK_HEADER_SIZE));
}
#endif


/* returns: a block that is greater than or equal in size to the
   request; block is initialized before being returned
 */
struct block *find_free_block(size_t size,
			      struct block **free_list_ptr,
			      struct block *small_blocks[])
{
  if (size <= SMALL_BLOCK_SIZE)
    {
      int index = size/ALIGN_TO;
      struct block *result;

      // make sure array index is not out of bounds
      assert(index >= 0 && index < NUM_SMALL_BLOCK_SIZES);

      result = small_blocks[index];

      if (result != NULL)
	{
	  // remove block from list
	  small_blocks[index] = result->markunion.next;
	  // initialize block
	  CLEAR_MARK(result);
	  // return
	  return result;
	}
    }

  // if we couldn't find a small block that fits, get any block
  return find_block_in_free_list(size, free_list_ptr, small_blocks);
}


/* requires: that the blocks in the free list are in order
   of size from smallest to biggest
   returns: smallest block in the free list that is greater 
   than or equal in size to request; block is initialized 
   before being returned
 */
struct block *find_block_in_free_list(size_t size,
				      struct block **free_list_ptr,
				      struct block *small_blocks[])
{
  struct block *curr_block = (*free_list_ptr);
  struct block *prev_block = NULL;

  while (curr_block != NULL)
    {
      if (curr_block->size < size)
	{
	  prev_block = curr_block;
	  curr_block = curr_block->markunion.next;
	}
      else
	// done!
	break;
    }

  // no free blocks
  if (curr_block == NULL)
    return NULL;

  // remove result from free list
  if (prev_block == NULL)
    // result is first element in list
    (*free_list_ptr) = curr_block->markunion.next;
  else
    prev_block->markunion.next = curr_block->markunion.next;

  // see if we need the whole block
  if (curr_block->size >= size + MIN_BLOCK_SIZE)
    {
      // this block is big enough to split
      struct block *new_block = (void *)curr_block + size;
      
      // split block up
      new_block->size = curr_block->size - size;
      curr_block->size = size;
      
      // insert in free list
      add_to_free_blocks(new_block, free_list_ptr, small_blocks);
    }
  // clear mark bit
  CLEAR_MARK(curr_block);

  return curr_block;
}
