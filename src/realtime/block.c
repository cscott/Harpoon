/* block.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "block.h"

inline struct Block* Block_new(void* superBlockTag, 
			       size_t size) {
  struct Block* bl = (struct Block*)
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc_uncollectable
#else
    malloc
#endif
    (sizeof(struct Block));
#ifdef DEBUG
  printf("Block_new(%08x, %d)\n", superBlockTag, size);
#endif
  (bl->free) = ((bl->begin) = 
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc_uncollectable
#else
    malloc
#endif
  (size)); 
#ifdef DEBUG
  printf("  block: %08x, block->begin: %08x\n", bl, bl->begin);
#endif
  (bl->end) = (bl->begin) + size;
  (bl->superBlockTag) = superBlockTag;
  (bl->next) = (bl->prev) = NULL;
  return bl;
}

inline void* Block_alloc(struct Block* block, size_t size) {
  void* ptr;
#ifdef DEBUG
  printf("Block_alloc(%08x, %d)\n", block, size);
#endif
  return ((ptr = (void*)exchange_and_add((void*)(&(block->free)), size)) 
	  > block->end)?NULL:ptr;
  /* Returns NULL in case the allocation failed - all subsequent
     allocations will fail as well..., and block->free will be
     trashed. */
}

inline void Block_free(struct Block* block) {
#ifdef DEBUG
  printf("Block_free(%08x)\n", block);
#endif
#ifdef BDW_CONSERVATIVE_GC
  GC_free(block->begin);
  GC_free(block);
#else
  free(block->begin);
  free(block);
#endif
}
