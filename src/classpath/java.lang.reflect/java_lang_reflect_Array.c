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
  /* find base component type */
  jclass baseType = arrayType;
  while (fni_class_isArray(env, baseType))
    baseType = fni_class_getComponentType(env, baseType);
  /* call the static initializer of the base type */
  if (!REFLECT_staticinit(env, baseType))
    return NULL; // exception in static initializer
  /* okay, *now* call the standard createObjectArray() method */
  return Java_java_lang_reflect_Array_createObjectArray
    (env, thisClass, arrayType, arrayLength);
}
#endif /* WITH_INIT_CHECK */
