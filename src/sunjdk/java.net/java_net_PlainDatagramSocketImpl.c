/* Implementation for class java_net_PlainDatagramSocketImpl */
#include <jni.h>
#include "java_net_PlainDatagramSocketImpl.h"

#include <assert.h>
#include <errno.h> /* for errno */
#include <netinet/in.h> /* for AF_INET, SOCK_DGRAM, etc */
#include <sys/socket.h> /* for socket(2), send(2) */
#include <string.h> /* for strerror(3) */
#include <unistd.h> /* for close(2) */

#include "flexthread.h" /* for mutex ops */
#include "../java.io/javaio.h" /* for getfd/setfd */

static jfieldID DSI_fdObjID = 0; /* The field ID of DatagramSocketImpl.fd */
static jfieldID DSI_localPortID = 0; /* The field ID of DSocketImpl.localPort*/
static jfieldID DP_bufID = 0; /* The field ID of DatagramPacket.buf */
static jfieldID DP_offsetID = 0; /* The field ID of DatagramPacket.offset */
static jfieldID DP_lengthID = 0; /* The field ID of DatagramPacket.length */
static jfieldID DP_addressID = 0; /* The field ID of DatagramPacket.address */
static jfieldID DP_portID = 0; /* The field ID of DatagramPacket.port */
static jfieldID IA_addrID  = 0; /* The field ID of InetAddress.address */
static jfieldID IA_familyID= 0; /* The field ID of InetAddress.family */
static jmethodID IA_consID  = 0; /* no-arg constructor for InetAddress */
static jclass IACls = 0; /* The java/net/InetAddress class object */
static jclass IOExcCls  = 0; /* The java/io/IOException class object */
static jint jSO_BINDADDR, jSO_REUSEADDR, jSO_LINGER, jSO_TIMEOUT;
static jint jTCP_NODELAY, jIP_MULTICAST_IF;
static int inited = 0; /* whether the above variables have been initialized */
FLEX_MUTEX_DECLARE_STATIC(init_mutex);

