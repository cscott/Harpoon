/* Implementation for class java_net_InetAddressImpl */
#include "config.h" /* for WITH_TRANSACTIONS */
#include <jni.h>
#include "java_net_InetAddressImpl.h"

#include <assert.h>
#include <netdb.h> /* for gethostbyaddr and struct hostent */
#include <netinet/in.h> /* for INADDR_ANY */
#include <sys/socket.h> /* for AF_INET */
#define __USE_BSD /* required to get glibc to define gethostname */
#include <unistd.h> /* for gethostname */

/** IMPLEMENTATION OF AF_INET internet addresses */

static void throwUHException(JNIEnv *env, const char *msg) {
    jclass exc = (*env)->FindClass(env, "java/net/UnknownHostException");
    if (!(*env)->ExceptionOccurred(env))
	(*env)->ThrowNew(env, exc, msg);
}

/*
 * Class:     java_net_InetAddressImpl
 * Method:    getLocalHostName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_net_InetAddressImpl_getLocalHostName
  (JNIEnv *env, jobject _this) {
    int size=32;
    while (size < 2048) {
	char buf[size*=2];
	if ( gethostname(buf, size) == 0 )
	    return (*env)->NewStringUTF(env, buf);
    }
    // hmm.  some sort of error.
    throwUHException(env, "can't look up local host!");
    return NULL;
}

/*
 * Class:     java_net_InetAddressImpl
 * Method:    makeAnyLocalAddress
 * Signature: (Ljava/net/InetAddress;)V
 */
JNIEXPORT void JNICALL Java_java_net_InetAddressImpl_makeAnyLocalAddress
  (JNIEnv *env, jobject _this, jobject inetAddress) {
    jclass clzz = (*env)->GetObjectClass(env, inetAddress);
    jfieldID addrID = (*env)->GetFieldID(env, clzz, "address", "I");
    (*env)->SetIntField(env, inetAddress, addrID, INADDR_ANY);
}

/*
 * Class:     java_net_InetAddressImpl
 * Method:    lookupAllHostAddr
 * Signature: (Ljava/lang/String;)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_java_net_InetAddressImpl_lookupAllHostAddr
  (JNIEnv *env, jobject _this, jstring name) {
    const char *cname; struct hostent *h;
    jclass baclazz; jobjectArray baarray;
    int n;
    // first call gethostbyname with UTF-string arg.
    cname = (*env)->GetStringUTFChars(env, name, NULL);
    h = gethostbyname(cname);
    (*env)->ReleaseStringUTFChars(env, name, cname);
    // return array of (4-byte arrays)
    // first count # of addresses
    for (n=0; h->h_addr_list[n]; n++) /* do nothing */ ;
    // now make byte[][] of appropriate length;
    baclazz = (*env)->FindClass(env, "[B");
    baarray = (*env)->NewObjectArray(env, n, baclazz, NULL);
    // for each address...
    for (n=0; h->h_addr_list[n]; n++) {
	jbyteArray addr = (*env)->NewByteArray(env, h->h_length);
	(*env)->SetByteArrayRegion(env, addr, 0, h->h_length,
				   h->h_addr_list[n]);
	(*env)->SetObjectArrayElement(env, baarray, n, addr);
    }
    // yay, done.
    return baarray;
}
#ifdef WITH_TRANSACTIONS
#include "../../transact/transact.h"
JNIEXPORT jobjectArray JNICALL Java_java_net_InetAddressImpl_lookupAllHostAddr_00024_00024withtrans
  (JNIEnv *env, jobject _this, jobject commitrec, jstring name) {
  return Java_java_net_InetAddressImpl_lookupAllHostAddr
    (env, _this, FNI_StrTrans2Str(env, commitrec, name));
}
#endif /* WITH_TRANSACTIONS */

/*
 * Class:     java_net_InetAddressImpl
 * Method:    getHostByAddr
 * Signature: (I)Ljava/lang/String;
 */
/* addr is address, in local byte order. */
JNIEXPORT jstring JNICALL Java_java_net_InetAddressImpl_getHostByAddr
  (JNIEnv *env, jobject _this, jint address) {
  jstring result;
  char addr[] = { (address >> 24) & 0xFF,
		  (address >> 16) & 0xFF,
		  (address >>  8) & 0xFF,
		  (address >>  0) & 0xFF };
  struct hostent *h = gethostbyaddr(addr, sizeof(addr), AF_INET);
  if (h!=NULL) return (*env)->NewStringUTF(env, h->h_name);
  // else can't find host...
  throwUHException(env, "gethostbyaddr returned NULL");
  return NULL;
}

/*
 * Class:     java_net_InetAddressImpl
 * Method:    getInetFamily
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_net_InetAddressImpl_getInetFamily
  (JNIEnv *env, jobject _this) {
  return AF_INET; /* ARPA Internet protocols */
}
