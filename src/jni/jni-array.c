/* Array-related JNI functions. [CSA] */
#include <jni.h>
#include "jni-private.h"

#include <assert.h>
#include <stdlib.h>
#include <string.h>

/* Returns the number of elements in the array.
 */
jsize FNI_GetArrayLength(JNIEnv *env, jarray array) {
  struct aarray *a = (struct aarray *) FNI_UNWRAP(array);
  return a->length;
}

#if 0
/* Constructs a new array holding objects in class elementClass. 
 * All elements are initially set to initialElement.
 *
 * Returns a Java array object, or NULL if the array cannot be constructed. 
 */
jarray FNI_NewObjectArray(JNIEnv *env, jsize length, 
			  jclass elementClass, jobject initialElement) {
}

/* Returns an element of an Object array. 
 * THROWS: 
 *  ArrayIndexOutOfBoundsException: if index does not specify a valid index
 *                                  in the array.
 */
jobject FNI_GetObjectArrayElement(JNIEnv *env, 
				  jobjectArray array, jsize index) {
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
}

/* A family of operations used to construct a new primitive array object.
 * Returns a Java array, or NULL if the array cannot be constructed. 
 */
#define NEWPRIMITIVEARRAY(name,type)\
type##Array FNI_New##name##Array(JNIEnv *env, jsize length) {\
}
FORPRIMITIVETYPES(NEWPRIMITIVEARRAY);
#endif

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
  struct aarray *a = (struct aarray *) FNI_UNWRAP(array);\
  jsize length = a->length;\
  type * result = malloc(sizeof(type) * length);\
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
    struct aarray *a = (struct aarray *) FNI_UNWRAP(array);\
    FNI_Set##name##ArrayRegion(env,array,0,a->length,elems);\
  }\
  if (mode!=JNI_COMMIT) {\
    free(elems);\
  }\
}
FORPRIMITIVETYPES(RELEASEPRIMITIVEARRAYELEMENTS);

/* A family of functions that copies a region of a primitive array into a
 * buffer. 
 * THROWS: 
 *   ArrayIndexOutOfBoundsException: if one of the indexes in the region is
 *   not valid. 
 */
#define GETPRIMITIVEARRAYREGION(name,type)\
void FNI_Get##name##ArrayRegion(JNIEnv *env, type##Array array,\
				jsize start, jsize len, type *buf) {\
  struct aarray *a = (struct aarray *) FNI_UNWRAP(array);\
  if (start+len > a->length) {\
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
  struct aarray *a = (struct aarray *) FNI_UNWRAP(array);\
  if (start+len > a->length) {\
    jclass oob=FNI_FindClass(env,"java/lang/ArrayIndexOutOfBoundsException");\
    if (oob!=NULL) FNI_ThrowNew(env, oob, "JNI: Get" #name "ArrayRegion");\
    return;\
  }\
  memcpy(((char *) &(a->element_start))+(start*sizeof(type)), buf,\
	 len*sizeof(type));\
}
FORPRIMITIVETYPES(SETPRIMITIVEARRAYREGION);
