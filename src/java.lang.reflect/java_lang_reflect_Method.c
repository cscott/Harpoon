#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Method.h"

/*
 * Class:     java_lang_reflect_Method
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Method_getModifiers
  (JNIEnv *env, jobject _this) {
    return FNI_GetMethodInfo(_this)->modifiers;
}

#if 0
/*
 * Class:     java_lang_reflect_Method
 * Method:    invoke
 * Signature: (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Method_invoke
  (JNIEnv *, jobject, jobject, jobjectArray);
#endif
