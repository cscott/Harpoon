#include "config.h"
#ifdef WITH_HASHLOCK_SHRINK
# define GC_I_HIDE_POINTERS /* we need HIDE_POINTER from gc.h */
#endif /* WITH_HASHLOCK_SHRINK */

#include <jni.h>
#include <jni-private.h>
#include "java_lang_System.h"
#include "flexthread.h" /* for arrcls lock in arraycopy */
#include <sys/time.h> /* gettimeofday */
#include <time.h> /* gettimeofday */
#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif

#include "../../java.lang/system.h" /* useful library-indep implementations */
#include "../../java.lang/properties.h" /* same, for setting up properties */

/*
 * Class:     java_lang_System
 * Method:    setIn0
 * Signature: (Ljava/io/InputStream;)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_setIn0
  (JNIEnv *env, jclass syscls, jobject in) {
    jfieldID fid = (*env)->GetStaticFieldID(env, syscls, "in",
					    "Ljava/io/InputStream;");
    (*env)->SetStaticObjectField(env, syscls, fid, in);
}

/*
 * Class:     java_lang_System
 * Method:    setOut0
 * Signature: (Ljava/io/PrintStream;)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_setOut0
  (JNIEnv *env, jclass syscls, jobject out) {
    jfieldID fid = (*env)->GetStaticFieldID(env, syscls, "out",
					    "Ljava/io/PrintStream;");
    (*env)->SetStaticObjectField(env, syscls, fid, out);
}

/*
 * Class:     java_lang_System
 * Method:    setErr0
 * Signature: (Ljava/io/PrintStream;)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_setErr0
  (JNIEnv *env, jclass syscls, jobject err) {
    jfieldID fid = (*env)->GetStaticFieldID(env, syscls, "err",
					    "Ljava/io/PrintStream;");
    (*env)->SetStaticObjectField(env, syscls, fid, err);
}

/*
 * Class:     java_lang_System
 * Method:    currentTimeMillis
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_System_currentTimeMillis
(JNIEnv *env, jclass clazz) {
  struct timeval tv; jlong retval;
  gettimeofday(&tv, NULL);
  retval = tv.tv_sec; /* seconds */
  retval*=1000; /* milliseconds */
  retval+= (tv.tv_usec/1000); /* adjust milliseconds & add them in */
  return retval;
}


#ifdef WITH_TRANSACTIONS
 /* transactions has its own versions of arraycopy */
#else /* !WITH_TRANSACTIONS */
/*
 * Class:     java_lang_System
 * Method:    arraycopy
 * Signature: (Ljava/lang/Object;ILjava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_arraycopy
  (JNIEnv *env, jclass syscls,
   jobject src, jint srcpos, jobject dst, jint dstpos,
   jint length) {
  fni_system_arraycopy(env, syscls, src, srcpos, dst, dstpos, length);
}
#endif /* !WITH_TRANSACTIONS */

/*
 * Class:     java_lang_System
 * Method:    identityHashCode
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_System_identityHashCode
  (JNIEnv *env, jclass cls, jobject obj) {
  return fni_system_identityHashCode(env, cls, obj);
}

/*
 * Class:     java_lang_System
 * Method:    initProperties
 * Signature: (Ljava/util/Properties;)Ljava/util/Properties;
 */
JNIEXPORT jobject JNICALL Java_java_lang_System_initProperties
  (JNIEnv *env, jclass syscls, jobject propobj) {
  fni_properties_init(env, propobj, JNI_FALSE);
  return propobj;
}
