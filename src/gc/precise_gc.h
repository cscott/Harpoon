#ifndef INCLUDED_PRECISE_GC_H
#define INCLUDED_PRECISE_GC_H

//#define GC_EVERY_TIME

#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
# define WITH_THREADED_GC
#endif

#ifdef MARKSWEEP
#include "marksweep.h"
#define add_to_root_set  marksweep_add_to_root_set
#define handle_nonroot   marksweep_handle_nonroot
#else
#include "copying.h"
#define add_to_root_set  copying_add_to_root_set
#define handle_nonroot   copying_handle_nonroot
#endif

#define ALIGN                  7
#define BITMASK               (~ALIGN)
#define HEADERSZ               3 /* size of array header */

#define align(_unaligned_size_) (((_unaligned_size_) + ALIGN) & BITMASK)
#define aligned_size_of_np_array(_np_arr_) \
(align((HEADERSZ * WORDSZ_IN_BYTES + \
	(_np_arr_)->length * (_np_arr_)->obj.claz->component_claz->size)))
#define aligned_size_of_p_array(_p_arr_) \
(align((HEADERSZ + (_p_arr_)->length) * WORDSZ_IN_BYTES))

void trace_array(struct aarray *arr);

void trace_object(jobject_unwrapped obj);

/* halt thread so garbage collection can begin */
void halt_for_GC();

/* halt other threads and acquire necessary locks */
void setup_for_threaded_GC();

/* release locks and allow threads to continue */
void cleanup_after_threaded_GC();

/* x: jobject_unwrapped
   effects: if obj is an object, adds object fields to root set;
   if an array, adds object elements to root set. */
#define trace(x) ({ ((x)->claz->component_claz == NULL) ? \
                    trace_object(x) : trace_array((struct aarray *)(x)); })

#endif
