#include "java_io_FileOutputStream.h"
#include "config.h"
#include <assert.h>	/* for assert */
#include <errno.h>	/* for errno */
#include <fcntl.h>	/* for open */
#include <string.h>	/* for strerror */
#include <unistd.h>	/* read, etc. */
#include <sys/stat.h>	/* for open/stat */
#include <sys/ioctl.h>
#include <sys/types.h>
#if HAVE_SYS_TIME_H
# include <sys/time.h>	/* for struct timeval */
#else
# include <time.h>
#endif
#include "flexthread.h" /* for mutex ops */

#include "javaio.h" /* for getfd/setfd */

#if defined(WITH_USER_THREADS) && defined (WITH_EVENT_DRIVEN)
#define USERIO
#endif

static jfieldID fdObjID = 0; /* The field ID of fd in class FileInputStream */
static jclass IOExcCls  = 0; /* The java/io/IOException class object */
static int inited = 0; /* whether the above variables have been initialized */
FLEX_MUTEX_DECLARE_STATIC(init_mutex);

static int initializeFIS(JNIEnv *env) {
    jclass FISCls;

    FLEX_MUTEX_LOCK(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;

    FISCls  = (*env)->FindClass(env, "java/io/FileInputStream");
    if ((*env)->ExceptionOccurred(env)) goto done;
    fdObjID = (*env)->GetFieldID(env, FISCls, "fd","Ljava/io/FileDescriptor;");
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
 * Class:     java_io_FileInputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileInputStream_open
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */
    jobject	 fdObj;	    /* File descriptor object */

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFIS(env)) return; /* exception occurred; bail */

    cstr  = (*env)->GetStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_RDONLY|O_BINARY);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);

#ifdef USERIO
    Java_java_io_NativeIO_makeNonBlockJNI(env, NULL, fd);
#endif

    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    Java_java_io_FileDescriptor_setfd(env, fdObj, fd);

    /* Check for error condition */
    if (fd==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}


/*
 * Class:     java_io_FileInputStream
 * Method:    read
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_read
(JNIEnv * env, jobject obj) { 
    int            fd, result; 
    jobject        fdObj;
    unsigned char  buf[1];

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFIS(env)) return 0;/* exception occurred; bail */

    fdObj    = (*env)->GetObjectField(env, obj, fdObjID);
    fd       = Java_java_io_FileDescriptor_getfd(env, fdObj);

    do {
	result = read(fd, (void*)buf, 1);
#ifdef USERIO
	if (result<0 && errno == EAGAIN) {
	  SchedulerAddRead(fd); errno = EINTR;/* loop */
	}
#endif
	/* if we're interrupted by a signal, just retry. */
    } while (result<0 && errno == EINTR);

    if (result==-1) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return -1; /* could return anything; value is ignored. */
    }
    if (result==0) return -1; /* Java sez -1 at EOF; C says 0 */
    /* I guess everything worked fine then! */
    return (jint) buf[0];
}

/*
 * Class:     java_io_FileInputStream
 * Method:    readBytes
 * Signature: ([BII)I
 */
// keep in sync with Java_java_io_RandomAccessFile_readBytes
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_readBytes
(JNIEnv * env, jobject obj, jbyteArray ba, jint start, jint len) { 
    int              fd, result;
    int		     bufsize = (len > MAX_BUFFER_SIZE) ? MAX_BUFFER_SIZE : len;
    jobject          fdObj;
    jbyte            buf[bufsize];

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFIS(env)) return 0;/* exception occurred; bail */

    if (len==0) return 0; /* don't even try to read anything. */

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);

    do {
	result = read(fd, (void*)buf, bufsize);
#ifdef USERIO
	if (result<0 && errno == EAGAIN) {
	  SchedulerAddRead(fd); errno = EINTR;/* loop */
	}
#endif
	/* if we're interrupted by a signal, just retry. */
    } while (result<0 && errno == EINTR);

    if (result==-1) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return -1; /* could return anything; value is ignored. */
    }

    (*env)->SetByteArrayRegion(env, ba, start, result, buf); 

    /* Java language spec requires -1 at EOF, not 0 */ 
    return (jint)(result ? result : -1);
}

/*
 * Class:     java_io_FileInputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileInputStream_close
(JNIEnv * env, jobject obj) { 
    int fd, result;
    jclass fdObj;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFIS(env)) return; /* exception occurred; bail */

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);
    result = close(fd);
    Java_java_io_FileDescriptor_setfd(env, fdObj, -1);

    if (result==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}


