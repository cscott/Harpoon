/** This file defines operations (lookup/creation) on versions. */

// VALUETYPE and VALUENAME must be defined unless NO_VALUETYPE is.

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */
// could define DECL to be "extern inline"
#define DECL

// for now, turn on debugging assertions that are not necessarily true
// in a multi-threaded context.
#ifndef SINGLE_THREADED_DEBUGGING
#define SINGLE_THREADED_DEBUGGING
#endif
// bogosity.  let's get it working first, though.
#ifndef HACKS
#define HACKS
#endif

////////////////////////////////////////////////////////////////////////
//                         prototypes
#if defined(IN_VERSIONS_HEADER)
#if defined(NO_VALUETYPE)
enum opstatus { FAILURE=0, SUCCESS=1, SAW_FALSE_FLAG=2, MISSING_FLAG=3 };
#endif

// xxx don't export *all* of these; make some static.
#if !defined(NO_VALUETYPE)
extern enum opstatus TA(copy_back)
     (struct oobj *obj, struct vinfo *nonce, struct vinfo *version,
      unsigned offset, int copy_aliased);
extern enum opstatus TA(getVersion_readNT)
     (struct oobj *obj,unsigned offset,unsigned flag_offset,unsigned flag_bit);
extern void TA(getVersion_writeNT)(struct oobj *obj, unsigned offset,
				   unsigned flag_offset, unsigned flag_bit);
extern struct vinfo *TA(createVersion)(struct oobj *obj, struct commitrec *cr,
				       struct vinfo *template);
extern struct vinfo *TA(EXACT_setReadFlags)(struct oobj *obj, unsigned offset,
					    unsigned flag_offset,
					    unsigned flag_bit,
					    struct vinfo *version,
					    struct commitrec*cr/*this trans*/);
extern void TA(EXACT_setWriteFlags)(struct oobj *obj, unsigned offset,
				    unsigned flag_offset, unsigned flag_bit);
#endif
#if defined(NO_VALUETYPE)
extern struct vinfo *resizeVersion(struct oobj *obj, struct vinfo *version);
extern enum opstatus copyBackAndKill(struct oobj *obj, unsigned offset,
				     unsigned flag_offset, unsigned flag_bit,
				     enum opstatus (*copyback_f)
				     (struct oobj *obj, struct vinfo *nonce,
				      struct vinfo *version, unsigned offset,
				      int copy_aliased),
				     int kill_readers);
extern struct vinfo *getVersion_readT(struct oobj *obj,
				      struct commitrec *currentTrans);

// dispatches based on the runtime type of 'obj'
extern struct vinfo *createVersion(struct oobj *obj, struct commitrec *cr,
				   struct vinfo *template);
// ensure we're in readers list (per object)
extern void EXACT_ensureReader(struct oobj *obj, struct commitrec *cr);
extern struct vinfo *EXACT_ensureWriter(struct oobj *obj,
					struct commitrec *currentTrans);
