#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Constructor.h"

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Constructor_getModifiers
  (JNIEnv *env, jobject _this) {
    return FNI_GetMethodInfo(_this)->modifiers;
}

#if 0
/*
 * Class:     java_lang_reflect_Constructor
 * Method:    newInstance
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Constructor_newInstance
  (JNIEnv *, jobject, jobjectArray);
#endif
