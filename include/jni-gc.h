#ifndef INCLUDED_JNI_GC_H
#define INCLUDED_JNI_GC_H

//#define DEBUG_GC

#ifdef DEBUG_GC
# define error_gc(fs,a) ({ printf(fs, a); fflush(stdout); })
#else
# define error_gc(fs,a) (/* do nothing */0)
#endif

#ifdef WITH_THREADS
# define WITH_THREADED_GC
#endif /* WITH_THREADS */

#if defined(WITH_PRECISE_GC) || defined(WITH_SEMI_PRECISE_GC)
# define WORDSZ          (SIZEOF_VOID_P*8)
# define WORDSZ_IN_BYTES  SIZEOF_VOID_P
#endif /* WITH_PRECISE_GC/WITH_SEMI_PRECISE_GC */

#ifdef WITH_PRECISE_GC

/* effects: setup and initialization for the GC */
void precise_gc_init ();

/* effects: given the struct FNI_Thread_State ptr of a
            thread, adds its thread-local references to 
	    the root set using add_to_root_set */
void handle_local_refs_for_thread(struct FNI_Thread_State *thread_state_ptr);

/* --------- new garbage collection stuff ---------- */
#ifdef WITH_PRECISE_C_BACKEND
void *precise_malloc (size_t size_in_bytes);
void *copying_malloc (size_t size_in_bytes);

#ifdef WITH_THREADED_GC
/* only the garbage collector should write this */
extern jint halt_for_GC_flag; 
void halt_for_GC();
#endif /* WITH_THREADED_GC */

#else /* !WITH_PRECISE_C_BACKEND */
void *precise_malloc_int (size_t size_in_bytes, void *saved_registers[]);
void *copying_malloc (size_t size_in_bytes, void *saved_registers[]);
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

void add_to_root_set(jobject_unwrapped *obj);

void find_root_set();

#endif /* WITH_PRECISE_GC */

#endif /* INCLUDED_JNI_GC_H */




