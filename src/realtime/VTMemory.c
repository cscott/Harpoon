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
  struct BlockInfo* bi = getInflatedObject(env, realtimeThread)->temp->block_info;
#ifdef DEBUG
  printf("VTMemory.newMemBlock(%08x, %08x, %08x)\n", env, memoryArea, 
	 realtimeThread);
  checkException(env);
#endif
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = VTScope_NoHeapRThread_MemBlock_alloc;
    bi->free      = VTScope_NoHeapRThread_MemBlock_free;
    bi->allocator = VTScope_NoHeapRThread_MemBlock_allocator(memoryArea);
  } else {
    bi->alloc     = VTScope_RThread_MemBlock_alloc;
    bi->free      = VTScope_RThread_MemBlock_free;
    bi->allocator = VTScope_RThread_MemBlock_allocator(memoryArea);
  }
}

