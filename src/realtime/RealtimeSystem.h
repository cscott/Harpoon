/* RealtimeSystem.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include <jni.h>

#ifndef _Included_javax_realtime_RealtimeSystem
#define _Included_javax_realtime_RealtimeSystem
#include "MemBlock.h"
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     javax_realtime_RealtimeSystem
 * Method:    getCurrentGC
 * Signature: ()Ljavax/realtime/GarbageCollector;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_RealtimeSystem_getCurrentGC
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif
