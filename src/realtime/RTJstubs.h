/* RTJstubs.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

/* Stubs in case you don't want to use RTJ, but your Java input 
   to the Flex compiler still has references to CTMemory's, etc. */

#include <jni.h>

#ifndef _Included_RTJstubs
#define _Included_RTJstubs

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     javax_realtime_MemoryArea
 * Method:    enterMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;Ljavax/realtime/MemAreaStack;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_enterMemBlock
(JNIEnv* env, jobject memoryArea, 
 jobject realtimeThread, jobject memAreaStack) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    exitMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_exitMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;ILjava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2ILjava_lang_Object_2
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, 
 jclass claz, jint size, jobject memBlock) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;[ILjava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2_3ILjava_lang_Object_2
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, 
 jclass claz, jintArray dims, jobject memBlock) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newInstance
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/reflect/Constructor;[Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newInstance
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, 
 jobject constructor, jobjectArray args, jobject memBlock) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     HeapMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_initNative
(JNIEnv* env, jobject heapMemory, jlong size) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     HeapMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_newMemBlock
(JNIEnv* env, jobject heapMemory, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     CTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_initNative
(JNIEnv* env, jobject CTMemory, jlong size) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     CTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_newMemBlock
(JNIEnv* env, jobject CTMemory, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     javax_realtime_CTMemory
 * Method:    doneNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_doneNative
(JNIEnv *env, jobject CTMemory) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     VTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_VTMemory_initNative
(JNIEnv* env, jobject VTMemory, jlong size) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     VTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_VTMemory_newMemBlock
(JNIEnv* env, jobject VTMemory, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     ImmortalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalMemory_initNative
(JNIEnv* env, jobject ImmortalMemory, jlong size) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     ImmortalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalMemory_newMemBlock
(JNIEnv* env, jobject ImmortalMemory, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     ImmortalPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalPhysicalMemory_initNative
(JNIEnv* env, jobject ImmortalPhysicalMemory, jlong size) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     ImmortalPhysicalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalPhysicalMemory_newMemBlock
(JNIEnv* env, jobject ImmortalPhysicalMemory, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     LTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_initNative
(JNIEnv* env, jobject LTMemory, jlong size) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     LTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_LTMemory_newMemBlock
(JNIEnv* env, jobject LTMemory, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     NullMemoryArea
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_initNative
(JNIEnv* env, jobject NullMemoryArea, jlong size) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     NullMemoryArea
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NullMemoryArea_newMemBlock
(JNIEnv* env, jobject NullMemoryArea, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     ScopedPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_initNative
(JNIEnv* env, jobject ScopedPhysicalMemory, jlong size) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

/*
 * Class:     ScopedPhysicalMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_newMemBlock
(JNIEnv* env, jobject ScopedPhysicalMemory, jobject realtimeThread) {
  printf("Did you forget to compile --with-realtime-java?\n");
}

void* RTJ_malloc(size_t size) {
  printf("Did you forget to compile --with-realtime-java?\n");
  return NULL;
}

#ifdef __cplusplus
}
#endif
#endif

