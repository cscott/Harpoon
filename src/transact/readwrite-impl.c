/** This file defines read and write operations in the presence of
 *  transactions.  These functions are inlined into emitted code. */

// assume VALUETYPE and VALUENAME are defined
// allhashimpl.h must have been included

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */
// could define DECL to be "extern inline"
#define DECL

// prototypes.
#if defined(IN_READWRITE_HEADER)
extern VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset);
extern VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
				 struct vinfo *version, struct commitrec *cr);
extern void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
			      VALUETYPE value);
extern void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
			     VALUETYPE value, struct vinfo *version);
// used in version-impl.c: copy_back
extern VALUETYPE TA(readFromVersion)(struct vinfo *version, unsigned offset);
extern void TA(writeToVersion)(struct oobj *obj, struct vinfo *version,
			       unsigned offset, VALUETYPE value);

#if 0
// external references:
//  from versions.c:
extern struct versionPair findVersion(struct oobj *obj, struct commitrec *cr);
extern enum opstatus TA(copyBackField)(struct oobj *obj, unsigned offset,
                                       enum killer kill_whom);
extern struct vinfo *EXACT_ensureWriter(struct oobj *obj,
					struct commitrec *currentTrans);
extern void TA(EXACT_checkWriteField)(struct oobj *obj, unsigned offset);
#endif

#endif /* IN_READWRITE_HEADER */


// for heterogenerous objects, should just use a word-sized version table,
// and convert types as necessary (including splitting longs/doubles into
// two chunks).  For arrays, however, we might as well do the Right thing
// since the array is homogeneous.


#if (!defined(IN_READWRITE_HEADER)) && (!defined(DONT_REALLY_DO_TRANSACTIONS))

// XXX these should certainly be inlined!!!
/////////////////////////////////////////////////////////////////////
// reading from versions.
DECL VALUETYPE TA(readFromVersion)(struct vinfo *version, unsigned offset) {
#if !defined(ARRAY) // this expression is probably not constant for arrays.
  //assert(__builtin_constant_p(offset<=OBJ_CHUNK_SIZE-sizeof(VALUETYPE)));
#endif
  if ((!DO_HASH) || offset <= OBJ_CHUNK_SIZE-sizeof(VALUETYPE))
    return *(VALUETYPE*)(DIRECT_FIELDS(version)+offset);
  else {
#if defined(ARRAY)
    // arrays are homogeneous.
    return T(version_hash_read)
      ((struct T(version_hashtable) *) HASHED_FIELDS(version),
       offset);
#else
    // because this is heterogeneous, use a fixed jint-sized hashtable.
    // read from this and convert to desired type.
    struct version_hashtable_Int *table =
      (struct version_hashtable_Int *) HASHED_FIELDS(version);
    return T(version_hash_read_Int_to)(table, offset);
#endif
  }
}

// writing to versions.
DECL void TA(writeToVersion)(struct oobj *obj, struct vinfo *version,
			     unsigned offset, VALUETYPE value) {
#if !defined(ARRAY) // this expression is probably not constant for arrays.
  //assert(__builtin_constant_p(offset<=OBJ_CHUNK_SIZE-sizeof(VALUETYPE)));
#endif
  if ((!DO_HASH) || offset <= OBJ_CHUNK_SIZE-sizeof(VALUETYPE)) {
    *(VALUETYPE*)(DIRECT_FIELDS(version)+offset) = value;
    return; // done!
  } else {
#if defined(ARRAY)
    // arrays are homogeneous, so use the right version of the hashtable.
    struct T(version_hashtable) *table =
      (struct T(version_hashtable) *) HASHED_FIELDS(version);
    T(version_hash_write)(table, offset, value);
#else
    // because this is heterogeneous, use a fixed jint-sized hashtable.
    struct version_hashtable_Int *table =
      (struct version_hashtable_Int *) HASHED_FIELDS(version);
    T(version_hash_write_Int_from)(table, offset, value);
#endif
    return; // success!
  }
}


///////////////////////////////////////////////////////////////////////
// Now deep transaction magic.
///////////////////////////////////////////////////////////////////////

DECL VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset) {
  INCREMENT_STATS(transact_readnt, 1);
  do {
    VALUETYPE f = *(VALUETYPE*)(FIELDBASE(obj) + offset);
    if (likely(f!=T(TRANS_FLAG))) return f;
    if (unlikely(SAW_FALSE_FLAG ==
		 TA(copyBackField)(obj, offset, KILL_WRITERS))) {
      INCREMENT_STATS(transact_false_flag_read, 1);
      return T(TRANS_FLAG); // "false" transaction: field really is FLAG.
    }
    // okay, we've done a copy-back now.  retry.
  } while(1);
}

// must have already recorded itself as a reader (set flags, etc)
// XXX WANT TO RETURN THE NEW VERSION AS WELL AS THE VALUE.
DECL VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
			       struct vinfo *version,
			       struct commitrec *cr) {
  struct versionPair vp;
  /* version may be null. */
  /* we should always either be on the readerlist or aborted here. */
  do {
    VALUETYPE f = *(VALUETYPE*)(FIELDBASE(obj) + offset);
    if (likely(f!=T(TRANS_FLAG))) return f; /* not yet involved in xaction. */
    // object field is FLAG.
    if (likely(version!=NULL))
      return TA(readFromVersion)(version, offset); /* done! */
    vp = findVersion(obj, cr);
    if (vp.waiting==NULL) {  /* use value from committed version. */
      assert(vp.committed!=NULL);
      return TA(readFromVersion)(vp.committed, offset); /*perhaps false flag?*/
    }
    version = vp.waiting; /* XXX this should affect caller as well */
    /* try, try again. */
  } while(1);
}

DECL void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
			    VALUETYPE value) {
  INCREMENT_STATS(transact_writent, 1);
  if (
#if 1 /* XXX: should be '#ifndef HAS_64_BIT_STORE_CONDITIONAL' */
      (sizeof(VALUETYPE) <= sizeof(jint)) &&
#endif
      likely(value != T(TRANS_FLAG))) {
    do {
      // LL(readerList)/SC(field)
      if (likely(LL(OBJ_READERS_PTR(obj))==NULL) &&
	  // note that T(store_conditional) is a bit of a hack.
	  likely(T(store_conditional)(FIELDBASE(obj), offset, value)))
	return; // done!
      // unsuccessful LL or SC
      TA(copyBackField)(obj, offset, KILL_ALL); // ignore return status
    } while(1);
  } else { // need to create a false flag
    /* implement this as a short transactional write.  this may be slow,
     * but it greatly reduces the race conditions we have to think about. */
    jint st;
#ifdef WITH_STATISTICS
    if (unlikely(value == T(TRANS_FLAG)))
      INCREMENT_STATS(transact_false_flag_write, 1);
    else
      INCREMENT_STATS(transact_long_write, 1);
#endif /* WITH_STATISTICS */
    do {
      struct commitrec *tid = AllocCR();
      struct vinfo *ver = EXACT_ensureWriter(obj, tid);
      TA(EXACT_checkWriteField)(obj, offset);
      TA(EXACT_writeT)(obj, offset, value, ver);
      st = CommitCR(tid);
    } while (st!=COMMITTED);
  }
}

// prior to this point, we should have recorded the write
// (guaranteed that the object field had the FLAG value, and that flag bits
//  are set)
DECL void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
			   VALUETYPE value, struct vinfo *version) {
  // version should always be non-null
  // this is easy!
  TA(writeToVersion)(obj, version, offset, value);
}
#endif /* IN_READWRITE_HEADER */

/* clean up after ourselves */
#include "transact/preproc.h"
#undef DECL
