#ifndef INCLUDED_JNI_GCTHREADS_H
#define INCLUDED_JNI_GCTHREADS_H

/* mutex for garbage collection vs thread addition/deletion */
extern pthread_mutex_t gc_thread_mutex;

/* mutex for modifying list of threads */
extern pthread_mutex_t running_threads_mutex;

/* number of threads running */
extern int num_running_threads;

/* effects: adds thread-local references to root set using add_to_root_set */
void find_other_thread_local_refs(struct FNI_Thread_State *curr_thrstate);

/* effects: decrements the number of threads that the GC waits for */
void decrement_running_thread_count();

/* effects: increments the number of threads that the GC waits for */
void increment_running_thread_count();

#endif /* INCLUDED_JNI_GCTHREADS_H */
