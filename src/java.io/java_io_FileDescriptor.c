#include "java_io_FileDescriptor.h"

#define IO_ERROR(env, str) do { \
    (JNIEnv *)env;  (const char *)str;  /* Check types */             \
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");         \
    if (IOExcCls == NULL) return; /* give up */                       \
    else (*env)->ThrowNew(env, IOExcCls, "Couldn't write to file");   \
    } while (0)

static jfieldID fdID = 0;
static jclass IOExcCls;

/*
 * Class:     java_io_FileDescriptor
 * Method:    valid
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_FileDescriptor_valid
(JNIEnv * env, jobject obj) { 
    if (fdID==0) 
	if (!initialize_FD_data(env)) IO_ERROR(env,"Couldn't init native I/O");
    
    return (*env)->GetIntField(env, obj, fdID) >= 0;
}

/*
 * Class:     java_io_FileDescriptor
 * Method:    sync
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileDescriptor_sync
(JNIEnv * env, jobject obj) { 
    int    fd;
    jclass SFExcCls;  /* SyncFailedException class */
    
    if (fdID==0) 
	if (!initialize_FD_data(env)) IO_ERROR(env,"Couldn't init native I/O");
    
    fd = (*env)->GetIntField(env, obj, fdID);
    if (fsync(fd) < 0) { /* An error has occured */
	SFExcCls = (*env)->FindClass(env, "java/io/SyncFailedException");
	if (SFExcCls == NULL) { return; /* Give up */ }
	(*env)->ThrowNew(env, SFExcCls, "Couldn't write to file");
    }

  /* Success! */
}

/*
 * Class:     java_io_FileDescriptor
 * Method:    initSystemFD
 * Signature: (Ljava/io/FileDescriptor;I)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_java_io_FileDescriptor_initSystemFD
  (JNIEnv * env, jclass lcs, jobject obj, jint fd) { 
    if (fdID==0) 
	if (!initialize_FD_data(env)) IO_ERROR(env,"Couldn't init native I/O");
    
    (*env)->SetIntField(env, obj, fdID, fd);
    return obj;
}

int initialize_FD_data(JNIEnv * env) { 
    jclass FDCls = (*env)->FindClass(env, "java/io/FileDescriptor");
    if (FDCls == NULL) return 0;
    fdID = (*env)->GetFieldID(env,FDCls,"fd","I");
    return 1;
}

