// means to access the readers list and the version list
#ifndef INCLUDED_TRANSACT_OBJINFO_H
#define INCLUDED_TRANSACT_OBJINFO_H

#include "compiler.h" /* for likely/unlikely */
#include "fni-objlayout.h" /* for internal struct of struct oobj */
#include "fni-ptrmask.h" /* for PTRMASK */
#include "fni-wrap.h" /* for FNI_WRAP */

// eventually, we'd like to put these two fields in the object.
// but for now, we'll make them external.
extern inline void ENSURE_HAS_TRANSACT_INFO(struct oobj *obj) {
  struct oobj *masked = (struct oobj *)PTRMASK(obj);
  // xxx note that this violates the 'is inflated' abstraction boundary,
  //     because we don't want to have to wrap the object if we don't
  //     have to.
  if (unlikely(0 != (masked->hashunion.hashcode & 1))) {
    JNIEnv *env = FNI_GetJNIEnv();
    jobject o = FNI_WRAP(obj);
    FNI_InflateObject(env, o);
    FNI_DeleteLocalRef(env, o);
  }
}
extern inline struct tlist * volatile *OBJ_READERS_PTR(struct oobj *obj) {
  struct oobj *masked = (struct oobj *)PTRMASK(obj);
  // again, we violate abstraction for the sake of efficiency.
  // also, the paper deadline is really really soon.
  return &(masked->hashunion.inflated->readers);
}
extern inline struct vinfo * volatile *OBJ_VERSION_PTR(struct oobj *obj) {
  struct oobj *masked = (struct oobj *)PTRMASK(obj);
  return &(masked->hashunion.inflated->first_version);
}

#endif /* INCLUDED_TRANSACT_OBJINFO_H */