/*
 * Class:     java_io_FileInputStream
 * Method:    skip
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_java_io_FileInputStream_skip
(JNIEnv * env, jobject obj, jlong n) { 
    int    fd;
    off_t  result, orig;
    jclass fdObj;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFIS(env)) return 0;/* exception occurred; bail */

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = Java_java_io_FileDescriptor_getfd(env, fdObj);

    /* Get original offset, then reposition. */
    if ((orig = lseek(fd,0,SEEK_CUR)) == -1 ||
	(result = lseek(fd,n,SEEK_CUR)) == -1) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return 0;
    }
    return (jlong)(result - orig);
}
    
/*
 * Class:     java_io_FileInputStream
 * Method:    available
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_available
(JNIEnv * env, jobject obj) { 
    jobject         fdObj;
    fd_set          read_fds;
    int             fd, retval, result;
    off_t           orig;
    struct stat     fdStat;
    
    /* If static data has not been loaded, load it now */
    if (!inited && !initializeFIS(env)) return 0;/* exception occurred; bail */

    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    fd    = Java_java_io_FileDescriptor_getfd(env, fdObj);

    if ((orig = lseek(fd, 0, SEEK_CUR)) == -1) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return -1; /* could return anything; value is ignored. */
    }
    /* XXX: THIS ISN'T ACTUALLY CORRECT -- we want the # of bytes readable
     * _without blocking_ [CSA] */
    result = fstat(fd, &fdStat);
    if ((!result) && (S_ISREG(fdStat.st_mode))) { 
        retval = fdStat.st_size - orig;
    }
    else { 
#ifdef HAVE_IOCTL_FIONREAD
        /* File is not regular, attempt to use FIONREAD ioctl() */
        /* NOTE: FIONREAD ioctl() reports 0 for some fd's */        
        if ((ioctl(fd, FIONREAD, &retval) >= 0) && retval) { /* we're done */ }
#else /* we don't have this ioctl, so don't try it */
	if (0) ;
#endif
	else { 
	    /* The best we can do now is to use select to see if the fd is
	       available.  Returns 1 if true, 0 otherwise. */
	    struct timeval timeout = {0,0};
	    FD_ZERO(&read_fds);
	    FD_SET(fd, &read_fds);
	    if (select(fd+1, &read_fds, NULL, NULL, &timeout) == -1) {
		(*env)->ThrowNew(env, IOExcCls, strerror(errno));
		return -1; /* could return anything; value is ignored. */
	    } else { retval = (FD_ISSET(fd, &read_fds)) ? 1 : 0; }
	}
    }
    return (jint)retval;
}

#ifdef WITH_TRANSACTIONS
#include "../transact/transact.h" /* FNI_StrTrans2Str */
#include "../transact/java_lang_Object.h" /* version fetch methods */
#include "../java.lang/java_lang_Class.h" /* Class.getComponentType() */
/* transactional version of readBytes -- problematic!  how do we
 * undo bytes read from a file descriptor? punting on the problem
 * for now & just reading the bytes. */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_read_00024_00024withtrans
(JNIEnv * env, jobject obj) {
  return Java_java_io_FileInputStream_read(env, obj);
}
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_readBytes_00024_00024withtrans
(JNIEnv * env, jobject obj, jobject commitrec, jbyteArray ba, jint start, jint len) { 
  jbyteArray _ba;
  jsize length = (*env)->GetArrayLength(env, ba);
  jclass byteclass;
  int i;
  /* get writable version. */
  _ba = (jbyteArray)
    Java_java_lang_Object_getReadWritableVersion(env, ba, commitrec);
  /* now tag all elements as written. */
  byteclass =
    Java_java_lang_Class_getComponentType(env, (*env)->GetObjectClass(env,ba));
  for (i=0; i<length; i++)
    Java_java_lang_Object_writeArrayElementFlag(env, ba, i, byteclass);
  /* okay, now we can do the readBytes. */
  /* XXX: if GetByteArrayRegion is fixed, this won't work so well. */
  return Java_java_io_FileInputStream_readBytes(env, obj, _ba, start, len);
  /* XXX: tags all elements in array, even if not all are written. */
}
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_available_00024_00024withtrans
(JNIEnv * env, jobject obj, jobject commitrec) {
  /* XXX: let's hope the fd is readable here! */
  return Java_java_io_FileInputStream_available(env, obj);
}
JNIEXPORT void JNICALL Java_java_io_FileInputStream_open_00024_00024withtrans
  (JNIEnv * env, jobject obj, jobject commitrec, jstring jstr) {
  /* XXX: fd is write once only within JNI so should be okay. */
  Java_java_io_FileInputStream_open(env, obj,
				    FNI_StrTrans2Str(env, commitrec, jstr));
}
JNIEXPORT void JNICALL Java_java_io_FileInputStream_close_00024_00024withtrans
(JNIEnv * env, jobject obj, jobject commitrec) {
  /* XXX: close isn't undoable. =( fd is write-once so should be okay. */
  Java_java_io_FileInputStream_close(env, obj);
}
#endif /* WITH_TRANSACTIONS */
