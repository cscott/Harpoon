/* Implementation for class java_net_SocketOutputStream */
#include <jni.h>
#include "java_net_SocketOutputStream.h"

#include <assert.h> /* assert */
#include <errno.h>  /* errno */
#include <string.h> /* strerror */
#include <unistd.h> /* write */

#ifdef WITH_HEAVY_THREADS
#include <pthread.h>    /* for mutex ops */
#endif

#include "../java.io/javaio.h" /* for getfd/setfd */

static jfieldID fdObjID = 0; /* The field ID of SocketOutputStream.fd */
static jclass IOExcCls  = 0; /* The java/io/IOException class object. */
static int inited = 0; /* whether the above variables have been initialized */
#ifdef WITH_HEAVY_THREADS
static pthread_mutex_t init_mutex = PTHREAD_MUTEX_INITIALIZER;
#endif

static int initializeSOS(JNIEnv *env) {
    jclass SOSCls;

#ifdef WITH_HEAVY_THREADS
    pthread_mutex_lock(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;
#endif

    SOSCls  = (*env)->FindClass(env, "java/net/SocketOutputStream");
    if ((*env)->ExceptionOccurred(env)) goto done;
    fdObjID = (*env)->GetFieldID(env, SOSCls, "fd","Ljava/io/FileDescriptor;");
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
 * Class:     java_net_SocketOutputStream
 * Method:    socketWrite
 * Signature: ([BII)V
 */
// stolen from src/java.io/java_io_FileOutputStream.c
JNIEXPORT void JNICALL Java_java_net_SocketOutputStream_socketWrite
  (JNIEnv *env, jobject _this, jbyteArray ba, jint start, jint len) {
    int fd, result;
    jobject fdObj;
    jbyte buf[len];
    int written = 0;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeSOS(env)) return; /* exception occurred; bail */

    fdObj = (*env)->GetObjectField(env, _this, fdObjID);
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);
    (*env)->GetByteArrayRegion(env, ba, start, len, buf);
    if ((*env)->ExceptionOccurred(env)) return; /* bail */

    if (len==0) return; /* don't even try to write anything. */

    while (written < len) {
	result = write(fd, (void*)(buf+written), len-written);
	if (result==0) {
	    (*env)->ThrowNew(env, IOExcCls, "No bytes written");
	    return;
	}
	if (result==-1) {
	    (*env)->ThrowNew(env, IOExcCls, strerror(errno));
	    return;
	}
	written+=result;
    }
}
