/* Array-related JNI functions. [CSA] */
#include <jni.h>
#include "jni-private.h"
#include "write_barrier.h"

#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include "config.h"
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#define DMALLOC_PADDING 1 /* dmalloc doesn't like 0-byte allocations */
#else
#define DMALLOC_PADDING 0
#endif
#ifdef WITH_TRANSACTIONS
# include "transact/transjni.h" /* JNI transactions support functions */
#endif

#include "../java.lang/class.h" /* for fni_class_isPrimitive */

/* Returns the number of elements in the array.
 */
jsize FNI_GetArrayLength(JNIEnv *env, jarray array) {
  struct aarray *a = (struct aarray *) FNI_UNWRAP_MASKED(array);
  assert(FNI_CLAZ(&a->obj)->component_claz); /* else not an array */
  return a->length;
}

/* Constructs a new array holding objects in class elementClass. 
 * All elements are initially set to initialElement.
 *
 * Returns a Java array object, or NULL if the array cannot be constructed. 
 * THROWS: 
 *   OutOfMemoryError: if the system runs out of memory. 
 */
jarray FNI_NewObjectArray(JNIEnv *env, jsize length, 
			  jclass elementClass, jobject initialElement) {
  struct FNI_classinfo *info;
  jclass arrayclazz;
  jobject result;
  assert(FNI_NO_EXCEPTIONS(env) && length>=0 && elementClass!=NULL);
  assert(fni_class_isPrimitive(env, elementClass)==JNI_FALSE);
  info = FNI_GetClassInfo(elementClass);
  {
    char arraydesc[strlen(info->name)+4];
    arraydesc[0]='[';
    if (info->name[0]=='[') strcpy(arraydesc+1, info->name);
    else {
      arraydesc[1]='L';
      strcpy(arraydesc+2, info->name);
      arraydesc[strlen(arraydesc)+1]='\0';
      arraydesc[strlen(arraydesc)]=';';
    }
    arrayclazz = FNI_FindClass(env, arraydesc);
    if (arrayclazz==NULL) return NULL; /* bail on exception */
    info = FNI_GetClassInfo(arrayclazz);
  }
  result = FNI_Alloc(env, info, info->claz, NULL/* default alloc func */,
		     sizeof(struct aarray) + sizeof(ptroff_t)*length);
  if (result==NULL) return NULL; /* bail on error */
  ((struct aarray *)FNI_UNWRAP_MASKED(result))->length = length;
  if (initialElement != NULL) {
    jsize i;
    for (i=0; i<length; i++)
      (*env)->SetObjectArrayElement(env, (jobjectArray) result, i,
				    initialElement);
  }
#ifdef WITH_ROLE_INFER
  printf("Building Array\n");
  printf("Doing assignUID\n");
  NativeassignUID(env,result,arrayclazz);
#endif
  FNI_DeleteLocalRef(env, arrayclazz);

  return (jobjectArray) result;
}

/* A family of operations used to construct a new primitive array object.
 * Returns a Java array, or NULL if the array cannot be constructed. 
 */
#define NEWPRIMITIVEARRAY(name,type, sig)\
type##Array FNI_New##name##Array(JNIEnv *env, jsize length) {\
  jclass arrayclazz;\
  struct FNI_classinfo *info;\
  jobject result;\
\
  assert(FNI_NO_EXCEPTIONS(env) && length>=0);\
  arrayclazz = FNI_FindClass(env, sig);\
  if (arrayclazz==NULL) return NULL; /* bail on error */\
  info = FNI_GetClassInfo(arrayclazz);\
  result = FNI_Alloc(env, info, info->claz, NULL/*default alloc func*/,\
		     sizeof(struct aarray) + sizeof(type)*length);\
  if (result==NULL) return NULL; /* bail on error */\
  ((struct aarray *)FNI_UNWRAP_MASKED(result))->length = length;\
  ASSIGN_UID;\
  FNI_DeleteLocalRef(env, arrayclazz);\
  return (type##Array) result;\
}
/* special object fixup if we're building with role inference support */
#ifdef WITH_ROLE_INFER
#  define ASSIGN_UID  NativeassignUID(env,result,arrayclazz)
#else /* !WITH_ROLE_INFER */
#  define ASSIGN_UID
#endif /* WITH_ROLE_INFER */


NEWPRIMITIVEARRAY(Boolean, jboolean, "[Z");
NEWPRIMITIVEARRAY(Byte, jbyte, "[B");
NEWPRIMITIVEARRAY(Char, jchar, "[C");
NEWPRIMITIVEARRAY(Short, jshort, "[S");
NEWPRIMITIVEARRAY(Int, jint, "[I");
NEWPRIMITIVEARRAY(Long, jlong, "[J");
NEWPRIMITIVEARRAY(Float, jfloat, "[F");
NEWPRIMITIVEARRAY(Double, jdouble, "[D");

