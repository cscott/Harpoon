#ifndef __java_lang_Console__
#define __java_lang_Console__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT void JNICALL Java_java_lang_Console_print (JNIEnv *env, jclass, jstring);
JNIEXPORT void JNICALL Java_java_lang_Console_println (JNIEnv *env, jclass);

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_Console__ */
