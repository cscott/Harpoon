/* Implementation for class java_net_PlainSocketImpl */
#include <jni.h>
#include "java_net_PlainSocketImpl.h"

#include <assert.h>
#include <errno.h> /* for errno */
#include <netinet/in.h> /* for struct sockaddr_in */
#include <netinet/tcp.h> /* for TCP_NODELAY */
#include <sys/socket.h> /* for socket(2) */
#include <string.h> /* for strerror(3) */
#include <unistd.h> /* for close(2) */

/* NetBSD compatability. */
#ifndef SOL_TCP
#define SOL_TCP IPPROTO_TCP
#endif
#ifndef SOL_IP
#define SOL_IP IPPROTO_IP
#endif

#include "flexthread.h" /* for mutex ops */
#include "../java.io/javaio.h" /* for getfd/setfd */

#if defined(WITH_USER_THREADS) && defined (WITH_EVENT_DRIVEN)
#define USERIO
#endif

static jfieldID SI_fdObjID = 0; /* The field ID of SocketImpl.fd */
static jfieldID SI_addrID  = 0; /* The field ID of SocketImpl.address */
static jfieldID SI_portID  = 0; /* The field ID of SocketImpl.port */
static jfieldID SI_localportID = 0; /* The field ID of SocketImpl.localport */
static jfieldID IA_addrID  = 0; /* The field ID of InetAddress.address */
static jfieldID IA_familyID= 0; /* The field ID of InetAddress.family */
static jclass IOExcCls  = 0; /* The java/io/IOException class object */
static jint jSO_BINDADDR, jSO_REUSEADDR, jSO_LINGER, jSO_TIMEOUT;
static jint jTCP_NODELAY, jIP_MULTICAST_IF;
static int inited = 0; /* whether the above variables have been initialized */
FLEX_MUTEX_DECLARE_STATIC(init_mutex);

static int initializePSI(JNIEnv *env) {
    jclass PSICls, IACls;

    FLEX_MUTEX_LOCK(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;

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
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");
    if ((*env)->ExceptionOccurred(env)) goto done;
    /* make IOExcCls into a global reference for future use */
    IOExcCls = (*env)->NewGlobalRef(env, IOExcCls);
    /* initialize socket option values.  copied from source file at
     * at the moment; will work on being able to read these from the binary.*/
    jTCP_NODELAY=0x0001; jIP_MULTICAST_IF=0x10;
    jSO_BINDADDR=0x000F; jSO_REUSEADDR=0x04;
    jSO_LINGER=0x0080; jSO_TIMEOUT=0x1006;

    /* done. */
    inited = 1;
 done:
    FLEX_MUTEX_UNLOCK(&init_mutex);
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

    fd = socket(PF_INET, isStream ? SOCK_STREAM : SOCK_DGRAM, 0);
    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    Java_java_io_FileDescriptor_setfd(env, fdObj, fd);

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
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

    do {
      rc = connect(fd, (struct sockaddr *) &sa, sizeof(sa));
    } while (rc<0 && errno==EINTR); /* repeat if interrupted */

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
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

    rc = bind(fd, (struct sockaddr *) &sa, sizeof(sa));

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
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

    rc = listen(fd, backlog);

#ifdef USERIO
    /* Switch to NonBlocking mode */
    Java_java_io_NativeIO_makeNonBlockJNI(env,NULL,fd);
#endif

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
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

#ifdef USERIO
    /* Call Into Scheduler to wait for select on this socket*/
    do {
      rc = accept(fd, (struct sockaddr *) &sa, &sa_size);
      if (rc<0 && errno==EAGAIN)
	SchedulerAddRead(fd);
    } while (rc<0 && (errno==EINTR || errno==EAGAIN)); /* repeat if interrupted */
#else
    do {
      rc = accept(fd, (struct sockaddr *) &sa, &sa_size);
    } while (rc<0 && errno==EINTR); /* repeat if interrupted */
#endif
    /* Check for error condition */
    if (rc<0) {
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
	return;
    }

#ifdef USERIO
    Java_java_io_NativeIO_makeNonBlockJNI(env,NULL,rc);
#endif

    /* fill in SocketImpl */
    fdObj = (*env)->GetObjectField(env, s, SI_fdObjID);
    Java_java_io_FileDescriptor_setfd(env, fdObj, rc);
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
    // XXX: can we do something intelligent here?
    return 0;
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
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

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

static void throwSE(JNIEnv *env, const char * msg) {
    jclass exc = (*env)->FindClass(env, "java/net/SocketException");
    if (!(*env)->ExceptionOccurred(env))
	(*env)->ThrowNew(env, exc, msg);
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketSetOption
 * Signature: (IZLjava/lang/Object;)V
 */
/** set socket options */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketSetOption
  (JNIEnv *env, jobject _this, jint opt, jboolean on, jobject value) {
    struct linger li;
    int fd, optval = on ? 1 : 0;
    jobject fdObj; jclass cls;
    assert(inited);
    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);
    cls = (*env)->GetObjectClass(env, value);
    if (opt == jTCP_NODELAY) {
	if (setsockopt(fd, SOL_TCP, TCP_NODELAY, &optval, sizeof(optval))==0)
	    return;
    } else if (opt == jSO_REUSEADDR) {
	if (setsockopt(fd, SOL_SOCKET, SO_REUSEADDR,&optval,sizeof(optval))==0)
	    return;
    } else if (opt == jIP_MULTICAST_IF) {
	struct in_addr sa;
	sa.s_addr = htonl( (*env)->GetIntField(env, value, IA_addrID) );
	if (setsockopt(fd, SOL_IP, IP_MULTICAST_IF,&sa,sizeof(sa))==0)
	    return;
    } else if (opt == jSO_LINGER) {
	// move from Integer to struct linger
        memset(&li, 0, sizeof(struct linger));
	if (on) {
	    jmethodID mid = (*env)->GetMethodID(env, cls, "intValue", "()I");
	    li.l_linger = (*env)->CallIntMethod(env, value, mid);
	    li.l_onoff = 1;
	}
	if (setsockopt(fd, SOL_SOCKET, SO_LINGER, &li, sizeof(li))==0)
	    return;
    } else { /* unknown option */
	throwSE(env, "Unknown socket option.");
	return;
    }
    /* error in setsockopt */
    throwSE(env, strerror(errno));
    return;
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
    struct linger li;
    int fd, optval, optlen;
    jobject fdObj;
    assert(inited);
    fdObj = (*env)->GetObjectField(env, _this, SI_fdObjID);
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);
    if (opt == jTCP_NODELAY) {
	optlen = sizeof(optval);
	if (getsockopt(fd, SOL_TCP, TCP_NODELAY, &optval, &optlen) == 0)
	    return optval ? 1 : -1;
    } else if (opt == jSO_LINGER) {
	memset(&li, 0, sizeof(li));
	optlen = sizeof(li);
	if (getsockopt(fd, SOL_SOCKET, SO_LINGER, &li, &optlen) == 0)
	    return li.l_onoff ? li.l_linger : -1;
    } else {
	throwSE(env, "Unknown socket option.");
	return 0;
    }
    /* error in getsockopt */
    throwSE(env, strerror(errno));
    return 0;
}
