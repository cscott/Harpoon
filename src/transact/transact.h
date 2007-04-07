/* transact.h --- definitions of transaction structures. */

#ifndef INCLUDED_TRANSACT_H
#define INCLUDED_TRANSACT_H

#include "jni-private.h"
//#include "asm/atomicity.h" /* for compare_and_swap */
#include "asm/llsc.h" /* for load-linked */
#include "config.h" /* for OBJECT_PADDING */
#include "transact/transact-ty.h" /* versionPair, opstatus, killer */

/* package name for CommitRecord & etc */
#define TRANSPKG "harpoon/Runtime/Transactions/"
/* flag value to denote 'not here' */
#define FLAG_VALUE (0xCACACACACACACACALL)

// OBJ_CHUNK_SIZE must be larger than zero and divisible by eight
#define DO_HASH 0 // set to 1 to enable object chunking at all
#ifndef OBJ_CHUNK_SIZE
# define OBJ_CHUNK_SIZE (0x80000000/*24*/)
#endif
#define INITIAL_CACHE_SIZE 24

/* Commit record information. Commit records are full-fledged objects. */
#include "harpoon_Runtime_Transactions_CommitRecord.h"
struct commitrec {
    struct oobj header;
    char _padding_[OBJECT_PADDING]; /* by default, OBJECT_PADDING is zero */
    /* keep the order of these fields synchronized with CommitRecord.java */
    struct commitrec *parent;/* Transaction that this depends upon, if any. */
    jint state; /* initialized to W and write-once to C or A */
#ifdef COMMITREC_PRIVATE /* private variables */
    jint retry_count;
#endif
};
enum commitstatus {
  WAITING=harpoon_Runtime_Transactions_CommitRecord_WAITING,     /* 0 */
  COMMITTED=harpoon_Runtime_Transactions_CommitRecord_COMMITTED, /* 1 */
  ABORTED=harpoon_Runtime_Transactions_CommitRecord_ABORTED      /* 2 */
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
    /* anext is the 'real' next version, which may be a parent of this one. */
    /* wnext is the "next transaction not my parent" */
    struct vinfo *anext; /* next version to look at if transid is aborted. */
    struct vinfo *wnext; /* next version to look at if transid is waiting. */
#endif
    struct vinfo *next; /* simple linked list of versions. */
    /* cached values are below this point. */
    char _direct_fields[0/*OBJ_CHUNK_SIZE*/];
    char _hashed_fields[0/* n times INITIAL_CACHE_SIZE */];
};
#define DIRECT_FIELDS(v) ((void*)v->_direct_fields)
#define HASHED_FIELDS(v) ((void*)v->_direct_fields+OBJ_CHUNK_SIZE)

/* functions on commit records */
static inline struct commitrec *AllocCR() {
#ifdef BDW_CONSERVATIVE_GC
  // must zero-fill.
#ifdef GC_malloc_atomic
# error "We want the real GC_malloc_atomic, not GC_malloc_atomic_trans"
#endif
  // XXX: shouldn't be atomic if we ever have nested transactions.
  return GC_malloc_atomic(sizeof(struct commitrec));
#else
  extern void *calloc(size_t nmemb, size_t size);
  return calloc(1, sizeof(struct commitrec));
#endif
}
static inline enum commitstatus CommitCR(struct commitrec *cr) {
    enum commitstatus s;
    if (cr==NULL) return COMMITTED;
    do {
	/* avoid the atomic operation if possible */
	if (WAITING != (s = LL(&cr->state))) return s;
	/* atomically set to ABORTED */
	if (SC(&(cr->state), COMMITTED))
	    return COMMITTED;
    } while (1);
}
static inline enum commitstatus AbortCR(struct commitrec *cr) {
    enum commitstatus s;
    if (cr==NULL) return COMMITTED;
    do {
	/* avoid the atomic operation if possible */
	if (WAITING != (s = LL(&cr->state))) return s;
	/* atomically set to ABORTED */
	if (SC(&(cr->state), ABORTED))
	    return ABORTED;
    } while (1);
}
#if 0
static inline enum commitstatus AbortCRorParent(struct commitrec *cr) {
  // go up the chain until we either find a transaction we can abort,
  // or we can report that this transaction is *completely* committed.
  for (; cr!=NULL; cr=cr->parent)
    if (AbortCR(cr)==ABORTED)
      return ABORTED;
  return COMMITTED;
}
/** Return true iff cr1 is a subtransaction of cr2. */
DECL int isSubtransaction(struct commitrec *cr1,
			  struct commitrec *cr2) {
  // xxx faster test possible.
  for ( ; cr1!=NULL; cr1=cr1->parent)
    if (cr1==cr2)
      return 1;
  return 0;
}
#endif

#endif /* INCLUDED_TRANSACT_H */