extern struct vinfo *findReadableVersion(struct vinfo *first_version,
					 struct commitrec *current_trans);
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
DECL enum opstatus TA(copy_back)(struct oobj *obj, struct vinfo *nonce,
				 struct vinfo *version, unsigned offset,
				 int copy_aliased) {
  enum opstatus result = SUCCESS;
#if defined(ARRAY)
  struct aarray *arr = (struct aarray *)obj;
  unsigned orig_offset = offset;
  // if copy_aliased is true, must copy back all read-bits-aliased versions
  // of this field, too, as we're going to clear the read bits when we're done.
  jsize length = arr->length * sizeof(VALUETYPE);
  jsize stride = 32*sizeof(VALUETYPE);
  if (copy_aliased) offset = offset & (stride-1);
  do {
#endif
    struct vinfo *first_version;
    VALUETYPE value = TA(readFromVersion)(version, offset);
    VALUETYPE old;
    first_version =  LL(OBJ_VERSION_PTR(obj));
    if (first_version != nonce)
      return FAILURE; // whoops, someone's changing things
    old = *(VALUETYPE*)(FIELDBASE(obj) + offset);
    // only copy back if this field was FLAG'ged: sometimes only read bit was 1
    if (likely(old==T(TRANS_FLAG))) {
      // if the versioned value really *is* FLAG, this is a false transaction
      if (unlikely(value==T(TRANS_FLAG))) {
	// XXX since this is a false transaction, ought we to
	//  have some protection against parallel NT threads
	//  modifying the value in the transaction?
#if defined(ARRAY)
	if (offset==orig_offset)
#endif
	  result=SAW_FALSE_FLAG;
#if defined(ARRAY)
	else assert(0); // we haven't fixed this problem yet.
#endif
      } else // no need to copy back if this is a false transaction
	if (unlikely(!T(store_conditional)(FIELDBASE(obj), offset, value)))
	  return FAILURE; // again, someone's changing things
    } else {
#if defined(ARRAY)
      if (offset==orig_offset)
#endif
	result=MISSING_FLAG; // we expected this to be FLAG. =(
    }
#if defined(ARRAY)
    if (!copy_aliased) break; // if !copy_aliased, we only need copy one.
    offset+=stride;
  } while (offset < length);
#endif
  return result;
}

#endif
#if defined(NO_VALUETYPE)

// make a new aborted version, for use as a per-thread identifier.
DECL struct vinfo *newAbortedVersion() {
  struct vinfo *v = MALLOC( sizeof(struct vinfo) +
			    sizeof(struct commitrec) );
  // use the space at the end of the version for the commit record.
  struct commitrec *cr = (struct commitrec *) DIRECT_FIELDS(v);
  cr->state = ABORTED;
  // we should initialize cr->header.claz and .hashcode, but we're lazy.
  v->transid = cr;
  return v; // that's it!
}