/* Returns an element of an Object array. 
 * THROWS: 
 *  ArrayIndexOutOfBoundsException: if index does not specify a valid index
 *                                  in the array.
 */
jobject FNI_GetObjectArrayElement(JNIEnv *env, 
				  jobjectArray array, jsize index) {
  struct aarray *a = (struct aarray *) FNI_UNWRAP_MASKED(array);
  jobject_unwrapped result;
  /* check array bounds */
  if (index > a->length || index < 0) {
    jclass oob=FNI_FindClass(env,"java/lang/ArrayIndexOutOfBoundsException");
    if (oob!=NULL) FNI_ThrowNew(env, oob, "JNI: GetObjectArrayElement");
    return NULL;
  }
  /* do get */
#if defined(WITH_TRANSACTIONS) && !defined(TRANS_NO_ARRAY)
  result = TRANSJNI_Get_Array_Object(env, a, index);
#else
  result = *( (jobject_unwrapped *)
	      ( ((char *) &(a->element_start)) + (index*sizeof(result)) ) );
#endif
  return FNI_WRAP(result);
}

/* Sets an element of an Object array. 
 * THROWS: 
 *  ArrayIndexOutOfBoundsException: if index does not specify a valid index
 *                                  in the array. 
 *  ArrayStoreException: if the class of value is not a subclass of the
 *                       element class of the array.
 */
void FNI_SetObjectArrayElement(JNIEnv *env, jobjectArray array, 
			       jsize index, jobject value) {
  struct aarray *a = (struct aarray *) FNI_UNWRAP_MASKED(array);
  jclass objCls, arrCls, comCls;
  struct FNI_classinfo *info;
  assert(FNI_NO_EXCEPTIONS(env) && array != NULL && a != NULL);
  /* check array bounds */
  if (index > a->length || index < 0) {
    jclass oob=FNI_FindClass(env,"java/lang/ArrayIndexOutOfBoundsException");
    if (oob!=NULL) FNI_ThrowNew(env, oob, "JNI: GetObjectArrayElement");
    return;
  }
  /* check type */
  objCls = (value==NULL) ? NULL : FNI_GetObjectClass(env, value);
  arrCls = FNI_GetObjectClass(env, array);
  info = FNI_GetClassInfo(arrCls);
  assert(info && info->name[0]=='[' && info->claz->component_claz);
  comCls = FNI_WRAP(info->claz->component_claz->class_object);
  if ((objCls==NULL && info->name[1]!='[' && info->name[1]!='L') ||
      (objCls!=NULL && FNI_IsAssignableFrom(env, objCls, comCls)==JNI_FALSE)) {
    jclass ase = FNI_FindClass(env,"java/lang/ArrayStoreException");
    if (ase!=NULL) FNI_ThrowNew(env, ase, "JNI: SetObjectArrayElement");
    return;
  }
  /* clean up a bit */
  if (objCls!=NULL) FNI_DeleteLocalRef(env, objCls);
  FNI_DeleteLocalRef(env, arrCls);
  FNI_DeleteLocalRef(env, comCls);
#ifdef WITH_GENERATIONAL_GC
  generational_write_barrier((jobject_unwrapped *)
			     ( ((char *) &(a->element_start)) + 
			       (index*sizeof(result)) ));
#endif
  /* do set */
#if defined(WITH_TRANSACTIONS) && !defined(TRANS_NO_ARRAY)
  TRANSJNI_Set_Array_Object(env, a, index, FNI_UNWRAP(value));
#else
  *( (jobject_unwrapped *)
     ( ((char *) &(a->element_start)) + (index*sizeof(jobject_unwrapped)) ) ) =
    FNI_UNWRAP(value);
#endif
}

/* A family of functions that returns the body of the primitive array.
 * The result is valid until the corresponding
 * Release<PrimitiveType>ArrayElements() function is called.
 * Since the returned array may be a copy of the Java array,
 * changes made to the returned array will not necessarily be reflected in
 * the original array until Release<PrimitiveType>ArrayElements() is called.
 *
 * If isCopy is not NULL, then *isCopy is set to JNI_TRUE if a copy is made;
 * or it is set to JNI_FALSE if no copy is made.
 *
 * Regardless of how boolean arrays are represented in the Java VM,
 * GetBooleanArrayElements() always returns a pointer to jbooleans, with
 * each byte denoting an element (the unpacked representation). All arrays
 * of other types are guaranteed to be contiguous in memory. 
 *
 * Returns a pointer to the array elements, or NULL if the operation fails.
 */
