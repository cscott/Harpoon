#include <jni.h>
#ifndef _Role_Infer
#define _Role_Infer

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     java_lang_Object
 * Method:    assignUID
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Object_assignUID
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_RoleInference
 * Method:    arrayassign
 */
JNIEXPORT void JNICALL Java_java_lang_RoleInference_arrayassign
  (JNIEnv *, jobject, jint, jobject);

/*
 * Class:     java_lang_RoleInference
 * Method:    fieldassign
 */
JNIEXPORT void JNICALL Java_java_lang_RoleInference_fieldassign
  (JNIEnv *, jobject, jobject, jobject);

/*
 * Class:     java_lang_RoleInference
 * Method:    marklocal
 */
JNIEXPORT void JNICALL Java_java_lang_RoleInference_marklocal
  (JNIEnv *, jobject, jobject);

JNIEXPORT void JNICALL Java_java_lang_RoleInference_returnmethod
  (JNIEnv *);

JNIEXPORT void JNICALL Java_java_lang_RoleInference_entermethod
  (JNIEnv *, jobject);


#ifdef __cplusplus
}
#endif
#endif
