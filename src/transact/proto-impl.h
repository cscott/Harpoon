/** Prototypes for the various transactions-related functions. */

#include "transact/preproc.h"

//////////////////////////////////////////////////////////////////////////
#if !defined(NO_VALUETYPE)

//////////////////////// in atomic-impl.h
#if 0 /* static, not exported */
static int T(store_conditional)(void *base, unsigned offset, VALUETYPE value);
static VALUETYPE T(load_linked)(void *base, unsigned offset);
#endif

//////////////////////// in readwrite-impl.c
// reading from versions.
extern VALUETYPE TA(readFromVersion)(struct vinfo *version, unsigned offset);
// writing to versions.
extern void TA(writeToVersion)(struct oobj *obj, struct vinfo *version,
			       unsigned offset, VALUETYPE value);
// deep transaction magic.
extern VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset,
				  unsigned flag_offset, unsigned flag_bit);
// must have already recorded itself as a reader (set flags, etc)
extern VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
				 struct vinfo *version, struct commitrec *cr);
extern void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
			      VALUETYPE value,
			      unsigned flag_offset, unsigned flag_bit);
// prior to this point, we should have recorded the write
// (guaranteed that the object field had the FLAG value, and that flag bits
//  are set)
extern void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
			     VALUETYPE value, struct vinfo *version);

//////////////////////// in versions.c
extern enum opstatus TA(copy_back)(struct oobj *obj, struct vinfo *nonce,
				   struct vinfo *version, unsigned offset,
				   int copy_aliased);
extern enum opstatus TA(getVersion_readNT)(struct oobj *obj, unsigned offset,
					   unsigned flag_offset,
					   unsigned flag_bit);
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

#else

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

/* clean up after ourselves */
#include "transact/preproc.h"