#define GETPRIMITIVEARRAYELEMENTS(name,type)\
type * FNI_Get##name##ArrayElements(JNIEnv *env,\
				    type##Array array, jboolean *isCopy) {\
  struct aarray *a = (struct aarray *) FNI_UNWRAP_MASKED(array);\
  jsize length = a->length;\
  /* safe to use malloc; no pointers to garbage collected objects in array */\
  type * result = malloc(sizeof(type) * length + DMALLOC_PADDING);\
  FNI_Get##name##ArrayRegion(env,array,0,length,result);\
  if (isCopy!=NULL) *isCopy=JNI_TRUE;\
  return result;\
}
FORPRIMITIVETYPES(GETPRIMITIVEARRAYELEMENTS);

/* A family of functions that informs the VM that the native code no longer
 * needs access to elems. The elems argument is a pointer derived from array
 * using the corresponding Get<PrimitiveType>ArrayElements() function. If
 * necessary, this function copies back all changes made to elems to the
 * original array. 
 *
 * The mode argument provides information on how the array buffer should be
 * released. mode has no effect if elems is not a copy of the elements in
 * array. Otherwise, mode has the following impact, as shown in the following
 * table: 
 *    mode                           actions 
 *    ----------   ---------------------------------------------------------
 *    0            copy back the content and free the elems buffer 
 *    JNI_COMMIT   copy back the content but do not free the elems buffer 
 *    JNI_ABORT    free the buffer without copying back the possible changes 
 *
 * In most cases, programmers pass "0" to the mode argument to ensure
 * consistent behavior for both pinned and copied arrays. The other options
 * give the programmer more control over memory management and should be used
 * with extreme care.
 */
#define RELEASEPRIMITIVEARRAYELEMENTS(name,type)\
void FNI_Release##name##ArrayElements(JNIEnv *env, type##Array array,\
				      type *elems, jint mode) {\
  if (mode!=JNI_ABORT) {\
    struct aarray *a = (struct aarray *) FNI_UNWRAP_MASKED(array);\
    FNI_Set##name##ArrayRegion(env,array,0,a->length,elems);\
  }\
  if (mode!=JNI_COMMIT) {\
    free(elems);\
  }\
}
FORPRIMITIVETYPES(RELEASEPRIMITIVEARRAYELEMENTS);

#if defined(WITH_TRANSACTIONS) && !defined(TRANS_NO_ARRAY)
/* all of the following methods have different implementations when
 * running with transactions support: */
# include "transact/jni-array.h"
#else
/* your regularly scheduled program: */


/* A family of functions that copies a region of a primitive array into a
 * buffer. 
 * THROWS: 
 *   ArrayIndexOutOfBoundsException: if one of the indexes in the region is
 *   not valid. 
 */
#define GETPRIMITIVEARRAYREGION(name,type)\
void FNI_Get##name##ArrayRegion(JNIEnv *env, type##Array array,\
				jsize start, jsize len, type *buf) {\
  struct aarray *a = (struct aarray *) FNI_UNWRAP_MASKED(array);\
  if (start+len > a->length || start < 0 || len < 0) {\
    jclass oob=FNI_FindClass(env,"java/lang/ArrayIndexOutOfBoundsException");\
    if (oob!=NULL) FNI_ThrowNew(env, oob, "JNI: Get" #name "ArrayRegion");\
    return;\
  }\
  memcpy(buf, (char *)(&(a->element_start))+(start*sizeof(type)),\
	 len*sizeof(type));\
}
FORPRIMITIVETYPES(GETPRIMITIVEARRAYREGION);

/* A family of functions that copies back a region of a primitive array from
 * a buffer.
 * THROWS: 
 *   ArrayIndexOutOfBoundsException: if one of the indexes in the region is
 *   not valid. 
 */
#define SETPRIMITIVEARRAYREGION(name,type)\
void FNI_Set##name##ArrayRegion(JNIEnv *env, type##Array array,\
				jsize start, jsize len, const type *buf) {\
  struct aarray *a = (struct aarray *) FNI_UNWRAP_MASKED(array);\
  if (start+len > a->length || start < 0 || len < 0) {\
    jclass oob=FNI_FindClass(env,"java/lang/ArrayIndexOutOfBoundsException");\
    if (oob!=NULL) FNI_ThrowNew(env, oob, "JNI: Get" #name "ArrayRegion");\
    return;\
  }\
  memcpy(((char *) &(a->element_start))+(start*sizeof(type)), buf,\
	 len*sizeof(type));\
}
FORPRIMITIVETYPES(SETPRIMITIVEARRAYREGION);

#endif /* !WITH_TRANSACTIONS */