// this function kills all readers and copies back *the specified field*,
// setting its read bit to 0.  other fields will likely stay in the
// most-recently-committed version object.  Unknown: is it important
// to eventually get rid of it?  Intent is to write to this field.
// on return, the first item in the versions list will be a
// completely-committed transaction.
DECL enum opstatus copyBackAndKill(struct oobj *obj, unsigned offset,
				   unsigned flag_offset, unsigned flag_bit,
				   enum opstatus (*copyback_f)
				   (struct oobj *obj, struct vinfo *nonce,
				    struct vinfo *version, unsigned offset,
				    int copy_aliased),
				   int kill_readers) {
  // timing is everything.
  /* goal of this method is *just* to:
   *   a) kill all of the readers.
   *   b) find a committed version.
   *   c) copy it back.
   *   d) reset the read flag to zero (contingent on there still being
   *      no readers in the readers list)
   *
   * What if:
   *  a) versions are added to the versions list after we've traversed it?
   *     that's fine: they can't read or write the field without setting
   *     the read flag, and we guarantee it's zero at the end of the method.
   *  b) readers are added to the list after we've truncated it?
   *     no problem:
   *       our (future) write is LL/SC linked to the read bit still being zero.
   */
  struct vinfo * volatile *versions, *expected;
  struct tlist * volatile * readers=NULL; /* quiet a warning by init'ing */
  jint volatile *flagp;
  jint old_flags;
  enum opstatus sawFalseFlag=FAILURE;
  ENSURE_HAS_TRANSACT_INFO(obj);

#ifdef SINGLE_THREADED_DEBUGGING
  /* the read bit will be set (if coming from writeNT; kill_readers==1) *or*
   * the write flag will be set (if coming from readNT; kill_readers==0).
   * The write flag implies the read bit, but not the other way 'round. */
  assert( *(jint*)(FLAGBASE(obj)+flag_offset) & flag_bit );
#endif

  while (1) {
    // abort all readers.
    if (kill_readers) while (1) {
      struct tlist *first_reader, *r;
      readers = OBJ_READERS_PTR(obj);
      first_reader = *readers;
      if (first_reader==NULL) break; // our work is done here.
      // if this reader is committed, but its parent is not, then
      // we need to abort the parent.  thus we cann AbortCRorParent()
      r = first_reader;
      AbortCRorParent(r->transid);
      for (r=r->next; r!=NULL; r=r->next) {
	AbortCRorParent(r->transid);
      }
      // every transaction on the list is now either committed or aborted.
      if (first_reader == LL(readers) &&
	  SC_PTR(readers, NULL))
	break; // okay, successfully cleared the list.
      // something's changed, do it again.
    }
    // now find first committed version.
  kill_versions:
    while(1) {
      struct vinfo *first_version, *v;
      versions = OBJ_VERSION_PTR(obj);
      first_version = *versions;
      if (first_version==NULL) {
	expected=NULL; break; // our work is done here.
      }
      // check that read bit is still set
      //  (protects against races w/ writeNT and other threads doing copyback)
      if (unlikely(!(flag_bit &
		     *(jint volatile *)(FLAGBASE(obj)+flag_offset))))
	// careful: we don't want to get into an infinite loop in
	// EXACT_readNT with read bit = 0 and field value = FLAG.
	// but this ought never happen: we should never write FLAG
	// to a field unless the read bit is set.
	return sawFalseFlag;// someone's apparently already done the copy-back.
      for (v=first_version; v!=NULL; v=v->anext) {
	// transaction isn't *really* committed unless all of its parent
	// transactions are committed, too.  we need this version to
	// be either totally committed or aborted; if this subtrans is
	// committed, we need to try to abort a parent transaction.
	// if we can't, then this version is *really* committed.
	// AbortCRorParent only returns COMMITTED if the trans is committed
	// all the way up.
	if (AbortCRorParent(v->transid)==COMMITTED) {
	  // okay, this version is totally committed.
	  // copy back.  make this the first version after a nonce.
	  // the unique nonce protects us if another thread tries to copy
	  // back at the same time.
	  // XXX for added efficiency, we might want a per-thread
	  //     singleton nonce.  We'd have to unlink it after we
	  //     successfully did our copy back, though.
	  struct vinfo *nonce = newAbortedVersion();
	  nonce->anext = nonce->wnext = v;
	  if (first_version==LL(versions) &&
	      SC_PTR(versions, nonce)) {
	    // okay, now copy back, keeping an eye on the version pointer.
	    sawFalseFlag = copyback_f(obj, nonce, v, offset, kill_readers);
	    if (sawFalseFlag==FAILURE) goto kill_versions;
	    // XXX MISSING_FLAG might be considered a failure if we're called
	    // from readNT (not from writeNT); but I think we can just
	    // continue.
	    expected = nonce;
#ifdef HACKS
	    // xxx free some memory; not entirely safe
	    // because other threads may see our null pointers and crash.
	    v->anext = v->wnext = NULL;
#endif
	    // go on, now, let's get a move on!
	    goto found_committed; // yay, us!
	  }
	}
	// we managed to abort this version; now try the next.
      }
      assert(0); // a committed transaction must always anchor a non-empty list
    }
  found_committed:
    if (!kill_readers) return sawFalseFlag; // done for readNT case.
    // finally, reset read flags to zero.
    //  the correctness proof here is very subtle:
    //   as long as readers is still null, no one can have *set* an
    //   extra flag bit between our read of old_flags, and our update
    //   (since we have to add ourself as a reader *before* we set the
    //    read flag)
    //   AND we could race with another copy of ourself, clearing a
    //    different flag, but that's okay, too.  worst case is that
    //    one of our flags doesn't really get cleared here.  But
    //    in the write routine after we return we're going to test the
    //    flag once more anyway, so things will work out fine.
    //  last case: someone could reflag the field between found_committed
    //    and here.  flagging a field required putting your nonce on
    //    the head of the versions chain, which will cause the
    //    bit clear below to fail.
    if (NULL!=*readers) // double check that readers list is still null.
      continue;
    flagp = (jint volatile *)(FLAGBASE(obj)+flag_offset);
    old_flags = *flagp;
    if (expected!=LL(versions))
      continue; // store conditional on the header staying the same.
    if (SC(flagp, old_flags & (~flag_bit)))
      return sawFalseFlag;  // success! we reset the read flags!
    // nope, new readers have been added; go back and start agin.
  }
}
////////////////////////////////////////////////////////

