/* private FNI structures for JNI implementation. CSA. */

#ifndef INCLUDED_JNI_PRIVATE_H
#define INCLUDED_JNI_PRIVATE_H

#include <sys/types.h>
#include <stdarg.h>
#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#ifdef WITH_SEMI_PRECISE_GC
#include "gc_typed.h"
#endif
#include "flexthread.h"
#include <time.h>

#if SIZEOF_VOID_P==4
  typedef u_int32_t ptroff_t;
#else
# if SIZEOF_VOID_P==8
   typedef u_int64_t ptroff_t;
# else
#  error unsupported pointer size.
# endif
#endif

#ifdef WITH_CLUSTERED_HEAPS
struct clustered_heap; /* defined in src/clheap/clheap.h */
#endif
#ifdef WITH_REALTIME_JAVA
struct MemBlock; /* defined in src/realtime/MemBlock.h */
#endif

/* --------------------- data structure internals --------------------- */

struct _jmethodID {
  char *name;	   /* method name. */
  char *desc;	   /* method descriptor */
  struct FNI_method2info *reflectinfo; /* reflection information */
  ptroff_t offset; /* an absolute address for static methods, else an offset */
  ptroff_t nargs;  /* number of argument words for the method */
};
struct _jfieldID {
  char *name;	   /* field name. */
  char *desc;	   /* field descriptor. */
  struct FNI_field2info *reflectinfo; /* reflection information */
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
  union {
    void *_ignore;		/* just to set the size of the union.  */
#ifdef WITH_SEMI_PRECISE_GC
    GC_word bitmap;		/* garbage collection field bitmap, or */
    GC_bitmap ptr;	        /* pointer to larger gc bitmap. */
	/* The least significant bit of the first word is one if	*/
	/* the first word in the object may be a pointer.		*/
#elif WITH_PRECISE_GC
    ptroff_t bitmap;		/* garbage collection field bitmap, or */
    ptroff_t *ptr;	        /* pointer to larger gc bitmap. */
	/* The least significant bit of the first word is one if	*/
	/* the first word in the object may be a pointer.		*/
#endif
  } gc_info;
  u_int32_t scaled_class_depth; /* sizeof(struct claz *) * class_depth */
  struct claz *display[0];	/* sized by FLEX */
  /* class method dispatch table after display */
};

/* the inflated_oobj structure has various bits of information that we
 * want to associate with *some* (not all) objects. */
struct inflated_oobj {
  ptroff_t hashcode; /* the real hashcode, since we've booted it */
  void *jni_data; /* information associated with this object by the JNI */
  void (*jni_cleanup_func)(void *jni_data);
  /* TRANSACTION SUPPORT */
#if WITH_TRANSACTIONS
  struct vinfo *first_version; /* linked list of object versions */
#endif
  /* REALTIME JAVA SUPPORT */
#ifdef WITH_REALTIME_JAVA
  struct MemBlock* memBlock;
  struct MemBlock* temp;
#endif
  /* locking information */
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
  pthread_t tid; /* can be zero, if no one has this lock */
  jint nesting_depth; /* recursive lock nesting depth */
  pthread_mutex_t mutex; /* simple (not recursive) lock */
  pthread_cond_t  cond; /* condition variable */
  pthread_rwlock_t jni_data_lock; /*read/write lock for jni_data field, above*/
#endif
#ifdef WITH_CLUSTERED_HEAPS
  struct clustered_heap * heap;
  void (*heap_release)(struct clustered_heap *);
#endif
#ifdef BDW_CONSERVATIVE_GC
  /* for cleanup via finalization */
  GC_finalization_proc old_finalizer;
  GC_PTR old_client_data;
#endif
};

/* the oobj structure tells you what's inside the object layout. */
struct oobj {
  struct claz *claz;
  /* if low bit is one, then this is a fair-dinkum hashcode. else, it's a
   * pointer to a struct inflated_oobj. this pointer needs to be freed
   * when the object is garbage collected, which is done w/ a finalizer. */
  union { ptroff_t hashcode; struct inflated_oobj *inflated; } hashunion;
  /* fields below this point */
  char field_start[0];
};
/** use this version of the oobj structure if you're looking at an array. */
struct aarray {
  struct oobj obj;
  char _padding_[OBJECT_PADDING]; /* by default, OBJECT_PADDING is zero */
  jsize length; /* first field in an array is the length */
  char element_start[0]; /* place holder for start of elements */
};

struct FNI_classinfo {
  struct claz *claz;
  const char *name;
  jint modifiers;
  union _jmemberID *memberend;
  union _jmemberID memberinfo[0];
};

