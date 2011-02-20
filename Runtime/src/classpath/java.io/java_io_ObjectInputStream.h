#include "config.h"
#include <jni.h>

/* From classpath sources */
JNIEXPORT jobject JNICALL Java_java_io_ObjectInputStream_allocateObject
  (JNIEnv *env, jobject self, jclass clazz);

#ifdef WITH_INIT_CHECK
JNIEXPORT jobject JNICALL Java_java_io_ObjectInputStream_allocateObject_00024_00024initcheck
  (JNIEnv *env, jobject self, jclass clazz);
#endif

