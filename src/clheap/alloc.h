/* Declarations for the Nifty Allocation Strategy. */

#ifndef INCLUDED_NIFTY_ALLOC_H
#define INCLUDED_NIFTY_ALLOC_H

#include <jni.h>
#include <jni-private.h>
#include <stdlib.h> /* for size_t */

void *NGBL_malloc(size_t size);
void *NGBL_malloc_atmoic(size_t size);
void *NSTK_malloc(size_t size);
void *NTHR_malloc(size_t size);
void *NTHR_malloc_first(size_t size);
void *NTHR_malloc_other(size_t size, struct oobj *obj);
/* release a thread-clustered heap */
void NTHR_free(jobject thread);

#endif /* INCLUDED_NIFTY_ALLOC_H */
