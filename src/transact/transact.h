/* transact.h --- definitions of transaction structures. */

#ifndef INCLUDED_TRANSACT_H
#define INCLUDED_TRANSACT_H

#include "jni-private.h"
//#include "asm/atomicity.h" /* for compare_and_swap */
#include "asm/llsc.h" /* for load-linked */
#include "config.h" /* for OBJECT_PADDING */

/* package name for CommitRecord & etc */
#define TRANSPKG "harpoon/Runtime/Transactions/"
/* flag value to denote 'not here' */
#define FLAG_VALUE (0xCACACACACACACACALL)

// OBJ_CHUNK_SIZE must be larger than zero and divisible by eight
#ifndef OBJ_CHUNK_SIZE
# define OBJ_CHUNK_SIZE 24
#endif
#define INITIAL_CACHE_SIZE 24

/* Commit record information. Commit records are full-fledged objects. */
#include "harpoon_Runtime_Transactions_CommitRecord.h"
struct commitrec {
    struct oobj header;
    char _padding_[OBJECT_PADDING]; /* by default, OBJECT_PADDING is zero */
    /* keep the order of these fields synchronized with CommitRecord.java */
    struct commitrec *parent;/* Transaction that this depends upon, if any. */
#   define WAITING   /*0*/ harpoon_Runtime_Transactions_CommitRecord_WAITING
#   define COMMITTED /*1*/ harpoon_Runtime_Transactions_CommitRecord_COMMITTED
#   define ABORTED   /*2*/ harpoon_Runtime_Transactions_CommitRecord_ABORTED
    jint state; /* initialized to W and write-once to C or A */
#ifdef COMMITREC_PRIVATE /* private variables */
    jint retry_count;
#endif
};

/* A simple linked list of transaction identifiers */
struct tlist {
    struct commitrec *transid;
    struct tlist *next; /* next version */
};

/* The vinfo structure sits above a versioned object and provides
 * versioning information. */
struct vinfo {
    struct commitrec *transid; /* transaction id */ 
#if 0
    struct tlist readers; /* list of readers.  first node is inlined. */
#endif
    /* anext is the 'real' next version, which may be a parent of this one. */
    /* wnext is the "next transaction not my parent" */
    struct vinfo *anext; /* next version to look at if transid is aborted. */
    struct vinfo *wnext; /* next version to look at if transid is waiting. */
    /* cached values are below this point. */
    char _direct_fields[0/*OBJ_CHUNK_SIZE*/];
    char _hashed_fields[0/* n times INITIAL_CACHE_SIZE */];
};
#define DIRECT_FIELDS(v) ((void*)v->_direct_fields)
#define HASHED_FIELDS(v) ((void*)v->_direct_fields+OBJ_CHUNK_SIZE)

/* functions on commit records */
static inline jint CommitCR(struct commitrec *cr) {
    jint s;
    if (cr==NULL) return COMMITTED;
    do {
	/* avoid the atomic operation if possible */
	if (WAITING != (s = LL(&cr->state))) return s;
	/* atomically set to ABORTED */
	if (SC(&(cr->state), COMMITTED))
	    return COMMITTED;
    } while (1);
}
static inline jint AbortCR(struct commitrec *cr) {
    jint s;
    if (cr==NULL) return COMMITTED;
    do {
	/* avoid the atomic operation if possible */
	if (WAITING != (s = LL(&cr->state))) return s;
	/* atomically set to ABORTED */
	if (SC(&(cr->state), ABORTED))
	    return ABORTED;
    } while (1);
}
static inline jint AbortCRorParent(struct commitrec *cr) {
  // go up the chain until we either find a transaction we can abort,
  // or we can report that this transaction is *completely* committed.
  for (; cr!=NULL; cr=cr->parent)
    if (AbortCR(cr)==ABORTED)
      return ABORTED;
  return COMMITTED;
}

#endif /* INCLUDED_TRANSACT_H */
