/** This file defines operations (lookup/creation) on versions. */

// VALUETYPE and VALUENAME must be defined unless NO_VALUETYPE is.

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */
// could define DECL to be "extern inline"
#define DECL

////////////////////////////////////////////////////////////////////////
//                         prototypes
#if defined(IN_VERSIONS_HEADER)

// xxx don't export *all* of these; make some static.
#if !defined(NO_VALUETYPE)
extern enum opstatus TA(copyBackField)(struct oobj *obj, unsigned offset,
				       enum killer kill_whom);
extern struct vinfo *TA(createVersion)(struct oobj *obj, struct commitrec *cr,
				       struct vinfo *template);
extern void TA(EXACT_checkReadField)(struct oobj *obj, unsigned offset);
extern void TA(EXACT_checkWriteField)(struct oobj *obj, unsigned offset);
#endif
#if defined(NO_VALUETYPE)
extern struct vinfo *resizeVersion(struct oobj *obj, struct vinfo *version);

// dispatches based on the runtime type of 'obj'
extern struct vinfo *createVersion(struct oobj *obj, struct commitrec *cr,
				   struct vinfo *template);
// ensure we're in readers list (per object)
extern struct vinfo *EXACT_ensureReader(struct oobj *obj,
					struct commitrec *cr);
extern struct vinfo *EXACT_ensureWriter(struct oobj *obj,
					struct commitrec *currentTrans);
#endif

#endif /* IN_VERSIONS_HEADER */
//                         end prototypes
////////////////////////////////////////////////////////////////////////


/**********************************************************************

  Some discussion of locking and races:

a. to set FLAG field to non-FLAG: (can't race with b)
   a) read bits are set (prevents races from writeNT)
   b) put your unique nonce on the head of the versions chain.
   c) check that field is still FLAG
   d) conditional on your nonce still being at head of the chain,
      set the field to the new value.
      (prevents races from others trying to set FLAG to non-FLAG)

b. to set non-FLAG field to FLAG: (can't race with a)
   0) versions list must be non-null?
   b) look at header
   a)  set read flag. (now writes to the field must first clear read flag)
   c)  field must be non-FLAG at this point.
   c)  copy canonical value across to all versions, cond. on header same.
   d) conditional on header still being the same, store the FLAG.

c. to set field to any value (even FLAG?), read flags must be cleared.
   (writeNT)

d. to clear read flag:
   a) set the head of the versions chain to a unique nonce, unless it
      is NULL
   b) check that the reader list is NULL
   c) clear the flag conditional on the head of the versions chain.
      -e- this means we might overwrite a flag set by a call to
          setReadFlags() -- but the transaction involved was on the
          readers list and is now aborted, so no matter.
      -d- not a big deal if flag gets inadvertently reset by this race
      -c- n/a
      -a-/-b- protected by the head of the version chain

e. to add a reader to the reader list
   a) if head of reader list is NULL, must add nonce to versions chain.

f. can always set a read flag:
   a) transaction involved in the readers list
   b) field must be non-FLAG [may change before step (c) ]
   c) atomically update read flag.

*************************************************************************/

#if !defined(IN_VERSIONS_HEADER)
#if defined(NO_VALUETYPE)
DECL struct vinfo *resizeVersion(struct oobj *obj, struct vinfo *version) {
  // resize strategy:
  //  each hashtable has a linked list of "child" hashtable (as well as
  //  a link to the "parent" transaction, but that's different).
  //  we create a double-size version of the hashtable, and link it
  //  in at the head of the list.  each hashtable on the list defers
  //  to its children (copying back values opportunistically) before
  //  reporting not-found (actually, before delegating to the parent
  //  transaction).  We keep a num_keys entry, and set the child free
  //  once num_keys reaches zero (careful about other tasks still
  //  stuck in the past & reading from the child)
  //  ALTERNATIVELY (slightly) we can do the entire copyback eagerly
  //  once we've put the new list at the head, doing the standard
  //  "double-check to ensure nothing's changed" technique.  Might
  //  be slower, but frees up memory quicker. (Although the list of
  //  smaller lists should only use up ~2N space)
  assert(0); // let's get this working before we start worrying about this.
  return NULL;
}
#endif
#if !defined(NO_VALUETYPE)
///////////////////////////////////////////////////////////
// copy back methods. (nee' getVersion_writeNT)

