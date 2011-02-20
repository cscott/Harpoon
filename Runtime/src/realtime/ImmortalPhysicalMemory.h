/* ImmortalPhysicalMemory.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include <jni.h>

#ifndef _Included_ImmortalPhysicalMemory
#define _Included_ImmortalPhysicalMemory
#include "RTJmalloc.h"
#ifdef __cplusplus
extern "C" {
#endif

/*
 * Class:     ImmortalPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalPhysicalMemory_initNative
  (JNIEnv *, jobject, jlong);

#ifdef __cplusplus
}
#endif
#endif
