/** Prototypes for the various transactions-related functions. */

#include "transact/preproc.h"

#include "transact/transact-ty.h" /* versionPair/opstatus/killer */
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
extern VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset);
// must have already recorded itself as a reader (set flags, etc)
extern VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
				 struct vinfo *version, struct commitrec *cr);
extern void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
			      VALUETYPE value);
// prior to this point, we should have recorded the write
// (guaranteed that the object field had the FLAG value, and that flag bits
//  are set)
extern void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
			     VALUETYPE value, struct vinfo *version);

//////////////////////// in versions.c
extern void TA(EXACT_checkReadField)(struct oobj *obj, unsigned offset);
extern void TA(EXACT_checkWriteField)(struct oobj *obj, unsigned offset);
extern enum opstatus TA(copyBackField)(struct oobj *obj, unsigned offset,
                                       enum killer kill_whom);
extern struct vinfo *TA(createVersion)(struct oobj *obj, struct commitrec *cr,
				       struct vinfo *template);

#else

// in versions.c
extern struct versionPair findVersion(struct oobj *obj, struct commitrec *cr);
// ensure we're in readers list (per object)
extern struct vinfo *EXACT_ensureReader(struct oobj *obj,
					struct commitrec *cr);
extern struct vinfo *EXACT_ensureWriter(struct oobj *obj,
					struct commitrec *currentTrans);
// dispatches based on the runtime type of 'obj'
extern struct vinfo *createVersion(struct oobj *obj, struct commitrec *cr,
				   struct vinfo *template);
#endif

///////////////////////////////////////////////////////
// graveyard
#if 0
extern enum opstatus TA(getVersion_readNT)(struct oobj *obj, unsigned offset);
extern void TA(getVersion_writeNT)(struct oobj *obj, unsigned offset);
extern struct vinfo *findReadableVersion(struct vinfo *first_version,
					 struct commitrec *current_trans);
extern struct vinfo *resizeVersion(struct oobj *obj, struct vinfo *version);
extern struct vinfo *getVersion_readT(struct oobj *obj,
				      struct commitrec *currentTrans);
#endif


/* clean up after ourselves */
#include "transact/preproc.h"
