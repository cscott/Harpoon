#include <assert.h>
#include <string.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Method.h"
#include "reflect-util.h"

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

#if !defined(WITHOUT_HACKED_REFLECTION) /* this is our hacked implementation */
/*
 * Class:     java_lang_reflect_Method
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Method_getDeclaringClass
  (JNIEnv *env, jobject _this) {
    return FNI_WRAP(FNI_GetMethodInfo(_this)->declaring_class_object);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Method_getName
  (JNIEnv *env, jobject _this) {
    return (*env)->NewStringUTF(env, FNI_GetMethodInfo(_this)->methodID->name);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getReturnType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Method_getReturnType
  (JNIEnv *env, jobject _this) {
    char *desc = FNI_GetMethodInfo(_this)->methodID->desc;
    assert(*desc=='('); /* method descriptors start with lparen */
    desc = strchr(desc, ')');
    assert(desc!=NULL); /* all method descriptors have a matched paren set */
    return REFLECT_parseDescriptor(env, desc+1);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getParameterTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Method_getParameterTypes
  (JNIEnv *env, jobject _this) {
    jclass clscls = (*env)->FindClass(env, "java/lang/Class");
    jobjectArray result;
    char *desc = FNI_GetMethodInfo(_this)->methodID->desc;
    char *sigptr;
    int nparams=0;
    assert(*desc=='(');
    /* count number of parameters */
    for (sigptr=desc+1; sigptr!=NULL && *sigptr!=')';
	 sigptr=REFLECT_advanceDescriptor(sigptr))
	nparams++;
    /* create array of proper length */
    result = (*env)->NewObjectArray(env, nparams, clscls, NULL);
    /* fill array with parameters */
    nparams=0;
    for (sigptr=desc+1; sigptr!=NULL && *sigptr!=')';
	 sigptr=REFLECT_advanceDescriptor(sigptr))
	(*env)->SetObjectArrayElement
	    (env, result, nparams++, REFLECT_parseDescriptor(env, sigptr));
    /* done */
    return result;
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getExceptionTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Method_getExceptionTypes
  (JNIEnv *env, jobject _this) {
  assert(0);
}

#endif /* !WITHOUT_HACKED_REFLECTION */
