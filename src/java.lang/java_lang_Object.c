#include <jni.h>
#include <jni-private.h>
#include "java_lang_Object.h"

#include <assert.h>

/*
 * Class:     java_lang_Object
 * Method:    getClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Object_getClass
  (JNIEnv *env, jobject obj) {
    return (*env)->GetObjectClass(env, obj);
}

/*
 * Class:     java_lang_Object
 * Method:    hashCode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Object_hashCode
  (JNIEnv *env, jobject obj) {
    return Java_java_lang_System_identityHashCode(env, NULL, obj);
}

/* helper for clone functions. */
static jobject cloneHelper(JNIEnv *env, jobject obj, jsize len) {
  jclass clazz = FNI_GetObjectClass(env, obj);
  struct FNI_classinfo *info = FNI_GetClassInfo(clazz);
  jobject clone = FNI_Alloc(env, info, len);
  memcpy(FNI_UNWRAP(clone)->field_start,
	 FNI_UNWRAP(obj  )->field_start,
	 len - sizeof(struct oobj));
  return clone;
}

/*
 * Class:     java_lang_Object
 * Method:    clone
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Object_clone
  (JNIEnv *env, jobject obj) {
    jclass clazz = (*env)->GetObjectClass(env, obj);
    struct FNI_classinfo *info = FNI_GetClassInfo(clazz);
    u_int32_t size = info->claz->size;
    assert(Java_java_lang_Class_isArray(env, clazz)==JNI_FALSE);
    return cloneHelper(env, obj, size);
}

#define PRIMITIVEARRAYCLONE(name, type, sig)\
JNIEXPORT jobject JNICALL Java__3##sig##_clone\
  (JNIEnv *env, jobject obj) {\
    jsize len = (*env)->GetArrayLength(env, (jarray) obj);\
    return cloneHelper(env, obj, sizeof(struct aarray) + sizeof(type)*len);\
}
PRIMITIVEARRAYCLONE(Boolean, jboolean, Z);
PRIMITIVEARRAYCLONE(Byte, jbyte, B);
PRIMITIVEARRAYCLONE(Char, jchar, C);
PRIMITIVEARRAYCLONE(Short, jshort, S);
PRIMITIVEARRAYCLONE(Int, jint, I);
PRIMITIVEARRAYCLONE(Long, jlong, J);
PRIMITIVEARRAYCLONE(Float, jfloat, F);
PRIMITIVEARRAYCLONE(Double, jdouble, D);

#if WITH_HEAVY_THREADS
/*
 * Class:     java_lang_Object
 * Method:    notify
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_notify
  (JNIEnv *env, jobject _this) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_lang_Object
 * Method:    notifyAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_notifyAll
  (JNIEnv *env, jobject _this) {
  assert(0/*unimplemented*/);
}
/*
 * Class:     java_lang_Object
 * Method:    wait
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_wait
  (JNIEnv *env, jobject _this, jlong val) {
  assert(0/*unimplemented*/);
}

#endif /* WITH_HEAVY_THREADS */
