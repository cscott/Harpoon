/** This file defines read and write operations in the presence of
 *  transactions.  These functions are inlined into emitted code. */

// assume VALUETYPE and VALUENAME are defined
// allhashimpl.h must have been included

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */
// could define DECL to be "extern inline"
#define DECL

// prototypes.
#if defined(IN_READWRITE_HEADER)
extern VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset,
				  unsigned flag_offset, unsigned flag_bit);
extern VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
				 struct vinfo *version, struct commitrec *cr);
extern void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
			      VALUETYPE value,
			      unsigned flag_offset, unsigned flag_bit);
extern void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
			     VALUETYPE value, struct vinfo *version);
// used in version-impl.c: copy_back
extern VALUETYPE TA(readFromVersion)(struct vinfo *version, unsigned offset);
#endif /* IN_READWRITE_HEADER */


// for heterogenerous objects, should just use a word-sized version table,
// and convert types as necessary (including splitting longs/doubles into
// two chunks).  For arrays, however, we might as well do the Right thing
// since the array is homogeneous.


#if !defined(IN_READWRITE_HEADER)

/////////////////////////////////////////////////////////////////////
// reading from versions.
DECL VALUETYPE TA(readFromVersion)(struct vinfo *version, unsigned offset) {
#if !defined(ARRAY) // this expression is probably not constant for arrays.
  //assert(__builtin_constant_p(offset<=OBJ_CHUNK_SIZE-sizeof(VALUETYPE)));
#endif
  if (offset <= OBJ_CHUNK_SIZE-sizeof(VALUETYPE))
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
  if (offset <= OBJ_CHUNK_SIZE-sizeof(VALUETYPE)) {
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

DECL VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset,
				unsigned flag_offset, unsigned flag_bit) {
  do {
    VALUETYPE f = *(VALUETYPE*)(FIELDBASE(obj) + offset);
    if (likely(f!=T(TRANS_FLAG))) return f;
    // now, it would seem that we need to keep anyone from committing a
    // more recent version now, but actually we only need to guarantee that
    // we get *a* value for this field (i.e. that no one collects or
    // scribbles on "version" between when we look it up and when we read it.
    if (unlikely(SAW_FALSE_FLAG ==
		 TA(getVersion_readNT)(obj, offset, flag_offset, flag_bit)))
      return T(TRANS_FLAG); // "false" transaction: field really is FLAG.
    // okay, we've done a copy-back now.  retry.
  } while(1);
}

// must have already recorded itself as a reader (set flags, etc)
DECL VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
			       struct vinfo *version,
			       struct commitrec *cr) {
  // version may be null, if there are no transactional writes outstanding.
  do {
    VALUETYPE f = *(VALUETYPE*)(FIELDBASE(obj) + offset);
    if (likely(f!=T(TRANS_FLAG))) return f;
    // now, it would seem that we need to keep anyone from committing a
    // more recent version now, but actually we only need to guarantee that
    // we get *a* value for this field (i.e. that no one collects or
    // scribbles on "version" between when we look it up and when we read it.
    //  xxx think about copy back.  i think we're okay here.
#if BROKEN
    if (version!=NULL) break;
#endif
    version = getVersion_readT(obj, cr);
    // version could be null again if we copy back a completed transaction.
  } while (unlikely(version==NULL));
  assert(version!=NULL);
  // XXX if we're reading from a COMMITTED version (and we may), parallel
  // writes may be made to it (behind our back) to implement a "false
  // transaction"
  //   is this a problem?
  return TA(readFromVersion)(version, offset);
}

DECL void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
			    VALUETYPE value,
			    unsigned flag_offset, unsigned flag_bit) {
  while (1) {
    if (unlikely(0!=(flag_bit & LL((jint*)(FLAGBASE(obj)+flag_offset))))) {
      // oops, gotta kill the readers
      TA(getVersion_writeNT)(obj, offset, flag_offset, flag_bit);
      // copies back the field and and always returns with read flag cleared.
      // So try again.  (note, other read flags will likely remain set)
    } else if (unlikely(value==T(TRANS_FLAG))) {
      // special means to write a "real" value of FLAG to the field.
      assert(0); // XXX write me.
    } else {
      // note that T(store_conditional) is a bit of a hack.
      if (likely(T(store_conditional)(FIELDBASE(obj), offset, value))) {
	// okay, the store succeeded
	return;
      }
      // hmm.  concurrent modification.  Try again.
    }
  }
}

// prior to this point, we should have recorded the write
// (guaranteed that the object field had the FLAG value, and that flag bits
//  are set)
DECL void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
			   VALUETYPE value, struct vinfo *version) {
  // version should always be non-null
  TA(writeToVersion)(obj, version, offset, value);
}
#endif /* IN_READWRITE_HEADER */

/* clean up after ourselves */
#include "transact/preproc.h"
#undef DECL
