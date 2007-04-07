/* Fast-path readNT/readT writeNT/writeT code */

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */

#ifdef IN_FASTPATH_HEADER
# define DECL extern inline
#else
# define DECL
#endif

// fast path stuff from versions-impl /////////////////
#if defined(NO_VALUETYPE)
extern struct vinfo *EXACT_ensureReader_full
    (struct oobj *obj, struct commitrec *cr) __attribute__((noinline));
DECL struct vinfo *EXACT_ensureReader(struct oobj *obj, struct commitrec *cr) {
    struct tlist * volatile *readers, *r;
    struct vinfo * volatile *versions, *first_version;
    struct commitrec *vtid;
    ENSURE_HAS_TRANSACT_INFO(obj); // now all other stuff doesn't have to.
    // fast path of ensureReaderList //////////////
    readers = OBJ_READERS_PTR(obj);
    r = *readers;
    if (likely(r!=NULL) && likely(r->transid == cr)) {
	// okay, we're on the reader list.
	// fast path of findVersion ///////////////////////
	versions = OBJ_VERSION_PTR(obj);
	first_version = *versions;
	if (first_version==NULL)
	    return NULL; /* no versions */
	vtid = first_version->transid; /* use local copy to avoid races */
	if (vtid==cr)
	    /* found a version: ourself! */
	    return first_version;
    }
    return EXACT_ensureReader_full(obj, cr);
}
extern struct vinfo *EXACT_ensureWriter_full
    (struct oobj *obj, struct commitrec *cr) __attribute__((noinline));
DECL struct vinfo *EXACT_ensureWriter(struct oobj *obj, struct commitrec *cr) {
    struct vinfo * volatile *versions, *first_version;
    struct commitrec *vtid;
    ENSURE_HAS_TRANSACT_INFO(obj); // now all other stuff doesn't have to.
    // fast path of findVersion ////////////
    versions = OBJ_VERSION_PTR(obj);
    first_version = *versions;
    if (likely(first_version!=NULL)) {
	vtid = first_version->transid; /* use local copy to avoid races */
	if (likely(vtid==cr))
	    /* found a version: ourself! */
	    return first_version;
    }
    return EXACT_ensureWriter_full(obj, cr);
}				      

#else /* !NO_VALUETYPE */

DECL void TA(EXACT_checkReadField)(struct oobj *obj, unsigned offset) {
  /* do nothing: no per-field read stats are kept. */
}

extern void TA(EXACT_checkWriteField_full)(struct oobj *obj, unsigned offset,
					   VALUETYPE canonical)
     __attribute__((noinline));
DECL void TA(EXACT_checkWriteField)(struct oobj *obj, unsigned offset) {
  /* set write flag, if not already set. */
  VALUETYPE canonical = *(VALUETYPE volatile *)(FIELDBASE(obj) + offset);
  if (likely(canonical==T(TRANS_FLAG))) return; /* done: flag already set. */
  // okay, slow path.  Note we've added 'canonical' to the passed params.
  TA(EXACT_checkWriteField_full)(obj, offset, canonical);
}

// fast path stuff from readwrite-impl ////////////////
/* non-transactional read */
extern VALUETYPE TA(EXACT_readNT_full)(struct oobj *obj, unsigned offset)
     __attribute__((noinline));
DECL VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset) {
    INCREMENT_STATS(transact_readnt, 1);
    VALUETYPE f = *(VALUETYPE*)(FIELDBASE(obj) + offset);
    if (likely(f!=T(TRANS_FLAG))) return f;
    return TA(EXACT_readNT_full)(obj, offset);
}

/* non-transactional write */
extern void TA(EXACT_writeNT_full)(struct oobj *obj, unsigned offset,
				   VALUETYPE value)
     __attribute__((noinline));
DECL void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
			    VALUETYPE value) {
  INCREMENT_STATS(transact_writent, 1);
  if (
#if 1 /* XXX: should be '#ifndef HAS_64_BIT_STORE_CONDITIONAL' */
      (sizeof(VALUETYPE) <= sizeof(jint)) &&
#endif
      likely(value != T(TRANS_FLAG)) &&
      // LL(readerList)/SC(field)
      likely(LL(OBJ_READERS_PTR(obj))==NULL) &&
	  // note that T(store_conditional) is a bit of a hack.
      likely(T(store_conditional)(FIELDBASE(obj), offset, value)))
      return; // done!
  TA(EXACT_writeNT_full)(obj, offset, value);
}

// reading from versions
extern VALUETYPE TA(readFromVersion_full)(struct vinfo *version,
					  unsigned offset);
DECL VALUETYPE TA(readFromVersion)(struct vinfo *version, unsigned offset)
     __attribute__((always_inline));
DECL VALUETYPE TA(readFromVersion)(struct vinfo *version, unsigned offset) {
  if ((!DO_HASH) || offset <= OBJ_CHUNK_SIZE-sizeof(VALUETYPE))
    return *(VALUETYPE*)(DIRECT_FIELDS(version)+offset);
  else return TA(readFromVersion_full)(version, offset);
}

// writing to versions.
extern void TA(writeToVersion_full)(struct oobj *obj, struct vinfo *version,
				    unsigned offset, VALUETYPE value);
DECL void TA(writeToVersion)(struct oobj *obj, struct vinfo *version,
			     unsigned offset, VALUETYPE value)
     __attribute__((always_inline));
DECL void TA(writeToVersion)(struct oobj *obj, struct vinfo *version,
			     unsigned offset, VALUETYPE value) {
  if ((!DO_HASH) || offset <= OBJ_CHUNK_SIZE-sizeof(VALUETYPE))
    *(VALUETYPE*)(DIRECT_FIELDS(version)+offset) = value;
  else TA(writeToVersion_full)(obj, version, offset, value);
}


/* transactional read */
extern VALUETYPE TA(EXACT_readT_full)(struct oobj *obj, unsigned offset,
				      struct vinfo *version,
				      struct commitrec *cr)
     __attribute__((noinline));
DECL VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
			       struct vinfo *version,
			       struct commitrec *cr) {
  VALUETYPE f = *(VALUETYPE*)(FIELDBASE(obj) + offset);
  if (likely(f!=T(TRANS_FLAG))) return f; /* not yet involved in xaction. */
  // object field is FLAG.
  if (likely(version!=NULL))
    return TA(readFromVersion)(version, offset); /* done! */
  return TA(EXACT_readT_full)(obj, offset, version, cr);
}

/* transactional write */
DECL void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
			   VALUETYPE value, struct vinfo *version) {
  // version should always be non-null
  // this is easy!
  TA(writeToVersion)(obj, version, offset, value);
}
#endif /* NO_VALUETYPE */

/* clean up after ourselves */
#undef DECL
#include "transact/preproc.h"
