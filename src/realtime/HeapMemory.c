/* HeapMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "HeapMemory.h"

/*
 * Class:     HeapMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
#ifdef RTJ_DEBUG
  printf("HeapMemory.initNative(0x%08x, 0x%08x, %d)\n", env, memoryArea, size); 
#endif  
}

/*
 * Class:     HeapMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct MemBlock* mb = getInflatedObject(env, realtimeThread)->temp;
  struct BlockInfo* bi = mb->block_info;
#ifdef RTJ_DEBUG
  checkException();
  printf("HeapMemory.newMemBlock(0x%08x, 0x%08x, 0x%08x)\n", env, memoryArea,
	 realtimeThread);
#ifdef WITH_NOHEAP_SUPPORT
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    assert("Can't enter a HeapMemory in a NoHeapRealtimeThread!\n");
  }
#endif
#endif
  bi->alloc     = Heap_RThread_MemBlock_alloc;
  bi->free      = Heap_RThread_MemBlock_free;
  bi->allocator = Heap_RThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
  bi->gc        = NULL;
#endif
  mb->ref_info  = RefInfo_new(0);
}

inline struct MemBlock* HeapMemory_RThread_MemBlock_new() {
  /* This function is called to create the initial MemBlock. */
  struct MemBlock* memBlock = 
    (struct MemBlock*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct MemBlock));
  struct BlockInfo* bi = (struct BlockInfo*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct BlockInfo));
#ifdef RTJ_DEBUG_REF
  struct PTRinfo* old_ptr_info;
#endif
#ifdef RTJ_DEBUG
  checkException();
  printf("HeapMemory_RThread_MemBlock_new()\n");
#endif
  memBlock->block_info = bi;
  bi->memoryArea = NULL;
  bi->realtimeThread = NULL;
  bi->alloc = Heap_RThread_MemBlock_alloc;
  bi->free = Heap_RThread_MemBlock_free;
#ifdef WITH_PRECISE_GC
  bi->gc = NULL;
#endif
  bi->superBlock = NULL;
  bi->allocator = NULL;
  memBlock->ref_info = RefInfo_new(0);
  MemBlock_INCREF(memBlock);
#ifdef RTJ_DEBUG_REF
  old_ptr_info = ptr_info;
  ptr_info = (struct PTRinfo*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct PTRinfo));
  ptr_info->next = old_ptr_info;
  ptr_info->memBlock = memBlock;
  flex_mutex_init(&(memBlock->ptr_info_lock));
#endif
  return memBlock;
}

void* Heap_RThread_MemBlock_alloc(struct MemBlock* mem, 
				  size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Heap_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return HeapMemory_alloc(size);
}

void  Heap_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Heap_RThread_MemBlock_free()\n");
#endif
  HeapMemory_freeAll();
}

inline Allocator Heap_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Heap_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

#ifdef WITH_PRECISE_GC
void  Heap_RThread_MemBlock_gc(struct MemBlock* mem) {
  assert("Should never reach Heap_RThread_MemBlock_gc()\n");
  checkException();
}
#endif

