#include <jni.h>
#include "jni-private.h"
#include "write_barrier.h"
#include "asm/atomicity.h"

/* Implementation for class AtomicInteger */

/*
 * Class:     AtomicInteger
 * Method:    compareAndSet
 * Signature: (II)Z
 */
JNIEXPORT jboolean JNICALL Java_java_util_concurrent_atomic_AtomicInteger_compareAndSet
(JNIEnv *env, jobject this, jint expect, jint update){
  jclass claz = (*env)->GetObjectClass(env, this);
  jfieldID id = (*env)->GetFieldID(env, claz, "myValue", "I");  
  jint *valPtr = ((jint *)(id->offset + (ptroff_t)FNI_UNWRAP_MASKED(this)));
  return compare_and_swap (valPtr, expect, update);
}

/*
 * Class:     AtomicInteger
 * Method:    getAndAdd
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_java_util_concurrent_atomic_AtomicInteger_getAndAdd
(JNIEnv *env, jobject this, jint delta){
  jclass claz = (*env)->GetObjectClass(env, this);
  jfieldID id = (*env)->GetFieldID(env, claz, "myValue", "I");  
  jint *valPtr = ((jint *)(id->offset + (ptroff_t)FNI_UNWRAP_MASKED(this)));
  return exchange_and_add (valPtr, delta);
}

/*
 * Class:     AtomicInteger
 * Method:    getAndSet
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_java_util_concurrent_atomic_AtomicInteger_getAndSet
(JNIEnv *env, jobject this, jint newValue){
  jclass claz = (*env)->GetObjectClass(env, this);
  jfieldID id = (*env)->GetFieldID(env, claz, "myValue", "I");  
  jint *valPtr = ((jint *)(id->offset + (ptroff_t)FNI_UNWRAP_MASKED(this)));
  jint tmp;

  while(!compare_and_swap(valPtr, tmp=*valPtr, newValue));
  
  return tmp;
}
