#include <jni.h>
#include <config.h>
#include "java_lang_Double.h"

#include <stdlib.h>

union longdouble { jlong l; jdouble d; jint i[2]; };

#ifdef LONG_AND_DOUBLE_ARE_REVERSED
#define MAYBESWAP(u) { jint _x; _x=u.i[1]; u.i[1]=u.i[0]; u.i[0]=_x; }
#else
#define MAYBESWAP(u)
#endif

#define DOUBLETOLONGBITS(d) \
({ union longdouble _u; _u.d = d; MAYBESWAP(_u); _u.l; })
#define LONGBITSTODOUBLE(l) \
({ union longdouble _u; _u.l = l; MAYBESWAP(_u); _u.d; })


/*
 * Class:     java_lang_Double
 * Method:    doubleToLongBits
 * Signature: (D)J
 */
JNIEXPORT jlong JNICALL Java_java_lang_Double_doubleToLongBits
  (JNIEnv *env, jclass clsDouble, jdouble d) {
    return DOUBLETOLONGBITS(d);
}

/*
 * Class:     java_lang_Double
 * Method:    longBitsToDouble
 * Signature: (J)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Double_longBitsToDouble
  (JNIEnv *env, jclass clsDouble, jlong l) {
    return LONGBITSTODOUBLE(l);
}

/*
 * Class:     java_lang_Double
 * Method:    valueOf0
 * Signature: (Ljava/lang/String;)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_Double_valueOf0
  (JNIEnv *env, jclass clsDouble, jstring str) {
    const char *cstr = (*env)->GetStringUTFChars(env, str, NULL);
    char *endptr;
    jdouble d = strtod(cstr, &endptr);
    (*env)->ReleaseStringUTFChars(env, str, cstr);
    if (endptr==cstr) {
      jclass excls = (*env)->FindClass(env, "java/lang/NumberFormatException");
      (*env)->ThrowNew(env, excls, cstr);
    }
    return d;
}
#ifdef WITH_TRANSACTIONS
#include "../../transact/transact.h"
JNIEXPORT jdouble JNICALL Java_java_lang_Double_valueOf0_00024_00024withtrans
  (JNIEnv *env, jclass clsDouble, jobject commitrec, jstring str) {
  return Java_java_lang_Double_valueOf0(env, clsDouble,
					FNI_StrTrans2Str(env, commitrec, str));
}
#endif /* WITH_TRANSACTIONS */
