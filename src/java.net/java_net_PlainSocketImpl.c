/* Implementation for class java_net_PlainSocketImpl */
#include <jni.h>
#include "java_net_PlainSocketImpl.h"

#include <assert.h>
#include <errno.h> /* for errno */
#include <netinet/in.h> /* for struct sockaddr_in */
#include <sys/socket.h> /* for socket(2) */
#include <string.h> /* for strerror(3) */
#include <unistd.h> /* for close(2) */

#ifdef WITH_HEAVY_THREADS
#include <pthread.h>    /* for mutex ops */
#endif

static jfieldID SI_fdObjID = 0; /* The field ID of SocketImpl.fd */
static jfieldID SI_addrID  = 0; /* The field ID of SocketImpl.address */
static jfieldID SI_portID  = 0; /* The field ID of SocketImpl.port */
static jfieldID SI_localportID = 0; /* The field ID of SocketImpl.localport */
static jfieldID FD_fdID    = 0; /* The field ID of FileDescriptor.fd */
static jfieldID IA_addrID  = 0; /* The field ID of InetAddress.address */
static jfieldID IA_familyID= 0; /* The field ID of InetAddress.family */
static jclass IOExcCls  = 0; /* The java/io/IOException class object */
static int inited = 0; /* whether the above variables have been initialized */
#ifdef WITH_HEAVY_THREADS
static pthread_mutex_t init_mutex = PTHREAD_MUTEX_INITIALIZER;
#endif

