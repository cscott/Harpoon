#include "config.h"

#ifdef WITH_INIT_CHECK
#include "../../java.lang.reflect/reflect-util.h" 

JNIEXPORT jobject JNICALL
Java_java_lang_reflect_Array_createObjectArray
(JNIEnv *env, jclass thisClass, jclass arrayType, jint arrayLength);

JNIEXPORT jobject JNICALL
Java_java_lang_reflect_Array_createObjectArray_00024_00024initcheck
(JNIEnv *env, jclass thisClass, jclass arrayType, jint arrayLength);

#endif
