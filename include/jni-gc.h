/* #define DEBUG_GC */

#ifdef DEBUG_GC
# define error_gc(fs,a) ({ printf(fs, a); fflush(stdout); })
#else
# define error_gc(fs,a) ({/* do nothing */})
#endif

/* structures for gc */

#ifndef INCLUDED_JNI_GC_H
#define INCLUDED_JNI_GC_H

#if defined(WITH_PRECISE_GC) || defined(WITH_SEMI_PRECISE_GC)
# define WORDSZ          (SIZEOF_VOID_P*8)
# define WORDSZ_IN_BYTES  SIZEOF_VOID_P
#endif /* WITH_PRECISE_GC/WITH_SEMI_PRECISE_GC */

#ifdef WITH_PRECISE_GC

/* --------- new garbage collection stuff ---------- */
#ifdef WITH_PRECISE_C_BACKEND
void *precise_malloc (size_t size_in_bytes);
void *copying_malloc (size_t size_in_bytes);
#else
void *precise_malloc_int (size_t size_in_bytes, void *saved_registers[]);
void *copying_malloc (size_t size_in_bytes, void *saved_registers[]);
#endif

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




