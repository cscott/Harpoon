#ifndef __java_lang_VMClassLoader__
#define __java_lang_VMClassLoader__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jclass JNICALL Java_java_lang_VMClassLoader_defineClass (JNIEnv *env, jclass, jobject, jstring, jbyteArray, jint, jint, jobject);
JNIEXPORT void JNICALL Java_java_lang_VMClassLoader_resolveClass (JNIEnv *env, jclass, jclass);
JNIEXPORT jclass JNICALL Java_java_lang_VMClassLoader_loadClass (JNIEnv *env, jclass, jstring, jboolean);
JNIEXPORT jclass JNICALL Java_java_lang_VMClassLoader_getPrimitiveClass (JNIEnv *env, jclass, jstring);

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_VMClassLoader__ */