struct FNI_name2class {
  char *name;
  struct oobj * class_object;
};
extern struct FNI_name2class name2class_start[], name2class_end[];

struct FNI_class2info {
  struct oobj * class_object;
  struct FNI_classinfo *info;
};
extern struct FNI_class2info class2info_start[], class2info_end[];

struct FNI_field2info {
  struct oobj *field_object; /* java.lang.reflect.Field object */
  struct _jfieldID *fieldID; /* JNI information */
  struct oobj *declaring_class_object; /* reflection info: declaring class */
  jint modifiers; /* reflection info: access modifiers of field. */
};
extern struct FNI_field2info field2info_start[], field2info_end[];

struct FNI_method2info {
  struct oobj *method_object; /* java.lang.reflect.Method object */
  struct _jmethodID *methodID; /* JNI information */
  struct oobj *declaring_class_object; /* reflection info: declaring class */
  jint modifiers; /* reflection info: access modifiers of method. */
};
extern struct FNI_method2info method2info_start[], method2info_end[];

/* --------------- wrapping and unwrapping objects. ------------ */
/* MOVED: to fni-wrap.h, for better precise-c backend integration */
#include "fni-wrap.h"

/* ---------------------- thread state ---------------------------------*/
/* MOVED: to fni-threadstate.h, for better precise-c backend integration */
#include "fni-threadstate.h"

/* ---------------------- object size ---------------------------------*/
/* broken out to fni-objsize.h to aid readability of this file somewhat */
#include "fni-objsize.h"

/* -------------- internal function prototypes. ------------- */

/* Initialize JNIEnv management code at startup. */
void FNI_InitJNIEnv(void);
/* Create a JNIEnv for the current thread. Must happen only once per thread!
 * Returns a pointer to the new JNIEnv. */
JNIEnv *FNI_CreateJNIEnv(void);
/* Return the JNIEnv for the current thread. Called by java native stub code.*/
JNIEnv *FNI_GetJNIEnv(void);

/* this function will make a wrapper. */
jobject FNI_NewLocalRef(JNIEnv *env, jobject_unwrapped obj);
/* Look up classinfo from class object. */
struct FNI_classinfo *FNI_GetClassInfo(jclass clazz);
/* Look up fieldID from field object. */
struct FNI_field2info *FNI_GetFieldInfo(jobject field);
/* Look up methodID from method object. */
struct FNI_method2info *FNI_GetMethodInfo(jobject method);

/* raw allocation routine */
void *FNI_RawAlloc(JNIEnv *env, jsize length);
/* allocate and zero memory for the specified object type, using the
 * supplied allocation function.  If allocfunc is NULL, uses FNI_RawAlloc.
 * Efficiency hack: 'info' may be NULL (in which case it is only
 * looked up if claz has a finalizer that needs to be registered). */
jobject FNI_Alloc(JNIEnv *env,
		  struct FNI_classinfo *info, struct claz *claz,
		  void *(*allocfunc)(jsize length), jsize length);

/* inflation routine/macro */
#define FNI_IS_INFLATED(obj) ((FNI_UNWRAP(obj)->hashunion.hashcode & 1) == 0)
void FNI_InflateObject(JNIEnv *env, jobject obj);

/* JNI-local object data storage. */
void * FNI_GetJNIData(JNIEnv *env, jobject obj);
void FNI_SetJNIData(JNIEnv *env, jobject obj, // frees old data if present.
		    void *jni_data, void (*cleanup_func)(void *jni_data));

/* auxilliary thread synchronization operations */
void FNI_MonitorWait(JNIEnv *env, jobject obj, const struct timespec *abstime);
void FNI_MonitorNotify(JNIEnv *env, jobject obj, jboolean wakeall);

/* ----- miscellaneous information embedded in the compiler output ----- */

/* name of the 'main' class. */
extern char *FNI_javamain;
/* null-terminated ordered list of static initializer names */
extern char *FNI_static_inits[];

/* starts and ends of various segments */
extern struct _fixup_info fixup_start[], fixup_end[];

extern jobject_unwrapped static_objects_start[], static_objects_end[];
extern int *string_constants_start, *string_constants_end;
extern int *code_start, *code_end;

struct _fixup_info {
  void *return_address;
  void *handler_target;
};
extern struct _fixup_info fixup_start[], fixup_end[];

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
jobject FNI_AllocObject_using (JNIEnv *env, jclass clazz,
			       void *(*allocfunc)(jsize length));/* FLEX ext */
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
