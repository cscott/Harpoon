#ifndef INCLUDED_JNI_GC_H
#define INCLUDED_JNI_GC_H

#include "config.h"
#include "jni-types.h"
#include "jni-private.h"

#ifdef WITH_PRECISE_GC

//#define DEBUG_GC
//#define WITH_STATS_GC

#ifdef DEBUG_GC
# define error_gc(fs,a) ({ printf(fs, a); fflush(stdout); })
#else
# define error_gc(fs,a) (/* do nothing */0)
#endif

#ifndef WITH_STATS_GC
# define precise_gc_print_stats()
#else
void precise_gc_print_stats();
#endif

/* returns: amt of free memory available */
jlong precise_free_memory ();

/* effects: setup and initialization for the GC */
void precise_gc_init ();

/* returns: size of heap */
jlong precise_get_heap_size ();

/* effects: forces garbage collection to occur */
void precise_collect ();

/* effects: registers inflated obj for resources to be freed after GC */
void precise_register_inflated_obj(jobject_unwrapped obj,
				   void (*deflate_fcn)(jobject_unwrapped obj, 
						       ptroff_t client_data));

/* effects: given the struct FNI_Thread_State ptr of a
            thread, adds its thread-local references to 
	    the root set using add_to_root_set */
void handle_local_refs_for_thread(struct FNI_Thread_State *thread_state_ptr);

/* --------- new garbage collection stuff ---------- */
#ifdef WITH_PRECISE_C_BACKEND
inline void *precise_malloc (size_t size_in_bytes);
#else /* !WITH_PRECISE_C_BACKEND */
void *precise_malloc_int (size_t size_in_bytes, void *saved_registers[]);
#endif /* !WITH_PRECISE_C_BACKEND */

/* structures for gc */

struct gc_table {
  jint descriptor;
};

struct base_table {
  ptroff_t bt[0];
};

struct gc_index {
  void * retaddr;
  struct gc_table * gc_data;
  struct base_table * bt_ptr; /* pointer to base table */
};
extern struct gc_index gc_index_start[], gc_index_end[];

extern void *gc_start[], *gc_end[];

void find_root_set();

#endif /* WITH_PRECISE_GC */

#endif /* INCLUDED_JNI_GC_H */




