/* RefCountArea.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */
#include <jni.h>

#ifndef _Included_javax_realtime_RefCountArea
#define _Included_javax_realtime_RefCountArea
#ifdef __cplusplus
extern "C" {
#endif
#include "refCountAllocator.h"

/*
 * Class:     javax_realtime_RefCountArea
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_RefCountArea_initNative
(JNIEnv* env, jobject refCountArea, jlong ignored);

/*
 * Class:     javax_realtime_RefCountArea
 * Method:    INCREF
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_RefCountArea_INCREF
(JNIEnv* env, jobject refCountArea, jobject obj);

/*
 * Class:     javax_realtime_RefCountArea
 * Method:    DECREF
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_RefCountArea_DECREF
(JNIEnv* env, jobject refCountArea, jobject obj);

#ifdef __cplusplus
}
#endif
#endif
