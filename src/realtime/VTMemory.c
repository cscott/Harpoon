/* VTMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "VTMemory.h"

/*
 * Class:     VTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_VTMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
}

/*
 * Class:     VTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_VTMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct MemBlock* mb = getInflatedObject(env, realtimeThread)->temp;
  struct BlockInfo* bi = mb->block_info;
#ifdef RTJ_DEBUG
  printf("VTMemory.newMemBlock(0x%08x, 0x%08x, 0x%08x)\n", env, 
	 memoryArea, realtimeThread);
  checkException();
#endif
#ifdef WITH_NOHEAP_SUPPORT
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = VTScope_NoHeapRThread_MemBlock_alloc;
    bi->free      = VTScope_NoHeapRThread_MemBlock_free;
    bi->allocator = VTScope_NoHeapRThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
    bi->gc        = NULL;
#endif
  } else {
#endif
    bi->alloc     = VTScope_RThread_MemBlock_alloc;
    bi->free      = VTScope_RThread_MemBlock_free;
    bi->allocator = VTScope_RThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
    bi->gc        = VTScope_RThread_MemBlock_gc;
    add_MemBlock_to_roots(mb);
#endif
#ifdef WITH_NOHEAP_SUPPORT
  }
#endif
  mb->ref_info = RefInfo_new(0);
}

void* VTScope_RThread_MemBlock_alloc(struct MemBlock* mem, 
				     size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_RThread_MemBlock_alloc(0x%08x, %d)\n", mem, (int)size);
#endif
  return ListAllocator_alloc((struct ListAllocator*)(mem->block_info->allocator), 
			     size);
}

void  VTScope_RThread_MemBlock_free(struct MemBlock* mem) {
  JNIEnv* env = FNI_GetJNIEnv();
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_RThread_MemBlock_free(0x%08x)\n", mem);
#endif
#ifdef WITH_PRECISE_GC
  remove_MemBlock_from_roots(mem);
#endif
  ListAllocator_free((struct ListAllocator*)(mem->block_info->allocator));
#ifdef RTJ_DEBUG
  printf("  free(0x%08x)\n", mem);
#endif
  (*env)->DeleteGlobalRef(env, mem->block_info->memoryArea);
  (*env)->DeleteGlobalRef(env, mem->block_info->realtimeThread);
  RTJ_FREE(mem->block_info);
  RTJ_FREE(mem); 
}

inline Allocator VTScope_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_RThread_MemBlock_allocator()\n");
#endif
  return ListAllocator_new(0);
}

#ifdef WITH_PRECISE_GC
void  VTScope_RThread_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_RThread_MemBlock_gc(0x%08x)\n", mem);
#endif
  ListAllocator_gc(mem->block_info->allocator);
}
#endif

#ifdef WITH_NOHEAP_SUPPORT
void* VTScope_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
					   size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_NoHeapRThread_MemBlock_alloc(0x%08x, %d)\n", mem, size);
#endif
  return ListAllocator_alloc((struct ListAllocator*)(mem->block_info->allocator),
			     size);
}

void  VTScope_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_NoHeapRThread_MemBlock_free(0x%08x)\n", mem);
#endif
  ListAllocator_free((struct ListAllocator*)(mem->block_info->allocator));
#ifdef RTJ_DEBUG
  printf("  free(0x%08x)\n", mem);
#endif
  RTJ_FREE(mem);
}

inline Allocator VTScope_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return ListAllocator_new(1);
}
#endif
