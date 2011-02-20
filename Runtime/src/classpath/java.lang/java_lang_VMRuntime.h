#ifndef __java_lang_VMRuntime__
#define __java_lang_VMRuntime__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jint JNICALL Java_java_lang_VMRuntime_availableProcessors (JNIEnv *env, jclass);
JNIEXPORT jlong JNICALL Java_java_lang_VMRuntime_freeMemory (JNIEnv *env, jclass);
JNIEXPORT jlong JNICALL Java_java_lang_VMRuntime_totalMemory (JNIEnv *env, jclass);
JNIEXPORT jlong JNICALL Java_java_lang_VMRuntime_maxMemory (JNIEnv *env, jclass);
JNIEXPORT void JNICALL Java_java_lang_VMRuntime_gc (JNIEnv *env, jclass);
JNIEXPORT void JNICALL Java_java_lang_VMRuntime_runFinalization (JNIEnv *env, jclass);
JNIEXPORT void JNICALL Java_java_lang_VMRuntime_runFinalizationForExit (JNIEnv *env, jclass);
JNIEXPORT void JNICALL Java_java_lang_VMRuntime_traceInstructions (JNIEnv *env, jclass, jboolean);
JNIEXPORT void JNICALL Java_java_lang_VMRuntime_traceMethodCalls (JNIEnv *env, jclass, jboolean);
JNIEXPORT void JNICALL Java_java_lang_VMRuntime_runFinalizersOnExit (JNIEnv *env, jclass, jboolean);
JNIEXPORT void JNICALL Java_java_lang_VMRuntime_exit (JNIEnv *env, jclass, jint);
JNIEXPORT jint JNICALL Java_java_lang_VMRuntime_nativeLoad (JNIEnv *env, jclass, jstring);
JNIEXPORT jstring JNICALL Java_java_lang_VMRuntime_nativeGetLibname (JNIEnv *env, jclass, jstring, jstring);
JNIEXPORT jobject JNICALL Java_java_lang_VMRuntime_exec (JNIEnv *env, jclass, jobjectArray, jobjectArray, jobject);
JNIEXPORT void JNICALL Java_java_lang_VMRuntime_insertSystemProperties (JNIEnv *env, jclass, jobject);

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_VMRuntime__ */