/** Get the correct readable version after we've seen a field
 *  with FLAG_VALUE.  If the only appropriate version is
 *  COMMITTED, try to copy it back.  If copy-back works, we
 *  can return NULL.  Otherwise return the version corresponding
 *  to the current transaction. */
DECL struct vinfo *getVersion_readT(struct oobj *obj,
			       struct commitrec *currentTrans) {
  // ensureReader has done the ENSURE_HAS_TRANSACT_INFO for us.
  struct vinfo * volatile *versions, *first_version, *v;
  versions = OBJ_VERSION_PTR(obj);
  first_version = v = *versions;
  while (likely(v!=NULL)) {
    struct commitrec *cr, *vTrans=v->transid;
    // common case is that the version we're looking for is on top.
    if (likely(vTrans==currentTrans)) return v;
    // okay.  this version is either completely committed, or
    // a parent of currentTrans.  in either case, it's the one we want.

    // find the first non-committed parent of vTrans
    while (vTrans!=NULL && vTrans->state==COMMITTED)
      vTrans = vTrans->parent;
    if (vTrans==NULL) 
      return v; // committed all the way down.  it's what we want.
    switch (vTrans->state) {
    case ABORTED:
      // this version is aborted; clearly not the one we want.
      v = v->anext;
      break;
    case WAITING:
      // if this version corresponds to one of our parents, it's the
      // version we want.
      // XXX note that if a subtransaction commits, this may be
      //     the wrong version.   Think about this more when we have
      //     parallel subtransactions.  For the moment, we must be
      //     certain to invalidate all cached read-versions after
      //     any commit (i.e. call to possibly-synchronized method)
      for (cr=currentTrans; cr!=NULL; cr=cr->parent)
	if (vTrans==cr) return v; // v corresponds to one of our parents.
      // not one of our parents; try the next version in the list
      v = v->wnext;
      break;
    }
    // try the next version in the list.
  }
  // hmm.  out of versions.  we must have done a copyback already.
  return NULL;
}

#endif
#if !defined(NO_VALUETYPE)

/** We've seen a flag with FLAG_VALUE.  Kill all outstanding
 *  writers, and try to copy back.  When successful, return.
 *  Return true if the copied back value is FLAG_VALUE.
 */
DECL enum opstatus TA(getVersion_readNT)
     (struct oobj *obj, unsigned offset,
      unsigned flag_offset, unsigned flag_bit) {
  enum opstatus sawFalseFlag;
  // difference here is that we can skip transactional *readers*.
  // but we already do that by not invoking this method unless the
  // field is X'ed out.  Since we don't keep close track of
  // *which* transactions are responsible for writing *which* fields,
  // we just abort the transaction list (though not the reader list)
  // until we get to a committed transaction.
#ifdef SINGLE_THREADED_DEBUGGING
  // WRITE FLAG is set here
  assert( *(VALUETYPE*)(FIELDBASE(obj)+offset) == T(TRANS_FLAG) );
  // we guarantee that the WRITE FLAG always implies the read bit.
  assert( *(jint*)(FLAGBASE(obj)+flag_offset) & flag_bit );
#endif
  sawFalseFlag=copyBackAndKill(obj, offset, flag_offset, flag_bit,
			       TA(copy_back), 0/*don't need to kill readers*/);
#ifdef SINGLE_THREADED_DEBUGGING
  // unless sawFalseFlag is true, the write flag shouldn't be set anymore.
  // (but the read flag may still be set, esp. for arrays)
  assert( sawFalseFlag==SAW_FALSE_FLAG ?
	  *(VALUETYPE*)(FIELDBASE(obj)+offset) == T(TRANS_FLAG) :
	  *(VALUETYPE*)(FIELDBASE(obj)+offset) != T(TRANS_FLAG) );
#endif
  return sawFalseFlag;
}

