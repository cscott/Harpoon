#include <assert.h>
#include <jni.h>
#include <jni-private.h>
#include "java_lang_Object.h"
#include "asm/atomicity.h"
#include "config.h"
#include "transact.h"

/* deal with allocation variations */
#ifdef BDW_CONSERVATIVE_GC
# define MALLOC GC_malloc
#else
# define MALLOC malloc
#endif

#define TRANSPKG "harpoon/Runtime/Transactions/"

/* we redefine this up here to minimize pointer casts down there.
 * for some reason gcc complains that (long int) and (ptroff_t) are
 * incompatible (even if sizes are equal) and that (struct commitrec **)
 * and (void **) are incompatible. */
static inline char
compare_and_swapP(void *p/*void **, really*/, void *oldv, void *newv) {
    return compare_and_swap((ptroff_t *)p, (ptroff_t)oldv, (ptroff_t)newv);
}

/* helpers */
static struct vinfo *CreateNewVersion(struct inflated_oobj *infl,
				      struct commitrec *cr,
				      struct oobj *template,
				      jboolean from_scratch) {
    struct vinfo *nv; /* new version goes here */
    struct claz *claz = template->claz;
    u_int32_t size = claz->size;/* size including header */
    if (claz->component_claz!=NULL) { /* is an array */
	assert(0); size+=0;
    }
    /** size includes header.  sizeof(struct vinfo) also includes header. */
    nv = MALLOC(size+sizeof(*nv)-sizeof(struct oobj));
    memcpy(&(nv->obj), template, size);
    /* initialize vinfo fields */
    nv->transid = cr;
    /* RACE CONDITIONS: multiple simultaneous create operations. */
    if (from_scratch) {
	compare_and_swapP(&(infl->first_version), NULL, nv);
	return infl->first_version; /* non-null. */
    } else {
	/* only other subtransactions of this can create a new version,
	 * and we assert that subtransactions are not simultaneously
	 * active. XXX: upgrade to parallel subtrans? */
	nv->wnext = (nv->anext = infl->first_version)->wnext;
	infl->first_version = nv;
    }
    return nv;
}

/* get the state of a version, pruning as we go. */
static inline jint StateP2(struct inflated_oobj *infl, struct vinfo *v) {
    struct commitrec *c = v->transid;
    jint s = COMMITTED;
    jboolean l = JNI_FALSE;
    while (c!=NULL) {
	s = c->state; /* atomic */
	if (s!=COMMITTED) break;
	c=c->parent;
	l=JNI_TRUE;
    }
    if (l) { v->transid=c; }
    /* below this point it is the StateP' function from the writeup */
    switch (s) {
    case WAITING:
#if 0
	if (l) /* transid was pruned, snip out parents */
	    while (v->transid== v->anext->transid)
		v->anext = v->anext->anext; /* race here */
#endif
	break;
    case COMMITTED:
	v->anext = v->wnext = NULL; /* perhaps better done by gc */
	break;
    default: /*ABORTED*/
	/* we remove v carefully, knowing that v could already be removed. */
	compare_and_swapP(&(infl->first_version), v, v->anext);
	break;
    }
    return s;
}

/* determine if cs is a non-aborted subtransaction of v->transid */
static inline jboolean IsNAST(struct commitrec *cs, struct vinfo *v) {
    struct commitrec *cp = v->transid;
    if (cs != cp) {
	if (cp!=NULL && cp->state==COMMITTED) {
	    do {
		cp = cp->parent;
	    } while (cp!=NULL && cp->state==COMMITTED);
	    v->transid = cp; /* atomic? */
	    /* XXX: check to see whether successor needs to be pruned. */
	}
	if (cp==NULL) { /* committed all the way down. */
	    v->anext = v->wnext = NULL; /* prune */
	} else if (cp->state==ABORTED)
	    return JNI_FALSE; /* cs either not subtrans, or is aborted */
	/* the possible WAITING-becomes-COMMITTED race between
	 * do/while loop above and cp->state==ABORTED check is
	 * not harmful. */
	else while (cs != cp) {
	    if (cs->state==ABORTED)
		return JNI_FALSE; /* aborted */
	    cs = cs->parent;
	    if (cs==NULL)
		return JNI_FALSE; /* not a subtransaction */
	}
    }
    /* quick, before we return, check that cs hasn't been aborted */
    for ( ; cs!=NULL; cs=cs->parent)
	if (cs->state==ABORTED)
	    return JNI_FALSE; /* aborted */
    return JNI_TRUE;
}
static inline jint typesize(JNIEnv *env, jclass type) {
  if (Java_java_lang_Class_isPrimitive(env, type))
    return FNI_GetClassInfo(type)->claz->size;
  return sizeof(ptroff_t);
}

/*
 * Class:     java_lang_Object
 * Method:    getReadableVersion
 * Signature: (Lharpoon/Runtime/Transactions/CommitRecord;)Ljava/lang/Object;
 */
    /** Get a version suitable for reading. */
