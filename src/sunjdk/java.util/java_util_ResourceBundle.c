/* Implementations for class java_util_ResourceBundle */
#include "java_util_ResourceBundle.h"

/*
 * Class:     java_util_ResourceBundle
 * Method:    getClassContext
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_util_ResourceBundle_getClassContext
  (JNIEnv *env, jclass cls) {
  /* this function is supposed to return an array of Class objects
   * describing the calling context of this method.  Element 0
   * represents the caller of this function (typically
   * ResourceBundle.getLoder()), element 1 is our caller's caller, etc.
   * There should always be at least 3 elements in the returned array. */
  // XXX PUNT: just return nulls.
  jclass clsclz = (*env)->FindClass(env, "java/lang/Class");
  if (clsclz==NULL) return NULL;
  return (*env)->NewObjectArray(env, 3, clsclz, NULL);
}
