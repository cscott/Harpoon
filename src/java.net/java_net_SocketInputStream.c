/* Implementation for class java_net_SocketInputStream */
#include <jni.h>
#include "java_net_SocketInputStream.h"

#include <assert.h> /* assert */
#include <errno.h>  /* errno */
#include <string.h> /* strerror */
#include <unistd.h> /* write */

#ifdef WITH_HEAVY_THREADS
#include <pthread.h>    /* for mutex ops */
#endif

#include "../java.io/javaio.h" /* for getfd/setfd */

static jfieldID fdObjID = 0; /* The field ID of SocketInputStream.fd */
static jclass IOExcCls  = 0; /* The java/io/IOException class object. */
static int inited = 0; /* whether the above variables have been initialized */
#ifdef WITH_HEAVY_THREADS
static pthread_mutex_t init_mutex = PTHREAD_MUTEX_INITIALIZER;
#endif

static int initializeSIS(JNIEnv *env) {
    jclass SISCls;

#ifdef WITH_HEAVY_THREADS
    pthread_mutex_lock(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;
#endif

    SISCls  = (*env)->FindClass(env, "java/net/SocketOutputStream");
    if ((*env)->ExceptionOccurred(env)) goto done;
    fdObjID = (*env)->GetFieldID(env, SISCls, "fd","Ljava/io/FileDescriptor;");
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
 * Class:     java_net_SocketInputStream
 * Method:    socketRead
 * Signature: ([BII)I
 */
// stolen from src/java.io/java_io_FileInputStream.c
JNIEXPORT jint JNICALL Java_java_net_SocketInputStream_socketRead
  (JNIEnv *env, jobject _this, jbyteArray ba, jint start, jint len) {
    int              fd, result;
    jobject          fdObj;
    jbyte            buf[len];

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeSIS(env)) return 0;/* exception occurred; bail */

    if (len==0) return 0; /* don't even try to read anything. */

    fdObj  = (*env)->GetObjectField(env, _this, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    
    result = read(fd, (void*)buf, len);

    if (result==-1) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return -1; /* could return anything; value is ignored. */
    }

    (*env)->SetByteArrayRegion(env, ba, start, result, buf); 

    /* Java language spec requires -1 at EOF, not 0 */ 
    return (jint)(result ? result : -1);
}
