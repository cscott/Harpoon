/* DO NOT EDIT THIS FILE - it is machine generated */

#ifndef __java_lang_Object__
#define __java_lang_Object__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

extern JNIEXPORT jobject JNICALL Java_java_lang_Object_getReadableVersion (JNIEnv *env, jobject, jobject);
extern JNIEXPORT jobject JNICALL Java_java_lang_Object_getReadWritableVersion (JNIEnv *env, jobject, jobject);
extern JNIEXPORT jobject JNICALL Java_java_lang_Object_getReadCommittedVersion (JNIEnv *env, jobject);
extern JNIEXPORT jobject JNICALL Java_java_lang_Object_getWriteCommittedVersion (JNIEnv *env, jobject);
extern JNIEXPORT jobject JNICALL Java_java_lang_Object_makeCommittedVersion (JNIEnv *env, jobject);
extern JNIEXPORT void JNICALL Java_java_lang_Object_setFieldReadFlag (JNIEnv *env, jobject, jobject, jint);
extern JNIEXPORT void JNICALL Java_java_lang_Object_setFieldWriteFlag (JNIEnv *env, jobject, jobject);
extern JNIEXPORT void JNICALL Java_java_lang_Object_setArrayElementWriteFlag (JNIEnv *env, jobject, jint, jclass);

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_Object__ */