/* this is safe as long as the given version is committed: i.e. no
 * more changes to it are allowed.  Since the given version is
 * the *first* version, no one can add an extra committed
 * transaction in front of us without our realizing it. */
/* [Note: the given version is *unique* to this thread
 *  (created in copyBackAndKill), which guarantees that
 *  another copy of copyBackAndKill can't race with us
 *  without our noticing:  we can only change a FLAG
 *  value to a non-flag value while our nonce is at
 *  the head of the chain.  Checking to make sure that
 *  the field is FLAG while we have the header reservation
 *  is sufficient to prevent the race.] */
// PROBLEM: what if one element in the array really *is*/ought to be
//  FLAG?  But *not* the one we're looking at.  Then we'll do
//  the copyback and reset flag, but field=FLAG bit=0 will still
//  exist for the aliased element. =(
DECL enum opstatus TA(copyBackField)(struct oobj *obj, unsigned offset,
				     enum killer kill_whom) {
  struct vinfo * volatile *versions, *first_version, *nonce=NULL;
  struct commitrec *vtid;
  enum opstatus st = SUCCESS;
  versions = OBJ_VERSION_PTR(obj);
  do {
    first_version = *versions;
    if (first_version==NULL) {
      st = SAW_RACE;
      break;
    }
    /* move owner to local var to avoid races (owner set to NULL behind
     * our back). */
    vtid = first_version->transid;
    if (vtid==NULL)
      break; /* found a committed version */
    if (AbortCR(vtid)==COMMITTED) {
      first_version->transid = NULL; /* opportunistic free */
      first_version->next = NULL; /* opportunistic free */
      break; /* found a committed version */
    }
    /* link out an aborted version */
    if (first_version == LL(versions))
      SC_PTR(versions, first_version->next);
  } while(1);
  /* okay, link in our nonce.  this will prevent others from doing the
   * copyback. */
  if (st==SUCCESS) {
    nonce = newAbortedVersion();
    nonce->next = first_version;
    if (!(first_version==LL(versions) &&
	  SC_PTR(versions, nonce)))
      st = SAW_RACE; /* don't need to unlink nonce, as it wasn't linked in */
  }
  /* check that no one's beaten us to the copy back */
  if (st==SUCCESS) {
    VALUETYPE canonical = *(VALUETYPE volatile *)(FIELDBASE(obj) + offset);
    if (canonical==T(TRANS_FLAG)) {
      VALUETYPE val = TA(readFromVersion)(first_version, offset);
      if (val==T(TRANS_FLAG)) /* false flag... */
	st = SAW_FALSE_FLAG; /* ... no copy back needed. */
      else { /* not a false flag */
	/* this could be a DCAS */
	if (!(nonce==LL(versions) &&
	      // note that T(store_conditional) is a bit of a hack.
	      T(store_conditional)(FIELDBASE(obj), offset, val)))
	  /* hmm, fail.  Must retry. */
	  st = SAW_RACE_CLEANUP; /* need to unlink nonce */
      }
    } else {
      /* may arrive here because of readT, which doesn't set canonical=FLAG*/
      st = SAW_RACE_CLEANUP; /* need to unlink nonce */
    }
  }
  /* always kill readers, whether successful or not.  This ensures that we
   * make progress if called from writeNT after a readNT sets readerList
   * non-null without changing FLAG to val (see immediately above; st will
   * equal SAW_RACE_CLEANUP in this scenario). */
  if (kill_whom == KILL_ALL) {
    /* kill all readers */
    struct tlist * volatile * readers = OBJ_READERS_PTR(obj);
    struct tlist * first_reader;
    do {
      first_reader = *readers;
      if (first_reader==NULL) break; /* no more readers */
      AbortCR(first_reader->transid);
      if (first_reader==LL(readers))
	SC_PTR(readers, first_reader->next);
    } while (1);
  }
  /* no more killing needed. */

  /* finally, clean up our mess. */
  if (st == SAW_RACE_CLEANUP || st == SUCCESS || st==SAW_FALSE_FLAG) {
    if (nonce==LL(versions))
      SC_PTR(versions, nonce->next);
    if (st==SAW_RACE_CLEANUP)
      st = SAW_RACE;
  }
  /* done. */
  return st;
}

