/* private FNI structures for JNI implementation. CSA. */

#ifndef INCLUDED_JNI_PRIVATE_H
#define INCLUDED_JNI_PRIVATE_H

#include <sys/types.h>
#include <stdarg.h>
#include "config.h"

#if SIZEOF_VOID_P==4
  typedef u_int32_t ptroff_t;
#else
# if SIZEOF_VOID_P==8
   typedef u_int64_t ptroff_t;
# else
#  error unsupported pointer size.
# endif
#endif

/* --------------------- data structure internals --------------------- */

struct _jmethodID {
  char *name;	   /* method name. */
  char *desc;	   /* method descriptor */
  ptroff_t offset; /* an absolute address for static methods, else an offset */
  ptroff_t nargs;  /* number of argument words for the method */
};
struct _jfieldID {
  char *name;	   /* field name. */
  char *desc;	   /* field descriptor. */
  ptroff_t offset; /* an absolute address for static fields, else an offset */
  ptroff_t _zero;  /* unused.  should be zero. */
};
union _jmemberID {
  struct _jmethodID m;
  struct _jfieldID  f;
};

/* the claz structure is primarily for method dispatch and instanceof tests */
struct claz {
  /* interface method dispatch table above this point. */
  struct oobj * class_object; /* pointer to (unwrapped) class object. */
  struct claz *component_claz;	/* component type, or NULL if non-array. */
  struct claz **interfaces; /* NULL terminated list of implemented interfaces*/
  u_int32_t size;		/* object size, including header */
  u_int32_t scaled_class_depth; /* sizeof(struct claz *) * class_depth */
  struct claz *display[0];	/* sized by FLEX */
  /* class method dispatch table after display */
};

/* the oobj structure tells you what's inside the object layout. */
struct oobj {
  /* hash code above this point */
  struct claz *claz;
  /* fields below this point */
};
/** use this version of the oobj structure if you're looking at an array. */
struct aarray {
  struct oobj obj;
  jsize length; /* first field in an array is the length */
  char element_start[0]; /* place holder for start of elements */
};
/* use this version of the oobj structure if you need to get at the
 * hashcode value, which is stored *above* the pointed-at location.
 * remember to offset your pointer using the OOBJ_OFFSET macro.
 */
struct oobj_offset {
  u_int32_t hashcode;
  struct oobj obj;
};
/* this structure is really useful only if you're figuring out array sizes */
struct aarray_offset {
  u_int32_t hashcode;
  struct aarray obj;
};
#define OOBJ_OFFSET(unscaled) \
	((struct oobj_offset *) (((char *)(unscaled)) - sizeof(u_int32_t)))
#define OOBJ_UNOFFSET(offset) \
	((struct oobj *) (((char *)(offset)) + sizeof(u_int32_t)))

struct FNI_classinfo {
  struct claz *claz;
  const char *name;
  struct _jmethodID *memberend;
  struct _jmethodID memberinfo[0];
};

struct FNI_name2class {
  char *name;
  struct oobj * class_object;
};
extern struct FNI_name2class name2class_start, name2class_end;

struct FNI_class2info {
  struct oobj * class_object;
  struct FNI_classinfo *info;
};
extern struct FNI_class2info class2info_start, class2info_end;

/* --------------- wrapping and unwrapping objects. ------------ */
/* an unwrapped jobject is a struct oobj *...*/
typedef struct oobj * jobject_unwrapped;
/* a wrapped object is a struct _jobject *...*/
struct _jobject {
  struct oobj * obj;
  struct _jobject * next;
};
/* define handy (un)wrapper macros */
#define FNI_WRAP(x) (FNI_NewLocalRef(env, x))
#define FNI_UNWRAP(_x) ({jobject x=_x; (x==NULL)?NULL:x->obj; })

/* ---------------------- thread state ---------------------------------*/
struct FNI_Thread_State {
  JNIEnv vtable;
  jthrowable exception; /* outstanding exception, or NULL if no exception. */
  struct _jobject localrefs; /* header node in a local refs list. */
};
extern struct _jobject FNI_globalrefs; /* header node in global refs list. */

#define FNI_NO_EXCEPTIONS(env) \
	(((struct FNI_Thread_State *)(env))->exception==NULL)

/* -------------- internal function prototypes. ------------- */

