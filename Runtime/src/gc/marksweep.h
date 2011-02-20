#ifndef INCLUDED_MARKSWEEP_H
#define INCLUDED_MARKSWEEP_H

#include "config.h"
#include "jni-gc.h"

#ifdef WITH_PRECISE_C_BACKEND

/* ---- constants for POINTER-REVERSED MARKSWEEP GC ---- */
#define NO_POINTERS  0

#define INDEX_OFFSET 1

void marksweep_add_to_root_set(jobject_unwrapped *obj);

void marksweep_collect();

jlong marksweep_free_memory();

void marksweep_gc_init();

jlong marksweep_get_heap_size();

#ifndef WITH_POINTER_REVERSAL
void marksweep_handle_reference(jobject_unwrapped *ref);
#else /* WITH_POINTER_REVERSAL */
void pointerreversed_handle_reference(jobject_unwrapped *ref);
#endif /* WITH_POINTER_REVERSAL */

void *marksweep_malloc (size_t size_in_bytes);

void marksweep_handle_nonroot(jobject_unwrapped *nonroot);

#ifdef WITH_PRECISE_GC_STATISTICS
// from harpoon_Runtime_PreciseGC_WriteBarrier.c
void init_statistics();
#else /* !WITH_PRECISE_GC_STATISTICS */
#define init_statistics() ((void)0)
#endif /* !WITH_PRECISE_GC_STATISTICS */

#endif /* WITH_PRECISE_C_BACKEND */
#endif /* !INCLUDED_MARKSWEEP_H */
