/* NoHeapRealtimeThread.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include <jni.h>

#ifndef _Included_javax_realtime_NoHeapRealtimeThread
#define _Included_javax_realtime_NoHeapRealtimeThread
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__Ljava_lang_String_2
  (JNIEnv *, jclass, jstring);

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__D
  (JNIEnv *, jclass, jdouble);

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__I
  (JNIEnv *, jclass, jint);

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__J
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
