/** Testing prototypes for the various transactions-related functions. */

#include "transact/preproc.h"

///////////////////////////////////////////////////////////////////////////
#if !defined(NO_VALUETYPE)
extern inline VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset) {
  return *((VALUETYPE*)(FIELDBASE(obj)+offset));
}
// must have already recorded itself as a reader (set flags, etc)
extern inline VALUETYPE TA(EXACT_readT)(struct oobj *obj, unsigned offset,
					struct vinfo *version,
					struct commitrec *cr) {
  assert(cr!=NULL);
  return *((VALUETYPE*)(FIELDBASE(obj)+offset));
}
extern inline void TA(EXACT_writeNT)(struct oobj *obj, unsigned offset,
				     VALUETYPE value) {
  *((VALUETYPE*)(FIELDBASE(obj)+offset)) = value;
}
extern inline void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
				    VALUETYPE value, struct vinfo *version) {
  assert(version!=NULL);
  *((VALUETYPE*)(FIELDBASE(obj)+offset)) = value;
}
extern inline void TA(EXACT_checkReadField)(struct oobj *obj, unsigned offset){
  /* do nothing */
}
extern inline void TA(EXACT_checkWriteField)(struct oobj *obj,unsigned offset){
  /* do nothing */
}
#endif
#if defined(NO_VALUETYPE)
extern inline struct vinfo *EXACT_ensureReader(struct oobj *obj,
					       struct commitrec *cr) {
  /* do nothing */
  return NULL;
}
extern inline struct vinfo *EXACT_ensureWriter(struct oobj *obj,
					       struct commitrec *currentTrans){
  /* do nothing */
  return NULL;
}
#endif


/* clean up after ourselves */
#include "transact/preproc.h"
