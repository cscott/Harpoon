/* MemoryArea.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "MemoryArea.h"

/*
 * Class:     MemoryArea
 * Method:    enterMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_enterMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  MemBlock_setCurrentMemBlock(env, realtimeThread,
			      MemBlock_new(env, memoryArea, realtimeThread,
					   MemBlock_currentMemBlock()));
}

/*
 * Class:     MemoryArea
 * Method:    exitMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_exitMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct MemBlock* memBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread, 
			      MemBlock_prevMemBlock(memBlock));
  MemBlock_free(memBlock);
}

