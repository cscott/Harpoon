#include <jni.h>
#include "java_lang_Math.h"

#include <math.h>
/*
 * Class:     java_lang_Math
 * Method:    sin
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_sin
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return sin(x);
}

/*
 * Class:     java_lang_Math
 * Method:    cos
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_cos
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return cos(x);
}

/*
 * Class:     java_lang_Math
 * Method:    tan
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_tan
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return tan(x);
}

/*
 * Class:     java_lang_Math
 * Method:    asin
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_asin
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return asin(x);
}

/*
 * Class:     java_lang_Math
 * Method:    acos
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_acos
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return acos(x);
}

/*
 * Class:     java_lang_Math
 * Method:    atan
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_atan
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return atan(x);
}

/*
 * Class:     java_lang_Math
 * Method:    exp
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_exp
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return exp(x);
}

/*
 * Class:     java_lang_Math
 * Method:    log
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_log
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return log(x);
}

/*
 * Class:     java_lang_Math
 * Method:    sqrt
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_sqrt
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return sqrt(x);
}

/*
 * Class:     java_lang_Math
 * Method:    IEEEremainder
 * Signature: (DD)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_IEEEremainder
  (JNIEnv *env, jclass clsMath, jdouble x, jdouble y) {
    return drem(x, y);
}

/*
 * Class:     java_lang_Math
 * Method:    ceil
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_ceil
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return ceil(x);
}

/*
 * Class:     java_lang_Math
 * Method:    floor
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_floor
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return floor(x);
}

/*
 * Class:     java_lang_Math
 * Method:    rint
 * Signature: (D)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_rint
  (JNIEnv *env, jclass clsMath, jdouble x) {
    return rint(x);
}

/*
 * Class:     java_lang_Math
 * Method:    atan2
 * Signature: (DD)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_atan2
  (JNIEnv *env, jclass clsMath, jdouble y, jdouble x) {
    return atan2(y, x);
}

/*
 * Class:     java_lang_Math
 * Method:    pow
 * Signature: (DD)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Math_pow
  (JNIEnv *env, jclass clsMath, jdouble x, jdouble y) {
    return pow(x, y);
}
