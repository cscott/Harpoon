/* JNI transactions support functions */

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */

// prototypes.
#if !defined(NO_VALUETYPE)
VALUETYPE T(TRANSJNI_Get)(JNIEnv *env, jobject obj, jfieldID fieldID);
void T(TRANSJNI_Set)(JNIEnv *env, jobject obj, jfieldID fieldID,
		     VALUETYPE value);
/* all length, etc, checks done before this is invoked */
VALUETYPE T(TRANSJNI_Get_Array)(JNIEnv *env, struct aarray *arr, int index);
void T(TRANSJNI_Set_Array)(JNIEnv *env, struct aarray *arr, int index,
			   VALUETYPE value);
#endif /* !NO_VALUETYPE */

// implementation.
#if !defined(IN_TRANSJNI_HEADER)

# if defined(NO_VALUETYPE)

static void TRANSJNI_Abort(JNIEnv *env) {
  struct commitrec *cr;
  jclass cls;
  jmethodID mid;
  jthrowable thr;
  // the transaction abort exception escapes the transaction
  cr = setCurrTrans(env, NULL);
  cls = (*env)->FindClass(env, "harpoon/Runtime/Transactions/"
			  "TransactionAbortException");
  if ((*env)->ExceptionOccurred(env)) goto finally;
  mid = (*env)->GetMethodID(env, cls, "<init>",
			    "(Lharpoon/Runtime/Transactions/CommitRecord;)V");
  if ((*env)->ExceptionOccurred(env)) goto finally;
  thr = (*env)->NewObject(env, cls, mid, FNI_WRAP((struct oobj*)cr));
  if ((*env)->ExceptionOccurred(env)) goto finally;
  (*env)->Throw(env, thr);
 finally:
  // restore transaction context
  setCurrTrans(env, cr);
}

# else /* !NO_VALUETYPE */

#define OFFSETBASE(x) (((void*)FIELDBASE(o))-((void*)o))

VALUETYPE T(TRANSJNI_Get)(JNIEnv *env, jobject obj, jfieldID fieldID) {
  struct oobj *o = FNI_UNWRAP(obj);
  struct commitrec *cr = currTrans(env);
  struct vinfo *v;
  if (cr) { /* in a transaction! */
    assert (fieldID->trans_isvalid);
    EXACT_ensureReader(o, cr);
    v = T(EXACT_setReadFlags)(o, fieldID->offset - OFFSETBASE(o),
			      fieldID->trans_bitoff, 1<<fieldID->trans_bitnum,
			      NULL, cr);
    return T(EXACT_readT)(o, fieldID->offset - OFFSETBASE(o), v, cr);
  } else { /* non-transactional read */
    return T(EXACT_readNT)(o, fieldID->offset - OFFSETBASE(o),
			   fieldID->trans_bitoff, 1<<fieldID->trans_bitnum);
  }
}

void T(TRANSJNI_Set)(JNIEnv *env, jobject obj, jfieldID fieldID,
		     VALUETYPE value) {
  struct oobj *o = FNI_UNWRAP(obj);
  struct commitrec *cr = currTrans(env);
  struct vinfo *v;
  if (cr) { /* in a transaction! */
    assert (fieldID->trans_isvalid);
    v = EXACT_ensureWriter(o, cr);
    if (v==NULL) { /* transaction wants to commit suicide */
      TRANSJNI_Abort(env); return; // bail.
    }
    T(EXACT_setWriteFlags)(o, fieldID->offset - OFFSETBASE(o),
			   fieldID->trans_bitoff, 1<<fieldID->trans_bitnum);
    T(EXACT_writeT)(o, fieldID->offset - OFFSETBASE(o), value, v);
  } else { /* non-transactional write */
    T(EXACT_writeNT)(o, fieldID->offset - OFFSETBASE(o), value,
		     fieldID->trans_bitoff, 1 << fieldID->trans_bitnum);
  }
}

#undef OFFSETBASE

/* all length, etc, checks done before this is invoked */
VALUETYPE T(TRANSJNI_Get_Array)(JNIEnv *env, struct aarray *arr, int index) {
  struct oobj *o = &arr->obj;
  struct commitrec *cr = currTrans(env);
  struct vinfo *v;
  unsigned offset = sizeof(VALUETYPE) * index;
  if (cr) { /* in a transaction! */
    EXACT_ensureReader(o, cr);
    v = T(EXACT_setReadFlags_Array)(o, offset, 0, 1 << (index & 31), NULL, cr);
    return T(EXACT_readT_Array)(o, offset, v, cr);
  } else { /* non-transactional read */
    return T(EXACT_readNT_Array)(o, offset, 0, 1 << (index & 31));
  }
}

/* all length, etc, checks done before this is invoked */
void T(TRANSJNI_Set_Array)(JNIEnv *env, struct aarray *arr, int index,
			   VALUETYPE value) {
  struct oobj *o = &arr->obj;
  struct commitrec *cr = currTrans(env);
  struct vinfo *v;
  unsigned offset = sizeof(VALUETYPE) * index;
  if (cr) { /* in a transaction! */
    v = EXACT_ensureWriter(o, cr);
    if (v==NULL) { /* transaction wants to commit suicide */
      TRANSJNI_Abort(env); return; // bail.
    }
    T(EXACT_setWriteFlags_Array)(o, offset, 0, 1 << (index & 31));
    T(EXACT_writeT_Array)(o, offset, value, v);
  } else { /* non-transactional write */
    T(EXACT_writeNT_Array)(o, offset, value, 0, 1 << (index & 31));
  }
}

# endif /* !NO_VALUETYPE */

#endif /* !IN_TRANSJNI_HEADER */

/* clean up after ourselves */
#include "transact/preproc.h"
