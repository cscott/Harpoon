/* Java Native Interface header file.  C. Scott Ananian. */
/* Implemented from the JNI spec, v 1.1 */

#ifndef INCLUDED_JNI_TYPES_H
#define INCLUDED_JNI_TYPES_H

#include <inttypes.h> /* ISO C99 header */

/* java primitive types and their machine-dependent native equivalents */
typedef  uint8_t  jboolean;
typedef   int8_t  jbyte;
typedef  uint16_t jchar;
typedef   int16_t jshort;
typedef   int32_t jint;
typedef   int64_t jlong;

typedef    float  jfloat;
typedef   double  jdouble;

/* the jsize integer type is used to describe cardinal indices and sizes */
typedef jint jsize;

/* reference types */
struct _jobject; /* opaque structure */
typedef struct _jobject * jobject;
typedef jobject jclass;
typedef jobject jstring;
typedef jobject jarray;
typedef jobject jthrowable;
typedef jarray jobjectArray;
typedef jarray jbooleanArray;
typedef jarray jbyteArray;
typedef jarray jcharArray;
typedef jarray jshortArray;
typedef jarray jintArray;
typedef jarray jlongArray;
typedef jarray jfloatArray;
typedef jarray jdoubleArray;
/* you can be a bit more clever in c++, but i rather dislike c++ */

typedef jobject jweak; /* added in JNI v1.2 */

/* field and method IDs are regular C pointer types. */
struct _jfieldID; /* opaque structure */
typedef struct _jfieldID *jfieldID; /* field IDs */

struct _jmethodID; /* opaque structure */
typedef struct _jmethodID *jmethodID; /* method IDs */

/* the jvalue union type is used as the element type in argument arrays. */
typedef union jvalue {
  jboolean z;
  jbyte    b;
  jchar    c;
  jshort   s;
  jint     i;
  jlong    j;
  jfloat   f;
  jdouble  d;
  jobject  l;
} jvalue;

/* each function is accessible at a fixed offset through the JNIEnv argument.
 * The JNIEnv type is a pointer to a structure storing all JNI function
 * pointers. */
typedef const struct JNINativeInterface *JNIEnv;

/* register natives... */
typedef struct {
  char *name;
  char *signature;
  void *fnPtr;
} JNINativeMethod;

/* we're not actually planning on implementing the Invocation API... */
typedef const struct JNIInvokeInterface *JavaVM;
    
struct JNIInvokeInterface
{
  void * reserved0;
  void * reserved1;
  void * reserved2;

  jint (*DestroyJavaVM)         (JavaVM *vm); /* 3 */
  jint (*AttachCurrentThread)   (JavaVM *vm, void **penv, void *args); /* 4 */
  jint (*DetachCurrentThread)   (JavaVM *vm); /* 5 */
  jint (*GetEnv)                (JavaVM *vm, void **env, jint version); /* 6 (JNI v1.2) */
  jint (*AttachCurrentThreadAsDaemon) (JavaVM *vm, void **penv, void *args); /* 7 (JNI v1.4) */
};

#endif /* INCLUDED_JNI_TYPES_H */