/* make a new JNIEnv *... */
JNIEnv * FNI_ThreadInit(void);
/* this function will make a wrapper. */
jobject FNI_NewLocalRef(JNIEnv *env, jobject_unwrapped obj);
/* Look up classinfo from class object. */
struct FNI_classinfo *FNI_GetClassInfo(jclass clazz);

/* raw allocation routine */
void *FNI_RawAlloc(JNIEnv *env, jsize length);
/* allocate and zero memory for the specified object type */
jobject FNI_Alloc(JNIEnv *env, struct FNI_classinfo *info, jsize length);

/* --------------- JNI function prototypes. ------------------ */

/* our abort stub */
void FNI_Unimplemented(void);

/* version information */
jint FNI_GetVersion (JNIEnv *env);

/* class operations */
jclass FNI_DefineClass (JNIEnv *env, jobject loader,
			const jbyte *buf, jsize bufLen);
jclass FNI_FindClass (JNIEnv *env, const char *name);
jclass FNI_GetSuperclass (JNIEnv *env, jclass clazz);
jboolean FNI_IsAssignableFrom (JNIEnv *env, jclass clazz1, jclass clazz2);

/* exceptions */
jint FNI_Throw (JNIEnv *env, jthrowable obj);
jint FNI_ThrowNew (JNIEnv *env, jclass clazz, const char *message);
jthrowable FNI_ExceptionOccurred (JNIEnv *env);
void FNI_ExceptionDescribe (JNIEnv *env);
void FNI_ExceptionClear (JNIEnv *env);
void FNI_FatalError (JNIEnv *env, const char *msg) __attribute__ ((noreturn));

/* global and local references */
jobject FNI_NewGlobalRef (JNIEnv *env, jobject obj);
void FNI_DeleteGlobalRef (JNIEnv *env, jobject globalRef);
void FNI_DeleteLocalRef (JNIEnv *env, jobject localRef);
jboolean FNI_IsSameObject (JNIEnv *env, jobject ref1, jobject ref2);

/* Object Operations */
jobject FNI_AllocObject (JNIEnv *env, jclass clazz);
# define NEWOBJECTPROTO(suffix, argtype) \
jobject FNI_NewObject##suffix (JNIEnv *env, jclass clazz, \
			       jmethodID methodID, argtype);
FORALLVARARGS(NEWOBJECTPROTO);
# undef NEWOBJECTPROTO
jclass FNI_GetObjectClass (JNIEnv *env, jobject obj);
jboolean FNI_IsInstanceOf (JNIEnv *env, jobject obj, jclass clazz);

/* Calling instance methods */
jmethodID FNI_GetMethodID (JNIEnv *env, jclass clazz,
			   const char *name, const char *sig);
# define CALLMETHODPROTO(name, type) \
type FNI_Call##name##Method (JNIEnv *env, jobject obj, jmethodID methodID,\
			     ...); \
type FNI_Call##name##MethodV (JNIEnv *env, jobject obj, jmethodID methodID,\
			      va_list args); \
type FNI_Call##name##MethodA (JNIEnv *env, jobject obj, jmethodID methodID,\
			      jvalue *args);
# define CALLNONVIRTUALPROTO(name, type) \
type FNI_CallNonvirtual##name##Method (JNIEnv *env, jobject obj, \
				       jclass clazz, jmethodID methodID, \
				       ...); \
type FNI_CallNonvirtual##name##MethodV (JNIEnv *env, jobject obj, \
					jclass clazz, jmethodID methodID, \
					va_list args); \
type FNI_CallNonvirtual##name##MethodA (JNIEnv *env, jobject obj, \
					jclass clazz, jmethodID methodID, \
					jvalue *args);
FORALLTYPES(CALLMETHODPROTO);
FORALLTYPES(CALLNONVIRTUALPROTO);
# undef CALLMETHODPROTO
# undef CALLNONVIRTUALPROTO

/* Accessing fields of objects */
jfieldID FNI_GetFieldID (JNIEnv *env, jclass clazz,
			 const char *name, const char *sig);
# define GETFIELDPROTO(name, type) \
type FNI_Get##name##Field (JNIEnv *env, jobject obj, jfieldID fieldID);
# define SETFIELDPROTO(name, type) \
  void FNI_Set##name##Field (JNIEnv *env, jobject obj, jfieldID fieldID, \
			     type value);
