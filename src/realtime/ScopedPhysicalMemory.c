/* ScopedPhysicalMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "ScopedPhysicalMemory.h"

/*
 * Class:     ScopedPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
  
}

/*
 * Class:     ScopedPhysicalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct BlockInfo* bi = getInflatedObject(env, realtimeThread)->temp->block_info;
#ifdef RTJ_DEBUG
  printf("ScopedPhysicalMemory.newMemBlock(0x%08x, 0x%08x, 0x%08x)\n", env,
	 memoryArea, realtimeThread);
#endif
#ifdef WITH_NOHEAP_SUPPORT
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = ScopedPhysical_NoHeapRThread_MemBlock_alloc;
    bi->free      = ScopedPhysical_NoHeapRThread_MemBlock_free;
    bi->allocator = ScopedPhysical_NoHeapRThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
    bi->gc        = NULL;
#endif
  } else {
#endif
    bi->alloc     = ScopedPhysical_RThread_MemBlock_alloc;
    bi->free      = ScopedPhysical_RThread_MemBlock_free;
    bi->allocator = ScopedPhysical_RThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
    bi->gc        = ScopedPhysical_RThread_MemBlock_gc;
#endif
#ifdef WITH_NOHEAP_SUPPORT
  }
#endif
}

void* ScopedPhysical_RThread_MemBlock_alloc(struct MemBlock* mem, 
					    size_t size) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_RThread_MemBlock_alloc(mem, size);
}

void  ScopedPhysical_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_free()\n");
#endif
  Scope_RThread_MemBlock_free(mem);
}

inline Allocator ScopedPhysical_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

#ifdef WITH_PRECISE_GC
void  ScopedPhysical_RThread_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_gc(0x%08x)\n", mem);
#endif
}
#endif

#ifdef WITH_NOHEAP_SUPPORT
void* ScopedPhysical_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
						  size_t size) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_NoHeapRThread_MemBlock_alloc(mem, size);
}

void  ScopedPhysical_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_NoHeapRThread_MemBlock_free()\n");
#endif
  Scope_NoHeapRThread_MemBlock_free(mem);
}

inline Allocator ScopedPhysical_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}
#endif


