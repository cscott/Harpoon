#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_VMSecurityManager.h"

#include <assert.h>

/*
 * Class:     java_lang_VMSecurityManager
 * Method:    getClassContext
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_VMSecurityManager_getClassContext
  (JNIEnv *env, jclass cls) {
    assert(0);
    return NULL;
}

/*
 * Class:     java_lang_VMSecurityManager
 * Method:    currentClassLoader
 * Signature: ()Ljava/lang/ClassLoader;
 */
JNIEXPORT jobject JNICALL Java_java_lang_VMSecurityManager_currentClassLoader
  (JNIEnv *env, jclass cls) {
    return NULL;
}
