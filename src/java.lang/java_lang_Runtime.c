#include <jni.h>
#include "java_lang_Runtime.h"

#include <assert.h>
#include <unistd.h>
/*
 * Class:     java_lang_Runtime
 * Method:    exitInternal
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_exitInternal
  (JNIEnv *env, jobject objRuntime, jint status) {
    exit(status);
}

/*
 * Class:     java_lang_Runtime
 * Method:    runFinalizersOnExit0
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_runFinalizersOnExit0
  (JNIEnv *env, jclass clsRuntime, jboolean value) {
    /* ignore this setting. */
}

/*
 * Class:     java_lang_Runtime
 * Method:    execInternal
 * Signature: ([Ljava/lang/String;[Ljava/lang/String;)Ljava/lang/Process;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Runtime_execInternal
  (JNIEnv *env, jobject objRuntime, jobjectArray cmdarray, jobjectArray envp) {
    assert(0);
}

/*
 * Class:     java_lang_Runtime
 * Method:    freeMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_freeMemory
  (JNIEnv *env, jobject objRuntime) {
    assert(0);
}

/*
 * Class:     java_lang_Runtime
 * Method:    totalMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_totalMemory
  (JNIEnv *env, jobject objRuntime) {
    assert(0);
}

/*
 * Class:     java_lang_Runtime
 * Method:    gc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_gc
  (JNIEnv *env, jobject objRuntime) {
    assert(0);
}

/*
 * Class:     java_lang_Runtime
 * Method:    runFinalization
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_runFinalization
  (JNIEnv *env, jobject objRuntime) {
    assert(0);
}

/*
 * Class:     java_lang_Runtime
 * Method:    traceInstructions
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_traceInstructions
  (JNIEnv *env, jobject objRuntime, jboolean on) {
    /* we're allowed to ignore this if we don't implement this feature. */
}

/*
 * Class:     java_lang_Runtime
 * Method:    traceMethodCalls
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_traceMethodCalls
  (JNIEnv *env, jobject objRuntime, jboolean on) {
    /* we're allowed to ignore this if we don't implement this feature. */
}

/*
 * Class:     java_lang_Runtime
 * Method:    initializeLinkerInternal
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_Runtime_initializeLinkerInternal
  (JNIEnv *env, jobject objRuntime) {
    assert(0);
}

/*
 * Class:     java_lang_Runtime
 * Method:    buildLibName
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_Runtime_buildLibName
  (JNIEnv *env, jobject objRuntime, jstring pathname, jstring filename) {
    assert(0);
}

/*
 * Class:     java_lang_Runtime
 * Method:    loadFileInternal
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_Runtime_loadFileInternal
  (JNIEnv *env, jobject objRuntime, jstring filename) {
    assert(0);
}
