#include "java_io_FileDescriptor.h"
#include <errno.h>	/* for errno */
#include <unistd.h>	/* for fsync */
#include <string.h>	/* for strerror */
#ifdef WITH_HEAVY_THREADS
#include <pthread.h>    /* for mutex ops */
#endif

static jfieldID fdID   = 0; /* The field ID of fd in class FileDescriptor */
static jclass IOExcCls = 0; /* The java/io/IOException class object. */
static int inited = 0;
#ifdef WITH_HEAVY_THREADS
static pthread_mutex_t init_mutex = PTHREAD_MUTEX_INITIALIZER;
#endif

int initializeFD(JNIEnv *env) {
#ifdef WITH_HEAVY_THREADS
    pthread_mutex_lock(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;
#endif
    jclass FDCls = (*env)->FindClass(env, "java/io/FileDescriptor");
    if ((*env)->ExceptionOccurred(env)) goto done;
    fdID = (*env)->GetFieldID(env,FDCls,"fd","I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");
    if ((*env)->ExceptionOccurred(env)) goto done;
    /* make IOExcCls into a global reference for future use */
    IOExcCls = (*env)->NewGlobalRef(env, IOExcCls);
    /* done. */
    inited = 1;
 done:
#ifdef WITH_HEAVY_THREADS
    pthread_mutex_unlock(&init_mutex);
#endif
    return inited;
}

/*
 * Class:     java_io_FileDescriptor
 * Method:    valid
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_FileDescriptor_valid
  (JNIEnv * env, jobject obj) {
    if (!inited && !initializeFD(env)) return 0; /* exception occurred; bail */
    
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
    
    if (!inited && !initializeFD(env)) return; /* exception occurred; bail */
    
    fd = (*env)->GetIntField(env, obj, fdID);
    if (fsync(fd) < 0) { /* An error has occured */
	SFExcCls = (*env)->FindClass(env, "java/io/SyncFailedException");
	if (SFExcCls == NULL) { return; /* Give up */ }
	(*env)->ThrowNew(env, SFExcCls, strerror(errno));
    }
}

/*
 * Class:     java_io_FileDescriptor
 * Method:    initSystemFD
 * Signature: (Ljava/io/FileDescriptor;I)Ljava/io/FileDescriptor;
 */
JNIEXPORT jobject JNICALL Java_java_io_FileDescriptor_initSystemFD
  (JNIEnv * env, jclass lcs, jobject obj, jint fd) { 
    if (!inited && !initializeFD(env)) return 0; /* exception occurred; bail */
    
    (*env)->SetIntField(env, obj, fdID, fd);
    return obj;
}