int initializePSI(JNIEnv *env) {
    jclass PSICls, FDCls, IACls;

#ifdef WITH_HEAVY_THREADS
    pthread_mutex_lock(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;
#endif

    PSICls  = (*env)->FindClass(env, "java/net/PlainSocketImpl");
    if ((*env)->ExceptionOccurred(env)) goto done;
    SI_fdObjID = (*env)->GetFieldID(env, PSICls,
				    "fd","Ljava/io/FileDescriptor;");
    if ((*env)->ExceptionOccurred(env)) goto done;
    SI_addrID  = (*env)->GetFieldID(env, PSICls,
				    "address", "Ljava/net/InetAddress;");
    if ((*env)->ExceptionOccurred(env)) goto done;
    SI_portID  = (*env)->GetFieldID(env, PSICls, "port", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    SI_localportID  = (*env)->GetFieldID(env, PSICls, "localport", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IACls   = (*env)->FindClass(env, "java/net/InetAddress");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IA_addrID  = (*env)->GetFieldID(env, IACls, "address", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IA_familyID= (*env)->GetFieldID(env, IACls, "family", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    FDCls   = (*env)->FindClass(env, "java/io/FileDescriptor");
    if ((*env)->ExceptionOccurred(env)) goto done;
    FD_fdID    = (*env)->GetFieldID(env, FDCls, "fd", "I");
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
 * Class:     java_net_PlainSocketImpl
 * Method:    socketCreate
 * Signature: (Z)V
 */
/**
 * Creates a socket with a boolean that specifies whether this
 * is a stream socket (true) or an unconnected UDP socket (false).
 * Throws IOException.
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketCreate
  (JNIEnv *env, jobject _this, jboolean isStream) {
    jobject fdObj;
    int fd;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializePSI(env)) return; /* exception occurred; bail */

    fd = socket(AF_INET, isStream ? SOCK_STREAM : SOCK_DGRAM, 0);
    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    (*env)->SetIntField(env, fdObj, FD_fdID, fd);

    /* Check for error condition */
    if (fd==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketConnect
 * Signature: (Ljava/net/InetAddress;I)V
 */
/** Try once to connect this socket to the given address and port.
 *  Throws IOException. */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketConnect
  (JNIEnv *env, jobject _this, jobject/*InetAddress*/ address, jint port) {
    struct sockaddr_in sa;
    jobject fdObj;
    int fd, rc;

    assert(inited && address);
    memset(&sa, 0, sizeof(sa));
    sa.sin_family      = (*env)->GetIntField(env, address, IA_familyID);
    sa.sin_addr.s_addr = htonl((*env)->GetIntField(env, address, IA_addrID));
    sa.sin_port        = htons(port);

    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    fd = (*env)->GetIntField(env, fdObj, FD_fdID);

    rc = connect(fd, &sa, sizeof(sa));

    /* Check for error condition */
    if (rc<0)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketBind
 * Signature: (Ljava/net/InetAddress;I)V
 */
/**
 * Binds the socket to the specified address of the specified local port.
 * Throws IOException.
 */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketBind
  (JNIEnv *env, jobject _this, jobject/*InetAddress*/ address, jint lport) {
    struct sockaddr_in sa;
    jobject fdObj;
    int fd, rc;

    assert(inited && address);
    memset(&sa, 0, sizeof(sa));
    sa.sin_family      = (*env)->GetIntField(env, address, IA_familyID);
    sa.sin_addr.s_addr = htonl((*env)->GetIntField(env, address, IA_addrID));
    sa.sin_port        = htons(lport);

    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    fd = (*env)->GetIntField(env, fdObj, FD_fdID);

    rc = bind(fd, &sa, sizeof(sa));

    /* Check for error condition */
    if (rc<0)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}
/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketListen
 * Signature: (I)V
 */
 /**
  * Listens, with the specified maximum backlog, for connections.
  */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketListen
  (JNIEnv *env, jobject _this, jint backlog) {
    jobject fdObj;
    int fd, rc;

    assert(inited);
    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    fd = (*env)->GetIntField(env, fdObj, FD_fdID);

    rc = listen(fd, backlog);

    /* Check for error condition */
    if (rc<0)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketAccept
 * Signature: (Ljava/net/SocketImpl;)V
 */
/** Accepts connections. */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketAccept
  (JNIEnv *env, jobject _this, jobject/*SocketImpl*/ s) {
    struct sockaddr_in sa;
    jobject fdObj, address;
    int fd, rc, sa_size = sizeof(sa);

    assert(inited);
    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    fd = (*env)->GetIntField(env, fdObj, FD_fdID);

    rc = accept(fd, &sa, &sa_size);
    /* Check for error condition */
    if (rc<0) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return;
    }

    /* fill in SocketImpl */
    fdObj = (*env)->GetObjectField(env, s, SI_fdObjID);
    (*env)->SetIntField(env, fdObj, FD_fdID, rc);
    address = (*env)->GetObjectField(env, s, SI_addrID);
    (*env)->SetIntField(env, address, IA_familyID, sa.sin_family);
    (*env)->SetIntField(env, address, IA_addrID, ntohl(sa.sin_addr.s_addr));
    (*env)->SetIntField(env, s, SI_portID, ntohs(sa.sin_port));
    /* done */
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketAvailable
 * Signature: ()I
 */
/**
 * Returns the number of bytes that can be read without blocking.
 */
JNIEXPORT jint JNICALL Java_java_net_PlainSocketImpl_socketAvailable
  (JNIEnv *env, jobject _this) {
  assert(0);
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketClose
 * Signature: ()V
 */
/** Closes the socket. */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketClose
  (JNIEnv *env, jobject _this) {
    int fd, rc;
    jobject fdObj;

    if (!inited && !initializePSI(env)) return; /* exception occurred; bail */
    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    fd = (*env)->GetIntField(env, fdObj, FD_fdID);

    rc = close(fd);
    if (rc<0)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    initProto
 * Signature: ()V
 */
/** initialize the protocol-handler library? */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_initProto
  (JNIEnv *env, jclass _plainsocketimpl) {
  /* do nothing. */
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketSetOption
 * Signature: (IZLjava/lang/Object;)V
 */
/** set socket options */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketSetOption
  (JNIEnv *env, jobject _this, jint opt, jboolean on, jobject value) {
  assert(0);
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketGetOption
 * Signature: (I)I
 */
/** get socket options.
 * The native socketGetOption() knows about 3 options.
 * The 32 bit value it returns will be interpreted according
 * to what we're asking.  A return of -1 means it understands
 * the option but its turned off.  It will raise a SocketException
 * if "opt" isn't one it understands.
 */
JNIEXPORT jint JNICALL Java_java_net_PlainSocketImpl_socketGetOption
  (JNIEnv *env, jobject _this, jint opt) {
  assert(0);
}
