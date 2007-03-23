#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_VMMain.h"

#include <assert.h>
#include <stdlib.h>
#define CHECK_EXCEPTIONS(env) \
if ((*env)->ExceptionOccurred(env)){ (*env)->ExceptionDescribe(env); exit(1); }

JNIEXPORT void JNICALL 
Java_java_lang_VMMain_invokeMain (JNIEnv *env, jclass thrcls,
				  jobjectArray args) {
    jclass cls;
    jmethodID mid;

    /* Execute main() method. */
    cls = (*env)->FindClass(env, FNI_javamain);
    CHECK_EXCEPTIONS(env);
    mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallStaticVoidMethod(env, cls, mid, args);
}

JNIEXPORT void JNICALL 
Java_java_lang_VMMain_invokeMain_00024_00024initcheck(JNIEnv *env,
						      jclass thrcls,
						      jobjectArray args) {
    assert(0); /* impossible to reach this point */
}
