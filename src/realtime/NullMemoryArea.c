/* NullMemoryArea.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "NullMemoryArea.h"

/*
 * Class:     NullMemoryArea
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
  
}

/*
 * Class:     NullMemoryArea
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
#ifdef DEBUG
  assert("Should never enter a NullMemoryArea!\n");
#endif
}
