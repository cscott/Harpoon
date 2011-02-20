#include <jni.h>
#include "config.h"
#include "java_lang_reflect_Array.h"
#include "../../java.lang/class.h" /* for fni_class_getComponentType() */

#ifdef WITH_INIT_CHECK
#include "../../java.lang.reflect/reflect-util.h"
#endif /* WITH_INIT_CHECK */

#ifdef WITH_INIT_CHECK
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_createObjectArray_00024_00024initcheck
  (JNIEnv *env, jclass thisClass, jclass arrayType, jint arrayLength) {
  /* call the static initializer of the array type (and its components) */
  if (!REFLECT_staticinit(env, arrayType))
    return NULL; // exception in static initializer
  /* okay, *now* call the standard createObjectArray() method */
  return Java_java_lang_reflect_Array_createObjectArray
    (env, thisClass, arrayType, arrayLength);
}
#endif /* WITH_INIT_CHECK */
