/* Java Native Interface header file.  C. Scott Ananian. */
/* Implemented from the JNI spec, v 1.1 */

#ifndef INCLUDED_JNI_FUNC_H
#define INCLUDED_JNI_FUNC_H

#include <stdarg.h>

/* convenience macros. */
#define FORPRIMITIVETYPES(what) \
	what(Boolean, jboolean) \
	what(Byte, jbyte) \
	what(Char, jchar) \
	what(Short, jshort) \
	what(Int, jint) \
	what(Long, jlong) \
	what(Float, jfloat) \
	what(Double, jdouble)
#define FORNONVOIDTYPES(what) \
	what(Object, jobject) \
	FORPRIMITIVETYPES(what)
#define FORALLTYPES(what) \
	FORNONVOIDTYPES(what) \
	what(Void, void)
#define FORALLVARARGS(what) \
     	what(,...) what(V, va_list args) what(A, jvalue *args)

/** Function prototypes. **/

/* each function is accessible at a fixed offset through the JNIEnv argument.
 * The JNIEnv type is a pointer to a structure storing all JNI function
 * pointers. */
struct JNINativeInterface {
  void *reserved1, *reserved2, *reserved3, *reserved4;

  /* version information */
  jint (*GetVersion) (JNIEnv *env);

  /* class operations */
  jclass (*DefineClass) (JNIEnv *env, jobject loader,
			 const jbyte *buf, jsize bufLen);
  jclass (*FindClass) (JNIEnv *env, const char *name);
  jmethodID (*FromReflectedMethod) (JNIEnv *env, jobject method); /* 7 (JNI 1.2) */
  jfieldID (*FromReflectedField) (JNIEnv *env, jobject field); /* 8 (JNI 1.2) */
  jobject (*ToReflectedMethod) (JNIEnv *env, jclass cls, jmethodID methodID); /* 9 (JNI 1.2) */
  jclass (*GetSuperclass) (JNIEnv *env, jclass clazz);
  jboolean (*IsAssignableFrom) (JNIEnv *env, jclass clazz1, jclass clazz2);
  jobject (*ToReflectedField) (JNIEnv *env, jclass cls, jfieldID fieldID); /* 12 (JNI 1.2) */

  /* exceptions */
  jint (*Throw) (JNIEnv *env, jthrowable obj);
  jint (*ThrowNew) (JNIEnv *env, jclass clazz, const char *message);
  jthrowable (*ExceptionOccurred) (JNIEnv *env);
  void (*ExceptionDescribe) (JNIEnv *env);
  void (*ExceptionClear) (JNIEnv *env);
  void (*FatalError) (JNIEnv *env, const char *msg);

  /* global and local references */
  jint (*PushLocalFrame) (JNIEnv *env, jint capacity); /* 19 (JNI 1.2) */
  jobject (*PopLocalFrame) (JNIEnv *env, jobject result); /* 20 (JNI 1.2) */
  jobject (*NewGlobalRef) (JNIEnv *env, jobject obj);
  void (*DeleteGlobalRef) (JNIEnv *env, jobject globalRef);
  void (*DeleteLocalRef) (JNIEnv *env, jobject localRef);
  jboolean (*IsSameObject) (JNIEnv *env, jobject ref1, jobject ref2);
  jobject (*NewLocalRef) (JNIEnv *env, jobject ref); /* 25 (JNI 1.2) */
  jint (*EnsureLocalCapacity) (JNIEnv *env, jint capacity); /* 26 (JNI 1.2) */

  /* Object Operations */
  jobject (*AllocObject) (JNIEnv *env, jclass clazz);
# define NEWOBJECTPROTO(suffix, argtype) \
  jobject (*NewObject##suffix) (JNIEnv *env, jclass clazz, \
				jmethodID methodID, argtype);
  FORALLVARARGS(NEWOBJECTPROTO);
# undef NEWOBJECTPROTO
  jclass (*GetObjectClass) (JNIEnv *env, jobject obj);
  jboolean (*IsInstanceOf) (JNIEnv *env, jobject obj, jclass clazz);

