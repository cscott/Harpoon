/* LTMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "LTMemory.h"

/*
 * Class:     LTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong initial, jlong maximum) {
  Java_javax_realtime_CTMemory_initNative(env, memoryArea, initial, maximum);
}

/*
 * Class:     CTMemory
 * Method:    doneNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_doneNative
(JNIEnv* env, jobject memoryArea) {
  Java_javax_realtime_CTMemory_doneNative(env, memoryArea);
}
