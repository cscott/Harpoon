#ifndef INCLUDED_FREE_LIST_H
#define INCLUDED_FREE_LIST_H

#include "jni-gc.h"
#include "jni-types.h"
#include "jni-private.h"

/* data structure for a block of memory */
struct block
{
#ifdef WITH_STATS_GC
  jint time;
#endif
  size_t size;
  union { ptroff_t mark; struct block *next; } markunion;
  struct oobj object[0];
};

#define BLOCK_HEADER_SIZE     (sizeof(struct block))
#define SMALL_BLOCK_SIZE      256
#define SMALL_OBJ_SIZE        (SMALL_BLOCK_SIZE - BLOCK_HEADER_SIZE)
#define MIN_BLOCK_SIZE        (BLOCK_HEADER_SIZE + sizeof(struct oobj))
#define NUM_SMALL_BLOCK_SIZES (SMALL_BLOCK_SIZE/ALIGN_TO + 1)

#define UNREACHABLE 1
#define REACHABLE   2
#define MARK_OFFSET 3

#define CLEAR_MARK(bl) ({ (bl)->markunion.mark = UNREACHABLE; })
#define MARK_AS_REACHABLE(bl) ({ (bl)->markunion.mark = REACHABLE; })

#define MARKED_AS_REACHABLE(bl) ((bl)->markunion.mark == REACHABLE)
#define NOT_MARKED(bl) ((bl)->markunion.mark == UNREACHABLE)

#define GET_INDEX(bl) (bl->markunion.mark - MARK_OFFSET);
#define SET_INDEX(bl,index) ({ (bl)->markunion.mark = (index) + MARK_OFFSET; })

#ifdef DEBUG_GC
void debug_clear_memory(struct block *bl);
#else
# define debug_clear_memory(bl)
#endif

struct block *find_free_block(size_t size,
			      struct block **free_list_ptr,
			      struct block *small_blocks[]);

#endif
