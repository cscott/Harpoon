/* block.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "block.h"

static int fd = -1;

inline struct Block* Block_new(size_t size) {
  struct Block* bl;
#ifdef RTJ_TIMER
  struct timeval begin;
  struct timeval end;
  gettimeofday(&begin, NULL);
#endif 
  bl = (struct Block*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct Block));
#ifdef RTJ_DEBUG
  printf("Block_new(%d)\n", size);
#endif
#ifdef BDW_CONSERVATIVE_GC
  bl->begin = (void*)RTJ_CALLOC_UNCOLLECTABLE(size, 1);
#else
  if (size > START_MMAP) {
    if (fd < 0) fd = open("/dev/zero", O_RDONLY);
    bl->begin = mmap(0, size, PROT_READ | PROT_WRITE, MAP_PRIVATE, fd, 0);
  } else {
    bl->begin = (void*)RTJ_CALLOC_UNCOLLECTABLE(size, 1);
  }
#endif
#ifdef RTJ_DEBUG
  printf("  block: 0x%08x, block->begin: 0x%08x\n", bl, bl->begin);
#endif
  (bl->end) = (bl->begin) + size;
  bl->free = (void*)RTJ_ALIGN((bl->begin)+sizeof(void*));
#ifdef RTJ_TIMER
  gettimeofday(&end, NULL);
  printf("Block_new: %ds %dus\n", 
	 end.tv_sec-begin.tv_sec, 
	 end.tv_usec-begin.tv_usec);
#endif
  return bl;
}

inline void* Block_alloc(struct Block* block, size_t size) {
  void* ptr;
  ptroff_t objSize = RTJ_ALIGN(size+sizeof(void*));
#ifdef RTJ_DEBUG
  printf("Block_alloc(0x%08x, %d)\n", block, size);
#endif
  if ((ptr = (void*)exchange_and_add((void*)(&(block->free)), objSize)) 
      >= block->end) {
    return NULL;
  } else {
    *((void**)(ptr-sizeof(void*))) = ptr+objSize;
    return ptr;
  }
}

#ifdef WITH_PRECISE_GC
inline void Block_scan(struct Block* block) {
  struct oobj* oobj_ptr;
#ifdef RTJ_DEBUG
  printf("Block_scan(0x%08x)\n  ", block);
#endif
  for(oobj_ptr = (struct oobj*)RTJ_ALIGN(block->begin+sizeof(void*)); 
      oobj_ptr; oobj_ptr = *(((struct oobj**)oobj_ptr)-1)) {
    if (oobj_ptr->claz) {
#ifdef RTJ_DEBUG
      printf("0x%08x ", oobj_ptr);
#endif
      trace(oobj_ptr);
    }
  }
#ifdef RTJ_DEBUG
  printf("\n");
#endif
}
#endif

#ifdef WITH_GC_STATS
extern struct timespec total_finalize_time;
struct timespec total_finalize_time;
#endif

inline void Block_finalize(struct Block* block) {
  struct oobj* obj;
#ifdef WITH_GC_STATS
  struct timespec begin, end, elapsed;
  clock_gettime(CLOCK_REALTIME, &begin);
#endif
#ifdef RTJ_DEBUG
  printf("Block_finalize(0x%08x)\n  ", block);
#endif
  for(obj = (struct oobj*)RTJ_ALIGN(block->begin+sizeof(void*)); obj; 
      obj = *(((struct oobj**)obj)-1)) {
#ifdef RTJ_DEBUG
    if ((obj->claz)&&(RTJ_should_finalize(obj))) {
      printf("0x%08x ", obj);
    }
#endif
    if (obj->claz) {
      RTJ_finalize(obj);
    }
  }
#ifdef RTJ_DEBUG
  printf("\n");
#endif
#ifdef WITH_GC_STATS
  clock_gettime(CLOCK_REALTIME, &end);
  elapsed.tv_sec = (end.tv_sec-begin.tv_sec)+
    ((end.tv_nsec<begin.tv_nsec)?(-1):0); 
  elapsed.tv_nsec = (end.tv_nsec-begin.tv_nsec)+
    ((end.tv_nsec<begin.tv_nsec)?1000000000:0); 
  total_finalize_time.tv_sec += elapsed.tv_sec + 
    (total_finalize_time.tv_nsec + elapsed.tv_nsec)/1000000000; 
  total_finalize_time.tv_nsec = 
    (total_finalize_time.tv_nsec + elapsed.tv_nsec)%1000000000;
#endif
}

inline void Block_free(struct Block* block) {
  size_t size = (size_t)((block->end)-(block->begin));
#ifdef RTJ_TIMER
  struct timeval begin, end;
  gettimeofday(&begin, NULL);
#endif 
#ifdef RTJ_DEBUG
  printf("Block_free(0x%08x)\n", block);
#endif
  Block_finalize(block);
#ifdef BDW_CONSERVATIVE_GC
  RTJ_FREE(block->begin);
#else
  if (size > START_MMAP) {
    munmap(block->begin, size);
  } else {
    RTJ_FREE(block->begin);
  }
#endif
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
  memset(block->begin, 0, (size_t)((block->free)-(block->begin)));
  block->free = (void*)RTJ_ALIGN((block->begin)+sizeof(void*));
}

