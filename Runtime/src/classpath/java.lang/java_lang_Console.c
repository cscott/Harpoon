#include "config.h"
#include <jni.h>
#include "java_lang_Console.h"

#include <stdio.h>

/*
 * Class:     java_lang_Console
 * Method:    print
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Console_print
  (JNIEnv *env, jclass cls, jstring str) {
  const char *cstr = (*env)->GetStringUTFChars(env, str, NULL);
  fputs(cstr, stdout);
  (*env)->ReleaseStringUTFChars(env, str, cstr);
}

/*
 * Class:     java_lang_Console
 * Method:    println
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Console_println
  (JNIEnv *env, jclass cls) {
  fputs("\n", stdout);
}
