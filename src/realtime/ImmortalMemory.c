/* ImmortalMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "ImmortalMemory.h"

/*
 * Class:     ImmortalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
  struct MemBlock* mbHeap = (struct MemBlock*)
      RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct MemBlock));
#ifdef WITH_NOHEAP_SUPPORT
  struct MemBlock* mbNoHeap = (struct MemBlock*)
      RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct MemBlock));
#endif
  struct inflated_oobj* inflated_ma;
#ifdef RTJ_DEBUG
  printf("ImmortalMemory.initNative(0x%08x, 0x%08x, %d)\n",
	 env, memoryArea, (size_t)size);
  checkException();
#endif
  (inflated_ma = getInflatedObject(env, memoryArea))->memBlock = mbHeap;
#ifdef WITH_NOHEAP_SUPPORT
  inflated_ma->temp = mbNoHeap;
  mbNoHeap->block_info = (struct BlockInfo*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct BlockInfo)); 
  mbNoHeap->block_info->allocator = ListAllocator_new(1);
#endif
  mbHeap->block_info = (struct BlockInfo*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct BlockInfo));
  mbHeap->block_info->allocator = ListAllocator_new(0);
  mbHeap->ref_info = RefInfo_new(0);
  /* Make sure this is never freed */
  MemBlock_INCREF(mbHeap); 
#ifdef WITH_PRECISE_GC
  mbHeap->block_info->gc = Immortal_RThread_MemBlock_gc;
  add_MemBlock_to_roots(mbHeap);
#endif  
}

/*
 * Class:     ImmortalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct MemBlock* rtmb = getInflatedObject(env, realtimeThread)->temp;
  struct MemBlock* mamb = getInflatedObject(env, memoryArea)->memBlock;
  struct BlockInfo* bi = rtmb->block_info;
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalMemory.newMemBlock(0x%08x, 0x%08x, 0x%08x)\n", env, memoryArea,
	 realtimeThread);
#endif
#ifdef WITH_NOHEAP_SUPPORT
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = Immortal_NoHeapRThread_MemBlock_alloc;
    bi->free      = Immortal_NoHeapRThread_MemBlock_free;
    bi->allocator = Immortal_NoHeapRThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
    bi->gc        = NULL;
#endif
  } else {
#endif
    bi->alloc     = Immortal_RThread_MemBlock_alloc;
    bi->free      = Immortal_RThread_MemBlock_free;
    bi->allocator = Immortal_RThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
    bi->gc        = Immortal_RThread_MemBlock_gc;
#endif
#ifdef WITH_NOHEAP_SUPPORT
  }
#endif
  rtmb->ref_info = mamb->ref_info;
}

void* Immortal_RThread_MemBlock_alloc(struct MemBlock* mem, 
				      size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return ListAllocator_alloc((struct ListAllocator*)(mem->block_info->allocator),
			     size);
}

void  Immortal_RThread_MemBlock_free(struct MemBlock* mem) {
  assert("Immortal_RThread_MemBlock_free called!\nThis can't happen!\n");
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_RThread_MemBlock_free()\n");
#endif
}

inline Allocator Immortal_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_RThread_MemBlock_allocator()\n");
#endif
  return getInflatedObject(FNI_GetJNIEnv(), 
			   memoryArea)->memBlock->block_info->allocator;
}

#ifdef WITH_PRECISE_GC
void Immortal_RThread_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_RThread_MemBlock_gc(0x%08x)\n", mem);
#endif
  ListAllocator_gc(mem->block_info->allocator);
}
#endif

#ifdef WITH_NOHEAP_SUPPORT
void* Immortal_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
					    size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return ListAllocator_alloc((struct ListAllocator*)(mem->block_info->allocator),
			     size);
}

void  Immortal_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
  assert("Immortal_RThread_MemBlock_free called!\nThis can't happen!\n");
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_NoHeapRThread_MemBlock_free()\n");
#endif
}

inline Allocator Immortal_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return getInflatedObject(FNI_GetJNIEnv(),
			   memoryArea)->temp->block_info->allocator;
}
#endif
