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

}

/*
 * Class:     ImmortalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct BlockInfo* bi = getInflatedObject(env, realtimeThread)->temp->block_info;
#ifdef RTJ_DEBUG
  printf("ImmortalMemory.newMemBlock(%08x, %08x, %08x)\n", env, memoryArea,
	 realtimeThread);
#endif
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = Immortal_NoHeapRThread_MemBlock_alloc;
    bi->free      = Immortal_NoHeapRThread_MemBlock_free;
    bi->allocator = Immortal_NoHeapRThread_MemBlock_allocator(memoryArea);
  } else {
    bi->alloc     = Immortal_RThread_MemBlock_alloc;
    bi->free      = Immortal_RThread_MemBlock_free;
    bi->allocator = Immortal_RThread_MemBlock_allocator(memoryArea);
  }
}

