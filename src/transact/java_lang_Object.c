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

#define RACES 1 /* XXX: haven't gotten rid of them yet */

/* we redefine this up here to minimize pointer casts down there.
 * for some reason gcc complains that (long int) and (ptroff_t) are
 * incompatible (even if sizes are equal) and that (struct commitrec **)
 * and (void **) are incompatible. */
static inline char
compare_and_swapP(void *p/*void **, really*/, void *oldv, void *newv) {
    return compare_and_swap((ptroff_t *)p, (ptroff_t)oldv, (ptroff_t)newv);
}

/* helpers */
static inline jboolean isPrimitive(struct claz *thisclz) {
    /* from java_lang_Class.c: */
    /* primitives have null in the first slot of the display. */
    if (thisclz->display[0]!=NULL) return JNI_FALSE;
    /* but so do interfaces.  weed them out using the interface list. */
    if (*(thisclz->interfaces)!=NULL) return JNI_FALSE;
    return JNI_TRUE;
}

static struct vinfo *CreateNewVersion(struct inflated_oobj *infl,
				      struct commitrec *cr,
				      struct oobj *template,
				      jboolean from_scratch) {
    struct vinfo *nv; /* new version goes here */
    struct claz *claz = template->claz;
    u_int32_t size = claz->size;/* size including header */
    struct claz *cclaz;
    if (NULL != (cclaz=claz->component_claz)) { /* is an array */
	int elsize = isPrimitive(cclaz) ? cclaz->size : sizeof(ptroff_t);
	size += (elsize * ((struct aarray *)template)->length);
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
	if (!nv->wnext) nv->wnext=nv->anext;
	infl->first_version = nv;
    }
    return nv;
}

