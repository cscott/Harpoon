#ifndef INCLUDED_FNI_RUNTIME_H
#define INCLUDED_FNI_RUNTIME_H

#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
# include "gc.h"
#endif

#ifdef WITH_PRECISE_GC
#include "jni-gc.h"
#endif

#include <assert.h>
#include <unistd.h>
#include <stdlib.h> /* for exit */

static inline
void fni_runtime_exitInternal(JNIEnv *env, jint status) {
#ifdef WITH_STATISTICS
  /* print out collected statistics */
  { void print_statistics(void); print_statistics(); }
#endif
    exit(status);
}

static inline
jlong fni_runtime_freeMemory(JNIEnv *env) {
#ifdef BDW_CONSERVATIVE_GC
  return (jlong) GC_get_free_bytes();
#elif defined(WITH_PRECISE_GC)
  return precise_free_memory();
#else
  assert(0/*unimplemented*/);
#endif
}

static inline
jlong fni_runtime_totalMemory(JNIEnv *env) {
#ifdef BDW_CONSERVATIVE_GC
  return (jlong) GC_get_heap_size();
#elif defined(WITH_PRECISE_GC)
  return precise_get_heap_size();
#else
  assert(0/*unimplemented*/);
#endif
}

static inline
void fni_runtime_gc(JNIEnv *env) {
#ifdef BDW_CONSERVATIVE_GC
  GC_gcollect();
#elif defined(WITH_PRECISE_GC)
  precise_collect();
#else
  assert(0/*unimplemented*/);
#endif
}

static inline
void fni_runtime_runFinalization(JNIEnv *env) {
#ifdef BDW_CONSERVATIVE_GC
  GC_invoke_finalizers();
#elif defined(WITH_PRECISE_GC)
  /* unimplemented */
  printf("WARNING: Finalization not implemented for precise GC.\n");
#else
  /* unimplemented */
  printf("WARNING: Finalization not implemented.\n");
#endif
}

#endif /* INCLUDED_FNI_RUNTIME_H */