FORNONVOIDTYPES(GETFIELDPROTO);
FORNONVOIDTYPES(SETFIELDPROTO);
# undef GETFIELDPROTO
# undef SETFIELDPROTO

/* Calling static methods */
jmethodID FNI_GetStaticMethodID (JNIEnv *env, jclass clazz,
				 const char *name, const char *sig);
# define CALLSTATICPROTO(name, type) \
type FNI_CallStatic##name##Method (JNIEnv *env, jclass clazz, \
				   jmethodID methodID, ...); \
type FNI_CallStatic##name##MethodV (JNIEnv *env, jclass clazz, \
				    jmethodID methodID, va_list args); \
type FNI_CallStatic##name##MethodA (JNIEnv *env, jclass clazz, \
				    jmethodID methodID, jvalue *args);
FORALLTYPES(CALLSTATICPROTO);
# undef CALLSTATICPROTO

/* Accessing static fields */
jfieldID FNI_GetStaticFieldID (JNIEnv *env, jclass clazz,
			       const char *name, const char *sig);
# define GETSTATICFIELDPROTO(name, type) \
  type FNI_GetStatic##name##Field (JNIEnv *env, jclass clazz, \
				   jfieldID fieldID);
# define SETSTATICFIELDPROTO(name, type) \
void FNI_SetStatic##name##Field (JNIEnv *env, jclass clazz, \
				 jfieldID fieldID, type value);
FORNONVOIDTYPES(GETSTATICFIELDPROTO);
FORNONVOIDTYPES(SETSTATICFIELDPROTO);
# undef GETSTATICFIELDPROTO
# undef SETSTATICFIELDPROTO

/* String operations */
jstring FNI_NewString (JNIEnv *env, const jchar *unicodeChars, jsize len);
jsize FNI_GetStringLength (JNIEnv *env, jstring string);
const jchar* FNI_GetStringChars (JNIEnv *env, jstring string,
				 jboolean *isCopy);
void FNI_ReleaseStringChars (JNIEnv *env, jstring string, const jchar *chars);
jstring FNI_NewStringUTF (JNIEnv *env, const char *bytes);
jsize FNI_GetStringUTFLength (JNIEnv *env, jstring string);
const char* FNI_GetStringUTFChars (JNIEnv *env, jstring string,
				   jboolean *isCopy);
void FNI_ReleaseStringUTFChars (JNIEnv *env, jstring string, const char *utf);

/* Array Operations */
jsize FNI_GetArrayLength (JNIEnv *env, jarray array);
jarray FNI_NewObjectArray (JNIEnv *env, jsize length,
			   jclass elementClass, jobject initialElement);
jobject FNI_GetObjectArrayElement (JNIEnv *env, jobjectArray array,
				   jsize index);
void FNI_SetObjectArrayElement (JNIEnv *env, jobjectArray array, jsize index,
				jobject value);
# define NEWARRAYPROTO(name, type) \
type##Array FNI_New##name##Array (JNIEnv *env, jsize length);
# define GETARRAYELEMENTSPROTO(name, type) \
type* FNI_Get##name##ArrayElements (JNIEnv *env, type##Array array, \
				    jboolean *isCopy);
# define RELEASEARRAYELEMENTSPROTO(name, type) \
void FNI_Release##name##ArrayElements (JNIEnv *env, type##Array array, \
				       type * elems, jint mode);
# define GETARRAYREGIONPROTO(name, type) \
void FNI_Get##name##ArrayRegion (JNIEnv *env, type##Array array, \
				 jsize start, jsize len, type * buf);
#define SETARRAYREGIONPROTO(name, type) \
void FNI_Set##name##ArrayRegion (JNIEnv *env, type##Array array, \
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
jint FNI_RegisterNatives (JNIEnv *env, jclass clazz,
			  const JNINativeMethod *methods, jint nMethods);
jint FNI_UnregisterNatives (JNIEnv *env, jclass clazz);

/* Monitor Operations */
jint FNI_MonitorEnter (JNIEnv *env, jobject obj);
jint FNI_MonitorExit (JNIEnv *env, jobject obj);

/* we don't plan on supporting the invocation api... */
#if 0
jint FNI_GetJavaVM (JNIEnv *env, JavaVM **vm);
#endif

#endif /* INCLUDED_JNI_PRIVATE_H */
