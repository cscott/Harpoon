#include <jni.h>
#include <jni-private.h> 

#include <assert.h>
#include "config.h"
#include "flexthread.h"

FLEX_MUTEX_DECLARE_STATIC(uid_mutex);

static int inited=0;
static jlong lastuid=1000000;

static jfieldID UIDfd=0; /* Field ID of Object.UID*/

static void initialize(JNIEnv *env) {
  jclass objcls;
  objcls=(*env)->FindClass(env, "java/lang/Object");
  UIDfd=(*env)->GetFieldID(env, objcls, "UID","J");
  inited=1;
}

JNIEXPORT void JNICALL Java_java_lang_Object_assignUID(JNIEnv *env, jobject obj) {
  FLEX_MUTEX_LOCK(&uid_mutex);
  if (!inited)
    initialize(env);
  printf("Assigning uid %ld\n",lastuid);
  (*env)->SetLongField(env, obj, UIDfd, lastuid++);
  FLEX_MUTEX_UNLOCK(&uid_mutex);
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_arrayassign(JNIEnv *env, jobject array, jint index, jobject component) {
  printf("Arrayassign\n");
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_fieldassign(JNIEnv *env, jobject source, jobject field, jobject component) {
  printf("Fieldassign\n");
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_marklocal(JNIEnv *env, jobject localvar, jobject obj) {
    printf("Marklocal\n");
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_returnmethod(JNIEnv *env) {
    printf("ReturnMethod\n");
}

JNIEXPORT void JNICALL Java_java_lang_RoleInference_entermethod(JNIEnv *env, jobject cls) {
  printf("EnterMethod\n");
}
