/* HeapMemory.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include <jni.h>

#ifndef _Included_HeapMemory
#define _Included_HeapMemory
#include "RTJmalloc.h"
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     HeapMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_initNative
  (JNIEnv *, jobject, jlong);

/*
 * Class:     HeapMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_newMemBlock
  (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
