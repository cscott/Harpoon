#include "java_io_FileOutputStream.h"

static int fdObjID = 0;  /* The field ID of fd in class FileOutputStream */
static int fdID    = 0;  /* The field ID of fd in class FileDescriptor */

#define IO_ERROR(env, str)                                          \
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");       \
    if (IOExcCls == NULL) return; /* give up */                     \
    else (*env)->ThrowNew(env, IOExcCls, "Couldn't write to file")

/*
 * Class:     java_io_FileOutputStream
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_open
  (JNIEnv * env, jobject obj, jstring jstr) { 
    const char * cstr;      /* C-representation of filename to open */
    int          fd;        /* File descriptor of opened file */

    /* If static data has not been loaded, load it now */
    if ((fdObjID & fdID) == 0) 
      if (!initialize_FOS_data(env)) IO_ERROR("Couldn't init native I/O");

    cstr  = (*env)->getStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_WRONLY|O_CREAT|O_BINARY|O_TRUNC|O_NONBLOCK);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    (*env)->SetIntField(env, fdObj, fdID, fd);

    /* Check for error condition */
    if (fd==-1) { IO_ERROR("Couldn't open file"); }
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
    if ((fdObjID & fdID) == 0) 
      if (!initialize_FOS_data(env)) IO_ERROR("Couldn't init native I/O");

    cstr  = (*env)->getStringUTFChars(env, jstr, 0);
    fd    = open(cstr, O_WRONLY|O_CREAT|O_BINARY|O_APPEND|O_NONBLOCK);
    (*env)->ReleaseStringUTFChars(env, jstr, cstr);
    fdObj = (*env)->GetObjectField(env, obj, fdObjID);
    (*env)->SetIntField(env, fdObj, fdID, fd);

    /* Check for error condition */
    if (fd==-1) { IO_ERROR("Couldn't open file"); }
}
    

/*
 * Class:     java_io_FileOutputStream
 * Method:    write
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_write
  (JNIEnv * env, jobject obj, jint i) { 
    unsigned char  buf[1];
    int            fd; 
    jobject        fdObj;

    fdObj    = (*env)->GetObjectField(env, obj, fdObjID);
    fd       = (*env)->GetIntField(env, fdObj, fdID);
    buf[0]   = (unsigned char)i;

    if (write(fd, (void*)buf, 1) != 1) { IO_ERROR("Couldn't write to file"); }
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    writeBytes
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_writeBytes
(JNIEnv * env, jobject obj, jbyteArray buf, jint start, jint len) { 
    unsigned char *  buf;
    int              fd; 
    jobject          fdObj;

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    (*env)->GetByteArrayRegion(env, buf, start, len, buf); 

    if (write(fd,(void*)buf,len)!=len) { IO_ERROR("Couldn't write to file"); }
}

/*
 * Class:     java_io_FileOutputStream
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_io_FileOutputStream_close
(JNIEnv * env, jobject obj) { 
    int      fd;
    jobject  fdObj;

    fdObj  = (*env)->GetObjectField(env, obj, fdObjID);
    fd     = (*env)->GetIntField(env, fdObj, fdID);
    close(fd);
    (*env)->SetIntField(env, fdObj, fdID, -1);
}


int initialize_FOS_data(JNIEnv * env) { 
    jclass FOSCls, FDCls;

    FOSCls  = (*env)->FindClass(env, "Ljava/io/FileOutputStream");
    if (FOSCls == NULL) return 0;
    fdObjID = (*env)->GetFieldID(env, FOSCls,"fd","Ljava/io/FileDescriptor");
    FDCls   = (*env)->FindClass(env, "Ljava/io/FileDescriptor");
    if (FDCls == NULL)  return 0;
    fdID    = (*env)->GetFieldID(env, cls, "fd", "I");

    return 1;  /* Success */
}



