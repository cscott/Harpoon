/* Definitions of 'safe' types related to transactions. */
#ifndef INCLUDED_TRANSACT_TY_H
#define INCLUDED_TRANSACT_TY_H

/* a pair of versions; returned by findVersion. */
struct versionPair {
  struct vinfo *committed;
  struct vinfo *waiting;
};

/* enumerated types */
enum opstatus { SUCCESS=-1, SAW_FALSE_FLAG=0, SAW_RACE=1, SAW_RACE_CLEANUP };
enum killer { KILL_WRITERS=-1, KILL_ALL=0 };

/* opaque type here, because it references oobj, etc. */
struct commitrec;

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

#endif /* INCLUDED_TRANSACT_TY_H */
