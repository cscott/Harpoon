#include <jni.h>
/* Header for class java_lang_reflect_Method */

#ifndef _Included_java_lang_reflect_Method
#define _Included_java_lang_reflect_Method
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_lang_reflect_Method
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Method_getModifiers
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_reflect_Method
 * Method:    invoke
 * Signature: (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Method_invoke
  (JNIEnv *, jobject, jobject, jobjectArray);

#if !defined(WITHOUT_HACKED_REFLECTION) /* this is our hacked implementation */
/*
 * Class:     java_lang_reflect_Method
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Method_getDeclaringClass
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_reflect_Method
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Method_getName
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_reflect_Method
 * Method:    getReturnType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Method_getReturnType
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_reflect_Method
 * Method:    getParameterTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Method_getParameterTypes
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_reflect_Method
 * Method:    getExceptionTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Method_getExceptionTypes
  (JNIEnv *, jobject);

#endif /* !WITHOUT_HACKED_REFLECTION */

#ifdef __cplusplus
}
#endif
#endif