  /* Calling instance methods */
  jmethodID (*GetMethodID) (JNIEnv *env, jclass clazz,
			    const char *name, const char *sig);
# define CALLMETHODPROTO(name, type) \
  type (*Call##name##Method) (JNIEnv *env, jobject obj, jmethodID methodID,\
			      ...); \
  type (*Call##name##MethodV) (JNIEnv *env, jobject obj, jmethodID methodID,\
			       va_list args); \
  type (*Call##name##MethodA) (JNIEnv *env, jobject obj, jmethodID methodID,\
			       jvalue *args);
# define CALLNONVIRTUALPROTO(name, type) \
  type (*CallNonvirtual##name##Method) (JNIEnv *env, jobject obj, \
					jclass clazz, jmethodID methodID, \
					...); \
  type (*CallNonvirtual##name##MethodV) (JNIEnv *env, jobject obj, \
					 jclass clazz, jmethodID methodID, \
					 va_list args); \
  type (*CallNonvirtual##name##MethodA) (JNIEnv *env, jobject obj, \
					 jclass clazz, jmethodID methodID, \
					 jvalue *args);
  FORALLTYPES(CALLMETHODPROTO);
  FORALLTYPES(CALLNONVIRTUALPROTO);
# undef CALLMETHODPROTO
# undef CALLNONVIRTUALPROTO

