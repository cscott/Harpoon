#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Constructor.h"

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Constructor_getModifiers
  (JNIEnv *env, jobject _this) {
    return FNI_GetMethodInfo(_this)->modifiers;
}

#if 0
/*
 * Class:     java_lang_reflect_Constructor
 * Method:    newInstance
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Constructor_newInstance
  (JNIEnv *, jobject, jobjectArray);
#endif

#if !defined(WITHOUT_HACKED_REFLECTION) /* this is our hacked implementation */
/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Constructor_getDeclaringClass
  (JNIEnv *env, jobject obj) {
  assert(0);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Constructor_getName
  (JNIEnv *env, jobject obj) {
  assert(0);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getParameterTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Constructor_getParameterTypes
  (JNIEnv *env, jobject obj) {
  assert(0);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getExceptionTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Constructor_getExceptionTypes
  (JNIEnv *env, jobject obj) {
  assert(0);
}

#endif /* !WITHOUT_HACKED_REFLECTION */