static int initializePDSI(JNIEnv *env) {
    jclass PDSICls, DPCls;

    FLEX_MUTEX_LOCK(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;

    PDSICls  = (*env)->FindClass(env, "java/net/PlainDatagramSocketImpl");
    if ((*env)->ExceptionOccurred(env)) goto done;
    DSI_fdObjID = (*env)->GetFieldID(env, PDSICls,
				     "fd","Ljava/io/FileDescriptor;");
    if ((*env)->ExceptionOccurred(env)) goto done;
    DSI_localPortID  = (*env)->GetFieldID(env, PDSICls, "localPort", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    DPCls = (*env)->FindClass(env, "java/net/DatagramPacket");
    if ((*env)->ExceptionOccurred(env)) goto done;
    DP_bufID = (*env)->GetFieldID(env, DPCls, "buf", "[B");
    if ((*env)->ExceptionOccurred(env)) goto done;
    DP_offsetID = (*env)->GetFieldID(env, DPCls, "offset", "I");
    if ((*env)->ExceptionOccurred(env)) {
	/* this field not present before JDK1.2! */
	DP_offsetID=0;
	(*env)->ExceptionClear(env);
    }
    DP_lengthID = (*env)->GetFieldID(env, DPCls, "length", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    DP_addressID = (*env)->GetFieldID(env, DPCls, "address",
				      "Ljava/net/InetAddress;");
    if ((*env)->ExceptionOccurred(env)) goto done;
    DP_portID = (*env)->GetFieldID(env, DPCls, "port", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IACls   = (*env)->FindClass(env, "java/net/InetAddress");
    /* make IACls into a global reference for future use */
    IACls = (*env)->NewGlobalRef(env, IACls);
    if ((*env)->ExceptionOccurred(env)) goto done;
    IA_consID  = (*env)->GetMethodID(env, IACls, "<init>", "()V");
    if ((*env)->ExceptionOccurred(env)) {
      /* not entirely unexpected that this method might not be present. */
      IA_consID = 0;
      (*env)->ExceptionClear(env);
      fprintf(stderr, "WARNING: constructor InetAddress() not found.\n");
    }
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

/* Not used.
static void throwSE(JNIEnv *env, const char * msg) {
    jclass exc = (*env)->FindClass(env, "java/net/SocketException");
    if (!(*env)->ExceptionOccurred(env))
	(*env)->ThrowNew(env, exc, msg);
}
*/

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    init
 * Signature: ()V
 */
/* perform class load-time initialization */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_init
  (JNIEnv *env, jclass _plaindatagramsocketimpl) {
    /* do nothing */
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    datagramSocketCreate
 * Signature: ()V
 */
/* creates a datagram socket */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_datagramSocketCreate
  (JNIEnv *env, jobject _this) {
    jobject fdObj;
    int fd;

    /* If static data has not been loaded, load it now */
    if (!inited && !initializePDSI(env)) return; /* exception occurred; bail */

    fd = socket(AF_INET, SOCK_DGRAM, 0);
    fdObj = (*env)->GetObjectField(env, _this, DSI_fdObjID);
    Java_java_io_FileDescriptor_setfd(env, fdObj, fd);

    /* Check for error condition */
    if (fd==-1)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    datagramSocketClose
 * Signature: ()V
 */
/* close the socket */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_datagramSocketClose
  (JNIEnv *env, jobject _this) {
    int fd, rc;
    jobject fdObj;

    assert(inited && _this);
    fdObj = (*env)->GetObjectField(env, _this, DSI_fdObjID);
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

    rc = close(fd);
    if (rc<0)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    bind
 * Signature: (ILjava/net/InetAddress;)V
 */
/* binds a datagram socket to a local port */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_bind
  (JNIEnv *env, jobject _this, jint lport, jobject laddr/*InetAddress*/) {
    struct sockaddr_in sa;
    jobject fdObj;
    int fd, rc, sa_size;

    assert(inited && _this && laddr);
    memset(&sa, 0, sizeof(sa));
    sa.sin_family      = (*env)->GetIntField(env, laddr, IA_familyID);
    sa.sin_addr.s_addr = htonl((*env)->GetIntField(env, laddr, IA_addrID));
    sa.sin_port        = htons(lport);

    fdObj = (*env)->GetObjectField(env, _this, DSI_fdObjID);
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

    rc = bind(fd, (struct sockaddr *) &sa, sizeof(sa));
    /* Check for error condition */
    if (rc<0) goto error;

    /* update instance variables */
    sa_size = sizeof(sa);
    rc = getsockname(fd, (struct sockaddr *) &sa, &sa_size);
    if (rc<0) goto error;
    (*env)->SetIntField(env, _this, DSI_localPortID, (int)ntohs(sa.sin_port));

    /* done! */
    return;

  error:
    (*env)->ThrowNew(env, IOExcCls, strerror(errno));
    return;
}

#if 0
/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    getTTL
 * Signature: ()B
 */
/* get the TTL (time-to-live) option */
JNIEXPORT jbyte JNICALL Java_java_net_PlainDatagramSocketImpl_getTTL
  (JNIEnv *env, jobject _this) {
    assert(inited && _this);

    assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    getTimeToLive
 * Signature: ()I
 */
/* get the TTL (time-to-live) option */
JNIEXPORT jint JNICALL Java_java_net_PlainDatagramSocketImpl_getTimeToLive
  (JNIEnv *env, jobject _this) {
    assert(inited && _this);

    assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    join
 * Signature: (Ljava/net/InetAddress;)V
 */
/* join the multicast group */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_join
  (JNIEnv *env, jobject _this, jobject inetaddr /*InetAddress*/) {
    assert(inited && _this);

    assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    leave
 * Signature: (Ljava/net/InetAddress;)V
 */
/* leave the multicast group */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_leave
  (JNIEnv *env, jobject _this, jobject inetaddr /*InetAddress*/) {
  assert(0/*unimplemented*/);
}
#endif

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    peek
 * Signature: (Ljava/net/InetAddress;)I
 */
/* peek at the packet to see who it is from. */
/* fills the inetaddress and returns the port number */
JNIEXPORT jint JNICALL Java_java_net_PlainDatagramSocketImpl_peek
  (JNIEnv *env, jobject _this, jobject addrObj/*InetAddress*/) {
  struct sockaddr_in sa;
  int salen = sizeof(sa);
  jobject fdObj;
  char buf[1];
  int fd, rc;

  /* oh, this is ugly */
  assert(inited && addrObj);

  fdObj = (*env)->GetObjectField(env, _this, DSI_fdObjID);
  fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

  do {
    rc = recvfrom(fd, buf, 0, MSG_PEEK, (struct sockaddr *) &sa, &salen);
  } while (rc<0 && errno==EINTR); /* repeat if interrupted */
  
  /* Check for error condition */
  if (rc<0) {
    (*env)->ThrowNew(env, IOExcCls, strerror(errno));
    return 0;
  }

  /* fill in inetaddress/extract port */

  (*env)->SetIntField(env, addrObj, IA_familyID, sa.sin_family);
  (*env)->SetIntField(env, addrObj, IA_addrID, ntohl(sa.sin_addr.s_addr));
  return ntohs(sa.sin_port);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    receive
 * Signature: (Ljava/net/DatagramPacket;)V
 */
/* receive the datagram packet */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_receive
  (JNIEnv *env, jobject _this, jobject p /*DatagramPacket*/) {
  struct sockaddr_in sa;
  int salen = sizeof(sa);
  jobject fdObj, addrObj, bufObj;
  jbyte *bufp;
  jint len, off;
  int fd, rc;

  /* oh, this is ugly */
  assert(inited && p);

  /* get file descriptor */
  fdObj = (*env)->GetObjectField(env, _this, DSI_fdObjID);
  fd = Java_java_io_FileDescriptor_getfd(env, fdObj);

  /* get inetaddress object */
  addrObj = (*env)->GetObjectField(env, p, DP_addressID);
  /* if null, create a new inetaddr object */
  if (addrObj==NULL) {
    if (IA_consID)
      addrObj = (*env)->NewObject(env, IACls, IA_consID);
    else
      addrObj = (*env)->AllocObject(env, IACls); /* RISKY! */
    (*env)->SetObjectField(env, p, DP_addressID, addrObj);
  }
  assert(addrObj);

  /* get message buffer */
  bufObj = (*env)->GetObjectField(env, p, DP_bufID);
  bufp = (*env)->GetByteArrayElements(env, (jbyteArray) bufObj, NULL);
  off = DP_offsetID ? (*env)->GetIntField(env, p, DP_offsetID) : 0;
  len = (*env)->GetIntField(env, p, DP_lengthID);

  do {
    rc = recvfrom
      (fd, bufp+off, len, MSG_WAITALL, (struct sockaddr *) &sa, &salen);
  } while (rc<0 && errno==EINTR); /* repeat if interrupted */
  
  (*env)->ReleaseByteArrayElements(env, (jbyteArray)bufObj, bufp,
				   rc < 0 ? JNI_ABORT : 0);
  /* Check for error condition */
  if (rc<0) {
    (*env)->ThrowNew(env, IOExcCls, strerror(errno));
    return;
  }

  /* fill in inetaddress/extract port */
  (*env)->SetIntField(env, addrObj, IA_familyID, sa.sin_family);
  (*env)->SetIntField(env, addrObj, IA_addrID, ntohl(sa.sin_addr.s_addr));
  (*env)->SetIntField(env, p, DP_portID, ntohs(sa.sin_port));
  /* set length of received datagram */
  (*env)->SetIntField(env, p, DP_lengthID, rc);
  return;
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    send
 * Signature: (Ljava/net/DatagramPacket;)V
 */
/* sends a datagram packet.  the packet contains the data and the
 * destination address to send the packet to. */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_send
  (JNIEnv *env, jobject _this, jobject p/*DatagramPacket*/) {
    struct sockaddr_in sa;
    jobject fdObj, addrObj, bufObj;
    jbyte *bufp;
    jint len, off;
    int fd, rc;

    assert(inited && _this && p);
    /* get file descriptor */
    fdObj = (*env)->GetObjectField(env, _this, DSI_fdObjID);
    fd = Java_java_io_FileDescriptor_getfd(env, fdObj);
    /* get inetaddress and port from datagrampacket */
    addrObj = (*env)->GetObjectField(env, p, DP_addressID);
    memset(&sa, 0, sizeof(sa));
    sa.sin_family      = (*env)->GetIntField(env, addrObj, IA_familyID);
    sa.sin_addr.s_addr = htonl((*env)->GetIntField(env, addrObj, IA_addrID));
    sa.sin_port        = htons((*env)->GetIntField(env, p, DP_portID));
    /* get message buffer */
    bufObj = (*env)->GetObjectField(env, p, DP_bufID);
    bufp = (*env)->GetByteArrayElements(env, (jbyteArray) bufObj, NULL);
    off = DP_offsetID ? (*env)->GetIntField(env, p, DP_offsetID) : 0;
    len = (*env)->GetIntField(env, p, DP_lengthID);
    /* actually send data gram */
    do {
      rc = sendto
	(fd, bufp+off, len, 0/*no flags*/, (struct sockaddr *)&sa, sizeof(sa));
    } while (rc<0 && errno==EINTR); /* repeat if interrupted */

    /* free buffers */
    (*env)->ReleaseByteArrayElements(env, (jbyteArray)bufObj, bufp, JNI_ABORT);
				     
    if (rc<0)
	(*env)->ThrowNew(env, IOExcCls, strerror(errno));
}

#if 0
/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    setTTL
 * Signature: (B)V
 */
/* set the TTL (time-to-live) option */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_setTTL
  (JNIEnv *env, jobject _this, jbyte ttl) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    setTimeToLive
 * Signature: (I)V
 */
/* set the TTL (time-to-live) option */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_setTimeToLive
  (JNIEnv *env, jobject _this, jint ttl) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    socketGetOption
 * Signature: (I)I
 */
/* get option's state - set or not */
JNIEXPORT jint JNICALL Java_java_net_PlainDatagramSocketImpl_socketGetOption
  (JNIEnv *env, jobject _this, jint optID) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    socketSetOption
 * Signature: (ILjava/lang/Object;)V
 */
/* set a value -- since we only support (setting) binary options here,
 * o must be a Boolean */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_socketSetOption
  (JNIEnv *env, jobject _this, jint optID, jobject o) {
  assert(0/*unimplemented*/);
}
#endif
