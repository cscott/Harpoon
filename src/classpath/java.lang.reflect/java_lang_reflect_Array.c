#include "java_lang_reflect_Array.h"

#ifdef WITH_INIT_CHECK
JNIEXPORT jobject JNICALL 
Java_java_lang_reflect_Array_createObjectArray_00024_00024initcheck
(JNIEnv *env, jclass thisClass, jclass arrayType, jint arrayLength) {
  if (!REFLECT_staticinit(env, arrayType)) return NULL;
  return Java_java_lang_reflect_Array_createObjectArray
    (env, thisClass, arrayType, arrayLength);
}
#endif
