#ifndef INCLUDED_PRECISE_GC_H
#define INCLUDED_PRECISE_GC_H

#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
# define WITH_THREADED_GC
#endif

#if defined(WITH_MARKSWEEP_GC)
#include "marksweep.h"
# ifdef WITH_POINTER_REVERSAL 
#  define add_to_root_set        pointerreversed_handle_reference
#  define handle_reference       pointerreversed_handle_reference
# else
#  define add_to_root_set        marksweep_handle_reference
#  define handle_reference       marksweep_handle_reference
# endif
# define internal_print_stats   print_write_barrier_stats
# define internal_register_inflated_obj(obj)
# define internal_gc_init       marksweep_gc_init
# define internal_collect       marksweep_collect
# define internal_free_memory   marksweep_free_memory
# define internal_get_heap_size marksweep_get_heap_size
# define internal_malloc        marksweep_malloc

#elif defined(WITH_COPYING_GC)
# include "copying.h"
# define add_to_root_set        copying_handle_reference
# define internal_gc_init       copying_gc_init
# define internal_print_stats   copying_print_stats
# define internal_register_inflated_obj copying_register_inflated_obj
# define handle_reference       copying_handle_reference
# define internal_collect()     copying_collect((int)0)
# define internal_free_memory   copying_free_memory
# define internal_get_heap_size copying_get_heap_size
# define internal_malloc        copying_malloc

#elif defined(WITH_GENERATIONAL_GC)
# include "generational.h"
void print_write_barrier_stats(); // harpoon_Runtime_PreciseGC_WriteBarrier.c 
# define internal_malloc        generational_malloc
# define add_to_root_set        generational_handle_reference
# define internal_gc_init       generational_gc_init
# define internal_print_stats   print_write_barrier_stats
# define internal_register_inflated_obj generational_register_inflated_obj
# define handle_reference       generational_handle_reference
# define internal_collect       generational_collect
# define internal_free_memory   generational_free_memory
# define internal_get_heap_size generational_get_heap_size
#endif

#ifdef WITH_MASKED_POINTERS
# define TAG_HEAP_PTR(x) ((void*) ((ptroff_t)(x) | 1))
#else
# define TAG_HEAP_PTR(x) ((void*) (x))
#endif

#define CLAZ_OKAY(x) (&claz_start <= (x)->claz && (x)->claz < &claz_end)

/* the number of bits in the in-line gc bitmap is platform-dependent */
#define BITS_IN_GC_BITMAP (SIZEOF_VOID_P*8)

#ifdef WITH_SINGLE_WORD_ALIGN
# define ALIGN_TO  4 /* bytes */
#else
# define ALIGN_TO  8 /* bytes */
#endif

#define ALIGNMENT               (ALIGN_TO - 1)
#define SIZE_MASK               (~ALIGNMENT)
#define OBJ_HEADER_SIZE         (sizeof(struct oobj))

#define align(_unaligned_size_) (((_unaligned_size_) + ALIGNMENT) & SIZE_MASK)

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

#ifndef DEBUG_GC
# define debug_verify_object(obj)
#else
void debug_verify_object(jobject_unwrapped obj);
#endif

/* ---- functions for COPYING and MARKSWEEP GC ---- */ 

/* trace takes a pointer to an object and traces the pointers w/in it */
void trace(jobject_unwrapped obj);

/* ---- functions for POINTER-REVERSED MARKSWEEP GC ---- */
ptroff_t get_next_index(jobject_unwrapped obj, ptroff_t next_index);

#endif