// writeNT version: we need to kill all readers as well.
DECL void TA(getVersion_writeNT)(struct oobj *obj, unsigned offset,
				 unsigned flag_offset, unsigned flag_bit) {
#ifdef SINGLE_THREADED_DEBUGGING
  // READ bit is set here; not necessarily the WRITE flag.
  assert( *(jint*)(FLAGBASE(obj)+flag_offset) & flag_bit );
#endif
  copyBackAndKill(obj, offset, flag_offset, flag_bit,
		  TA(copy_back), 1/*kill readers*/);
#ifdef SINGLE_THREADED_DEBUGGING
  // READ bit should be cleared here, unless field value really is FLAG.
  assert( 0 == (*(jint*)(FLAGBASE(obj)+flag_offset) & flag_bit) ||
	  *(VALUETYPE*)(FIELDBASE(obj)+offset) == T(TRANS_FLAG) );
#endif
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
  if (size <= OBJ_CHUNK_SIZE) {
    v = MALLOC(allocsize=(sizeof(struct vinfo) + size));
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
#ifdef HACKS
  // this is the hack.  we need to do this copy in a race-free manner.
  // (may be easy if a) we don't allow writes to committed transactions,
  // and b) we ensure no one's writing to a parent transaction while
  // a subtransaction is being created.
  // ALTERNATIVELY perhaps we can simply link a 'parent' version?
  // but how do we know when we have to consult the parent?
  if (template!=NULL) memcpy(v, template, allocsize);
#endif
  // initialize the rest of the vinfo.
  v->transid = cr;
  v->anext = v->wnext = NULL;
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
// ensure we're in readers list (per object)
DECL void EXACT_ensureReader(struct oobj *obj, struct commitrec *cr) {
  struct tlist * volatile *readers, *first_reader, *r, *nr=NULL;
  // XXX we should have per-version readers?
  //    hmm. if a subtransaction reads then commits, then the parent
  //    transaction is legitimately still on the hook.  the only benefit
  //    is for subtransactions which read, then abort; we don't need to
  //    kill the parent in that case.  But I think this is how we already
  //    do it.  Bears some additional thought, perhaps; especially for
  //    the multiple-reader case.
  // XXX whenever we're not the first item on the list, it's probably
  //    a good idea to try to trim completely-committed and aborted
  //    readers from the head.  certainly if we have to add a new
  //    entry, we ought to consider this.
  // when readers==NULL, you must add a nonce to the versions chain
  // before you can add yourself to the readers list.  this ensures
  // that you're not stomping on a clear readers list just as we're
  // about to clear the read flag.
  ENSURE_HAS_TRANSACT_INFO(obj); // now all other stuff doesn't have to.
  readers = OBJ_READERS_PTR(obj);
  do {
    first_reader = *readers;
    for (r=first_reader; r!=NULL; r=r->next)
      if (likely(r->transid == cr))
	return;  // found!
    // nope, not there.  let's try to add ourself.
    if (likely(nr==NULL)) {
      nr = MALLOC(sizeof(*r));
      nr->transid=cr;
    }
    if (first_reader==NULL) { // must add a nonce to prevent races.
      struct vinfo * volatile * versions=OBJ_VERSION_PTR(obj);
      struct vinfo *first_version, *nonce = newAbortedVersion();
      do {
	first_version = *versions;
	nonce->anext = nonce->wnext = first_version;
      } while (!(likely(first_version==LL(versions)) &&
		 likely(SC_PTR(versions, nonce))));
    }
    nr->next = first_reader;
    if (first_reader == LL(readers) &&
	SC_PTR(readers, nr))
      return; // okay, we're on there now!
    // nope, something went wrong.  Let's try all over again.
  } while (1);
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

// returns NULL to indicate suicide request
// this is the only place where new version objects are created/linked
DECL struct vinfo *EXACT_ensureWriter(struct oobj *obj,
				      struct commitrec *currentTrans) {
  // lookup (or create) appropriate version
  //       do ENSURE_HAS_TRANSACT_INFO here
  struct vinfo * volatile *versions, *first_version, *v;
  int committed = 0;
  ENSURE_HAS_TRANSACT_INFO(obj); // now all other stuff doesn't have to.
  
  versions = OBJ_VERSION_PTR(obj);
  do {
    first_version = v = *versions;
    while (v!=NULL) {
      struct commitrec *cr;
      // common case is that the version we're looking for is on top.
      if (likely(v->transid==currentTrans)) return v; // found our version.
      // is this a committed subtransaction of currentTrans? if so, use it.
      for (cr=v->transid; cr!=NULL; cr=cr->parent) {
	if (cr==currentTrans) return v; // found our version.
	if (cr->state != COMMITTED) break; // isn't committed.
	// for efficiency, attempt to link out the committed subtrans
	if (cr==LL(&(v->transid))) SC_PTR(&(v->transid), cr->parent);
	// this subtrans is committed, continue looking at parent.
      }
#ifdef SINGLE_THREADED_DEBUGGING
      // unless the LL/SC above fails, we should v->transid should always
      // be either WAITING, ABORTED, or NULL (perma-committed)
      // committed subtransactions should have been linked up to the
      // first WAITING parent.
      assert(v->transid==NULL || v->transid->state != COMMITTED);
#endif
      // is v->transid a subtransaction of currentTrans?
      if (isSubtransaction(currentTrans, v->transid))
	// if we're a subtransaction of v, then
	// we're done.  drop through and create a subversion.
	break;
      else {
	// before we start killing people, let's just make sure
	// we're not aborted outself?
	// xxx necessary?
	for (cr=currentTrans ; cr!=NULL; cr=cr->parent)
	  if (cr->state==ABORTED)
	    return NULL; // bail
	// okay, we're not a NAST of v, so try to abort this
	// version, going up to its parents if necessary.
	if (AbortCRorParent(v->transid)==COMMITTED) {
	  // if we make it all the way to the root without
	  // being able to abort anything (COMMITTED all the
	  // way down), then use this as our base and drop through.
	  committed = 1; // v has a fully-committed version
	  break;
	} else {
	  // okay, we were able to abort this transaction.
	  // go to v->anext. (always right for an aborted transaction)
	  v = v->anext;
	}
      }
    }
    if (v==NULL) {
      // well, we've manage to abort everything.  create and link
      // a fully-committed root version (empty as of yet) and then
      // drop through to use this as our base.
      v = createVersion(obj, NULL/*indicates perma-committed*/, NULL);
      committed = 1; // v has a fully-committed version
    }
    // okay, make *versions==first_version be this version, which is
    // either completely committed, or which we're a subtransaction of.
    // xxx if subtrans, should we check that we/it is not already aborted?
    if (first_version!=v &&
	!(first_version == LL(versions) &&
	  SC_PTR(versions, v)))
      continue; // something's changed; restart from top.
    first_version = v;
    // create the new version for this transaction
    v = createVersion(obj, currentTrans, v);
    // try to link us in before first_version; retrying the whole
    // shebang if we fail (maybe somebody else created a version,
    // which we need to abort, or some such)
    if (committed) {
      // we're linking against a committed version
      v->anext = v->wnext = first_version;
    } else {
      // we're a subtransaction of a waiting version
      v->anext = first_version; // points to parent.
      v->wnext = first_version->wnext; // skips the parent
    }
    if (first_version == LL(versions) &&
	SC_PTR(versions, v))
      return v; // linked up & done!
    // otherwise, retry.
  } while (1); // something went wrong; repeat from the top.
}

DECL struct vinfo *findReadableVersion(struct vinfo *first_version,
				       struct commitrec *current_trans) {
  struct vinfo *v = first_version; // should not be null!
  struct commitrec *cr;
  do {
    int state;
    cr = v->transid;
    // look up to find the first non-committed cr; stopping if we find
    // ourself (or we run out of crs!)
    if (cr==NULL) // a null transid means the version is COMMITTED
      state=COMMITTED;
    else { // find the first non-committed cr
      while (cr!=current_trans && cr->parent!=NULL && cr->state==COMMITTED)
	cr = cr->parent;
      state = cr->state; // use this state.
    }
    switch (state) {
    case WAITING:
      // xxx note this doesn't work well if we have noncommitted
      //     subtransactions of the current transaction active.
      // correct thing in that case *seems* to be to look up the chain
      // and find out if the WAITING transaction is your child; if so
      // to -- what, abort it and continue on with v->anext?
      // xxx in any case, we're doing this simply for now.
      v = v->wnext; // avoids race where this *and* parent are committed
                    // before we get to parent
      continue;
    case COMMITTED:
      // committed.  ok, committed up as far as we care about.  use this!
      return v;
    case ABORTED:
      // aborted.  look at the next version
      v = v->anext;
      continue;
    }
    // note that there should *always* be a committed transaction
    // at the end of the chain.
  } while (1);
}

#endif
#if !defined(NO_VALUETYPE)

DECL struct vinfo *TA(EXACT_setReadFlags)(struct oobj *obj, unsigned offset,
					  unsigned flag_offset,
					  unsigned flag_bit,
					  struct vinfo *version,
					  struct commitrec *cr
					  /*this transaction*/){
  // pass in version if we already know it.
  // scheme:
  //  ensure we're in readers list (per object) (in EXACT_ensureReader)
  //  if field is flagged, then look up version (read bits already set)
  //                           (just return given version if that's non-NULL)
  //  else ensure read bits are set.
  VALUETYPE f = *(VALUETYPE volatile *)(FIELDBASE(obj) + offset);
  if (f==T(TRANS_FLAG)) { // field already flagged.
    if (!version) {
      // race possible: value's been copied back since we read it.
      // but that's okay, the version will still have the right info.
      //  xxx think about this more?  i think we're okay.
      //  xxx similar to subtrans issue: fix by ensuring that
      //      readableVersion never returns a committed version?
      //      (i.e. committed versions need to be copied back)? xxxx
      struct vinfo * volatile *versions = OBJ_VERSION_PTR(obj);
      struct vinfo *first_version = *versions;
      if (first_version!=NULL) // protect against copy back.
	// look up appropriate version
	version = findReadableVersion(first_version, cr);
    }
    return version;
  } else { // field not flagged, just ensure read bits are set.
    jint volatile * read_flag_ptr =
      (jint volatile *) (FLAGBASE(obj) + flag_offset);
    while (1) {
      jint read_flags = LL(read_flag_ptr);
      if (likely(read_flags & flag_bit) ||
	  SC(read_flag_ptr, read_flags | flag_bit))
	break;
    }
    return NULL; // (at the moment) no version for this field.
  }
}

/*--------------------------------------------------------------------*
 * Exact_setWriteFlags()
 *   Outline:
 *    1) lookup (or create) appropriate version.
 *       [done 'per object' in EXACT_ensureWriter(); version given as param ]
 *    2) now check if flags are already set, if so, quick return.
 *    3) need to set flags.  Copy current value across to all versions.
 *       Then X out field with FLAG_VALUE.
 *
 * Step 3, expanded:
 *  a) set the read flag
 *  b) load header.
 *  c) LL(canonical), check canonical, check header, SC(version).
 *     for all versions.
 *     canonical and header must stay the same, every SC must succeed.
 *  d) LL(header) check header SC(flag)  if SC succeeds, we're done.
 *
 * NOTE: once the read flag is set, a write to canonical can't happen
 * without a) clearing the read flags, *AND* b) linking in NULL
 * to the header.
 *
 * Correctness in the face of:
 *  I) writes to canonical version while we're copying.
 *      in step (3) changes to canonical will be caught by the reservation.
 *       changes between SC and LL must  involve clearing the read flag
 *       which will change the header, hence will be caught before the SC.
 *      in step (4), changes to canonical requiring clearing the read
 *       flag and changing the header.
 *  II) someone else trying to set the write flags
 *      in step (3) success will involve a change to canonical which will
 *       kill the reservation.  Other thread can flag the thread
 *       before us btween steps 3 and the end of step 4, but the
 *       duplicate write will do no harm.  If someone tries to copy back
 *       the other thread's flag, the header will be changed and our
 *       SC(flag) will not succeed.
 *  III) what about someone else setting the write flags and *then* someone
 *      trying to copy back while we're in the middle of this.
 *       see last case of above.
 *  IV) someone adding a new transaction version between our last check
 *  	new transactions are added with all FLAG_VALUE, so we don't
 *       have to worry about the new transaction not getting the
 *       backup of the value we're about to flag.
 *      case 1: new top-level transaction.  will copy back first,
 *         which means watching the header will catch the copy back
 *         killing us.
 *      case 2: new subtransaction.
 *         again, the header must change to allow this to be linked in.
 */
DECL void TA(EXACT_setWriteFlags)(struct oobj *obj, unsigned offset,
				  unsigned flag_offset, unsigned flag_bit) {
  jint volatile *read_flag_ptr;
  jint read_flags;
  /*    1) lookup (or create) appropriate version.
   *       done in EXACT_ensureWriter(), passed in as non-null 'version' param
   *       do the ENSURE_HAS_TRANSACT_INFO there.
   *    2) check if flags already set; if so, quick return.		*/
  read_flag_ptr = (jint volatile *) (FLAGBASE(obj) + flag_offset);
 retry_copy_over:
  do {
    struct vinfo * volatile *versions, *first_version, *v;
    VALUETYPE canonical;
    // ensure read flags are set.
    read_flags = LL(read_flag_ptr);
    if (unlikely(0 == (read_flags & flag_bit)) &&
	unlikely(!SC(read_flag_ptr, read_flags | flag_bit)))
      goto retry_copy_over;
    // check if write flag is already set.
    canonical = *(VALUETYPE volatile *)(FIELDBASE(obj) + offset);
    if (likely(canonical==T(TRANS_FLAG))) return;/* done: flags already set. */
    /*    3)  need to set flags.  Copy current value across to all versions.
     *        Then X out field with FLAG_VALUE.
     *    3a) read flag already set.
     *    3b) load versions header.					*/
    versions = OBJ_VERSION_PTR(obj);
    first_version = *versions;
    // check that read flags are still set
    if (unlikely(0==(flag_bit & *read_flag_ptr)))
      goto retry_copy_over;
    /*    3c) LL(canonical), check canonical, check header, SC(version).
     *     for all versions.
     *     also version_hash_write_alloc() the field for each version;
     *      may require a call to resizeVersion(obj, version)
     *     canonical and header must stay the same, every SC must succeed. */
    for (v=first_version; v!=NULL; v=v->wnext) {
      // ensure space
      if (offset > OBJ_CHUNK_SIZE-sizeof(VALUETYPE)) {
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
      // LL and check canonical and header, then SC(version)
      if (offset <= OBJ_CHUNK_SIZE-sizeof(VALUETYPE)) {
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
    /*  d) LL(header) check header SC(flag)  if SC succeeds, we're done. */
    if (first_version == LL(versions) &&
	T(store_conditional)(FIELDBASE(obj), offset, T(TRANS_FLAG)))
      return; // done!
    // nope, something went wrong.  try again, please.
  } while(1);
}
#endif

#endif /* !IN_VERSIONS_HEADER */

/* clean up after ourselves */
#include "transact/preproc.h"
#undef DECL
