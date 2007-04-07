/* transact.h --- definitions of transaction structures. */

#ifndef INCLUDED_TRANSACT_H
#define INCLUDED_TRANSACT_H

#include "jni-private.h"
//#include "asm/atomicity.h" /* for compare_and_swap */
#include "asm/llsc.h" /* for load-linked */
#include "config.h" /* for OBJECT_PADDING */
#include "transact/transact-config.h" /* for DO_HASH, chunk size, etc. */
#include "transact/transact-ty.h" /* versionPair, opstatus, killer */

/* package name for CommitRecord & etc */
#define TRANSPKG "harpoon/Runtime/Transactions/"
/* flag value to denote 'not here' */
#define FLAG_VALUE (0xCACACACACACACACALL)

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
