/* Implementation for class java_net_PlainSocketImpl */
#include <jni.h>
#include <jni-private.h>
#include "java_net_PlainSocketImpl.h"

#include <assert.h>

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
  assert(0);
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
  assert(0);
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
  assert(0);
}
/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketListen
 * Signature: (I)V
 */
 /**
  * Listens, for a specified amount of time, for connections.
  * @param count the amount of time to listen for connections
  */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketListen
  (JNIEnv *env, jobject _this, jint count) {
  assert(0);
}

/*
 * Class:     java_net_PlainSocketImpl
 * Method:    socketAccept
 * Signature: (Ljava/net/SocketImpl;)V
 */
/** Accepts connections. */
JNIEXPORT void JNICALL Java_java_net_PlainSocketImpl_socketAccept
  (JNIEnv *env, jobject _this, jobject/*SocketImpl*/ s) {
  assert(0);
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
  assert(0);
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
