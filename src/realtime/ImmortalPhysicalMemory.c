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
#ifdef DEBUG
  printf("ImmortalPhysicalMemory.newMemBlock(%08x, %08x, %08x)\n", env,
	 memoryArea, realtimeThread);
#endif
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = ImmortalPhysical_NoHeapRThread_MemBlock_alloc;
    bi->free      = ImmortalPhysical_NoHeapRThread_MemBlock_free;
    bi->allocator = ImmortalPhysical_NoHeapRThread_MemBlock_allocator(memoryArea);
  } else {
    bi->alloc     = ImmortalPhysical_RThread_MemBlock_alloc;
    bi->free      = ImmortalPhysical_RThread_MemBlock_free;
    bi->allocator = ImmortalPhysical_RThread_MemBlock_allocator(memoryArea);
  }
}

