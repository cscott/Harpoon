#include "NoHeapRealtimeThread.h"

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (Ljava/lang/String;)V
 */

/* This method should really not be here... only so we can print stuff from
 * NoHeapRealtimeThread's in our Benchmarks... can replace by an appropriate
 * native method in the benchmark, but this was to provide code-reuse.
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__Ljava_lang_String_2
(JNIEnv* env, jclass noHeapRealtimeThread, jstring str) {
  const char* string = (*env)->GetStringUTFChars(env, str, NULL);
  printf(string);
}

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__D
(JNIEnv* env, jclass noHeapRealtimeThread, jdouble d) {
  printf("%f", d);
}

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__I
(JNIEnv* env, jclass noHeapRealtimeThread, jint n) {
  printf("%d", n);
}

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__J
(JNIEnv* env, jclass noHeapRealtimeThread, jlong l) {
  printf("%lld", l);
}