JNIEXPORT jobject JNICALL Java_java_lang_Object_getReadableVersion
    (JNIEnv *env, jobject _this, jobject commitrec) {
    struct commitrec *c = (struct commitrec *) FNI_UNWRAP(commitrec);
    struct inflated_oobj *infl;
    struct vinfo *v;
    struct tlist *r;
    printf("getReadableVersion\n");
    if (!FNI_IS_INFLATED(_this)) FNI_InflateObject(env, _this);
    /* get first_version */
    infl = FNI_UNWRAP(_this)->hashunion.inflated;
    v = infl->first_version;
    if (v==NULL) v = CreateNewVersion(infl, NULL, FNI_UNWRAP(_this), JNI_TRUE);
    /* make sure we're on the readers list */
    again:
    r = &(v->readers);
    do {
	if (r->transid==c) goto has_version_and_listed; /* we're on! */
	r = r->next;
    } while (r!=NULL);
    /* gasp! we're not on the readers list */
    switch (StateP2(infl, v)) {
    case COMMITTED: break; /* add us to this version. */
    case WAITING:
	/* abort unless c is a non-aborted subtransaction of v->transid */
	if (!IsNAST(c, v)) goto suicide;
	else break; /* add us to this version */
    default: /* ABORTED */
	v = v->anext;
	goto again; /* try again. */
    }
    /* has_version: */
    /* add c to head of readers list, atomically. */
    {
	struct commitrec *rc; struct tlist *rnext;
	r = MALLOC(sizeof(*r));
	do { rnext = r->next = v->readers.next; }
	while (!compare_and_swapP(&(v->readers.next), rnext, r));
	/* now r is linked in, but w/ a null transid */
	do { rc = v->readers.transid; r->transid = rc; }
	while (!compare_and_swapP(&(v->readers.transid), rc, c));
	/* yay! */
    }
    has_version_and_listed:
    /* v has correct version */
    return FNI_WRAP(&(v->obj));
    suicide:
    {
	jclass abortcls = (*env)->FindClass
	    (env, "L" TRANSPKG "TransactionAbortException;");
	jmethodID methodID=(*env)->GetMethodID
	    (env, abortcls, "<init>", "(L" TRANSPKG "CommitRecord;)V");
	(*env)->Throw(env, (*env)->NewObject(env, abortcls, methodID,
					     commitrec));
	return NULL;
    }
}

/*
 * Class:     java_lang_Object
 * Method:    getReadWriteableVersion
 * Signature: (Lharpoon/Runtime/Transactions/CommitRecord;)Ljava/lang/Object;
 */
    /** Get a version suitable for reading or writing. */
JNIEXPORT jobject JNICALL Java_java_lang_Object_getReadWritableVersion
    (JNIEnv *env, jobject _this, jobject commitrec) {
    assert(0);
}

/*
 * Class:     java_lang_Object
 * Method:    getReadCommittedVersion
 * Signature: ()Ljava/lang/Object;
 */
    /** Get the most recently committed version to read from. */
JNIEXPORT jobject JNICALL Java_java_lang_Object_getReadCommittedVersion
    (JNIEnv *env, jobject _this) {
    assert(0);
}

/*
 * Class:     java_lang_Object
 * Method:    getWriteCommittedVersion
 * Signature: ()Ljava/lang/Object;
 */
    /** Get the most recently committed version to write to (and read from). */
JNIEXPORT jobject JNICALL Java_java_lang_Object_getWriteCommittedVersion
    (JNIEnv *env, jobject _this) {
    assert(0);
}

/*
 * Class:     java_lang_Object
 * Method:    makeCommittedVersion
 * Signature: ()Ljava/lang/Object;
 */
    /** Create a new fully committed version (if one does not already exist)
     *  to write (values equal to the FLAG) to. */
JNIEXPORT jobject JNICALL Java_java_lang_Object_makeCommittedVersion
    (JNIEnv *env, jobject _this) {
    assert(0);
}

/*
 * Class:     java_lang_Object
 * Method:    writeFieldFlag
 * Signature: (Ljava/lang/reflect/Field;Ljava/lang/Class;)V
 */
    /** Ensure that a given field is flagged. */
JNIEXPORT void JNICALL Java_java_lang_Object_writeFieldFlag
    (JNIEnv *env, jobject _this, jobject field, jclass type) {
    jfieldID fieldID = FNI_GetFieldInfo(field);
    Java_java_lang_Object_writeFlag(env, _this, fieldID->offset,
				    typesize(env, type));
}

/*
 * Class:     java_lang_Object
 * Method:    writeArrayElementFlag
 * Signature: (ILjava/lang/Class;)V
 */
    /** Ensure that a given array element is flagged. */
JNIEXPORT void JNICALL Java_java_lang_Object_writeArrayElementFlag
    (JNIEnv *env, jobject _this, jint index, jclass type) {
    jint elsize = typesize(env, type);
    jint eloffset = sizeof(struct aarray) + (index * elsize) ;
    Java_java_lang_Object_writeFlag(env, _this, eloffset, elsize);
}

/*
 * Class:     java_lang_Object
 * Method:    writeFlag
 * Signature: (II)V
 */
    /** Ensure that a flag exists at the specified offset and size from
     *  this object. */
JNIEXPORT void JNICALL Java_java_lang_Object_writeFlag
    (JNIEnv *env, jobject _this, jint offset, jint size) {
  printf("WRITEFLAG %d %d\n", (int)offset, (int)size);
    assert(0);
}
