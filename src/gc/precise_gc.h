#ifndef INCLUDED_PRECISE_GC_H
#define INCLUDED_PRECISE_GC_H

//#define GC_EVERY_TIME

#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
# define WITH_THREADED_GC
#endif

#ifdef MARKSWEEP
#include "marksweep.h"
#define add_to_root_set  marksweep_handle_reference
#define internal_gc_init marksweep_gc_init
#define handle_reference marksweep_handle_reference
#define internal_malloc  marksweep_malloc
#else
#include "copying.h"
#define add_to_root_set  copying_handle_reference
#define internal_gc_init copying_gc_init
#define handle_reference copying_handle_reference
#define internal_malloc  copying_malloc
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

#ifndef WITH_THREADED_GC
#define halt_for_GC()
#define setup_for_threaded_GC()
#define cleanup_after_threaded_GC()
#else
/* halt thread so garbage collection can begin */
void halt_for_GC();

/* halt other threads and acquire necessary locks */
void setup_for_threaded_GC();

/* release locks and allow threads to continue */
void cleanup_after_threaded_GC();
#endif

/* x: jobject_unwrapped
   effects: if obj is an object, adds object fields to root set;
   if an array, adds object elements to root set. */
#define trace(x) ({ ((x)->claz->component_claz == NULL) ? \
                    trace_object(x) : trace_array((struct aarray *)(x)); })

#endif
