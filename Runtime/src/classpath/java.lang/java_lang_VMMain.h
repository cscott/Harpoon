#ifndef __java_lang_VMMain__
#define __java_lang_VMMain__

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT void JNICALL Java_java_lang_VMMain_invokeMain (JNIEnv *env, jclass, jobjectArray);

#ifdef __cplusplus
}
#endif

#endif /* __java_lang_VMMain__ */
