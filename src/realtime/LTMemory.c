/* LTMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "LTMemory.h"

/*
 * Class:     LTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {

}

/*
 * Class:     LTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct BlockInfo* bi = getInflatedObject(env, realtimeThread)->temp->block_info;
#ifdef RTJ_DEBUG
  printf("LTMemory.newMemBlock(%08x, %08x, %08x)\n", env, memoryArea,
	 realtimeThread);
#endif
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = LTScope_NoHeapRThread_MemBlock_alloc;
    bi->free      = LTScope_NoHeapRThread_MemBlock_free;
    bi->allocator = LTScope_NoHeapRThread_MemBlock_allocator(memoryArea);
  } else {
    bi->alloc     = LTScope_RThread_MemBlock_alloc;
    bi->free      = LTScope_RThread_MemBlock_free;
    bi->allocator = LTScope_RThread_MemBlock_allocator(memoryArea);
  }
}
