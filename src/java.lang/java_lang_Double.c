#include <jni.h>
#include "java_lang_Double.h"

#include <stdlib.h>

union longdouble { jlong l; jdouble d; };

/*
 * Class:     java_lang_Double
 * Method:    doubleToLongBits
 * Signature: (D)J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Double_doubleToLongBits
  (JNIEnv *env, jclass clsDouble, jdouble d) {
    union longdouble u;
    u.d = d;
    return u.l;
}

/*
 * Class:     java_lang_Double
 * Method:    longBitsToDouble
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Double_longBitsToDouble
  (JNIEnv *env, jclass clsDouble, jlong l) {
    union longdouble u;
    u.l = l;
    return u.d;
}

/*
 * Class:     java_lang_Double
 * Method:    valueOf0
 * Signature: (Ljava/lang/String;)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Double_valueOf0
  (JNIEnv *env, jclass clsDouble, jstring str) {
    const char *cstr = (*env)->GetStringUTFChars(env, str, NULL);
    const char *endptr;
    jdouble d = strtod(cstr, &endptr);
    (*env)->ReleaseStringUTFChars(env, str, cstr);
    if (endptr==cstr) {
      jclass excls = (*env)->FindClass(env, "java/lang/NumberFormatException");
      (*env)->ThrowNew(env, excls, cstr);
    }
    return d;
}
