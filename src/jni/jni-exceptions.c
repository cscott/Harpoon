/* Implement JNI exception-related functions. */
#include <jni.h>
#include <jni-private.h>

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>

/* Causes a java.lang.Throwable object to be thrown. */
jint FNI_Throw(JNIEnv *env, jthrowable obj) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  assert(fts->exception==NULL);
  assert(obj!=NULL);
  fts->exception = obj;
  return 0;
}
/* Constructs an exception object from the specified class with the message
 * specified by message and causes that exception to be thrown.
 * Returns 0 on success, negative value on failure.
 */
jint FNI_ThrowNew(JNIEnv *env, jclass clazz, const char *message) {
  jobject excobj;
  jmethodID consID;
  jstring strobj;

  /** No error-checking is being done here. */
  strobj = (*env)->NewStringUTF(env, message); 
  consID = (*env)->GetMethodID(env, clazz, "<init>", "(Ljava/lang/String;)V");
  excobj = (*env)->NewObject(env, clazz, consID, strobj);
  return (*env)->Throw(env, excobj);
}

/* Returns the exception object that is currently being thrown, or
 * NULL if no exception is currently being thrown. */
jthrowable FNI_ExceptionOccurred(JNIEnv *env) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  return fts->exception;
}
/* Prints an exception and a description to stderr. For debugging only. */
void FNI_ExceptionDescribe(JNIEnv *env) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  jclass exclz;
  jmethodID methodID;
  jstring jstr;
  const char *cstr;
  jthrowable saved_exception;

  assert(fts->exception != NULL);
  /* temporarily clear exception, or the following JNI methods will barf. */
  saved_exception = fts->exception; fts->exception = NULL;
  /* okay, get info about this here exception. */
  exclz = FNI_GetObjectClass(env, saved_exception);
  methodID = FNI_GetMethodID(env, exclz, "toString", "()Ljava/lang/String;");
  jstr = FNI_CallObjectMethod(env, saved_exception, methodID);
  cstr = FNI_GetStringUTFChars(env, jstr, NULL);
  fprintf(stderr, "JNI ExceptionDescribe: %s\n", cstr);
  FNI_ReleaseStringUTFChars(env, jstr, cstr);
  /* okay, reset exception. */
  fts->exception = saved_exception;
}
/* Clears any exception that is currently being thrown. */
void FNI_ExceptionClear(JNIEnv *env) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  fts->exception = NULL;
}
/* Raises a fatal error and does not expect the VM to recover. */
void FNI_FatalError(JNIEnv *env, const char *msg) {
  fprintf(stderr, "JNI Fatal Error: %s\n", msg);
  abort();
}
