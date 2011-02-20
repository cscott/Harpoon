/* RealtimeSystem.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "RealtimeSystem.h"

/*
 * Class:     javax_realtime_RealtimeSystem
 * Method:    getCurrentGC
 * Signature: ()Ljavax/realtime/GarbageCollector;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_RealtimeSystem_getCurrentGC
(JNIEnv* env, jclass realtimeSystem) {
  jclass gcCls;
  jmethodID gcClsConsID;
  jobject gc;
#ifdef RTJ_DEBUG
  printf("getCurrentGC: ");
#endif
#ifdef BDW_CONSERVATIVE_GC
  gcCls = (*env)->FindClass(env, "javax/realtime/BDWGarbageCollector");
#ifdef RTJ_DEBUG
  printf("BDW\n");
  checkException();
#endif
  gcClsConsID = (*env)->GetMethodID(env, gcCls, "<init>", "()V");
#ifdef RTJ_DEBUG
  checkException();
#endif
  gc = (*env)->NewObject(env, gcCls, gcClsConsID);
#ifdef RTJ_DEBUG
  checkException();
#endif
#elif defined(WITH_PRECISE_GC)
#ifdef WITH_MARKSWEEP_GC
  gcCls = (*env)->FindClass(env, "javax/realtime/MarkSweepGarbageCollector");
#ifdef RTJ_DEBUG
  printf("Mark & Sweep\n");
  checkException();
#endif  
  gcClsConsID = (*env)->GetMethodID(env, gcCls, "<init>", "()V");
#ifdef RTJ_DEBUG
  checkException();
#endif
  gc = (*env)->NewObject(env, gcCls, gcClsConsID);
#ifdef RTJ_DEBUG
  checkException();
#endif
#endif
#ifdef WITH_COPYING_GC
  gcCls = (*env)->FindClass(env, "javax/realtime/CopyingGarbageCollector");
#ifdef RTJ_DEBUG
  printf("Stop & Copy\n");
  checkException();
#endif
  gcClsConsID = (*env)->GetMethodID(env, gcCls, "<init>", "()V");
#ifdef RTJ_DEBUG
  checkException();
#endif
  gc = (*env)->NewObject(env, gcCls, gcClsConsID);
#ifdef RTJ_DEBUG
  checkException();
#endif
#endif
#else
  gcCls = (*env)->FindClass(env, "javax/realtime/NoGarbageCollector");
#ifdef RTJ_DEBUG
  printf("none\n");
  checkException();
#endif
  gcClsConsID = (*env)->GetMethodID(env, gcCls, "<init>", "()V");
#ifdef RTJ_DEBUG
  checkException();
#endif
  gc = (*env)->NewObject(env, gcCls, gcClsConsID);
#ifdef RTJ_DEBUG
  checkException();
#endif
#endif  
  return gc;
}
