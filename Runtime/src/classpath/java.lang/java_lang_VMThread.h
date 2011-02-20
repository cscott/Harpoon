#ifndef __java_lang_VMThread__
#define __java_lang_VMThread__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT void JNICALL Java_java_lang_VMThread_start (JNIEnv *env, jobject, jlong);
JNIEXPORT void JNICALL Java_java_lang_VMThread_interrupt (JNIEnv *env, jobject);
JNIEXPORT jboolean JNICALL Java_java_lang_VMThread_isInterrupted (JNIEnv *env, jobject);
JNIEXPORT void JNICALL Java_java_lang_VMThread_suspend (JNIEnv *env, jobject);
JNIEXPORT void JNICALL Java_java_lang_VMThread_resume (JNIEnv *env, jobject);
JNIEXPORT void JNICALL Java_java_lang_VMThread_nativeSetPriority (JNIEnv *env, jobject, jint);
JNIEXPORT void JNICALL Java_java_lang_VMThread_nativeStop (JNIEnv *env, jobject, jthrowable);
JNIEXPORT jobject JNICALL Java_java_lang_VMThread_currentThread (JNIEnv *env, jclass);
JNIEXPORT void JNICALL Java_java_lang_VMThread_yield (JNIEnv *env, jclass);
JNIEXPORT void JNICALL Java_java_lang_VMThread_sleep (JNIEnv *env, jclass, jlong, jint);
JNIEXPORT jboolean JNICALL Java_java_lang_VMThread_interrupted (JNIEnv *env, jclass);
JNIEXPORT jboolean JNICALL Java_java_lang_VMThread_holdsLock (JNIEnv *env, jclass, jobject);

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_VMThread__ */
