#include <jni.h>
#include "java_lang_ClassLoader.h"

/*
 * Class:     java_lang_ClassLoader
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_ClassLoader_init
  (JNIEnv *env, jobject _this) {
  /* do we need to do anything here? */
}

#if 0
/*
 * Class:     java_lang_ClassLoader
 * Method:    defineClass0
 * Signature: (Ljava/lang/String;[BII)Ljava/lang/Class;
 */
/* should be able to use (*env)->DefineClass for this function */
JNIEXPORT jclass JNICALL Java_java_lang_ClassLoader_defineClass0
  (JNIEnv *, jobject _this, jstring, jbyteArray, jint, jint);

/*
 * Class:     java_lang_ClassLoader
 * Method:    resolveClass0
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_java_lang_ClassLoader_resolveClass0
  (JNIEnv *, jobject, jclass);
#endif

/*
 * Class:     java_lang_ClassLoader
 * Method:    findSystemClass0
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_ClassLoader_findSystemClass0
  (JNIEnv *env, jobject _this, jstring name) {
  /* XXX: not sure if this is technically correct, but it will work. */
  const char *namestr = (*env)->GetStringUTFChars(env, name, NULL);
  jclass result = (*env)->FindClass(env, namestr);
  (*env)->ReleaseStringUTFChars(env, name, namestr);
  return result;
}

/*
 * Class:     java_lang_ClassLoader
 * Method:    getSystemResourceAsStream0
 * Signature: (Ljava/lang/String;)Ljava/io/InputStream;
 */
JNIEXPORT jobject JNICALL Java_java_lang_ClassLoader_getSystemResourceAsStream0
  (JNIEnv *env, jclass cls, jstring name) {
  /* XXX cheat: claim resource is never found.
   * in reality, we should search the CLASSPATH for the given
   * resource, and open it if found. */
  return NULL;
}

/*
 * Class:     java_lang_ClassLoader
 * Method:    getSystemResourceAsName0
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_ClassLoader_getSystemResourceAsName0
  (JNIEnv *env, jclass cls, jstring name) {
  /* XXX cheat: claim resource is never found.
   * in reality, we should search the CLASSPATH for the given
   * resource, and return a URL string that will enable the
   * user to open it. */
  return NULL;
}