#endif
#if defined(NO_VALUETYPE)

// make a new aborted version, for use as a per-thread identifier.
extern struct claz _Class_harpoon_Runtime_Transactions_CommitRecord;
DECL struct vinfo *newAbortedVersion() {
  struct vinfo *v = MALLOC( sizeof(struct vinfo) +
			    sizeof(struct commitrec) );
  // use the space at the end of the version for the commit record.
  struct commitrec *cr = (struct commitrec *) DIRECT_FIELDS(v);
  cr->state = ABORTED;
  // we should initialize cr->header.claz and .hashcode, but we're lazy.
  cr->header.claz = &_Class_harpoon_Runtime_Transactions_CommitRecord;
  cr->header.hashunion.hashcode = (((int)cr) & 0xFFF) | 1;
  v->transid = cr;
  return v; // that's it!
}

////////////////////////////////////////////////////////

/* look for a version read/writable by 'tid'.  Returns:
 *   vp.waiting!=NULL -- ver is the 'waiting' version for 'tid'.
 *                       vp.committed == NIL.
 *   vp.waiting==NULL, vp.committed!=NULL --
 *                  vp.committed is the first committed version in the chain.
 *   vp.waiting==NULL, vp.committed==NULL --
 *                           (i.e. obj->version==NULL)
 */
DECL struct versionPair findVersion(struct oobj *obj,
				    struct commitrec *currentTrans) {
  struct vinfo * volatile *versions, *first_version;
  struct commitrec *vtid;
  struct versionPair vp = { NULL, NULL };
  versions = OBJ_VERSION_PTR(obj);
  do {
    first_version = *versions;
    if (first_version==NULL)
      return vp; /* no versions */
    vtid = first_version->transid; /* use local copy to avoid races */
    if (vtid==currentTrans) {
      /* found a version: ourself! */
      vp.waiting = first_version;
      return vp;
    }
    if (vtid==NULL) {
      /* perma-committed version: return in vp.committed */
      first_version->next = NULL; /* opportunistic free */
      vp.committed = first_version;
      return vp;
    }
    /* strange version.  try to kill it. */
    /* XXX: double-check that our own transid is not aborted? */
    if (AbortCR(vtid)==COMMITTED) {
      /* committed version.  return this in vp.committed */
      first_version->transid = NULL; /* opportunistic free */
      first_version->next = NULL; /* opportunistic free */
      vp.committed = first_version;
      return vp;
    } else {
      /* this is an aborted version.  unlink it; it's useless. */
      if (first_version==LL(versions))
	SC_PTR(versions, first_version->next);
      /* repeat */
    }
  } while(1);
}

#endif

