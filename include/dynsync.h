/** This file defines a helper for the precisec backend to implement
 *  dynamic synchronization removal support. */
#ifndef INCLUDED_DYNSYNC_H
#define INCLDUED_DYNSYNC_H

#ifndef WITH_DYNAMIC_SYNC_REMOVAL
# error You should not include dynsync.h unless you need DynSynRem support.
#endif /* WITH_DYNAMIC_SYNC_REMOVAL */

#include <config.h>
#include <fni-ptrmask.h> /* for PTRMASK */
#include <fni-objlayout.h> /* for struct oobj */

/* For 'runtime2', we set the 2nd bit of the hashcode if synchronization
 * operations on this object can be skipped. */
extern inline int DYNSYNC_isSync(struct oobj *obj) {
  return 0==(((struct oobj*)PTRMASK(obj))->hashunion.hashcode & 2);
}

#endif /* INCLUDED_DYNSYNC_H */
