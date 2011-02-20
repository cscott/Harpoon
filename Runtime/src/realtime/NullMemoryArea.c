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

JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_initNative_00024_00024initcheck
(JNIEnv* env, jobject memoryArea, jlong size) {
#ifdef RTJ_DEBUG
  printf("\nNullMemoryArea.initNative_initcheck");
#endif
  Java_javax_realtime_NullMemoryArea_initNative(env, memoryArea, size);
}
