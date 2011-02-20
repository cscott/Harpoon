/* JNI transactions support functions */
#ifndef INCLUDED_TRANSACT_TRANSJNI_H
#define INCLUDED_TRANSACT_TRANSJNI_H

/* IN_TRANSJNI_HEADER indicates that we want prototypes,
 * and function bodies only for inlined methods. */
#define IN_TRANSJNI_HEADER
#include "transact/transjni.c"
#undef IN_TRANSJNI_HEADER

#endif /* INCLUDED_TRANSACT_TRANSJNI_H */
