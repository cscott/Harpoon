#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_VMSystem.h"

#include <assert.h>
#include "../../java.lang/system.h" /* useful library-indep implementations */

/*
 * Class:     java_lang_VMSystem
 * Method:    arraycopy
 * Signature: (Ljava/lang/Object;ILjava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMSystem_arraycopy
  (JNIEnv *env, jclass syscls,
   jobject src, jint srcpos, jobject dst, jint dstpos, jint length) {
#ifdef WITH_TRANSACTIONS
  assert(0); /* transactions has its own version of arraycopy */
#endif
  fni_system_arraycopy(env, syscls, src, srcpos, dst, dstpos, length);
}

/*
 * Class:     java_lang_VMSystem
 * Method:    identityHashCode
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_VMSystem_identityHashCode
  (JNIEnv *env, jclass cls, jobject obj) {
  return fni_system_identityHashCode(env, cls, obj);
}