/* get the state of a version, doing pruning. (StateP') */
static inline jint StateP1(struct commitrec **cp) {
  struct commitrec *c = *cp;
  jint s = COMMITTED;
  jboolean l = JNI_FALSE;
  while (c!=NULL) {
    s = c->state; /* atomic */
    if (s!=COMMITTED) break;
    c = c->parent;
    l = JNI_TRUE;
  }
  if (l) { *cp=c; /* atomic, one hopes */ }
  return s;
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
    /* below this point it is the StateP'' function from the writeup */
    switch (s) {
    case WAITING:
#if RACES
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
  struct claz *thisclz = FNI_GetClassInfo(type)->claz;
  return isPrimitive(thisclz) ? thisclz->size : sizeof(ptroff_t);
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
    struct tlist *r, *rp;
    if (!FNI_IS_INFLATED(_this)) FNI_InflateObject(env, _this);
    /* get first_version */
    infl = FNI_UNWRAP(_this)->hashunion.inflated;
    v = infl->first_version;
    if (v==NULL) v = CreateNewVersion(infl, NULL, FNI_UNWRAP(_this), JNI_TRUE);
    /* make sure we're on the readers list */
    again:
    if (v->transid==c) goto has_version_and_listed;
    r = &(v->readers); rp=NULL;
    do {
	if (r->transid==c) goto has_version_and_listed; /* we're on! */
	/* list maintenance: prune this link out if not WAITING */
	if (StateP1(&r->transid)!=WAITING)
	  if (rp) rp->next=r->next; else r->transid=NULL; /* XXX: race here */
	rp = r; r = r->next;
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
    struct commitrec *c = (struct commitrec *) FNI_UNWRAP(commitrec);
    struct inflated_oobj *infl;
    struct vinfo *v;
    struct tlist *r;
    if (!FNI_IS_INFLATED(_this)) FNI_InflateObject(env, _this);
    /* get first version */
    infl = FNI_UNWRAP(_this)->hashunion.inflated;
    v = infl->first_version;
    if (v==NULL) v = CreateNewVersion(infl, NULL, FNI_UNWRAP(_this), JNI_TRUE);
    /* find last committed transaction */
    again:
    if (v->transid==c) goto done;
    switch (StateP2(infl, v)) {
    case COMMITTED: break; /* go on and kill readers */
    case WAITING:
	/* abort unless c is a non-aborted subtransaction of v->transid */
	if (!IsNAST(c, v)) goto suicide;
	else break; /* go on and kill readers */
    default: /* ABORTED */
	v = v->anext;
	goto again; /* try again */
    }
    /* kill all readers except my parents */
    r = &(v->readers);
    do {
	struct commitrec *rc=r->transid, *vc=v->transid, *cp;
	if (rc==NULL) goto nextreader; /* skip null readers */
	for (cp=c; ; cp=cp->parent) {
	    if (cp==rc) goto nextreader;/* rc is a parent */
	    if (cp==vc) break; /* needn't go any higher */
	}
	/* not a parent.  kill it. */
	AbortCR(rc);
    nextreader:
	r = r->next;
    } while (r!=NULL);
    /* XXX: race condition: new non-parent created before NewVersion */
    v = CreateNewVersion(infl, c, &v->obj, JNI_FALSE);
    done:
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
 * Method:    getReadCommittedVersion
 * Signature: ()Ljava/lang/Object;
 */
    /** Get the most recently committed version to read from. */
JNIEXPORT jobject JNICALL Java_java_lang_Object_getReadCommittedVersion
    (JNIEnv *env, jobject _this) {
    struct inflated_oobj *infl;
    struct vinfo *v;
    int u = 0;
    assert(FNI_IS_INFLATED(_this));
    infl = FNI_UNWRAP(_this)->hashunion.inflated;
    v = infl->first_version;
    while (1) {
	assert(v!=NULL);
	switch (StateP2(infl, v)) {
	case COMMITTED: goto done;
	case WAITING: u++; v=v->wnext; break;
	default: /* ABORTED */ v=v->anext; break;
	}
    }
    done:
#if RACES
    /* XXX: unflag some fields if u==0.  Don't know how to do this. */
#endif
    return FNI_WRAP(&(v->obj));
}

/*
 * Class:     java_lang_Object
 * Method:    getWriteCommittedVersion
 * Signature: ()Ljava/lang/Object;
 */
    /** Get the most recently committed version to write to (and read from). */
JNIEXPORT jobject JNICALL Java_java_lang_Object_getWriteCommittedVersion
    (JNIEnv *env, jobject _this) {
    struct inflated_oobj *infl;
    struct vinfo *v;
    struct tlist *r;
    /* xxx: possibly downgrade this field to avoid an abort? */
    assert(FNI_IS_INFLATED(_this));
    infl = FNI_UNWRAP(_this)->hashunion.inflated;
    v = infl->first_version;
    while (AbortCR(v->transid) != COMMITTED) {
	/* prune list for the sake of writes w/o intervening reads */
	struct vinfo *nextv = v->anext;
	/* we remove v carefully, knowing that v could already be removed. */
	compare_and_swapP(&(infl->first_version), v, nextv);
	v = nextv;
    }
    /* v is a committed transaction. */
    v->anext = v->wnext = NULL; /* atomic stores, hopefully */
    /* abort all readers of this committed transaction */
    for (r=&(v->readers); r!=NULL; r=r->next)
	AbortCR(r->transid);
    /* done.  return v. */
    return FNI_WRAP(&(v->obj));
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
    struct inflated_oobj *infl;
    struct vinfo *v;
    if (!FNI_IS_INFLATED(_this)) FNI_InflateObject(env, _this);
    /* are there any versions? */
    infl = FNI_UNWRAP(_this)->hashunion.inflated;
    v = infl->first_version;
    if (v==NULL) v = CreateNewVersion(infl, NULL, FNI_UNWRAP(_this), JNI_TRUE);
    /* now that there's at least one version, find one that's committed. */
    /* XXX: if getWriteCommittedVersion does downgrades, we want to
     *      explicitly *NOT* do one in this case. */
    return Java_java_lang_Object_getWriteCommittedVersion(env, _this);
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

#define WRITEFLAG(type, size, FLAG) \
inline void Java_java_lang_Object_writeFlag##size \
    (JNIEnv *env, jobject _this, jint offset) { \
    struct oobj *oobj = FNI_UNWRAP(_this); \
    struct inflated_oobj *infl=oobj->hashunion.inflated; \
    struct vinfo *v; \
    type f; \
    assert(FNI_IS_INFLATED(_this)); \
    FLEX_MUTEX_LOCK(&infl->mutex); \
    f = *((type *)(((void *)oobj)+offset)); \
    if (f==FLAG) goto done; /* flagged before we got the lock. */ \
    for (v=infl->first_version; v!=NULL; v=v->anext) \
	*((type *)(((void *)&(v->obj))+offset)) = f; \
    *((type *)(((void *)oobj)+offset)) = FLAG; \
    done: \
    FLEX_MUTEX_UNLOCK(&infl->mutex); \
}
WRITEFLAG(jbyte,  1, ((jbyte)  FLAG_VALUE))
WRITEFLAG(jshort, 2, ((jshort) FLAG_VALUE))
WRITEFLAG(jint,   4, ((jint)   FLAG_VALUE))
WRITEFLAG(jlong,  8, (         FLAG_VALUE))

/*
 * Class:     java_lang_Object
 * Method:    writeFlag
 * Signature: (II)V
 */
    /** Ensure that a flag exists at the specified offset and size from
     *  this object. */
JNIEXPORT void JNICALL Java_java_lang_Object_writeFlag
    (JNIEnv *env, jobject _this, jint offset, jint size) {
  switch(size) {
  case 1: Java_java_lang_Object_writeFlag1(env, _this, offset); break;
  case 2: Java_java_lang_Object_writeFlag2(env, _this, offset); break;
  case 4: Java_java_lang_Object_writeFlag4(env, _this, offset); break;
  case 8: Java_java_lang_Object_writeFlag8(env, _this, offset); break;
  default: assert(0);
  }
}

/* -------------- utility adapter functions -------------- */
jstring FNI_StrTrans2Str(JNIEnv *env, jobject commitrec, jstring string) {
  /* extract char array from string. */
  jclass strcls = (*env)->FindClass(env, "java/lang/String");
  jmethodID mid = (*env)->GetMethodID(env, strcls, "toCharArray$$withtrans",
				      "(L" TRANSPKG "CommitRecord;)[C");
  jcharArray ca = (jcharArray) (*env)->CallObjectMethod(env, string, mid,
							commitrec);
  /* now de-transactionify the char array */
  jsize i, length = (*env)->GetArrayLength(env, ca);
  for (i=0; i<length; i++)
    Java_java_lang_Object_writeFlag2(env, ca, sizeof(struct aarray)+(2*i));
  ca = (jcharArray)
    Java_java_lang_Object_getReadableVersion(env, ca, commitrec);
  /* and create a new string */
  mid = (*env)->GetMethodID(env, strcls, "<init>", "([C)V");
  return (jstring) (*env)->NewObject(env, strcls, mid, ca);
}
