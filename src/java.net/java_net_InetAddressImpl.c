/* Implementation for class java_net_InetAddressImpl */
#include <jni.h>
#include "java_net_InetAddressImpl.h"

#include <assert.h>

/*
 * Class:     java_net_InetAddressImpl
 * Method:    getLocalHostName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_net_InetAddressImpl_getLocalHostName
  (JNIEnv *env, jobject _this) {
  assert(0);
}

/*
 * Class:     java_net_InetAddressImpl
 * Method:    makeAnyLocalAddress
 * Signature: (Ljava/net/InetAddress;)V
 */
JNIEXPORT void JNICALL Java_java_net_InetAddressImpl_makeAnyLocalAddress
  (JNIEnv *env, jobject _this, jobject inetAddress) {
  assert(0);
}

/*
 * Class:     java_net_InetAddressImpl
 * Method:    lookupAllHostAddr
 * Signature: (Ljava/lang/String;)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_java_net_InetAddressImpl_lookupAllHostAddr
  (JNIEnv *env, jobject _this, jstring unknown) {
  assert(0);
}

/*
 * Class:     java_net_InetAddressImpl
 * Method:    getHostByAddr
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_net_InetAddressImpl_getHostByAddr
  (JNIEnv *env, jobject _this, jint addr) {
  assert(0);
}

/*
 * Class:     java_net_InetAddressImpl
 * Method:    getInetFamily
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_net_InetAddressImpl_getInetFamily
  (JNIEnv *env, jobject _this) {
  assert(0);
}
