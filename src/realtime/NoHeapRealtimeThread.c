#include "NoHeapRealtimeThread.h"
#include <config.h>

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
#ifdef WITH_REALTIME_THREADS_DEBUG
  fflush(NULL); 
#endif
}

JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__Ljava_lang_String_2_00024_00024initcheck
(JNIEnv* env, jclass noHeapRealtimeThread, jstring str) {
#ifdef RTJ_DEBUG
  printf("\nNoHeapRealtimeThread.print_initcheck");
#endif
  Java_javax_realtime_NoHeapRealtimeThread_print__Ljava_lang_String_2(env, noHeapRealtimeThread, str);
}

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (D)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__D
(JNIEnv* env, jclass noHeapRealtimeThread, jdouble d) {
  printf("%f", d);
#ifdef WITH_REALTIME_THREADS_DEBUG
  fflush(NULL);
#endif
}

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__I
(JNIEnv* env, jclass noHeapRealtimeThread, jint n) {
  printf("%d", n);
#ifdef WITH_REALTIME_THREADS_DEBUG
  fflush(NULL);
#endif
}

/*
 * Class:     javax_realtime_NoHeapRealtimeThread
 * Method:    print
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_NoHeapRealtimeThread_print__J
(JNIEnv* env, jclass noHeapRealtimeThread, jlong l) {
  printf("%lld", l);
#ifdef WITH_REALTIME_THREADS_DEBUG
  fflush(NULL);
#endif
}

