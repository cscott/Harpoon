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

}

/*
 * Class:     HeapMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct BlockInfo* bi = getInflatedObject(env, realtimeThread)->temp->block_info;
#ifdef DEBUG
  printf("HeapMemory.newMemBlock(%08x, %08x, %08x)\n", env, memoryArea,
	 realtimeThread);
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    assert("Can't enter a HeapMemory in a NoHeapRealtimeThread!\n");
  }
#endif
  bi->alloc     = Heap_RThread_MemBlock_alloc;
  bi->free      = Heap_RThread_MemBlock_free;
  bi->allocator = Heap_RThread_MemBlock_allocator(memoryArea);
}

