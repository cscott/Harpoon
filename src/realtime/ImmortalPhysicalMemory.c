/* ImmortalPhysicalMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "ImmortalPhysicalMemory.h"

/*
 * Class:     ImmortalPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalPhysicalMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {

}

/*
 * Class:     ImmortalPhysicalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalPhysicalMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct BlockInfo* bi = getInflatedObject(env, realtimeThread)->temp->block_info;
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysicalMemory.newMemBlock(0x%08x, 0x%08x, 0x%08x)\n", env,
	 memoryArea, realtimeThread);
#endif
#ifdef WITH_NOHEAP_SUPPORT
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = ImmortalPhysical_NoHeapRThread_MemBlock_alloc;
    bi->free      = ImmortalPhysical_NoHeapRThread_MemBlock_free;
    bi->allocator = ImmortalPhysical_NoHeapRThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
    bi->gc        = NULL;
#endif
  } else {
#endif
    bi->alloc     = ImmortalPhysical_RThread_MemBlock_alloc;
    bi->free      = ImmortalPhysical_RThread_MemBlock_free;
    bi->allocator = ImmortalPhysical_RThread_MemBlock_allocator(memoryArea);
#ifdef WITH_PRECISE_GC
    bi->gc        = ImmortalPhysical_RThread_MemBlock_gc;
#endif
#ifdef WITH_NOHEAP_SUPPORT
  }
#endif
}

void* ImmortalPhysical_RThread_MemBlock_alloc(struct MemBlock* mem, 
					      size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_RThread_MemBlock_alloc(mem, size);
}

void  ImmortalPhysical_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_RThread_MemBlock_free()\n");
#endif
  Scope_RThread_MemBlock_free(mem);
}

inline Allocator ImmortalPhysical_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

#ifdef WITH_PRECISE_GC
void  ImmortalPhysical_RThread_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_RThread_MemBlock_gc(0x%08x)\n", mem);
#endif
}
#endif

#ifdef WITH_NOHEAP_SUPPORT
void* ImmortalPhysical_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem,
						    size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_NoHeapRThread_MemBlock_alloc(mem, size);
}

void  ImmortalPhysical_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_NoHeapRThread_MemBlock_free()\n");
#endif
  Scope_NoHeapRThread_MemBlock_free(mem);
}

inline Allocator ImmortalPhysical_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}
#endif


