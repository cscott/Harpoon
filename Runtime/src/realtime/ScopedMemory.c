#include "ScopedMemory.h"
#include "MemBlock.h"

/*
 * Class:     javax_realtime_ScopedMemory
 * Method:    DECREF
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedMemory_DECREF
(JNIEnv *env, jobject memoryArea) {
  MemBlock_DECREF(getInflatedObject(env, memoryArea)->memBlock);
  checkException(env);
} 

/*
 * Class:     javax_realtime_ScopedMemory
 * Method:    INCREF
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedMemory_INCREF
(JNIEnv *env, jobject memoryArea) {
  MemBlock_INCREF(getInflatedObject(env, memoryArea)->memBlock);
  checkException(env);
}

/*
 * Class:     javax_realtime_ScopedMemory
 * Method:    setPortalObj
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedMemory_setPortalObj
(JNIEnv *env, jobject memoryArea, jobject portalObj) {
  jobject obj = NULL;
  jobject* obj2 = &getInflatedObject(env, memoryArea)->portalObj;
  if (portalObj != NULL) {
    obj = (*env)->NewGlobalRef(env, portalObj);
  }
  if (*obj2 != NULL) {
    (*env)->DeleteGlobalRef(env, *obj2);
  }
  *obj2 = obj;
  checkException(env);
}

/*
 * Class:     javax_realtime_ScopedMemory
 * Method:    getPortalObj
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_ScopedMemory_getPortalObj
(JNIEnv *env, jobject memoryArea) {
  jobject result = getInflatedObject(env, memoryArea)->portalObj;
  checkException(env);
  return result;
}

