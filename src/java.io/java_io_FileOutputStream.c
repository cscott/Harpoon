#include "java_io_FileOutputStream.h"
#include "config.h"
#include <assert.h>	/* for assert */
#include <errno.h>	/* for errno */
#include <fcntl.h>	/* for open */
#include <string.h>	/* for strerror */
#include <unistd.h>	/* write, etc. */
#include "flexthread.h" /* for mutex ops */

#include "javaio.h" /* for getfd/setfd */

static jfieldID fdObjID = 0; /* The field ID of fd in class FileOutputStream */
static jclass IOExcCls  = 0; /* The java/io/IOException class object. */
static int inited = 0; /* whether the above variables have been initialized */
FLEX_MUTEX_DECLARE_STATIC(init_mutex);

static int initializeFOS(JNIEnv *env) {
    jclass FOSCls;

    FLEX_MUTEX_LOCK(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;

    FOSCls  = (*env)->FindClass(env, "java/io/FileOutputStream");
    if ((*env)->ExceptionOccurred(env)) goto done;
    fdObjID = (*env)->GetFieldID(env, FOSCls, "fd","Ljava/io/FileDescriptor;");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");
    if ((*env)->ExceptionOccurred(env)) goto done;
    /* make IOExcCls into a global reference for future use */
    IOExcCls = (*env)->NewGlobalRef(env, IOExcCls);
    /* done. */
    inited = 1;
 done:
    FLEX_MUTEX_UNLOCK(&init_mutex);
    return inited;
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_open
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */
    jobject      fdObj;	    /* File descriptor object */

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    cstr  = (*env)->GetStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_WRONLY|O_CREAT|O_BINARY|O_TRUNC, 0666);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    Java_java_io_FileDescriptor_setfd(env, fdObj, fd);

    /* Check for error condition */
    if (fd==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    openAppend
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_openAppend
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */
    jobject      fdObj;    

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    cstr  = (*env)->GetStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_WRONLY|O_CREAT|O_BINARY|O_APPEND, 0666);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    Java_java_io_FileDescriptor_setfd(env, fdObj, fd);

    /* Check for error condition */
    if (fd==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}
    

/*
 * Class:     java_io_FileOutputStream
 * Method:    write
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_write
  (JNIEnv * env, jobject obj, jint i) { 
    int            fd, result;
    jobject        fdObj;
    unsigned char  buf[1];

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    fdObj    = (*env)->GetObjectField(env, obj, fdObjID);
    fd       = Java_java_io_FileDescriptor_getfd(env, fdObj);
    buf[0]   = (unsigned char)i;

    do {
	result = write(fd, (void*)buf, 1);
	/* if we're interrupted by a signal, just retry. */
    } while (result < 0 && errno == EINTR);

    if (result==0)
	(*env)->ThrowNew(env, IOExcCls, "No bytes written");
    if (result==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    writeBytes
 * Signature: ([BII)V
 */
// note that this function is mirrored at 
// src/java.net/java_net_SocketOutputStream.c.  Keep changes in sync.
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_writeBytes
(JNIEnv * env, jobject obj, jbyteArray ba, jint start, jint len) { 
    int              fd, result;
    jobject          fdObj;
    jbyte            buf[len];
    int written = 0;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    (*env)->GetByteArrayRegion(env, ba, start, len, buf);
    if ((*env)->ExceptionOccurred(env)) return; /* bail */

    if (len==0) return; /* don't even try to write anything. */

    while (written < len) {
	result = write(fd, (void*)(buf+written), len-written);
	/* if we're interrupted by a signal, just retry. */
	if (result < 0 && errno == EINTR) continue;

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

#ifdef WITH_TRANSACTIONS
/* transactional version of writeBytes -- problematic!  how do we
 * undo bytes written to a file descriptor? punting on the problem
 * for now & just writing the bytes. */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_writeBytes_00024_00024withtrans
(JNIEnv * env, jobject obj, jobject commitrec, jbyteArray ba, jint start, jint len) { 
  Java_java_io_FileOutputStream_writeBytes(env, obj, ba, start, len);
}
#endif /* WITH_TRANSACTIONS */

/*
 * Class:     java_io_FileOutputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_close
(JNIEnv * env, jobject obj) { 
    int      fd, result;
    jobject  fdObj;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    result = close(fd);
    Java_java_io_FileDescriptor_setfd(env, fdObj, -1);

    if (result==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}
