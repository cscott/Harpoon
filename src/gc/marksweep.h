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

void marksweep_gc_init();

void *marksweep_malloc (size_t size_in_bytes);

void marksweep_handle_nonroot(jobject_unwrapped *nonroot);

#ifdef WITH_STATS_GC
// from harpoon_Runtime_PreciseGC_WriteBarrier.c
void init_statistics();
#else
#define init_statistics()
#endif

#endif

#endif