// creating new version
//  two cases:
//    new top-level transaction
//      do copy back, so no X'ed out fields, and then just initialize
//      direct_fields to FLAG_VALUE.
//    new subtransaction:
//      at the moment, we don't allow parallel subtransactions within
//      the transaction, so we can just clone the version for the
//      parent. [XXX: to do: represent subtransactions more compactly,
//      so they can avoid duplicating fields modified by their parent?]
//      XXX let FLAG_VALUE in lookup mean 'ask my parent'
// GOOD!  this means we just get to a point where we can create the
// new transaction with FLAG_VALUE everywhere.  (hey, this works
// even if we allow parallel subtransactions)
#if !defined(NO_VALUETYPE)
// created but not linked.
DECL struct vinfo *TA(createVersion)(struct oobj *obj, struct commitrec *cr,
				     struct vinfo *template) {
  struct vinfo *v;
  // get size, not including header, rounded up to word boundary
  uint32_t size = (3+FNI_ObjectSize(obj)-sizeof(struct oobj))&~3;
  uint32_t allocsize;
  // if that's larger than OBJ_CHUNK_SIZE, then allow for one
  // chunk of hashtable (INITIAL_CACHE_SIZE)
  if ((!DO_HASH) || size <= OBJ_CHUNK_SIZE) {
    v = 
#if defined(ARRAY) && !defined(NONPRIMITIVE)
	MALLOC_ATOMIC
#else
	MALLOC
#endif
	(allocsize=(sizeof(struct vinfo) + size));
    memset(DIRECT_FIELDS(v), TRANS_FLAG_Byte, size);
  } else {
    unsigned entries = INITIAL_CACHE_SIZE;
    v = MALLOC(allocsize=(sizeof(struct vinfo) + OBJ_CHUNK_SIZE +
#if defined(ARRAY)
	       T(version_hash_sizeof)(entries)
#else
	       version_hash_sizeof_Int(entries)
#endif
	       ));
    memset(DIRECT_FIELDS(v), TRANS_FLAG_Byte, OBJ_CHUNK_SIZE);
#if defined(ARRAY)
    T(version_hash_init)
#else
    version_hash_init_Int
#endif
      (
#if defined(ARRAY)
       (struct T(version_hashtable) *)
#else
       (struct version_hashtable_Int *)
#endif
       HASHED_FIELDS(v), entries);
  }
  // this copy must be done in a race-free manner.
  // this is easy if
  // a) we don't allow writes to committed transactions,
  // and b) we ensure no one's writing to a parent transaction while
  // a subtransaction is being created.
  // ALTERNATIVELY perhaps we can simply link a 'parent' version?
  // but how do we know when we have to consult the parent?
  if (template!=NULL) memcpy(v, template, allocsize);
  // initialize the rest of the vinfo.
  v->transid = cr;
  v->next = NULL;
  return v;
}
#endif
#if defined(NO_VALUETYPE)
// determine which version of 'createVersion' to call, based on the
// *runtime* type of the given object (static types are not precise enough!)
DECL struct vinfo *createVersion(struct oobj *obj, struct commitrec *cr,
				 struct vinfo *template) {
  // copied from fni_class_isArray, because we don't want to wrap objects.
  struct claz *cc = obj->claz->component_claz;
  if (cc==NULL)
    // not an array
    return createVersion_Object(obj, cr, template);
  // test for array of primitive type; copied from fni_class_isPrimitive
  else if (cc->display[0]==NULL && cc->interfaces==NULL) {
    // now cheat a little; just look at the size of the primitive
    // and create an appropriately sized version.  Type mentioned may
    // not match exactly, but it doesn't matter.
    switch(cc->size) {
    case 1:
      return createVersion_Array_Byte(obj, cr, template);
    case 2:
      return createVersion_Array_Short(obj, cr, template);
    case 4:
      return createVersion_Array_Int(obj, cr, template);
    case 8:
      return createVersion_Array_Long(obj, cr, template);
    default:
      assert(0);
      return NULL;
    }
  } else
    // array of Objects
    return createVersion_Array_Object(obj, cr, template);
}
#endif

// read/write flags have two parts:
//  1) lookup version (per object)
//  2) set flag (per field)

#if defined(NO_VALUETYPE)
/* make sure 'cr' is on the reader list for object 'obj'. */
static void ensureReaderList(struct oobj *obj, struct commitrec *cr) {
  struct tlist * volatile *readers, *first_reader, *r, *nr=NULL;
  readers = OBJ_READERS_PTR(obj);
  do {
    first_reader = *readers;
    for (r=first_reader; likely(r!=NULL); r=r->next)
      if (likely(r->transid == cr))
	return;  /* on the list; we're done! */
      else {
	/* opportunistic free */
	if (r==first_reader && r->transid->state != WAITING &&
	    first_reader == LL(readers))
	  SC_PTR(readers, first_reader->next);
      }
    /* we're not on the list.  Try to put ourselves on. */
    if (likely(nr==NULL)) {
      nr = MALLOC(sizeof(*nr));
      nr->transid = cr;
    }
    nr->next = first_reader;
    if (likely(first_reader == LL(readers)) &&
	likely(SC_PTR(readers, nr)))
      return; /* we've put ourselves on the list */
    /* otherwise, try, try, again. */
  } while(1);
}

// ensure we're in readers list (per object)
DECL struct vinfo *EXACT_ensureReader(struct oobj *obj, struct commitrec *cr) {
  struct vinfo *ver;
  // XXX we should have per-version readers?
  //    hmm. if a subtransaction reads then commits, then the parent
  //    transaction is legitimately still on the hook.  the only benefit
  //    is for subtransactions which read, then abort; we don't need to
  //    kill the parent in that case.  But I think this is how we already
  //    do it.  Bears some additional thought, perhaps; especially for
  //    the multiple-reader case.
  ENSURE_HAS_TRANSACT_INFO(obj); // now all other stuff doesn't have to.
  /* make sure we're on the readerlist */
  ensureReaderList(obj, cr);
  /* now kill any transactions associated with uncommitted versions, unless
   * the transactions is ourself! */
  ver = findVersion(obj, cr).waiting;
  /* don't care about which committed version to use, at the moment. */
  return ver;
}

