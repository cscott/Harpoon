#ifndef INCLUDED_FREE_LIST_H
#define INCLUDED_FREE_LIST_H

#include "jni-types.h"
#include "jni-private.h"

/* data structure for a block of memory */
struct block
{
  size_t size;
  union { ptroff_t mark; struct block *next; } markunion;
  struct oobj objects[0];
};

#define BLOCK_HEADER_SIZE     (sizeof(struct block))
#define SMALL_OBJ_SIZE        72
#define SMALL_BLOCK_SIZE      (BLOCK_HEADER_SIZE + SMALL_OBJ_SIZE)
#define MIN_BLOCK_SIZE        (BLOCK_HEADER_SIZE + sizeof(struct oobj))
#define NUM_SMALL_BLOCK_SIZES ((SMALL_BLOCK_SIZE - MIN_BLOCK_SIZE)/ALIGN_TO + 1)

#define UNREACHABLE 1
#define REACHABLE   2
#define MARK_OFFSET 3

#define CLEAR_MARK(bl) \
({ (bl)->markunion.mark = UNREACHABLE; })

#define MARK_AS_REACHABLE(bl) \
({ (bl)->markunion.mark = REACHABLE; })

#define MARKED_AS_REACHABLE(bl) ((bl)->markunion.mark == REACHABLE)
#define NOT_MARKED(bl) ((bl)->markunion.mark == UNREACHABLE)

struct block *faster_find_free_block(size_t size,
				     struct block **free_list_ptr,
				     struct block *small_blocks[]);

struct block *find_free_block(size_t size,
			      struct block **free_list_ptr,
			      struct block *small_blocks[]);

#endif
