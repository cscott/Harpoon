#include <jni.h>
#include "java_lang_Runtime.h"
#include <config.h>
#ifdef BDW_CONSERVATIVE_GC
# include "gc.h"
#endif

#include <assert.h>
#include "../../java.lang/runtime.h" /* useful library-indep implementations */
/*
 * Class:     java_lang_Runtime
 * Method:    exitInternal
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_exitInternal
  (JNIEnv *env, jobject objRuntime, jint status) {
    fni_runtime_exitInternal(status);
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
/* returns the amount of free memory in the system */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_freeMemory
  (JNIEnv *env, jobject objRuntime) {
    return fni_runtime_freeMemory(env);
}

/*
 * Class:     java_lang_Runtime
 * Method:    totalMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_totalMemory
  (JNIEnv *env, jobject objRuntime) {
    return fni_runtime_totalMemory(env);
}

/*
 * Class:     java_lang_Runtime
 * Method:    gc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_gc
  (JNIEnv *env, jobject objRuntime) {
    fni_runtime_gc(env);
}

/*
 * Class:     java_lang_Runtime
 * Method:    runFinalization
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_runFinalization
  (JNIEnv *env, jobject objRuntime) {
    fni_runtime_runFinalization(env);
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
    /* stub this out; we don't really pay attention to the search path */
    jstring str = (*env)->NewStringUTF(env, "/usr/lib");
    return str;
}

/*
 * Class:     java_lang_Runtime
 * Method:    buildLibName
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_Runtime_buildLibName
  (JNIEnv *env, jobject objRuntime, jstring pathname, jstring filename) {
    /* stub this out; we ignore the pathname */
    return filename;
}

/*
 * Class:     java_lang_Runtime
 * Method:    loadFileInternal
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_Runtime_loadFileInternal
  (JNIEnv *env, jobject objRuntime, jstring filename) {
    /* ignore! */
    return 1;
}
