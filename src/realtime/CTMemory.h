/* CTMemory.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */
#include <jni.h>

#ifndef _Included_CTMemory
#define _Included_CTMemory
#include "RTJmalloc.h"
#include "block.h"
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     CTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong minimum, jlong maximum);

/*
 * Class:     CTMemory
 * Method:    doneNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_doneNative
(JNIEnv* env, jobject memoryArea);

#ifdef __cplusplus
}
#endif
#endif
