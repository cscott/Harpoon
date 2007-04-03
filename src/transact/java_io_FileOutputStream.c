#include "java_io_FileOutputStream.h" /* check against prototype */
#include "config.h"

#if defined(WITH_TRANSACTIONS) && defined(CLASSPATH_VERSION)
/* this is the expected environment. */
#else
#error This file provides transactions functionality for GNU classpath.
#endif

#include "transact.h" /* for FNI_StrTrans2Str */
#include "java_lang_Object.h" /* version fetch methods */
#include "../java.lang/class.h" /* Class.getComponentType() */

// XXX NOTE THAT THIS METHOD IS NO LONGER CALLED
// we've instead patched the JNI methods to be transaction-aware, so
// we end up calling the 'usual' version of this method.
void Java_java_io_FileOutputStream_writeInternal_00024_00024withtrans
  (JNIEnv *env, jobject obj, jobject commitrec,
   jint native_fd, jbyteArray buf, jint offset, jint len) {
  /* TRICKY TRICKY */
  jbyteArray _ba;
  jsize length = (*env)->GetArrayLength(env, buf);
  jclass byteclass;
  int i;
  /* get readable version. */
  _ba = (jbyteArray)
    Java_java_lang_Object_getReadableVersion(env, buf, commitrec);
  /* now tag all elements as read. */
  byteclass =
    fni_class_getComponentType(env, (*env)->GetObjectClass(env,buf));
  for (i=0; i<length; i++)
    // XXX this marks as *written*.  I want to mark *read*
    Java_java_lang_Object_setArrayElementWriteFlag(env, buf, i, byteclass);
  /* okay, now we can do the writeInternal. */
  /* XXX: if GetByteArrayRegion is fixed, this won't work so well. */
  Java_java_io_FileOutputStream_writeInternal(env, obj, native_fd, _ba,
					      offset, len);
}
// XXX NOTE THAT THIS METHOD IS NO LONGER CALLED
// we've instead patched the JNI methods to be transaction-aware, so
// we end up calling the 'usual' version of this method.
jint Java_java_io_FileOutputStream_open_00024_00024withtrans
  (JNIEnv *env, jobject obj, jobject commitrec,
   jstring name, jboolean append) {
  /* XXX: fd is write once only within JNI so should be okay. */
  return Java_java_io_FileOutputStream_open
    (env, obj, FNI_StrTrans2Str(env, commitrec, name), append);
}
// XXX NOTE THAT THIS METHOD IS NO LONGER CALLED
// we've instead patched the JNI methods to be transaction-aware, so
// we end up calling the 'usual' version of this method.
void Java_java_io_FileOutputStream_closeInternal_00024_00024withtrans
  (JNIEnv *env, jobject obj, jobject commitrec, jint native_fd) {
  Java_java_io_FileOutputStream_closeInternal(env, obj, native_fd);
}

