/* blockAllocator.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

/* FIX ME! */

#ifndef __BLOCKALLOCATOR_H__
#define __BLOCKALLOCATOR_H__

/* Nothing uses this currently...
#include "block.h"

#define BLOCK_ALLOC_SIZE 1000000

struct BlockAllocator {
  struct Block* inUse;
  struct Block* freeList;
};

static struct BlockAllocator* ba;

inline void BlockAllocator_init();     
static struct BlockAllocator* BlockAllocator_new(size_t size);
static struct Block* BlockAllocator_splitBlock(struct Block* block,
					       size_t size_left);
static struct Block* BlockAllocator_mergeBlocks(struct Block* left, 
						struct Block* right);
void BlockAllocator_freeAll();
*/

#endif /* __BLOCKALLOCATOR_H__ */
