#ifndef INCLUDED_FNI_THREADLIST_H
#define INCLUDED_FNI_THREADLIST_H

#include <jni.h> /* for JNIEnv */
#include "config.h" /* for WITH_PRECISE_GC */

void add_running_thread(JNIEnv *env);
void remove_running_thread(void *cl);
void wait_on_running_thread();

extern pthread_key_t running_threads_key;

#if 0 /* these are defined in jni-gcthreads.h, don't need to be here? */
#ifdef WITH_PRECISE_GC
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
void decrement_running_thread_count();
void increment_running_thread_count();
void find_other_thread_local_refs(struct FNI_Thread_State *curr_thrstate);
#endif // WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
#endif // WITH_PRECISE_GC
#endif /* 0 */

#endif /* INCLUDED_FNI_THREADLIST_H */
