/* This file doesn't do anything but define symbols.  References to
 * these symbols are emitted by FLEX based on its configuration.
 * By controlling which symbols are defined here, we can force a
 * link error if the FLEX configuration and the runtime configuration
 * don't match. */

#include "config.h" /* get the configuration options */

typedef char FLAG; /* can we reduce memory footprint even further? */

/* we use a trick to allow symbol concatanation by the preprocessor */
/* much harder than it should be, we need to exploit pre-scan. */
/* basically, we need two levels of macro expansion, so we need two
 * macros:
 *  x1(OBJECT_PADDING) yields 'check_object_padding_should_be_4'
 *  x2(OBJECT_PADDING) yields 'check_object_padding_should_be_OBJECT_PADDING'
 */

#define x1(x) x2(x)
#define x2(x) \
FLAG check_object_padding_should_be_##x
x1(OBJECT_PADDING);

#define x3(x) x4(x)
#define x4(x) \
FLAG check_with_pointer_size_should_be_##x
x3(SIZEOF_VOID_P);

#define x5(x) x6(x)
#define x6(x) \
FLAG check_with_claz_shrink_should_be_##x
#ifdef WITH_CLAZ_SHRINK
 x5(WITH_CLAZ_SHRINK);
#else
 FLAG check_with_claz_shrink_not_needed;
#endif

/* okay, from here on out there's nothing complicated */

#ifdef WITH_PRECISE_C_BACKEND
 FLAG check_with_precise_c_needed;
#else
 FLAG check_with_precise_c_not_needed;
#endif

#ifdef WITH_USER_THREADS
 FLAG check_with_thread_model_should_be_user;
#endif
#ifdef WITH_HEAVY_THREADS
 FLAG check_with_thread_model_should_be_heavy;
#endif
#ifdef WITH_PTH_THREADS
 FLAG check_with_thread_model_should_be_pth;
#endif
#ifdef WITH_NO_THREADS
 FLAG check_with_thread_model_should_be_none;
#endif

#ifdef WITH_SEMI_PRECISE_GC
 FLAG check_with_gc_should_be_semi;
#endif
#ifdef WITH_COPYING_GC
 FLAG check_with_gc_should_be_copying;
#endif
#ifdef WITH_MARKSWEEP_GC
 FLAG check_with_gc_should_be_marksweep;
#endif
#ifdef WITH_GENERATIONAL_GC
 FLAG check_with_gc_should_be_generational;
#endif
#ifdef WITH_CONSERVATIVE_GC
 FLAG check_with_gc_should_be_conservative;
#endif
#ifdef WITH_NO_GC
 FLAG check_with_gc_should_be_none;
#endif

#ifdef WITH_CLUSTERED_HEAPS
 FLAG check_with_clustered_heaps_needed;
#endif

#ifdef WITH_TRANSACTIONS
 FLAG check_with_transactions_needed;
#endif

#ifdef WITH_MASKED_POINTERS
 FLAG check_with_masked_pointers_needed;
#endif

#ifdef WITH_INIT_CHECK
 FLAG check_with_init_check_needed;
#else
 FLAG check_with_init_check_not_needed;
#endif

#ifdef WITH_HASHLOCK_SHRINK
 FLAG check_with_hashlock_shrink_needed;
#else
 FLAG check_with_hashlock_shrink_not_needed;
#endif

/* at the moment, the runtime only supports sun's jdk. */
FLAG check_with_sunjdk_needed;
