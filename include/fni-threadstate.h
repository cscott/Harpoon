/* This is the thread state structure.  It's been moved from
 * jni-private.h so that it can more easily be referenced from
 * the precisec backend. */

#ifndef INCLUDED_FNI_THREADSTATE
#define INCLUDED_FNI_THREADSTATE

#include <jni.h>
#include <config.h>
#if WITH_PRECISE_C_BACKEND
#include <setjmp.h>
#endif
#include "fni-wrap.h" /* for struct _jobject */

/* ---------------------- thread state ---------------------------------*/
// any changes to this structure should be reflected in
// Code/Backend/Runtime1/StubCode.<foo>_OFFSET (see constructor)
// and Code/Backend/Runtime1/TreeBuilder._call_FNI_Monitor()
// (specifically, any changes above and including the localrefs field) */
struct FNI_Thread_State {
  JNIEnv vtable;
  jthrowable exception; /* outstanding exception, or NULL if no exception. */
  struct _jobject localrefs; /* header node in a local refs list. */
  jobject thread; /* thread object corresponding to this thread state. */
  void *stack_top; /* top of stack */
  jboolean is_alive; /* true while the thread is running */
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  pthread_t pthread; /* the pthread corresponding to this thread state. */
  pthread_cond_t sleep_cond; /* condition variable for sleep/suspend. */
  pthread_mutex_t sleep_mutex; /* mutex for sleep/suspend. */
#endif
#if WITH_PRECISE_C_BACKEND
  jmp_buf handler;
#endif
};
extern struct _jobject FNI_globalrefs; /* header node in global refs list. */

#define FNI_NO_EXCEPTIONS(env) \
	(((struct FNI_Thread_State *)(env))->exception==NULL)

/* -------------- end thread state structure. ------------- */

#endif /* INCLUDED_FNI_THREADSTATE */
