/* our own definitions of the JNI array methods */
#include "transact/transjni.h" /* JNI support functions */

#define GETPRIMITIVEARRAYREGION(name,type)\
void FNI_Get##name##ArrayRegion(JNIEnv *env, type##Array array,\
				jsize start, jsize len, type *buf) {\
  struct aarray *a = (struct aarray *) FNI_UNWRAP_MASKED(array);\
  jsize i;\
  if (start+len > a->length || start < 0 || len < 0) {\
    jclass oob=FNI_FindClass(env,"java/lang/ArrayIndexOutOfBoundsException");\
    if (oob!=NULL) FNI_ThrowNew(env, oob, "JNI: Get" #name "ArrayRegion");\
    return;\
  }\
  /* Hmm: probably more efficient ways to do this. */\
  for (i=0; i<len; i++)\
    buf[i] = TRANSJNI_Get_Array_##name(env, a, start+i);\
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
  jsize i;\
  if (start+len > a->length || start < 0 || len < 0) {\
    jclass oob=FNI_FindClass(env,"java/lang/ArrayIndexOutOfBoundsException");\
    if (oob!=NULL) FNI_ThrowNew(env, oob, "JNI: Get" #name "ArrayRegion");\
    return;\
  }\
  /* Hmm: probably more efficient ways to do this. */\
  for (i=0; i<len; i++) {\
    TRANSJNI_Set_Array_##name(env, a, start+i, buf[i]);\
    if (!FNI_NO_EXCEPTIONS(env)) return; /* abort! */\
  }\
}
FORPRIMITIVETYPES(SETPRIMITIVEARRAYREGION);
