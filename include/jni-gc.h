/* structures for gc */

#ifndef INCLUDED_JNI_GC_H
#define INCLUDED_JNI_GC_H

#if defined(WITH_PRECISE_GC) || defined(WITH_SEMI_PRECISE_GC)
# define WORDSZ          (SIZEOF_VOID_P*8)
# define WORDSZ_IN_BYTES  SIZEOF_VOID_P
#endif /* WITH_PRECISE_GC/WITH_SEMI_PRECISE_GC */

#ifdef WITH_PRECISE_GC

/* --------- new garbage collection stuff ---------- */

struct gc_index {
  void * retaddr;
  void * gc_data;
};
extern struct gc_index gc_index_start[], gc_index_end[];

extern void *gc_start[], *gc_end[];

/*
typedef union { 
  jobject_unwrapped unwrapped_obj; 
} pointer;

void add_to_root_set(pointer);
*/

void find_root_set();

#endif /* WITH_PRECISE_GC */

#endif /* INCLUDED_JNI_GC_H */




