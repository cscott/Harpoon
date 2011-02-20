/* NullMemoryArea.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include <jni.h>

#ifndef _Included_NullMemoryArea
#define _Included_NullMemoryArea
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     NullMemoryArea
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_initNative
  (JNIEnv *, jobject, jlong);

/*
 * Class:     NullMemoryArea
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_newMemBlock
  (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
