#ifndef __java_lang_VMObject__
#define __java_lang_VMObject__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jobject JNICALL Java_java_lang_VMObject_clone (JNIEnv *env, jclass, jobject);
JNIEXPORT void JNICALL Java_java_lang_VMObject_notify (JNIEnv *env, jclass, jobject);
JNIEXPORT void JNICALL Java_java_lang_VMObject_notifyAll (JNIEnv *env, jclass, jobject);
JNIEXPORT void JNICALL Java_java_lang_VMObject_wait (JNIEnv *env, jclass, jobject, jlong, jint);

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_VMObject__ */
