#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Constructor.h"
#include "java_lang_reflect_Method.h"

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Constructor_getModifiers
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getModifiers(env, _this);
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
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getDeclaringClass(env, _this);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Constructor_getName
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getName(env, _this);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getParameterTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Constructor_getParameterTypes
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getParameterTypes(env, _this);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getExceptionTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Constructor_getExceptionTypes
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getExceptionTypes(env, _this);
}

#endif /* !WITHOUT_HACKED_REFLECTION */
