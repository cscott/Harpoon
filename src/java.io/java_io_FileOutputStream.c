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
// keep in sync with Java_java_io_RandomAccessFile_writeBytes
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_writeBytes
(JNIEnv * env, jobject obj, jbyteArray ba, jint start, jint len) { 
    int              fd, result;
    jobject          fdObj;
    jbyte            *buf;
    char             *errmsg = NULL;
    int written = 0;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFOS(env)) return; /* exception occurred; bail */

    if (len==0) return; /* don't even try to write anything. */

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
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

#ifdef WITH_TRANSACTIONS
#include "../transact/transact.h" /* for FNI_StrTrans2Str */
#include "../transact/java_lang_Object.h" /* version fetch methods */
#include "../java.lang/java_lang_Class.h" /* Class.getComponentType() */
/* transactional version of writeBytes -- problematic!  how do we
 * undo bytes written to a file descriptor? punting on the problem
 * for now & just writing the bytes. */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_writeBytes_00024_00024withtrans
(JNIEnv * env, jobject obj, jobject commitrec, jbyteArray ba, jint start, jint len) { 
  jbyteArray _ba;
  jsize length = (*env)->GetArrayLength(env, ba);
  jclass byteclass;
  int i;
  /* get readable version. */
  _ba = (jbyteArray)
    Java_java_lang_Object_getReadableVersion(env, ba, commitrec);
  /* now tag all elements as read. */
  byteclass =
    Java_java_lang_Class_getComponentType(env, (*env)->GetObjectClass(env,ba));
  for (i=0; i<length; i++)
    Java_java_lang_Object_writeArrayElementFlag(env, ba, i, byteclass);
  /* okay, now we can do the writeBytes. */
  /* XXX: if GetByteArrayRegion is fixed, this won't work so well. */
  Java_java_io_FileOutputStream_writeBytes(env, obj, _ba, start, len);
}
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_open_00024_00024withtrans
  (JNIEnv * env, jobject obj, jobject commitrec, jstring jstr) {
    /* XXX: fd is write once only within JNI so should be okay. */
    Java_java_io_FileOutputStream_open(env, obj,
				       FNI_StrTrans2Str(env, commitrec, jstr));
}
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_close_00024_00024withtrans
(JNIEnv * env, jobject obj) { 
  Java_java_io_FileOutputStream_close(env, obj);
}
#endif /* WITH_TRANSACTIONS */
