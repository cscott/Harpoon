/* Implementation for class java_net_PlainDatagramSocketImpl */
#include <jni.h>
#include "java_net_PlainDatagramSocketImpl.h"

#include <assert.h>
#include <errno.h>

#if 0
/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    bind
 * Signature: (ILjava/net/InetAddress;)V
 */
/* binds a datagram socket to a local port */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_bind
  (JNIEnv *env, jobject _this, jint lport, jobject laddr/*InetAddress*/) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    datagramSocketClose
 * Signature: ()V
 */
/* close the socket */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_datagramSocketClose
  (JNIEnv *env, jobject _this) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    datagramSocketCreate
 * Signature: ()V
 */
/* creates a datagram socket */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_datagramSocketCreate
  (JNIEnv *env, jobject _this) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    getTTL
 * Signature: ()B
 */
/* get the TTL (time-to-live) option */
JNIEXPORT jbyte JNICALL Java_java_net_PlainDatagramSocketImpl_getTTL
  (JNIEnv *env, jobject _this) {
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
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    init
 * Signature: ()V
 */
/* perform class load-time initialization */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_init
  (JNIEnv *env, jclass cls) {
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

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    peek
 * Signature: (Ljava/net/InetAddress;)I
 */
/* peek at the packet to see who it is from. */
JNIEXPORT jint JNICALL Java_java_net_PlainDatagramSocketImpl_peek
  (JNIEnv *env, jobject _this, jobject i/*InetAddress*/) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    receive
 * Signature: (Ljava/net/DatagramPacket;)V
 */
/* receive the datagram packet */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_receive
  (JNIEnv *env, jobject _this, jobject p /*DatagramPacket*/) {
  assert(0/*unimplemented*/);
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
  assert(0/*unimplemented*/);
}

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
