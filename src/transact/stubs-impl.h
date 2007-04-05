/** Testing stubs for the various transactions-related functions. */

#include "transact/preproc.h" /* Defines 'T()' and 'TA()' macros. */

// prototypes.
#if defined(IN_STUBS_HEADER)
#if !defined(NO_VALUETYPE)
extern VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset);
extern VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
				 struct vinfo *version,
				 struct commitrec *cr);
extern void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
			      VALUETYPE value);
extern void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
			     VALUETYPE value, struct vinfo *version);
extern void TA(EXACT_checkReadField)(struct oobj *obj, unsigned offset);
extern void TA(EXACT_checkWriteField)(struct oobj *obj,unsigned offset);
#else /* defined(NO_VALUETYPE) */
extern struct vinfo *EXACT_ensureReader(struct oobj *obj,
					struct commitrec *cr);
extern struct vinfo *EXACT_ensureWriter(struct oobj *obj,
					struct commitrec *currentTrans);
#endif
#endif /* IN_STUBS_HEADER */

///////////////////////////////////////////////////////////////////////////
// implementations

#if !defined(IN_STUBS_HEADER)

#if !defined(NO_VALUETYPE)
VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset) {
  return *((VALUETYPE*)(FIELDBASE(obj)+offset));
}
// must have already recorded itself as a reader (set flags, etc)
VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
					struct vinfo *version,
					struct commitrec *cr) {
  assert(cr!=NULL);
  return *((VALUETYPE*)(FIELDBASE(obj)+offset));
}
void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
				     VALUETYPE value) {
  *((VALUETYPE*)(FIELDBASE(obj)+offset)) = value;
}
void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
				    VALUETYPE value, struct vinfo *version) {
  *((VALUETYPE*)(FIELDBASE(obj)+offset)) = value;
}
void TA(EXACT_checkReadField)(struct oobj *obj, unsigned offset){
  /* do nothing */
}
void TA(EXACT_checkWriteField)(struct oobj *obj,unsigned offset){
  /* do nothing */
}
#else /* defined(NO_VALUETYPE) */
struct vinfo *EXACT_ensureReader(struct oobj *obj,
					       struct commitrec *cr) {
  /* do nothing */
  return NULL;
}
struct vinfo *EXACT_ensureWriter(struct oobj *obj,
					       struct commitrec *currentTrans){
  /* do nothing */
  return NULL;
}
#endif

#endif

/* clean up after ourselves */
#include "transact/preproc.h"
