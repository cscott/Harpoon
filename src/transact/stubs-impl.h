/** Testing prototypes for the various transactions-related functions. */

#include "transact/preproc.h"

///////////////////////////////////////////////////////////////////////////
#if !defined(NO_VALUETYPE)
extern inline VALUETYPE TA(EXACT_readNT)(struct oobj *obj, unsigned offset,
					 unsigned flag_offset,
					 unsigned flag_bit) {
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
				     VALUETYPE value,
				     unsigned flag_offset, unsigned flag_bit) {
  *((VALUETYPE*)(FIELDBASE(obj)+offset)) = value;
}
extern inline void TA(EXACT_writeT)(struct oobj *obj, unsigned offset,
				    VALUETYPE value, struct vinfo *version) {
  assert(version!=NULL);
  *((VALUETYPE*)(FIELDBASE(obj)+offset)) = value;
}
extern inline struct vinfo *TA(EXACT_setReadFlags)
     (struct oobj *obj, unsigned offset,
      unsigned flag_offset, unsigned flag_bit,
      struct vinfo *version, struct commitrec*cr/*this trans*/) {
  assert(cr!=NULL);
  /* do nothing */
  return NULL;
}
extern inline void TA(EXACT_setWriteFlags)
     (struct oobj *obj, unsigned offset,
      unsigned flag_offset, unsigned flag_bit) {
  /* do nothing */
}
#endif
#if defined(NO_VALUETYPE)
extern inline void EXACT_ensureReader(struct oobj *obj, struct commitrec *cr) {
  /* do nothing */
}
extern inline struct vinfo *EXACT_ensureWriter(struct oobj *obj,
					       struct commitrec *currentTrans){
  /* do nothing */
  assert(currentTrans!=NULL);
  return (struct vinfo *)1;// 'null' indicates we want to abort.
}
#endif


/* clean up after ourselves */
#include "transact/preproc.h"
