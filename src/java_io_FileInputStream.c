include "java_io_FileOutputStream.h"

static int    fdObjID = 0;  /* The field ID of fd in class FileOutputStream */
static int    fdID    = 0;  /* The field ID of fd in class FileDescriptor */
static jclass IOExcCls;

#define IO_ERROR(env, str) \
    (JNIEnv *)env;  (const char *)str;  /* Check types */             \
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");         \   
    if (IOExcCls == NULL) return; /* give up */                       \
    else (*env)->ThrowNew(env, IOExcCls, "Couldn't write to file")

/*
 * Class:     java_io_FileInputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileInputStream_open
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */

    /* If static data has not been loaded, load it now */
    if ((fdObjID & fdID) == 0) 
	if (!initialize_FIS_data(env)) IO_ERROR("Couldn't init native I/O");

    cstr  = (*env)->getStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_RDONLY|O_BINARY|O_NONBLOCK);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    (*env)->SetIntField(env, fdObj, fdID, fd);

    /* Check for error condition */
    if (fd==-1) { IO_ERROR("Couldn't open file"); }
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

    /* NOTE: Assumes static data has been loaded */
    fdObj    = (*env)->GetObjectField(env, obj, fdObjID);
    fd       = (*env)->GetIntField(env, fdObj, fdID);

    if ((result = read(fd, (void*)buf, 1)) == -1) { 
        IO_ERROR("Couldn't read from file"); 
    }

    /* Java language spec requires -1 at EOF, not 0 */ 
    return (jint)(result ? buf[0] : -1);
}

/*
 * Class:     java_io_FileInputStream
 * Method:    readBytes
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_readBytes
(JNIEnv * env, jobject, obj, jbyteArray buf, jint start, jint len) { 
    int              fd; 
    jobject          fdObj;
    unsigned char    buf[len];

    /* NOTE: Assumes static data has been loaded (should be true) */
    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    
    if ((len = read(fd,(void*)buf,len)) == -1) { 
        IO_ERROR("Couldn't read from file"); 
    }

    (*env)->SetByteArrayRegion(env, buf, start, len, buf); 

    /* Java language spec requires -1 at EOF, not 0 */ 
    return (jint)(len ? len : -1);
}

/*
 * Class:     java_io_FileInputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileInputStream_close
(JNIEnv * env, jobject obj) { 
    int fd;
    jclass fdObj;

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    close(fd);
    (*env)->SetIntField(env, fdObj, fdID, -1);
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

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);

    /* Get original offset */
    if ((orig = lseek(fd,0,SEEK_SET)) == -1)   { IO_ERROR("Could not seek"); }
    if ((result = lseek(fd,n,SEEK_CUR)) == -1) { IO_ERROR("Could not seek"); }

    return (jlong)(result - orig);
}
    
/*
 * Class:     java_io_FileInputStream
 * Method:    available
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_io_FileInputStream_available
(JNIEnv * env, jobject obj) { 
    fd_set          read_fds;
    int             fd, retval, result;
    off_t           orig;
    struct stat     fdStat;
    struct timeval  timeout;
    
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    fd    = (*env)->GetIntField(env, fdObj, fdID);

    if ((orig = lseek(fd, 0, SEEK_SET)) == -1) { IO_ERROR("Could not seek"); }

    result = fstat(fd, &fdStat);
    if ((!result) && (S_ISREG(fdStat.st_mode))) { 
        retval = fdStat.st_size - orig;
    }
    else { 
        /* File is not regular, attempt to use FIONREAD ioctl() */
        /* NOTE: FIONREAD ioctl() reports 0 for some fd's */        
        if ((ioctl(fd, FIONREAD, &result) >= 0) && result) { /* we're done */ }
	else { 
	    /* The best we can do now is to use select to see if the fd is
	       available.  Returns 1 if true, 0 otherwise. */
	    timeout = {0,0};
	    FD_ZERO(&read_fds);
	    FD_SET(fd, &read_fds);
	    if (select(fd+1, &read_fds, NULL, NULL, &timeout) == -1) { 
	        IO_ERROR("Can't test availability of file descriptor");
	    }
	    else { retval = (FD_ISSET(fd, &read_fds)) ? 1 : 0; }
	}
    }
    return (jint)retval;
}
	  

void initialize_FIS_data(JNIEnv * env) { 
    jclass FOSCls, FDCls;

    FOSCls  = (*env)->FindClass(env, "Ljava/io/FileOutputStream");
    if (FOSCls == NULL) { IO_ERROR("Couldn't initialize native I/O"); }
    fdObjID = (*env)->GetFieldID(env, FOSCls, "fd", "Ljava/io/FileDescriptor");
    FDCls   = (*env)->FindClass(env, "Ljava/io/FileDescriptor");
    if (FDCls == NULL)  { IO_ERROR("Couldn't initialize native I/O"); }
    fdID    = (*env)->GetFieldID(env, FDCls, "fd", "I");
}


