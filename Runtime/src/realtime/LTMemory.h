/* LTMemory.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */
#include <jni.h>

#ifndef _Included_LTMemory
#define _Included_LTMemory
#include "RTJmalloc.h"
#include "CTMemory.h"
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     CTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong initial, jlong maximum);

/*
 * Class:     CTMemory
 * Method:    doneNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_doneNative
(JNIEnv* env, jobject memoryArea);

#ifdef __cplusplus
}
#endif
#endif
