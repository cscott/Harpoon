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
  (JNIEnv *, jclass, jobject, jint, jobject);

/*
 * Class:     java_lang_RoleInference
 * Method:    fieldassign
 */
JNIEXPORT void JNICALL Java_java_lang_RoleInference_fieldassign
  (JNIEnv *, jclass, jobject, jobject, jobject);

/*
 * Class:     java_lang_RoleInference
 * Method:    marklocal
 */
JNIEXPORT void JNICALL Java_java_lang_RoleInference_marklocal
  (JNIEnv *, jclass, jstring, jobject);

JNIEXPORT void JNICALL Java_java_lang_RoleInference_returnmethod
  (JNIEnv *, jclass);

JNIEXPORT void JNICALL Java_java_lang_RoleInference_invokemethod
  (JNIEnv *, jclass, jobject);


#ifdef __cplusplus
}
#endif
#endif
