/* private methods for java.io implementation. CSA. */

#ifndef INCLUDED_JAVAIO_H
#define INCLUDED_JAVAIO_H

/* in java_io_FileDescriptor.c: */
jint Java_java_io_FileDescriptor_getfd(JNIEnv *env, jobject fdObj);
void Java_java_io_FileDescriptor_setfd(JNIEnv *env, jobject fdObj, jint fd);

/* limit maximum buffer size (some machines have very small stacks) */
#define MAX_BUFFER_SIZE (64*1024)

#endif
