#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_Runtime.h"

#include <assert.h>
#include <stdio.h> /* for fprintf, stderr */
#include "../../java.lang/runtime.h" /* useful library-indep implementations */
#include "../../java.lang/properties.h" /* same, for setting up properties */
#include "../../java.lang.reflect/reflect-util.h" /* for REFLECT_staticinit */

/*
 * Class:     java_lang_Runtime
 * Method:    availableProcessors
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Runtime_availableProcessors
  (JNIEnv *env, jobject runtime) {
    assert(0); /* unimplemented */
}

/*
 * Class:     java_lang_Runtime
 * Method:    freeMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_freeMemory
  (JNIEnv *env, jobject runtime) {
    return fni_runtime_freeMemory(env);
}

/*
 * Class:     java_lang_Runtime
 * Method:    totalMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_totalMemory
  (JNIEnv *env, jobject runtime) {
    return fni_runtime_totalMemory(env);
}

/*
 * Class:     java_lang_Runtime
 * Method:    maxMemory
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Runtime_maxMemory
  (JNIEnv *env, jobject runtime) {
    assert(0); /* unimplemented */
}

/*
 * Class:     java_lang_Runtime
 * Method:    gc
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_gc
  (JNIEnv *env, jobject runtime) {
    fni_runtime_gc(env);
}

/*
 * Class:     java_lang_Runtime
 * Method:    runFinalization
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_runFinalization
  (JNIEnv *env, jobject runtime) {
    fni_runtime_runFinalization(env);
}

/* the callgraph for some programs apparently reveals a possible program
 * execution that could call runFinalization inside a static initializer.
 * Let's hope that never actually happens in practice, because it's wildly
 * unsafe.  Or would be if we were actually to run finalizers. */
#ifdef WITH_INIT_CHECK
JNIEXPORT void JNICALL Java_java_lang_Runtime_runFinalization_00024_00024initcheck
  (JNIEnv *env, jobject runtime) {
  fprintf(stderr,
	  "RUNNING FINALIZATION INSIDE A STATIC INITIALIZER IS NOT SAFE!\n");
  assert(0); /* die */
}
#endif /* WITH_INIT_CHECK */

/*
 * Class:     java_lang_Runtime
 * Method:    traceInstructions
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_traceInstructions
  (JNIEnv *env, jobject runtime, jboolean value) {
    /* ignore this, because we don't implement this feature */
}

/*
 * Class:     java_lang_Runtime
 * Method:    traceMethodCalls
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_traceMethodCalls
  (JNIEnv *env, jobject runtime, jboolean value) {
    /* ignore this, because we don't implement this feature */
}

/*
 * Class:     java_lang_Runtime
 * Method:    runFinalizersOnExitInternal
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_runFinalizersOnExitInternal
  (JNIEnv *env, jclass cls, jboolean value) {
    /* ignore this setting */
}

/*
 * Class:     java_lang_Runtime
 * Method:    exitInternal
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_exitInternal
  (JNIEnv *env, jobject runtime, jint status) {
    fni_runtime_exitInternal(env, status);
}

  /**
   * Load a file. If it has already been loaded, do nothing. The name has
   * already been mapped to a true filename.
   *
   * @param filename the file to load
   * @return 0 on failure, nonzero on success
   */
/*
 * Class:     java_lang_Runtime
 * Method:    nativeLoad
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_Runtime_nativeLoad
  (JNIEnv *env, jobject runtime, jstring filename) {
    /* yeah, we already loaded this. =) so do nothing. */
    return 1;
}

  /**
   * Map a system-independent "short name" to the full file name, and append
   * it to the path.
   * XXX This method is being replaced by System.mapLibraryName.
   *
   * @param pathname the path
   * @param libname the short version of the library name
   * @return the full filename
   */
/*
 * Class:     java_lang_Runtime
 * Method:    nativeGetLibname
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_Runtime_nativeGetLibname
  (JNIEnv *env, jclass runtime, jstring pathname, jstring libname) {
    /* somebody calls this, I don't know who. */
    return libname;
}

  /**
   * Execute a process. The command line has already been tokenized, and
   * the environment should contain name=value mappings. If directory is null,
   * use the current working directory; otherwise start the process in that
   * directory.  If env is null, then the new process should inherit
   * the environment of this process.
   *
   * @param cmd the non-null command tokens
   * @param env the environment setup
   * @param dir the directory to use, may be null
   * @return the newly created process
   * @throws NullPointerException if cmd or env have null elements
   */
/*
 * Class:     java_lang_Runtime
 * Method:    execInternal
 * Signature: ([Ljava/lang/String;[Ljava/lang/String;Ljava/io/File;)Ljava/lang/Process;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Runtime_execInternal
  (JNIEnv *env, jobject runtime, jobjectArray cmd, jobjectArray environment,
   jobject dir) {
    assert(0); /* unimplemented */
}

  /**
   * Get the system properties. This is done here, instead of in System,
   * because of the bootstrap sequence. Note that the native code should
   * not try to use the Java I/O classes yet, as they rely on the properties
   * already existing. The only safe method to use to insert these default
   * system properties is {@link Properties#setProperty(String, String)}.
   *
   * <p>These properties MUST include:
   * <dl>
   * <dt>java.version         <dd>Java version number
   * <dt>java.vendor          <dd>Java vendor specific string
   * <dt>java.vendor.url      <dd>Java vendor URL
   * <dt>java.home            <dd>Java installation directory
   * <dt>java.vm.specification.version <dd>VM Spec version
   * <dt>java.vm.specification.vendor  <dd>VM Spec vendor
   * <dt>java.vm.specification.name    <dd>VM Spec name
   * <dt>java.vm.version      <dd>VM implementation version
   * <dt>java.vm.vendor       <dd>VM implementation vendor
   * <dt>java.vm.name         <dd>VM implementation name
   * <dt>java.specification.version    <dd>Java Runtime Environment version
   * <dt>java.specification.vendor     <dd>Java Runtime Environment vendor
   * <dt>java.specification.name       <dd>Java Runtime Environment name
   * <dt>java.class.version   <dd>Java class version number
   * <dt>java.class.path      <dd>Java classpath
   * <dt>java.library.path    <dd>Path for finding Java libraries
   * <dt>java.io.tmpdir       <dd>Default temp file path
   * <dt>java.compiler        <dd>Name of JIT to use
   * <dt>java.ext.dirs        <dd>Java extension path
   * <dt>os.name              <dd>Operating System Name
   * <dt>os.arch              <dd>Operating System Architecture
   * <dt>os.version           <dd>Operating System Version
   * <dt>file.separator       <dd>File separator ("/" on Unix)
   * <dt>path.separator       <dd>Path separator (":" on Unix)
   * <dt>line.separator       <dd>Line separator ("\n" on Unix)
   * <dt>user.name            <dd>User account name
   * <dt>user.home            <dd>User home directory
   * <dt>user.dir             <dd>User's current working directory
   * </dl>
   *
   * @param p the Properties object to insert the system properties into
   */
/*
 * Class:     java_lang_Runtime
 * Method:    insertSystemProperties
 * Signature: (Ljava/util/Properties;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Runtime_insertSystemProperties
  (JNIEnv *env, jclass runtime, jobject properties) {
    fni_properties_init(env, properties, JNI_TRUE);
}
