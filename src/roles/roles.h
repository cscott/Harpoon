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
  (JNIEnv *, jobject,jclass);

void NativeassignUID
  (JNIEnv *, jobject,jclass);

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

JNIEXPORT void JNICALL Java_java_lang_RoleInference_fieldload
  (JNIEnv *, jclass, jstring, jobject, jobject, jobject);

JNIEXPORT void JNICALL Java_java_lang_RoleInference_killlocal
  (JNIEnv *, jclass, jstring);

JNIEXPORT void JNICALL Java_java_lang_RoleInference_returnmethod
  (JNIEnv *, jclass, jobject);

JNIEXPORT void JNICALL Java_java_lang_RoleInference_invokemethod
  (JNIEnv *, jclass, jobject, jint);


#ifdef __cplusplus
}
#endif
#endif
