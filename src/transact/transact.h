/* transact.h --- definitions of transaction structures. */

#ifndef INCLUDED_TRANSACT_H
#define INCLUDED_TRANSACT_H

#include "jni-private.h"
#include "asm/atomicity.h" /* for compare_and_swap */
#include "asm/llsc.h" /* for load-linked */
#include "config.h" /* for OBJECT_PADDING */

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
    struct tlist readers; /* list of readers.  first node is inlined. */
    struct vinfo *anext; /* next version to look at if transid is aborted. */
    struct vinfo *wnext; /* next version to look at if transid is waiting. */
    /* a regular object structure is below this point */
    struct oobj obj;
};

/* functions on commit records */
static inline jint CommitCR(struct commitrec *cr) {
    jint s;
    if (cr==NULL) return COMMITTED;
    do {
	/* avoid the atomic operation if possible */
	if (WAITING != (s = cr->state)) return s;
	/* atomically set to ABORTED */
	compare_and_swap(&(cr->state), WAITING, COMMITTED); /* atomic */
    } while (1);
}
static inline jint AbortCR(struct commitrec *cr) {
    jint s;
    if (cr==NULL) return COMMITTED;
    do {
	/* avoid the atomic operation if possible */
	if (WAITING != (s = cr->state)) return s;
	/* atomically set to ABORTED */
	compare_and_swap(&(cr->state), WAITING, ABORTED); /* atomic */
    } while (1);
}

/* ---------------------- utility adapter functions ----------------- */
/* Returns a pointer to a new "nontransactional" string with the same
 * contents as the given "transactional" string. */
extern jstring FNI_StrTrans2Str(JNIEnv *env, jobject commitrec, jstring str);


#endif /* INCLUDED_TRANSACT_H */