// returns NULL to indicate suicide request
// this is the only place where new version objects are created/linked
// other than in copyBackField, where nonce versions are added.
DECL struct vinfo *EXACT_ensureWriter(struct oobj *obj,
				      struct commitrec *cr) {
  // lookup (or create) appropriate version
  //       do ENSURE_HAS_TRANSACT_INFO here
  struct tlist * volatile *readers, *first_reader;
  struct vinfo * volatile *versions;
  struct versionPair vp;
  ENSURE_HAS_TRANSACT_INFO(obj); // now all other stuff doesn't have to.
  do {
    vp = findVersion(obj, cr);
    if (likely(vp.waiting!=NULL))
      return vp.waiting; /* found a writable version for us */
    versions = OBJ_VERSION_PTR(obj);
    if (vp.committed==NULL) {
      /* create and link a fully-committed root version, then use this as
       * our base. */
      vp.committed = createVersion(obj, NULL, NULL);
      if (!(LL(versions)==NULL &&
	    SC_PTR(versions, vp.committed)))
	continue; /* try, try, again. */
    }
    /* okay, vp.committed now has a committed version linked from this obj. */
    assert(vp.committed!=NULL);
    assert(vp.committed->transid==NULL ||
	   vp.committed->transid->state==COMMITTED);
    assert(vp.waiting==NULL);
    /* make a new version for this transaction. */
    vp.waiting = createVersion(obj, cr, vp.committed);
    vp.waiting->next = vp.committed;
    /* want copy of committed version vp.committed.  Race here because
     * vp.committed can be written to under peculiar circumstances,
     * namely: vp.committed has non-flag value, non-flag value is
     * copied back to parent, flag_value is written to parent -- this
     * forces flag_value to be written to committed version. */
    /* IF WRITES ARE ALLOWED TO COMMITTED VERSIONS, THERE IS A RACE HERE.
     * But our implementation of false_flag writes at the moment does
     * not permit *any* writes to committed versions. */
    /***** this copy happens inside createVersion *****/
    if (!(LL(versions)==vp.committed &&
	  SC_PTR(versions, vp.waiting)))
      continue; /* try again from the top */
    /* we've linked in our version object now. */

    /* kill all readers (except ourself) */
    /* note that all changes have to be made from the front of the
     * list, so we unlink ourself and then re-add us. */
    readers = OBJ_READERS_PTR(obj);
    do {
      first_reader = *readers;
      if (first_reader==NULL)
	break; /* no readers left */
      if (first_reader->transid != cr)
	AbortCR(first_reader->transid);
      /* link out this (aborted or committed or us) reader */
      if (LL(readers)==first_reader)
	SC_PTR(readers, first_reader->next);
    } while(1);
    /* okay, all pre-existing readers dead and gone. */
    /* link us back in. */
    ensureReaderList(obj, cr);
    /* done! */
    return vp.waiting;
  } while(1);
}

#endif
#if !defined(NO_VALUETYPE)

/* per-field, before read. */
DECL void TA(EXACT_checkReadField)(struct oobj *obj, unsigned offset){
  /* do nothing: no per-field read stats are kept. */
}

