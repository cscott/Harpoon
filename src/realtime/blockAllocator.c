/* blockAllocator.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "blockAllocator.h"

inline void BlockAllocator_init() {
  ba = BlockAllocator_new(BLOCK_ALLOC_SIZE);
}

static struct BlockAllocator* BlockAllocator_new(size_t size) {
  struct BlockAllocator* ba = 
    (struct BlockAllocator*)
#ifdef BDW_CONSERVATIVE_GC
#ifdef WITH_GC_STATS
    GC_malloc_uncollectable_stats
#else
    GC_malloc_uncollectable
#endif
#else
    malloc
#endif
    (sizeof(struct BlockAllocator));
  struct Block* block = (struct Block*)
#ifdef BDW_CONSERVATIVE_GC
#ifdef WITH_GC_STATS
    GC_malloc_uncollectable_stats
#else
    GC_malloc_uncollectable
#endif
#else
    malloc
#endif
    (sizeof(struct Block));
  block->end = (block->free = block->begin = 
#ifdef BDW_CONSERVATIVE_GC
#ifdef WITH_GC_STATS
    GC_malloc_uncollectable_stats
#else
    GC_malloc_uncollectable
#endif
#else
    malloc
#endif	 
(size)) + size;
  block->next = block->prev = ba->inUse = NULL;
  ba->freeList = block;
  return ba;
}

static struct Block* BlockAllocator_splitBlock(struct Block* block,
					       size_t size_left) {
  /* left - block - b2 - right */
  struct Block* b2 = (struct Block*)
#ifdef BDW_CONSERVATIVE_GC
#ifdef WITH_GC_STATS
    GC_malloc_uncollectable_stats
#else
    GC_malloc_uncollectable
#endif
#else
    malloc
#endif
    (sizeof(struct Block*));
  b2->begin = block->begin + size_left + 1;
  b2->end = block->end;
  block->end = block->begin + size_left;
  b2->next = block->next;
  block->next = b2;
  b2->prev = block;  
  b2->next->prev = b2;
  b2->superBlockTag = block->superBlockTag;
  return block;
}

static struct Block* BlockAllocator_mergeBlocks(struct Block* left, 
						struct Block* right) {
  /* b1 - left - right - b2 */
  if ((left->end + 1) == right->begin) {
    assert("Attempt to merge non-adjacent blocks.");
  }
  if (right->begin == right->free) {
    assert("Attempt to merge non-free right block.");
  }
  left->end = right->end;
  left->next = right->next;
  right->next->prev = left;
#ifdef BDW_CONSERVATIVE_GC
#ifdef WITH_GC_STATS
  GC_free_stats
#else
  GC_free
#endif
#else
    free
#endif
    (right);
  return left;
}

void BlockAllocator_freeBlocks(void* superBlockTag) {
  
}

void BlockAllocator_mergeAll() {
  

}

void BlockAllocator_freeAll() {
  struct Block* block = ba->freeList;
  while (block != NULL) { /* Finish this! */
    block = block->next;
  }
}