  /* Accessing fields of objects */
  jfieldID (*GetFieldID) (JNIEnv *env, jclass clazz,
			  const char *name, const char *sig);
# define GETFIELDPROTO(name, type) \
  type (*Get##name##Field) (JNIEnv *env, jobject obj, jfieldID fieldID);
# define SETFIELDPROTO(name, type) \
  void (*Set##name##Field) (JNIEnv *env, jobject obj, jfieldID fieldID, \
			    type value);
  FORNONVOIDTYPES(GETFIELDPROTO);
  FORNONVOIDTYPES(SETFIELDPROTO);
# undef GETFIELDPROTO
# undef SETFIELDPROTO

  /* Calling static methods */
  jmethodID (*GetStaticMethodID) (JNIEnv *env, jclass clazz,
				  const char *name, const char *sig);
# define CALLSTATICPROTO(name, type) \
  type (*CallStatic##name##Method) (JNIEnv *env, jclass clazz, \
				    jmethodID methodID, ...); \
  type (*CallStatic##name##MethodV) (JNIEnv *env, jclass clazz, \
				     jmethodID methodID, va_list args); \
  type (*CallStatic##name##MethodA) (JNIEnv *env, jclass clazz, \
				     jmethodID methodID, jvalue *args);
  FORALLTYPES(CALLSTATICPROTO);
# undef CALLSTATICPROTO

  /* Accessing static fields */
  jfieldID (*GetStaticFieldID) (JNIEnv *env, jclass clazz,
				const char *name, const char *sig);
# define GETSTATICFIELDPROTO(name, type) \
  type (*GetStatic##name##Field) (JNIEnv *env, jclass clazz, \
				  jfieldID fieldID);
# define SETSTATICFIELDPROTO(name, type) \
  void (*SetStatic##name##Field) (JNIEnv *env, jclass clazz, \
				  jfieldID fieldID, type value);
  FORNONVOIDTYPES(GETSTATICFIELDPROTO);
  FORNONVOIDTYPES(SETSTATICFIELDPROTO);
# undef GETSTATICFIELDPROTO
# undef SETSTATICFIELDPROTO

  /* String operations */
  jstring (*NewString) (JNIEnv *env, const jchar *unicodeChars, jsize len);
  jsize (*GetStringLength) (JNIEnv *env, jstring string);
  const jchar* (*GetStringChars) (JNIEnv *env, jstring string,
				  jboolean *isCopy);
  void (*ReleaseStringChars) (JNIEnv *env, jstring string, const jchar *chars);
  jstring (*NewStringUTF) (JNIEnv *env, const char *bytes);
  jsize (*GetStringUTFLength) (JNIEnv *env, jstring string);
  const char* (*GetStringUTFChars) (JNIEnv *env, jstring string,
				    jboolean *isCopy);
  void (*ReleaseStringUTFChars) (JNIEnv *env, jstring string, const char *utf);

  /* Array Operations */
  jsize (*GetArrayLength) (JNIEnv *env, jarray array);
  jarray (*NewObjectArray) (JNIEnv *env, jsize length,
			    jclass elementClass, jobject initialElement);
  jobject (*GetObjectArrayElement) (JNIEnv *env, jobjectArray array,
				    jsize index);
  void (*SetObjectArrayElement) (JNIEnv *env, jobjectArray array, jsize index,
				 jobject value);
# define NEWARRAYPROTO(name, type) \
  type##Array (*New##name##Array) (JNIEnv *env, jsize length);
# define GETARRAYELEMENTSPROTO(name, type) \
  type* (*Get##name##ArrayElements) (JNIEnv *env, type##Array array, \
				     jboolean *isCopy);
# define RELEASEARRAYELEMENTSPROTO(name, type) \
  void (*Release##name##ArrayElements) (JNIEnv *env, type##Array array, \
					type * elems, jint mode);
# define GETARRAYREGIONPROTO(name, type) \
  void (*Get##name##ArrayRegion) (JNIEnv *env, type##Array array, \
				  jsize start, jsize len, type * buf);
#define SETARRAYREGIONPROTO(name, type) \
  void (*Set##name##ArrayRegion) (JNIEnv *env, type##Array array, \
				  jsize start, jsize len, const type * buf);
  FORPRIMITIVETYPES(NEWARRAYPROTO);
  FORPRIMITIVETYPES(GETARRAYELEMENTSPROTO);
  FORPRIMITIVETYPES(RELEASEARRAYELEMENTSPROTO);
  FORPRIMITIVETYPES(GETARRAYREGIONPROTO);
  FORPRIMITIVETYPES(SETARRAYREGIONPROTO);
# undef NEWARRAYPROTO
# undef GETARRAYELEMENTSPROTO
# undef RELEASEARRAYELEMENTSPROTO
# undef GETARRAYREGIONPROTO
# undef SETARRAYREGIONPROTO

  /* Registering Native Methods */
  jint (*RegisterNatives) (JNIEnv *env, jclass clazz,
			   const JNINativeMethod *methods, jint nMethods);
  jint (*UnregisterNatives) (JNIEnv *env, jclass clazz);

  /* Monitor Operations */
  jint (*MonitorEnter) (JNIEnv *env, jobject obj); // 217
  jint (*MonitorExit) (JNIEnv *env, jobject obj); // 218

  /* we don't plan on supporting the invocation api... */
  jint (*GetJavaVM) (JNIEnv *env, JavaVM **vm); // 219

  /* more jni 1.2 methods */
  void (*GetStringRegion) (JNIEnv *env, jstring str, jsize start, jsize len, jchar *buf); /* 220 (JNI 1.2) */
  void (*GetStringUTFRegion) (JNIEnv *env, jstring str, jsize start, jsize len, char *buf); /* 221 (JNI 1.2) */

  void * (*GetPrimitiveArrayCritical) (JNIEnv *env, jarray arr, jboolean *isCopy); /* 222 (JNI 1.2) */
  void (*ReleasePrimitiveArrayCritical) (JNIEnv *env, jarray arr, void *carray, jint mode); /* 223 (JNI 1.2) */

  const jchar * (*GetStringCritical) (JNIEnv *env, jstring str, jboolean *isCopy); /* 224 (JNI 1.2) */
  void (*ReleaseStringCritical) (JNIEnv *env, jstring str, const jchar *carray); /* 225 (JNI 1.2) */

  jweak (*NewWeakGlobalRef) (JNIEnv *env, jobject obj); /* 226 (JNI 1.2) */
  void (*DeleteWeakGlobalRef) (JNIEnv *env, jweak obj); /* 227 (JNI 1.2) */

  jboolean (*ExceptionCheck) (JNIEnv *env); /* 228 (JNI 1.2) */

  /* nio support */
  jobject (*NewDirectByteBuffer) (JNIEnv *env, void *address, jlong capacity); /* 229 (JNI 1.4) */
  void * (*GetDirectBufferAddress) (JNIEnv *env, jobject buf); /* 230 (JNI 1.4) */
  jlong (*GetDirectBufferCapacity) (JNIEnv *env, jobject buf); /* 231 (JNI 1.4) */
};

#endif /* INCLUDED_JNI_FUNC_H */
