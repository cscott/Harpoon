/* transact.h --- definitions of transaction structures. */

#ifndef INCLUDED_TRANSACT_H
#define INCLUDED_TRANSACT_H

#include "jni-private.h"

/* Commit record information. Commit records are full-fledged objects. */
#include "harpoon_Runtime_Transactions_CommitRecord.h"
struct commitrec {
    struct oobj header;
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

#endif /* INCLUDED_TRANSACT_H */
