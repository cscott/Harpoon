#include <jni.h>
#include "java_lang_Runtime.h"
#include <config.h>
#ifdef BDW_CONSERVATIVE_GC
# include "gc.h"
#endif

#include <assert.h>
#include <unistd.h>
/*
 * Class:     java_lang_Runtime
 * Method:    exitInternal
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_exitInternal
  (JNIEnv *env, jobject objRuntime, jint status) {
#ifdef WITH_STATISTICS
  /* print out collected statistics */
  { void print_statistics(void); print_statistics(); }
#endif
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
/* returns the amount of free memory in the system */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_freeMemory
  (JNIEnv *env, jobject objRuntime) {
#ifdef BDW_CONSERVATIVE_GC
  return (jlong) GC_get_free_bytes();
#elif defined(WITH_PRECISE_GC)
  return precise_free_memory();
#else
  assert(0/*unimplemented*/);
#endif
}

/*
 * Class:     java_lang_Runtime
 * Method:    totalMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_totalMemory
  (JNIEnv *env, jobject objRuntime) {
#ifdef BDW_CONSERVATIVE_GC
  return (jlong) GC_get_heap_size();
#elif defined(WITH_PRECISE_GC)
  return precise_get_heap_size();
#else
  assert(0/*unimplemented*/);
#endif
}

/*
 * Class:     java_lang_Runtime
 * Method:    gc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_gc
  (JNIEnv *env, jobject objRuntime) {
#ifdef BDW_CONSERVATIVE_GC
  GC_gcollect();
#elif defined(WITH_PRECISE_GC)
  precise_collect();
#else
  assert(0/*unimplemented*/);
#endif
}

/*
 * Class:     java_lang_Runtime
 * Method:    runFinalization
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_runFinalization
  (JNIEnv *env, jobject objRuntime) {
#ifdef BDW_CONSERVATIVE_GC
  GC_invoke_finalizers();
#elif defined(WITH_PRECISE_GC)
  /* unimplemented */
  printf("WARNING: Finalization not implemented for precise GC.\n");
#else
  assert(0/*unimplemented*/);
#endif
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
