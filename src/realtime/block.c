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
  printf("Block_new(0x%08x, %d)\n", superBlockTag, size);
#endif
  (bl->free) = ((bl->begin) = (void*)RTJ_MALLOC_UNCOLLECTABLE(size));
#ifdef RTJ_TIMER
  gettimeofday(&end, NULL);
  printf("Block_new: %ds %dus\n", 
	 end.tv_sec-begin.tv_sec, 
	 end.tv_usec-begin.tv_usec);
#endif
#ifdef RTJ_DEBUG
  printf("  block: 0x%08x, block->begin: 0x%08x\n", bl, bl->begin);
#endif
  (bl->end) = (bl->begin) + size;
  (bl->superBlockTag) = superBlockTag;
  (bl->next) = (bl->prev) = NULL;
#ifdef WITH_NOHEAP_SUPPORT
  bl->begin = (void*)RTJ_ALIGN((bl->begin+1));
#ifdef RTJ_DEBUG
  printf("  adjusted begin for NoHeapRealtimeThread support: 0x%08x\n", 
	 bl->begin);
#endif
#endif
  return bl;
}

#ifdef WITH_NOHEAP_SUPPORT
inline void* Block_alloc(struct Block* block, size_t size, int noheap) {
#else
inline void* Block_alloc(struct Block* block, size_t size) {
#endif
  void* ptr;
#ifdef RTJ_DEBUG
  printf("Block_alloc(0x%08x, %d)\n", block, size
#ifdef WITH_NOHEAP_SUPPORT
	 +1
#endif	 
	 );
#endif
  if ((ptr = (void*)exchange_and_add((void*)(&(block->free)), 
#ifdef WITH_NOHEAP_SUPPORT
				     RTJ_ALIGN(size+1)
#else
				     RTJ_ALIGN(size)
#endif
				     ))
      > block->end) {
    printf("Ran out of space in MemoryArea: %d is not enough space.\n",
	   (block->end)-(block->begin));
    printf("Try increasing your MemoryArea size.\n");
    return NULL;
  } else {
#ifdef WITH_NOHEAP_SUPPORT
    *(((char*)ptr)-1) = (char)noheap;
#endif
    return ptr;
  }
  /* Returns NULL in case the allocation failed - all subsequent
     allocations will fail as well..., and block->free will be
     trashed. */
}

inline void Block_free(struct Block* block) {
#ifdef RTJ_TIMER
  struct timeval begin, end;
  gettimeofday(&begin, NULL);
#endif 
#ifdef RTJ_DEBUG
  printf("Block_free(0x%08x)\n", block);
#endif
  Block_finalize(block);
  RTJ_FREE(block->begin);
  RTJ_FREE(block);
#ifdef RTJ_TIMER
  gettimeofday(&end, NULL);
  printf("Block_free: %ds %dus\n", end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec);
#endif
}

inline void Block_reset(struct Block* block) {
#ifdef RTJ_DEBUG
  printf("Block_reset(0x%08x)\n", block);
#endif
  Block_finalize(block);
  block->free = block->begin;
}

#ifdef WITH_PRECISE_GC
inline void Block_scan(struct Block* block) {
#ifdef RTJ_DEBUG
  struct oobj* oobj_ptr_tmp;
#endif
  struct oobj* oobj_ptr;
#ifdef RTJ_TIMER
  struct timeval begin, end;
  gettimeofday(&begin, NULL);
#endif
#ifdef RTJ_DEBUG
  printf("Block_scan(0x%08x)\n  ", block);
#endif
  for(oobj_ptr = block->begin; ((void*)oobj_ptr) < block->free;
      ((void*)oobj_ptr) += 
#ifdef WITH_NOHEAP_SUPPORT
	RTJ_ALIGN(FNI_ObjectSize(oobj_ptr)+1)
#else
	RTJ_ALIGN(FNI_ObjectSize(oobj_ptr))
#endif
      ) {
#ifdef RTJ_DEBUG
    printf("0x%08x ", oobj_ptr_tmp = oobj_ptr);
#endif
#ifdef WITH_NOHEAP_SUPPORT
    if (*(((char*)oobj_ptr)-1)) {
#ifdef RTJ_DEBUG
      printf("(skipped) ");
#endif
    } else {
#endif
      trace((struct oobj*)(oobj_ptr));
#ifdef RTJ_DEBUG
      if (oobj_ptr_tmp != oobj_ptr) {
	assert("add_to_root_set moved pointer in RTJ block!\n");
      }
#endif
#ifdef WITH_NOHEAP_SUPPORT
    }
#endif
  }
#ifdef RTJ_DEBUG
  printf("\n");
#endif
#ifdef RTJ_TIMER
  gettimeofday(&end, NULL);
  printf("Block_scan: %ds %dus\n", end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec);
#endif
}
#endif

inline void Block_finalize(struct Block* block) {
  struct oobj* obj;
#ifdef RTJ_DEBUG
  printf("Block_finalize(0x%08x)\n  ", block);
#endif
  for (obj = block->begin; ((void*)obj) < block->free;
       ((void*)obj) +=
#ifdef WITH_NOHEAP_SUPPORT
	 RTJ_ALIGN(FNI_ObjectSize(obj)+1)
#else
	 RTJ_ALIGN(FNI_ObjectSize(obj))
#endif
       ) {
#ifdef RTJ_DEBUG
    if (RTJ_should_finalize(obj)) {
      printf("0x%08x ", obj);
    }
#endif
    RTJ_finalize(obj);
  }
#ifdef RTJ_DEBUG
  printf("\n");
#endif
}
