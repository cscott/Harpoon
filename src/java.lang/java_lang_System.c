#include "java_lang_System.h"
#include <sys/time.h>
#include <unistd.h>

/*
 * Class:     java_lang_System
 * Method:    currentTimeMillis
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_System_currentTimeMillis
(JNIEnv *env, jclass clazz) {
  struct timeval tv; struct timezone tz; jlong retval;
  gettimeofday(&tv, &tz);
  retval = tv.tv_sec; /* seconds */
  retval*=1000; /* milliseconds */
  retval+= (tv.tv_usec/1000); /* adjust milliseconds & add them in */
  return retval;
}
