/* CTMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "CTMemory.h"

/*
 * Class:     CTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong minimum, jlong maximum) {
  struct MemBlock* mb = MemBlock_new(env, memoryArea);
#ifdef RTJ_DEBUG
  printf("CTMemory.initNative(%p, %p, %d, %d)\n", 
	 env, memoryArea, minimum, maximum);
  checkException();
#endif
  mb->block = Block_new(minimum);
  if (minimum != maximum) {
    mb->alloc_union.lls = LListAllocator_new(maximum-minimum);
  }
#ifdef RTJ_DEBUG
  checkException();
  printf("  storing MemBlock in %p\n", mb->block); 
#endif
  mb->alloc =    CTScope_MemBlock_alloc;
  mb->free =     CTScope_MemBlock_free;
  mb->finalize = CTScope_MemBlock_finalize;
#ifdef WITH_PRECISE_GC
  mb->gc =       CTScope_MemBlock_gc; /* must be the last set */
#endif
}

/*
 * Class:     CTMemory
 * Method:    doneNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_doneNative
(JNIEnv* env, jobject memoryArea) {
  struct MemBlock* mb = getInflatedObject(env, memoryArea)->memBlock;
#ifdef RTJ_DEBUG
  checkException();
  printf("CTMemory.doneNative(%p, %p)\n", env, memoryArea);
#endif
  if (!mb->refCount) {
    Block_free(mb->block);
    RTJ_FREE(mb);
  } 
#ifdef RTJ_DEBUG
  else {
    printf("  Not freeing memory now because refCount = %d\n", mb->refCount);
  }
#endif
}

void* CTScope_MemBlock_alloc_alternative(struct MemBlock* mem, size_t size) {
  void* ptr;
  struct Block* bl = mem->block;
#ifdef RTJ_DEBUG
  checkException();
  printf("CTScope_MemBlock_alloc_alternative(%p, %d)\n", mem, size);
#endif
  if (!(ptr = Block_alloc(bl, size))) {
    ptr = LListAllocator_alloc(mem->alloc_union.lls, size);
  }
  if (!ptr) {
    printf("Ran out of space in MemoryArea: %ld is not enough space.\n",
	   ((bl->end)-(bl->begin))+(mem->alloc_union.lls->size));
    printf("Try increasing your MemoryArea size.\n");
  }
  return ptr;
}

void* CTScope_MemBlock_alloc(struct MemBlock* mem, size_t size) {
  void* ptr;
  struct Block* bl = mem->block;
#ifdef RTJ_DEBUG
  checkException();
  printf("CTScope_MemBlock_alloc(%p, %d)\n", mem, size);
  printf("  current usage: %d of %d\n", (size_t)((bl->free)-(bl->begin)), 
	 (size_t)((bl->end)-(bl->begin)));
  printf("  begin: %p, free: %p, end: %p\n", bl->begin, bl->free, bl->end);
  printf("  retrieving memBlock from %p\n", mem->block);
#endif
  ptr = Block_alloc(bl, size);
  if (!ptr) {
    if (mem->alloc_union.lls) {
      return (mem->alloc = CTScope_MemBlock_alloc_alternative)(mem, size);
    } else {
      printf("Ran out of space in MemoryArea: %d is not enough space.\n",
	     (bl->end)-(bl->begin));
      printf("Try increasing your MemoryArea size.\n");
    }
  }
#ifdef RTJ_DEBUG
  printf("  begin: %p, free: %p, end: %p\n", bl->begin, bl->free, bl->end);
  if (!ptr) {
    assert("Out of memory!!!");
    checkException();
  }
#endif
  return ptr;
}

void  CTScope_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("CTScope_MemBlock_free(%p)\n", mem);
#endif
  Block_reset(mem->block);
}

#ifdef WITH_PRECISE_GC
void  CTScope_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("CTScope_MemBlock_gc(%p)\n", mem);
#endif
  Block_scan(mem->block);
}
#endif

void  CTScope_MemBlock_finalize(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("CTScope_MemBlock_finalize(%p)\n", mem);
#endif
  Block_free(mem->block);
}
