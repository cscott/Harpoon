/* block.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "block.h"

inline struct Block* Block_new(void* superBlockTag, 
			       size_t size) {
  struct Block* bl;
#ifdef RTJ_TIMER
  struct timeval begin;
  struct timeval end;
  gettimeofday(&begin, NULL);
#endif 
  bl = (struct Block*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct Block));
#ifdef RTJ_DEBUG
  printf("Block_new(%08x, %d)\n", superBlockTag, size);
#endif
  (bl->free) = ((bl->begin) = RTJ_MALLOC_UNCOLLECTABLE(size));
#ifdef RTJ_TIMER
  gettimeofday(&end, NULL);
  printf("Block_new: %ds %dus\n", 
	 end.tv_sec-begin.tv_sec, 
	 end.tv_usec-begin.tv_usec);
#endif
#ifdef RTJ_DEBUG
  printf("  block: %08x, block->begin: %08x\n", bl, bl->begin);
#endif
  (bl->end) = (bl->begin) + size;
  (bl->superBlockTag) = superBlockTag;
  (bl->next) = (bl->prev) = NULL;
  return bl;
}

inline void* Block_alloc(struct Block* block, size_t size) {
  void* ptr;
#ifdef RTJ_DEBUG
  printf("Block_alloc(%08x, %d)\n", block, size);
#endif
  if ((ptr = (void*)exchange_and_add((void*)(&(block->free)), size))
      > block->end) {
    printf("Ran out of space in MemoryArea: %d is not enough space.\n",
	   (block->end)-(block->begin));
    printf("Try increasing your MemoryArea size.\n");
    return NULL;
  } else {
    return ptr;
  }
  /* Returns NULL in case the allocation failed - all subsequent
     allocations will fail as well..., and block->free will be
     trashed. */
}

inline void Block_free(struct Block* block) {
#ifdef RTJ_TIMER
  struct timeval begin;
  struct timeval end;
  gettimeofday(&begin, NULL);
#endif 
#ifdef RTJ_DEBUG
  printf("Block_free(%08x)\n", block);
#endif
  RTJ_FREE(block->begin);
  RTJ_FREE(block);
#ifdef RTJ_TIMER
  gettimeofday(&end, NULL);
  printf("Block_free: %ds %dus\n", end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec);
#endif
}

inline void Block_reset(struct Block* block) {
#ifdef RTJ_DEBUG
  printf("Block_reset(%08x)\n", block);
#endif
  block->free = block->begin;
}
