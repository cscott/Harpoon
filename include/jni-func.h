/* Java Native Interface header file.  C. Scott Ananian. */
/* Implemented from the JNI spec, v 1.1 */

#ifndef INCLUDED_JNI_FUNC_H
#define INCLUDED_JNI_FUNC_H

#include <varargs.h>

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
	what(Void, void) \
	FORNONVOIDTYPES(what)
#define FORALLVARARGS(what) \
     	what(,...) what(A, jvalue *args) what(V, va_list args)

/** Function prototypes. **/

/* version information */
jint GetVersion(JNIEnv *env);

/* class operations */
jclass DefineClass(JNIEnv *env, jobject loader,
		   const jbyte *buf, jsize bufLen);
jclass FindClass(JNIEnv *env, const char *name);
jclass GetSuperclass(JNIEnv *env, jclass clazz);
jboolean IsAssignableFrom(JNIEnv *env, jclass clazz1, jclass clazz2);

/* exceptions */
jint Throw(JNIEnv *env, jthrowable obj);
jint ThrowNew(JNIEnv *env, jclass clazz, const char *message);
jthrowable ExceptionOccurred(JNIEnv *env);
void ExceptionDescribe(JNIEnv *env);
void ExceptionClear(JNIEnv *env);
void FatalError(JNIEnv *env, const char *msg);

/* global and local references */
jobject NewGlobalRef(JNIEnv *env, jobject obj);
void DeleteGlobalRef(JNIEnv *env, jobject globalRef);
void DeleteLocalRef(JNIEnv *env, jobject localRef);

/* Object Operations */
jobject AllocObject(JNIEnv *env, jclass clazz);
#define NEWOBJECTPROTO(suffix, argtype) \
	jobject NewObject##suffix (JNIEnv *env, jclass clazz, \
				   jmethodID methodID, argtype);
FORALLVARARGS(NEWOBJECTPROTO)
jclass GetObjectClass(JNIEnv *env, jobject obj);
jboolean IsInstanceOf(JNIEnv *env, jobject obj, jclass clazz);
jboolean IsSameObject(JNIEnv *env, jobject ref1, jobject ref2);

/* Accessing fields of objects */
jfieldID GetFieldID(JNIEnv *env, jclass clazz,
		    const char *name, const char *sig);
#define GETFIELDPROTO(name, type) \
	type Get##name##Field(JNIEnv *env, jobject obj, jfieldID fieldID);
#define SETFIELDPROTO(name, type) \
	type Set##name##Field(JNIEnv *env, jobject obj, jfieldID fieldID, \
			      type value);
FORNONVOIDTYPES(GETFIELDPROTO)
FORNONVOIDTYPES(SETFIELDPROTO)

/* Calling instance methods */
jmethodID GetMethodID(JNIEnv *env, jclass clazz,
		      const char *name, const char *sig);
#define CALLMETHODPROTO(name, type) \
	type Call##name##Method(JNIEnv *env, jobject obj, jmethodID methodID,\
	...);\
	type Call##name##MethodA(JNIEnv *env, jobject obj, jmethodID methodID,\
	jvalue *args);\
	type Call##name##MethodV(JNIEnv *env, jobject obj, jmethodID methodID,\
	va_list args);
#define CALLNONVIRTUALPROTO(name, type) \
	type CallNonvirtual##name##Method(JNIEnv *env, jobject obj, \
					  jclass clazz, jmethodID methodID, \
					  ...); \
	type CallNonvirtual##name##MethodA(JNIEnv *env, jobject obj, \
					   jclass clazz, jmethodID methodID, \
					   jvalue *args); \
	type CallNonvirtual##name##MethodV(JNIEnv *env, jobject obj, \
					   jclass clazz, jmethodID methodID, \
					   va_list args);
FORALLTYPES(CALLMETHODPROTO)
FORALLTYPES(CALLNONVIRTUALPROTO)

