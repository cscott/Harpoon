#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_VMClassLoader.h"

#include <assert.h>
#include "../../java.lang/class.h" /* useful library-indep implementations */

/*
 * Class:     java_lang_VMClassLoader
 * Method:    defineClass
 * Signature: (Ljava/lang/ClassLoader;Ljava/lang/String;[BIILjava/security/ProtectionDomain;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_VMClassLoader_defineClass
  (JNIEnv *env, jclass cls, jobject cl, jstring name,
   jbyteArray data, jint offset, jint len, jobject pd) {
    assert(0); /* unimplemented */
    return NULL;
}

/*
 * Class:     java_lang_VMClassLoader
 * Method:    resolveClass
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMClassLoader_resolveClass
  (JNIEnv *env, jclass cls, jclass c) {
    /* do nothing: all classes are resolved */
}
#ifdef WITH_INIT_CHECK
JNIEXPORT void JNICALL Java_java_lang_VMClassLoader_resolveClass_00024_00024initcheck
  (JNIEnv *env, jclass cls, jclass c) {
    // xxx: if resolveClass ever does anything, then the initcheck version
    // will need to be smarter as well.
    Java_java_lang_VMClassLoader_resolveClass(env, cls, c);
}
#endif /* WITH_INIT_CHECK */

/*
 * Class:     java_lang_VMClassLoader
 * Method:    loadClass
 * Signature: (Ljava/lang/String;Z)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_VMClassLoader_loadClass
  (JNIEnv *env, jclass cls, jstring name, jboolean resolve) {
    return fni_class_forName(env, name);
}
#ifdef WITH_INIT_CHECK
JNIEXPORT jclass JNICALL Java_java_lang_VMClassLoader_loadClass_00024_00024initcheck
  (JNIEnv *env, jclass cls, jstring name, jboolean resolve) {
    return fni_class_forName_initcheck(env, name);
}
#endif /* WITH_INIT_CHECK */

/*
 * Class:     java_lang_VMClassLoader
 * Method:    getPrimitiveClass
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_VMClassLoader_getPrimitiveClass
  (JNIEnv *env, jclass cls, jstring type) {
    return fni_class_getPrimitiveClass(env, type);
}
