#include <jni.h>
#include <jni-private.h> /* for string_constants_start|end */
#include "java_lang_String.h"

#include <assert.h>
#include "config.h"
#include "flexthread.h"

static jobject internTable = 0; /* Table mapping strings to interned strings */
static jmethodID getID = 0; /* method ID of get() in class Hashtable */
static jmethodID putID = 0; /* method ID of put() in class Hashtable */
static int inited = 0; /* whether the above variables have been initialized */
FLEX_MUTEX_DECLARE_STATIC(init_mutex);

int initializeJLS(JNIEnv *env) {
  jclass cls;
  jobject hash;
  jmethodID mid;
  char *p;
  int strsize;

  FLEX_MUTEX_LOCK(&init_mutex);
  // other thread may win race to lock and init before we do.
  if (inited) goto done;

  /* Create global hashtable. */
  cls = (*env)->FindClass(env, "java/util/Hashtable");
  if ((*env)->ExceptionOccurred(env)) return 0;
  mid = (*env)->GetMethodID(env, cls, "<init>", "()V");
  if ((*env)->ExceptionOccurred(env)) return 0;
  hash = (*env)->NewObject(env, cls, mid);
  if ((*env)->ExceptionOccurred(env)) return 0;
  internTable = (*env)->NewGlobalRef(env, hash);
  if ((*env)->ExceptionOccurred(env)) return 0;
  (*env)->DeleteLocalRef(env, hash);

  /* Initialize methodIDs */
  getID = (*env)->GetMethodID(env, cls, "get",
			      "(Ljava/lang/Object;)Ljava/lang/Object;");
  if ((*env)->ExceptionOccurred(env)) return 0;
  putID = (*env)->GetMethodID(env, cls, "put",
			      "(Ljava/lang/Object;Ljava/lang/Object;)"
			      "Ljava/lang/Object;");
  if ((*env)->ExceptionOccurred(env)) return 0;
  (*env)->DeleteLocalRef(env, cls);

  /* Now initialize internTable. */
  cls = (*env)->FindClass(env, "java/lang/String");
  if ((*env)->ExceptionOccurred(env)) return 0;
  strsize = FNI_GetClassInfo(cls)->claz->size;
  (*env)->DeleteLocalRef(env, cls);
  for (p = (char*) &string_constants_start; 
       p < (char*) &string_constants_end;
       p += strsize) {
    jobject s = FNI_WRAP((struct oobj *)p);
    if ((*env)->ExceptionOccurred(env)) return 0;
    (*env)->CallObjectMethod(env, internTable, putID, s, s);
    if ((*env)->ExceptionOccurred(env)) return 0;
    (*env)->DeleteLocalRef(env, s);
  }

  /* done. */
  inited = 1;
 done:
  FLEX_MUTEX_UNLOCK(&init_mutex);
  return 1;
}
  
/*
 * Class:     java_lang_String
 * Method:    intern
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_String_intern
  (JNIEnv *env, jobject str) {
    jobject result;
    if (!inited && !initializeJLS(env)) return NULL;/*exception occurred;bail*/

    result = (*env)->CallObjectMethod(env, internTable, getID, str);
    if ((*env)->ExceptionOccurred(env)) return NULL;
    if (result!=NULL) return result;
    /* put str in table. */
    result = (*env)->CallObjectMethod(env, internTable, putID, str, str);
    if ((*env)->ExceptionOccurred(env)) return NULL;
    assert(result==NULL);
    return str;
}