/* Accessing static fields */
jfieldID GetStaticFieldID(JNIEnv *env, jclass clazz,
			  const char *name, const char *sig);
#define GETSTATICFIELDPROTO(name, type) \
	type GetStatic##name##Field(JNIEnv *env, jclass clazz, \
				    jfieldID fieldID);
#define SETSTATICFIELDPROTO(name, type) \
	void SetStatic##name##Field(JNIEnv *env, jclass clazz, \
				    jfieldID fieldID, type value);
FORNONVOIDTYPES(GETSTATICFIELDPROTO)
FORNONVOIDTYPES(SETSTATICFIELDPROTO)

/* Calling static methods */
jmethodID GetStaticMethodID(JNIEnv *env, jclass clazz,
			    const char *name, const char *sig);
#define CALLSTATICPROTO(name, type) \
	type CallStatic##name##Method(JNIEnv *env, jclass clazz, \
				      jmethodID methodID, ...); \
	type CallStatic##name##MethodA(JNIEnv *env, jclass clazz, \
				      jmethodID methodID, jvalue *args); \
	type CallStatic##name##MethodV(JNIEnv *env, jclass clazz, \
				      jmethodID methodID, va_list args);
FORALLTYPES(CALLSTATICPROTO)

/* String operations */
jstring NewString(JNIEnv *env, const jchar *unicodeChars, jsize len);
jsize GetStringLength(JNIEnv *env, jstring string);
const jchar * GetStringChars(JNIEnv *env, jstring string, jboolean *isCopy);
void ReleaseStringChars(JNIEnv *env, jstring string, const jchar *chars);
jstring NewStringUTF(JNIEnv *env, const char *bytes);
jsize GetStringUTFLength(JNIEnv *env, jstring string);
const char * GetStringUTFChars(JNIEnv *env, jstring string, jboolean *isCopy);
void ReleaseStringUTFChars(JNIEnv *env, jstring string, const char *utf);

/* Array Operations */
jsize GetArrayLength(JNIEnv *env, jarray array);
jarray NewObjectArray(JNIEnv *env, jsize length,
		      jclass elementClass, jobject initialElement);
jobject GetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index);
void SetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index,
			   jobject value);
#define NEWARRAYPROTO(name, type) \
	type##Array New##name##Array(JNIEnv *env, jsize length);
#define GETARRAYELEMENTSPROTO(name, type) \
	type * Get##name##ArrayElements(JNIEnv *env, type##Array array, \
					jboolean *isCopy);
#define RELEASEARRAYELEMENTSPROTO(name, type) \
	void Release##name##ArrayElements(JNIEnv *env, type##Array array, \
					  type * elems, jint mode);
#define GETARRAYREGIONPROTO(name, type) \
	void Get##name##ArrayRegion(JNIEnv *env, type##Array array, \
				    jsize start, jsize len, type * buf);
#define SETARRAYREGIONPROTO(name, type) \
	void Set##name##ArrayRegion(JNIEnv *env, type##Array array, \
				    jsize start, jsize len, type * buf);
FORPRIMITIVETYPES(NEWARRAYPROTO)
FORPRIMITIVETYPES(GETARRAYELEMENTSPROTO)
FORPRIMITIVETYPES(RELEASEARRAYELEMENTSPROTO)
FORPRIMITIVETYPES(GETARRAYREGIONPROTO)
FORPRIMITIVETYPES(SETARRAYREGIONPROTO)

/* Registering Native Methods */
jint RegisterNatives(JNIEnv *env, jclass clazz,
		     const JNINativeMethod *methods, jint nMethods);
jint UnregisterNatives(JNIEnv *env, jclass clazz);

/* Monitor Operations */
jint MonitorEnter(JNIEnv *env, jobject obj);
jint MonitorExit(JNIEnv *env, jobject obj);

/* we don't support the invocation api... */
#if 0
jint GetJavaVM(JNIEnv *env, JavaVM **vm);
#endif

#endif /* INCLUDED_JNI_FUNC_H */
