#ifndef __java_lang_VMSecurityManager__
#define __java_lang_VMSecurityManager__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jobjectArray JNICALL Java_java_lang_VMSecurityManager_getClassContext (JNIEnv *env, jclass);
JNIEXPORT jobject JNICALL Java_java_lang_VMSecurityManager_currentClassLoader (JNIEnv *env, jclass);

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_VMSecurityManager__ */
