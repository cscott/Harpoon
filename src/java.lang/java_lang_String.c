#include <jni.h>
#include "java_lang_String.h"

#include <assert.h>

/*
 * Class:     java_lang_String
 * Method:    intern
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_String_intern
  (JNIEnv *env, jobject str) {
    assert(0);
}
