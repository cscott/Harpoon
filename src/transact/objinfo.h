// means to access the readers list and the version list
#ifndef INCLUDED_TRANSACT_OBJINFO_H
#define INCLUDED_TRANSACT_OBJINFO_H

#include "config.h"
#include "compiler.h" /* for likely/unlikely */
#include "fni-objlayout.h" /* for internal struct of struct oobj */
#include "fni-ptrmask.h" /* for PTRMASK */
#include "fni-wrap.h" /* for FNI_WRAP */

extern inline void ENSURE_HAS_TRANSACT_INFO(struct oobj *obj) { }
extern inline struct vinfo * volatile *OBJ_VERSION_PTR(struct oobj *obj) {
  // first field is versions list.
  return (struct vinfo*volatile*) (((struct oobj *)PTRMASK(obj))->field_start);
}
extern inline struct tlist * volatile *OBJ_READERS_PTR(struct oobj *obj) {
  return (struct tlist*volatile*)
    (((struct oobj *)PTRMASK(obj))->field_start+SIZEOF_VOID_P);
}

#endif /* INCLUDED_TRANSACT_OBJINFO_H */
