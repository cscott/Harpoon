/* this file defines some additional methods of java.lang.String needed
 * when building with MZF transformations. */
#include <jni.h>
#include "../java.lang/java_lang_String.h"

/* special mzf version of String.intern() */
JNIEXPORT jstring JNICALL
Java_java_lang_String_00024_00024offset_00024_00024count_00024_00024value_intern
  (JNIEnv *env, jobject str) {
  /* just re-direct to normal version of method */
  return Java_java_lang_String_intern(env, str);
}
