#include <assert.h>
#include <stdio.h>
#include <jni.h>
#include "jni-private.h"
// XXX this is a dependency on the SunJDK implementation of java.lang.Class
#include "../sunjdk/java.lang/java_lang_Class.h" // for Java_java_lang_Class_isArray
#include "java_lang_reflect_Array.h"
#include "reflect-util.h"

/*
 * Class:     java_lang_reflect_Array
 * Method:    getLength
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Array_getLength
  (JNIEnv *env, jclass clazz, jobject arrayobj) {
  if (!arrayobj) {
    jclass excls = (*env)->FindClass(env, "java/lang/NullPointerException");
    (*env)->ThrowNew(env, excls, "null argument to Array.getLength()");
    return 0;
  }
  if (!Java_java_lang_Class_isArray
      (env, (*env)->GetObjectClass(env, arrayobj))) {
    jclass excls = (*env)->FindClass(env,
				     "java/lang/IllegalArgumentException");
    (*env)->ThrowNew(env, excls, "argument to Array.getLength() not array");
    return 0;
  }
  return (*env)->GetArrayLength(env, arrayobj);
}

#if 0
/*
 * Class:     java_lang_reflect_Array
 * Method:    get
 * Signature: (Ljava/lang/Object;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_get
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getBoolean
 * Signature: (Ljava/lang/Object;I)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_reflect_Array_getBoolean
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getByte
 * Signature: (Ljava/lang/Object;I)B
 */
JNIEXPORT jbyte JNICALL Java_java_lang_reflect_Array_getByte
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getChar
 * Signature: (Ljava/lang/Object;I)C
 */
JNIEXPORT jchar JNICALL Java_java_lang_reflect_Array_getChar
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getShort
 * Signature: (Ljava/lang/Object;I)S
 */
JNIEXPORT jshort JNICALL Java_java_lang_reflect_Array_getShort
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getInt
 * Signature: (Ljava/lang/Object;I)I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Array_getInt
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getLong
 * Signature: (Ljava/lang/Object;I)J
 */
JNIEXPORT jlong JNICALL Java_java_lang_reflect_Array_getLong
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getFloat
 * Signature: (Ljava/lang/Object;I)F
 */
JNIEXPORT jfloat JNICALL Java_java_lang_reflect_Array_getFloat
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    getDouble
 * Signature: (Ljava/lang/Object;I)D
 */
JNIEXPORT jdouble JNICALL Java_java_lang_reflect_Array_getDouble
  (JNIEnv *, jclass, jobject, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    set
 * Signature: (Ljava/lang/Object;ILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_set
  (JNIEnv *, jclass, jobject, jint, jobject);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setBoolean
 * Signature: (Ljava/lang/Object;IZ)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setBoolean
  (JNIEnv *, jclass, jobject, jint, jboolean);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setByte
 * Signature: (Ljava/lang/Object;IB)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setByte
  (JNIEnv *, jclass, jobject, jint, jbyte);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setChar
 * Signature: (Ljava/lang/Object;IC)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setChar
  (JNIEnv *, jclass, jobject, jint, jchar);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setShort
 * Signature: (Ljava/lang/Object;IS)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setShort
  (JNIEnv *, jclass, jobject, jint, jshort);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setInt
 * Signature: (Ljava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setInt
  (JNIEnv *, jclass, jobject, jint, jint);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setLong
 * Signature: (Ljava/lang/Object;IJ)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setLong
  (JNIEnv *, jclass, jobject, jint, jlong);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setFloat
 * Signature: (Ljava/lang/Object;IF)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setFloat
  (JNIEnv *, jclass, jobject, jint, jfloat);

/*
 * Class:     java_lang_reflect_Array
 * Method:    setDouble
 * Signature: (Ljava/lang/Object;ID)V
 */
JNIEXPORT void JNICALL Java_java_lang_reflect_Array_setDouble
  (JNIEnv *, jclass, jobject, jint, jdouble);
#endif /* 0 */

/*
 * Class:     java_lang_reflect_Array
 * Method:    newArray
 * Signature: (Ljava/lang/Class;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_newArray
  (JNIEnv *env, jclass cls, jclass componentcls, jint length) {
  switch(REFLECT_getDescriptor(env, componentcls)) {
  case 'L':
  case '[':
    return (*env)->NewObjectArray(env, length, componentcls, NULL);
#define TYPECASE(typename,shortname,longname,descchar,fieldname)\
  case descchar:\
    return (*env)->New##shortname##Array(env, length);
  FORPRIMITIVETYPESX(TYPECASE)
#undef TYPECASE
  default: assert(0);
  }
}

/* recursive helper */
static jobject __multiNewArray(JNIEnv *env, jclass cls, jclass componentcls,
			       jint dims[], jsize dimLength) {
  jclass arraycls; jobjectArray result; int i;
  if (dimLength==1)
    return Java_java_lang_reflect_Array_newArray
      (env, cls, componentcls, dims[0]);
  else {
    /* more than one dimension */
    struct FNI_classinfo *info = FNI_GetClassInfo(componentcls);
    char buf[strlen(info->name)+dimLength+2], *p;
    /* create descriptor for desired component class */
    for (i=1, p=buf; i<dimLength; i++, p++)
      *p='[';
    *p=REFLECT_getDescriptor(env, componentcls);
    if (*p=='L')
      sprintf(p, "L%s;", info->name);
    else if (*p=='[')
      sprintf(p, "%s", info->name);
    else *(p+1)='\0';
    /* create component class */
    arraycls=(*env)->FindClass(env, buf);
    if ((*env)->ExceptionOccurred(env)) return NULL;
  } /* don't need buf any more */
  /* create result array */
  result=(*env)->NewObjectArray(env, dims[0], arraycls, NULL);
  /* initialize elements of result array */
  for (i=0; i<dims[0]; i++) {
    jobject el = __multiNewArray(env, cls, componentcls, dims+1, dimLength-1);
    if ((*env)->ExceptionOccurred(env)) return NULL;
    (*env)->SetObjectArrayElement(env, result, i, el);
    assert(!(*env)->ExceptionOccurred(env));
  }
  /* done */
  return result;
}

/*
 * Class:     java_lang_reflect_Array
 * Method:    multiNewArray
 * Signature: (Ljava/lang/Class;[I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Array_multiNewArray
  (JNIEnv *env, jclass cls, jclass componentcls, jintArray dims) {
  jsize dimLength = (*env)->GetArrayLength(env, dims);
  jint *dimElements;
  jobject result;
  /* argument checking */
  if (componentcls==NULL || dims==NULL) {
    jclass excls = (*env)->FindClass(env, "java/lang/NullPointerException");
    (*env)->ThrowNew(env, excls, "null argument to Array.newInstance()");
    return NULL;
  }
  if (dimLength>255 || dimLength<1) {
    jclass excls = (*env)->FindClass(env,
				     "java/lang/IllegalArgumentException");
    (*env)->ThrowNew(env, excls, "invalid number of dimensions");
    return NULL;
  }
  dimElements = (*env)->GetIntArrayElements(env, dims, NULL);
  result = __multiNewArray(env, cls, componentcls, dimElements, dimLength);
  (*env)->ReleaseIntArrayElements(env, dims, dimElements, JNI_ABORT);
  return result;
}
