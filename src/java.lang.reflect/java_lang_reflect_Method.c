#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Method.h"

/*
 * Class:     java_lang_reflect_Method
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Method_getModifiers
  (JNIEnv *env, jobject _this) {
    return FNI_GetMethodInfo(_this)->modifiers;
}

#if 0
/*
 * Class:     java_lang_reflect_Method
 * Method:    invoke
 * Signature: (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Method_invoke
  (JNIEnv *, jobject, jobject, jobjectArray);
#endif

#if !defined(WITHOUT_HACKED_REFLECTION) /* this is our hacked implementation */
/*
 * Class:     java_lang_reflect_Method
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Method_getDeclaringClass
  (JNIEnv *env, jobject obj) {
  assert(0);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Method_getName
  (JNIEnv *env, jobject obj) {
  assert(0);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getReturnType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Method_getReturnType
  (JNIEnv *env, jobject obj) {
  assert(0);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getParameterTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Method_getParameterTypes
  (JNIEnv *env, jobject obj) {
  assert(0);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getExceptionTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Method_getExceptionTypes
  (JNIEnv *env, jobject obj) {
  assert(0);
}

#endif /* !WITHOUT_HACKED_REFLECTION */
