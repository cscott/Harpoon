/* private methods for java.io implementation. CSA. */

#ifndef INCLUDED_JAVAIO_H
#define INCLUDED_JAVAIO_H

/* in java_io_FileDescriptor.c: */
jint Java_java_io_FileDescriptor_getfd(JNIEnv *env, jobject fdObj);
void Java_java_io_FileDescriptor_setfd(JNIEnv *env, jobject fdObj, jint fd);

#endif
