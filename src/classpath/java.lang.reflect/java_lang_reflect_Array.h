#include "config.h"
#include <jni.h>

JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_createObjectArray
  (JNIEnv *env, jclass thisClass, jclass arrayType, jint arrayLength);

#ifdef WITH_INIT_CHECK
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_createObjectArray_00024_00024initcheck
  (JNIEnv *env, jclass thisClass, jclass arrayType, jint arrayLength);
#endif /* WITH_INIT_CHECK */