/* per-field, before write. */
DECL void TA(EXACT_checkWriteField)(struct oobj *obj, unsigned offset) {
  struct vinfo * volatile *versions, *first_version, *v;
  versions = OBJ_VERSION_PTR(obj);
  retry_copy_over:
  do {
    /* set write flag, if not already set. */
    VALUETYPE canonical = *(VALUETYPE volatile *)(FIELDBASE(obj) + offset);
    if (likely(canonical==T(TRANS_FLAG))) return; /* done: flag already set. */
    /* okay, need to set write flag. */
    /* LL(canonical), SC(version) for all versions. */
    first_version = *versions;
    for (v=first_version; v!=NULL; v=v->next) {
      // ensure space
      if (DO_HASH && offset > OBJ_CHUNK_SIZE-sizeof(VALUETYPE)) {
#if defined(ARRAY)
	// arrays are homogenous
	struct T(version_hashtable) *table =
	  (struct T(version_hashtable) *) HASHED_FIELDS(v);
	int success = T(version_hash_write_alloc)(table, offset);
#else
	// because this is heterogeneous, use a fixed jint-sized hashtable.
	struct version_hashtable_Int *table =
	  (struct version_hashtable_Int *) HASHED_FIELDS(v);
	int success = version_hash_write_alloc_Int(table, offset);
#endif
	if (!success) {
	  // XXX resize version <-- only place we'll need to do this.
	  // XXX (hmmm, subtrans might find they don't have enough space
	  //      to mirror their parent)
	  v = resizeVersion(obj, v); // XXX revisit
	}
      }
      // don't write to aborted versions.  this is needed only because
      // newAbortedVersion() doesn't allocate enough space for fields.
      if (v->transid && v->transid->state==ABORTED) continue; // link out?
      // LL and check canonical and header, then SC(version)
      if ((!DO_HASH) || offset <= OBJ_CHUNK_SIZE-sizeof(VALUETYPE)) {
	if (canonical != T(load_linked)(FIELDBASE(obj), offset) ||
	    first_version != *versions)
	  goto retry_copy_over;
	if (!T(store_conditional)(DIRECT_FIELDS(v), offset, canonical))
	  goto retry_copy_over;
      } else {
#if defined (ARRAY)
	// arrays are homogenous
	struct T(version_hashtable) *table =
	  (struct T(version_hashtable) *) HASHED_FIELDS(v);
	VALUETYPE *loc = T(version_hash_write_loc)(table, offset);
	if (canonical != T(load_linked)(FIELDBASE(obj), offset) ||
	    first_version != *versions)
	  goto retry_copy_over;
	if (!T(store_conditional)(table,((void*)loc)-((void*)table),
				  canonical) )
	  goto retry_copy_over;
	// success!
#else
	// heterogeneous; uses fixed jint-sized hashtable.
	struct version_hashtable_Int *table =
	  (struct version_hashtable_Int *) HASHED_FIELDS(v);
	// Ugggleeeey.
	if (sizeof(VALUETYPE)>sizeof(jint)) {
	  union { jint i[2]; VALUETYPE v; } u = { .v = canonical };
	  jint *loc = version_hash_write_loc_Int(table, offset);
	  if (canonical != T(load_linked)(FIELDBASE(obj), offset) ||
	      first_version != *versions)
	    goto retry_copy_over;
	  if (!SC(loc, u.i[0]) )
	    goto retry_copy_over;
	  loc = version_hash_write_loc_Int(table, offset+4);
	  if (canonical != T(load_linked)(FIELDBASE(obj), offset) ||
	      first_version != *versions)
	    goto retry_copy_over;
	  if (!SC(loc, u.i[1]) )
	    goto retry_copy_over;
	  // success!
	} else {
	  jint *loc = version_hash_write_loc_Int(table, offset);
	  jint val = __builtin_choose_expr
	    (sizeof(VALUETYPE) == sizeof(jint) &&
	     !__builtin_types_compatible_p(VALUETYPE,jint),
	     ({ union { jint i; VALUETYPE v; } u = { .v=canonical }; u.i; }),
	     /* simple cast suffices */ (jint) canonical);
	  if (canonical != T(load_linked)(FIELDBASE(obj), offset) ||
	      first_version != *versions)
	    goto retry_copy_over;
	  if (!SC(loc, val) )
	    goto retry_copy_over;
	  // success!
	}
#endif
      }
      // on to the next version
    }
    /* progress! */
    /* field has been successfully copied to all versions */
    /*  d) LL(header) check header SC(flag)  if SC succeeds, we're done. */
    if (first_version == LL(versions) &&
	/* object field is either 'canonical' or FLAG here:
	 * we can race with another copythrough and that's okay;
	 * the locking strategy above ensures that we're all
	 * writing the same values to all the versions and not
	 * overwriting anything. */
	T(store_conditional)(FIELDBASE(obj), offset, T(TRANS_FLAG)))
      return; /* success!  done! */
    // nope, something went wrong.  try again, please.
  } while(1);
}
#endif /* !NO_VALUE_TYPE */

#endif /* !IN_VERSIONS_HEADER */

/* clean up after ourselves */
#include "transact/preproc.h"
#undef DECL
