#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#ifdef BDW_CONSERVATIVE_GC
# include "gc.h"
#else
# error "These tables don't work unless you're using BDW"
#endif

#include "harpoon_Runtime_MZFExternalMap.h"

/* integer hash table */
#define TYPE jint
#define TABLE jint_table
#define TABLE_ELEMENT jint_table_element
#define GET jint_get
#define SET jint_set
#define REMOVE jint_remove
#include "hashimpl.h"

/*
 * Class:     harpoon_Runtime_MZFExternalMap
 * Method:    intGET
 * Signature: (Ljava/lang/Object;Ljava/lang/Object;I)I
 */
JNIEXPORT jint JNICALL Java_harpoon_Runtime_MZFExternalMap_intGET
  (JNIEnv *env, jclass cls, jobject key, jobject obj, jint default_value) {
  return GET(FNI_UNWRAP(key), FNI_UNWRAP(obj), default_value);
}

/*
 * Class:     harpoon_Runtime_MZFExternalMap
 * Method:    intSET
 * Signature: (Ljava/lang/Object;Ljava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_harpoon_Runtime_MZFExternalMap_intSET
  (JNIEnv *env, jclass cls, jobject key, jobject obj,
   jint new_value, jint default_value) {
  SET(FNI_UNWRAP(key), FNI_UNWRAP(obj), new_value, default_value);
}

#undef TYPE
#undef TABLE
#undef TABLE_ELEMENT
#undef GET
#undef SET
#undef REMOVE

/* long hash table */
#define TYPE jlong
#define TABLE jlong_table
#define TABLE_ELEMENT jlong_table_element
#define GET jlong_get
#define SET jlong_set
#define REMOVE jlong_remove
#include "hashimpl.h"

/*
 * Class:     harpoon_Runtime_MZFExternalMap
 * Method:    longGET
 * Signature: (Ljava/lang/Object;Ljava/lang/Object;J)J
 */
JNIEXPORT jlong JNICALL Java_harpoon_Runtime_MZFExternalMap_longGET
  (JNIEnv *env, jclass cls, jobject key, jobject obj, jlong default_value) {
  return GET(FNI_UNWRAP(key), FNI_UNWRAP(obj), default_value);
}

/*
 * Class:     harpoon_Runtime_MZFExternalMap
 * Method:    longSET
 * Signature: (Ljava/lang/Object;Ljava/lang/Object;JJ)V
 */
JNIEXPORT void JNICALL Java_harpoon_Runtime_MZFExternalMap_longSET
  (JNIEnv *env, jclass cls, jobject key, jobject obj,
   jlong new_value, jlong default_value) {
  SET(FNI_UNWRAP(key), FNI_UNWRAP(obj), new_value, default_value);
}

#undef TYPE
#undef TABLE
#undef TABLE_ELEMENT
#undef GET
#undef SET
#undef REMOVE

/* object hash table */
#define TYPE void *
#define TABLE ptr_table
#define TABLE_ELEMENT ptr_table_element
#define GET ptr_get
#define SET ptr_set
#define REMOVE ptr_remove
#include "hashimpl.h"

/** xxxxx: keeps fields alive.  should use val==null as tombstone marker */

/*
 * Class:     harpoon_Runtime_MZFExternalMap
 * Method:    ptrGET
 * Signature: (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_harpoon_Runtime_MZFExternalMap_ptrGET
  (JNIEnv *env, jclass cls, jobject key, jobject obj, jobject default_value) {
  return FNI_WRAP(GET(FNI_UNWRAP(key), FNI_UNWRAP(obj),
		      FNI_UNWRAP(default_value)));
}

/*
 * Class:     harpoon_Runtime_MZFExternalMap
 * Method:    ptrSET
 * Signature: (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_harpoon_Runtime_MZFExternalMap_ptrSET
  (JNIEnv *env, jclass cls, jobject key, jobject obj,
   jobject new_value, jobject default_value) {
  SET(FNI_UNWRAP(key), FNI_UNWRAP(obj),
      FNI_UNWRAP(new_value), FNI_UNWRAP(default_value));
}

#undef TYPE
#undef TABLE
#undef TABLE_ELEMENT
#undef GET
#undef SET
#undef REMOVE
