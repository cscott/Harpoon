#include <jni.h>
#include "java_lang_Float.h"

union floatint { jint i; jfloat f; };

/*
 * Class:     java_lang_Float
 * Method:    floatToIntBits
 * Signature: (F)I
 */
JNIEXPORT jint JNICALL Java_java_lang_Float_floatToIntBits
  (JNIEnv *env, jclass clsFloat, jfloat f) {
    union floatint u;
    u.f = f;
    return u.i;
}

/*
 * Class:     java_lang_Float
 * Method:    intBitsToFloat
 * Signature: (I)F
 */
JNIEXPORT jfloat JNICALL Java_java_lang_Float_intBitsToFloat
  (JNIEnv *env, jclass clsFloat, jint i) {
    union floatint u;
    u.i = i;
    return u.f;
}
