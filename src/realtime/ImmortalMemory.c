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
#ifdef RTJ_DEBUG
  printf("ImmortalMemory.initNative(0x%08x, 0x%08x, %d)\n",
	 env, memoryArea, (size_t)size);
  checkException();
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
  struct BlockInfo* bi = rtmb->block_info;
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalMemory.newMemBlock(0x%08x, 0x%08x, 0x%08x)\n", env, memoryArea,
	 realtimeThread);
#endif
#ifdef WITH_NOHEAP_SUPPORT
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = Immortal_NoHeapRThread_MemBlock_alloc;
    bi->free      = NULL;
    bi->allocator = NULL;
#ifdef WITH_PRECISE_GC
    bi->gc        = NULL;
#endif
  } else {
#endif
    bi->alloc     = Immortal_RThread_MemBlock_alloc;
    bi->free      = NULL;
    bi->allocator = NULL;
#ifdef WITH_PRECISE_GC
    bi->gc        = NULL;
#endif
#ifdef WITH_NOHEAP_SUPPORT
  }
#endif
  rtmb->ref_info = RefInfo_new(0);
  MemBlock_INCREF(rtmb);
}

void* Immortal_RThread_MemBlock_alloc(struct MemBlock* mem, 
				      size_t size) {
  void* result;
#ifdef WITH_PRECISE_GC
  JNIEnv* env = FNI_GetJNIEnv();
  struct _jobject obj;
#endif
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_RThread_MemBlock_alloc(%d)\n", size);
#endif
  result = (void*)RTJ_MALLOC_UNCOLLECTABLE(size);
#ifdef WITH_PRECISE_GC
  obj.obj = (struct oobj*)result;
  FNI_NewGlobalRef(env, &obj);
#endif
  return result;
}

#ifdef WITH_NOHEAP_SUPPORT
void* Immortal_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
					    size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("Immortal_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return (void*)RTJ_MALLOC_UNCOLLECTABLE(size);
}
#endif
