/* ScopedPhysicalMemory.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include <jni.h>
#include "MemBlock.h"

#ifndef _Included_ScopedPhysicalMemory
#define _Included_ScopedPhysicalMemory
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     ScopedPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_initNative
  (JNIEnv *, jobject, jlong);

/*
 * Class:     ScopedPhysicalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_newMemBlock
  (JNIEnv *, jobject, jobject);

#ifdef __cplusplus
}
#endif
#endif
