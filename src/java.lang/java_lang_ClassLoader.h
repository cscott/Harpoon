/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_lang_ClassLoader */

#ifndef _Included_java_lang_ClassLoader
#define _Included_java_lang_ClassLoader
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_lang_ClassLoader
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_ClassLoader_init
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_ClassLoader
 * Method:    defineClass0
 * Signature: (Ljava/lang/String;[BII)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_ClassLoader_defineClass0
  (JNIEnv *, jobject, jstring, jbyteArray, jint, jint);

/*
 * Class:     java_lang_ClassLoader
 * Method:    resolveClass0
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_java_lang_ClassLoader_resolveClass0
  (JNIEnv *, jobject, jclass);

/*
 * Class:     java_lang_ClassLoader
 * Method:    findSystemClass0
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_ClassLoader_findSystemClass0
  (JNIEnv *, jobject, jstring);

/*
 * Class:     java_lang_ClassLoader
 * Method:    getSystemResourceAsStream0
 * Signature: (Ljava/lang/String;)Ljava/io/InputStream;
 */
JNIEXPORT jobject JNICALL Java_java_lang_ClassLoader_getSystemResourceAsStream0
  (JNIEnv *, jclass, jstring);

/*
 * Class:     java_lang_ClassLoader
 * Method:    getSystemResourceAsName0
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_ClassLoader_getSystemResourceAsName0
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif
#endif
