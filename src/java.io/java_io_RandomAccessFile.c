#include <jni.h>
#include "java_io_RandomAccessFile.h"

#include <sys/types.h>

#include <assert.h>
#include "config.h"
#include <errno.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>
#include "flexthread.h"

#include "javaio.h" /* for getfd/setfd */

static jfieldID fdObjID = 0; /* The field ID of fd in class RandomAccessFile */
static jclass IOExcCls  = 0; /* The java/io/IOException class object */
static int inited = 0; /* whether the above variables have been initialized */
FLEX_MUTEX_DECLARE_STATIC(init_mutex);

int initializeRAF(JNIEnv *env) {
    jclass RAFCls;

    FLEX_MUTEX_LOCK(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;

    RAFCls  = (*env)->FindClass(env, "java/io/RandomAccessFile");
    if ((*env)->ExceptionOccurred(env)) return 0;
    fdObjID = (*env)->GetFieldID(env, RAFCls, "fd","Ljava/io/FileDescriptor;");
    if ((*env)->ExceptionOccurred(env)) return 0;
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");
    if ((*env)->ExceptionOccurred(env)) return 0;
    /* make IOExcCls into a global reference for future use */
    IOExcCls = (*env)->NewGlobalRef(env, IOExcCls);
    inited = 1;
 done:
    FLEX_MUTEX_UNLOCK(&init_mutex);
    return 1;
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    open
 * Signature: (Ljava/lang/String;Z)V
 */
JNIEXPORT void JNICALL Java_java_io_RandomAccessFile_open
  (JNIEnv *env, jobject objRAF, jstring name, jboolean writeable) {
    const char * cstr;	/* C-representation of filename string. */
    int fd;		/* file descriptor of opened file. */
    jobject fdObj;	/* file descriptor object. */

    /* If static data has not been loaded, load it now. */
    if (!inited && !initializeRAF(env)) return; /* exception occurred; bail */

    cstr = (*env)->GetStringUTFChars(env, name, NULL);
    fd = open(cstr, O_BINARY|O_CREAT|O_TRUNC|
	      (writeable==JNI_TRUE?O_RDWR:O_RDONLY), 0666);
    (*env)->ReleaseStringUTFChars(env, name, cstr);
    fdObj = (*env)->GetObjectField(env, objRAF, fdObjID);
    Java_java_io_FileDescriptor_setfd(env, fdObj, fd);

    /* Check for error condition. */
    if (fd==-1)
      (*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    read
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_io_RandomAccessFile_read
  (JNIEnv *env, jobject objRAF) {
    int            fd, result; 
    jobject        fdObj;
    unsigned char  buf[1];

    assert(inited);

    fdObj    = (*env)->GetObjectField(env, objRAF, fdObjID);
    fd       = Java_java_io_FileDescriptor_getfd(env, fdObj);

    result = read(fd, (void*)buf, 1);

    if (result==-1) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return -1; /* could return anything; value is ignored. */
    }
    if (result==0) return -1; /* Java sez -1 at EOF; C says 0 */
    /* I guess everything worked fine then! */
    return (jint) buf[0];
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    readBytes
 * Signature: ([BII)I
 */
// keep in sync with Java_java_io_FileInputStream_readBytes
JNIEXPORT jint JNICALL Java_java_io_RandomAccessFile_readBytes
  (JNIEnv *env, jobject objRAF, jbyteArray ba, jint start, jint len) {
    int              fd, result;
    int		     bufsize = (len > MAX_BUFFER_SIZE) ? MAX_BUFFER_SIZE : len;
    jobject          fdObj;
    jbyte            buf[bufsize];

    assert(inited);

    if (len==0) return 0; /* don't even try to read anything. */

    fdObj  = (*env)->GetObjectField(env, objRAF, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    
    result = read(fd, (void*)buf, bufsize);

    if (result==-1) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return -1; /* could return anything; value is ignored. */
    }

    (*env)->SetByteArrayRegion(env, ba, start, result, buf); 

    /* Java language spec requires -1 at EOF, not 0 */ 
    return (jint)(result ? result : -1);
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    write
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_RandomAccessFile_write
  (JNIEnv *env, jobject objRAF, jint i) {
    int            fd, result;
    jobject        fdObj;
    unsigned char  buf[1];

    assert(inited);

    fdObj    = (*env)->GetObjectField(env, objRAF, fdObjID);
    fd       = Java_java_io_FileDescriptor_getfd(env, fdObj);
    buf[0]   = (unsigned char)i;

    result = write(fd, (void*)buf, 1);
    if (result==0)
	(*env)->ThrowNew(env, IOExcCls, "No bytes written");
    if (result==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    writeBytes
 * Signature: ([BII)V
 */
// keep in sync with Java_java_io_FileOutputStream_writeBytes
JNIEXPORT void JNICALL Java_java_io_RandomAccessFile_writeBytes
  (JNIEnv *env, jobject objRAF, jbyteArray ba, jint start, jint len) {
    int              fd, result;
    jobject          fdObj;
    jbyte            *buf;
    char             *errmsg = NULL;
    int written = 0;

    assert(inited);

    if (len==0) return; /* don't even try to write anything. */

    fdObj  = (*env)->GetObjectField(env, objRAF, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    buf    = (*env)->GetByteArrayElements(env, ba, NULL);
    if ((*env)->ExceptionOccurred(env)) return; /* bail */

    while (written < len) {
	result = write(fd, (void*)(buf+start+written), len-written);
	/* if we're interrupted by a signal, just retry. */
	if (result < 0 && errno == EINTR) continue;

	if (result==0) {
	    errmsg = "No bytes written";
	    goto done;
	}
	if (result==-1) {
	    errmsg = strerror(errno);
	    if (!errmsg) errmsg = "Unknown I/O error";
	    goto done;
	}
	written+=result;
    }
 done:
    (*env)->ReleaseByteArrayElements(env, ba, buf, JNI_ABORT);
    if (errmsg) (*env)->ThrowNew(env, IOExcCls, errmsg);
    return;
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    getFilePointer
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_io_RandomAccessFile_getFilePointer
  (JNIEnv *env, jobject objRAF) {
    int fd;
    jobject fdObj;
    off_t result;

    assert(inited);

    fdObj  = (*env)->GetObjectField(env, objRAF, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    result = lseek(fd, 0, SEEK_CUR);

    if (result==(off_t)-1)
      (*env)->ThrowNew(env, IOExcCls, strerror(errno));

    return (jlong) result;
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    seek
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_java_io_RandomAccessFile_seek
  (JNIEnv *env, jobject objRAF, jlong pos) {
    int fd;
    jobject fdObj;
    off_t result;

    assert(inited);

    fdObj  = (*env)->GetObjectField(env, objRAF, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    result = lseek(fd, pos, SEEK_SET);

    if (result==(off_t)-1)
      (*env)->ThrowNew(env, IOExcCls, strerror(errno));

    return;
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    length
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_io_RandomAccessFile_length
  (JNIEnv *env, jobject objRAF) {
    int fd;
    jobject fdObj;
    off_t result, curpos;

    assert(inited);

    fdObj  = (*env)->GetObjectField(env, objRAF, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    curpos = lseek(fd, 0, SEEK_CUR);
    result = lseek(fd, 0, SEEK_END);
    if (curpos!=(off_t)-1)
      curpos=lseek(fd, curpos, SEEK_SET);

    if (result==(off_t)-1 || curpos==(off_t)-1)
      (*env)->ThrowNew(env, IOExcCls, strerror(errno));

    return (jlong) result;
}

/*
 * Class:     java_io_RandomAccessFile
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_RandomAccessFile_close
  (JNIEnv *env, jobject objRAF) {
    int      fd, result;
    jobject  fdObj;

    assert(inited);

    fdObj  = (*env)->GetObjectField(env, objRAF, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    result = close(fd);
    Java_java_io_FileDescriptor_setfd(env, fdObj, -1);

    if (result==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}
