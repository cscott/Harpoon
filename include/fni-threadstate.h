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
#define FLEXTHREAD_TYPEDEFS_ONLY
#include "flexthread.h" /* for pthread_* typedefs */
#undef FLEXTHREAD_TYPEDEFS_ONLY

/* ---------------------- thread state ---------------------------------*/
// any changes to this structure should be reflected in
// Code/Backend/Runtime1/StubCode.<foo>_OFFSET (see constructor)
// and Code/Backend/Runtime1/TreeBuilder._call_FNI_Monitor()
// (specifically, any changes above and including the localrefs_next field) */
struct FNI_Thread_State {
  JNIEnv vtable;
  jthrowable exception; /* outstanding exception, or NULL if no exception. */
  struct _jobject *localrefs_stack; /* bottom of local refs stack */
  struct _jobject *localrefs_next; /* points to next empty slot on lr stack */
  struct _jobject *localrefs_end;/*points after the last valid slot on lr stk*/
  jobject thread; /* thread object corresponding to this thread state. */
  void *stack_top; /* top of stack */
  jboolean is_alive; /* true while the thread is running */
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
#if WITH_HEAVY_THREAD || WITH_PTH_TREADS
  pthread_t pthread; /* the pthread corresponding to this thread state. */
#endif
  pthread_cond_t sleep_cond; /* condition variable for sleep/suspend. */
  pthread_mutex_t sleep_mutex; /* mutex for sleep/suspend. */
#endif
#if WITH_PRECISE_C_BACKEND
  jmp_buf *handler;
#endif
};
/* header node in doubly-linked global refs list. */
extern struct _jobject_globalref FNI_globalrefs;

#define FNI_NO_EXCEPTIONS(env) \
	(((struct FNI_Thread_State *)(env))->exception==NULL)

/* -------------- end thread state structure. ------------- */

#endif /* INCLUDED_FNI_THREADSTATE */
