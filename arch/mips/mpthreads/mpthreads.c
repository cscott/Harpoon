#include <assert.h>
#include <errno.h>
#include <omp.h>
#include <pthread.h>

#define REDIRECT_MAIN
#define NTHREADS 4
#define NKEYS 4

struct thread_list {
  int iam; /* which thread am i ? */

  omp_lock_t entry_lock; /* protects the contents of this struct */
  void * (*start)(void *); /* what work to do */
  void * arg; /* argument to the 'start' function */
  
  omp_lock_t exit_lock; /* ensures that the work is complete */
  void *result; /* result of the work */

  omp_lock_t xyzzy; /* magic */

  void *specific[NKEYS];

  struct thread_list *next;
} thread_list[NTHREADS], *ready = &thread_list[1];

typedef void (*destructor_t)(void*);
destructor_t key_destructors[NKEYS];
int nkeys=0;

pthread_t pthread_self(void) { return (pthread_t) omp_get_thread_num(); }

int pthread_create(pthread_t *ptid, const pthread_attr_t *attr,
		   void *(*start)(void *), void *arg) {
  struct thread_list *t;
  assert (omp_get_thread_num()==0); /* only allow one master to call this */
  assert (ready!=NULL); /* ran out of threads */
  assert (ptid!=NULL && start!=NULL);
  
  /* find available thread */
  t = ready; ready = t->next; // should be atomic.

  t->start = start;
  t->arg = arg;
  /* take the magic lock */
  omp_set_lock(&(t->xyzzy));
  /* release this thread & start it running. */
  omp_unset_lock(&(t->entry_lock));
  /* wait until the thread is running. */
  omp_set_lock(&(t->exit_lock));

  *ptid = (pthread_t) t->iam;
  /* always succeeds */
  return 0;
}
int pthread_join(pthread_t ptid, void **rp) {
  int i;
  struct thread_list *t = &thread_list[(int)ptid];
  assert (omp_get_thread_num()==0); /* only allow one master to call this */
  /* wait for thread to complete. */
  omp_set_lock(&(t->entry_lock));
  omp_unset_lock(&(t->xyzzy));
  omp_unset_lock(&(t->exit_lock));
  *rp = t->result;
  /* clean up thread-specific data */
  for (i=0; i<nkeys; i++) {
    void *data = t->specific[i];
    if (data!=NULL) {
      t->specific[i] = NULL;
      if (key_destructors[i]!=NULL)
	key_destructors[i](data);
    }
  }
  /* move back to ready list */
  t->next = ready; ready = t; // should be atomic.
  /* always succeeds */
  return 0;
}

static void task(int iam) {
  struct thread_list *t = &thread_list[iam];
  while (1) {
    /* wait for work to do. */
    omp_set_lock(&(t->entry_lock));
    omp_unset_lock(&(t->exit_lock));
    /* maybe we need to exit? */
    if (t->start==NULL) { omp_unset_lock(&(t->entry_lock)); return; }
    /* otherwise, do some work. */
    t->result = t->start(t->arg);
    /* note that we're done with the work */
    omp_unset_lock(&(t->entry_lock));
    omp_set_lock(&(t->xyzzy));
    omp_set_lock(&(t->exit_lock));
    omp_unset_lock(&(t->xyzzy));
  }
}

int pthread_init(int (*continuation)(int,char**), int argc, char **argv) {
  int i, result;
  omp_set_dynamic(0);
  omp_set_num_threads(NTHREADS);

  for (i=0; i<NTHREADS; i++) {
    thread_list[i].iam = i;
    omp_init_lock(&(thread_list[i].entry_lock));
    thread_list[i].start = NULL;
    omp_init_lock(&(thread_list[i].exit_lock));
    thread_list[i].result = NULL;
    omp_init_lock(&(thread_list[i].xyzzy));
    thread_list[i].next = (i==NTHREADS-1) ? NULL : &thread_list[i+1];

    omp_set_lock(&(thread_list[i].entry_lock));
    omp_set_lock(&(thread_list[i].exit_lock));//actually owned by thread i
  }

#pragma omp parallel shared(result) //num_threads(NTHREADS)
  {
    int iam = omp_get_thread_num();
    if (iam!=0) {
      task(iam);
    } else {
      struct thread_list *tl; int r;
      r = continuation(argc, argv);
      result = r;
      /* now finish off the threads */
      for (tl = ready; tl!=NULL; tl=tl->next) {
	tl->start = NULL;
	omp_unset_lock(&(tl->entry_lock));
      }
      /* done! */
    }
  }
  return result;
}
#ifdef REDIRECT_MAIN
extern int MPT_main(int argc, char **argv);
int main(int argc, char **argv) {
  return pthread_init(MPT_main, argc, argv);
}
#endif

int pthread_key_create(pthread_key_t *key, destructor_t destructor) {
  int i;
  assert (nkeys < NKEYS);
  key_destructors[nkeys] = destructor;
  *key = (pthread_key_t) nkeys;
  for (i=0; i<NTHREADS; i++)
    thread_list[i].specific[nkeys]=NULL; /* init to NULL */
#pragma omp atomic
  nkeys++;
  return 0;
}
int pthread_setspecific(pthread_key_t key, const void *value) {
  int iam = omp_get_thread_num();
  thread_list[iam].specific[key] = (void *) value;
  return 0;
}
void *pthread_getspecific(pthread_key_t key) {
    int iam = omp_get_thread_num();
    return thread_list[iam].specific[key];
}
/* ----------------- MUTEX support -------------- */
/* omp_lock_t is smaller than pthread_mutex_t, so we're just going
 * to pretend that our pthread_mutex_t pointer is really a pointer
 * to omp_lock_t.  Fun! */
int pthread_mutex_init(pthread_mutex_t *mutex,
		       const pthread_mutexattr_t *attr) {
  omp_lock_t *lock = (omp_lock_t *)mutex;
  omp_init_lock(lock);
  // mark as init'ed
  *( (int *)(&lock[1]) ) = 1238;
  return 0;
}
int pthread_mutex_lock(pthread_mutex_t *mutex) {
  omp_lock_t *lock = (omp_lock_t *)mutex;
  // make sure it's init'ed.  we bit of a race here.
#pragma omp critical
 {
   if (*( (int *)(&lock[1]) ) != 1238)
     pthread_mutex_init(mutex, NULL);
 }
  omp_set_lock(lock);
  return 0;
}
int pthread_mutex_trylock(pthread_mutex_t *mutex) {
  omp_lock_t *lock = (omp_lock_t *)mutex;
  int st = omp_test_lock(lock);
  return st ? 0 : EBUSY;
}
int pthread_mutex_unlock(pthread_mutex_t *mutex) {
  omp_lock_t *lock = (omp_lock_t *)mutex;
  omp_unset_lock(lock);
  return 0;
}
int pthread_mutex_destroy(pthread_mutex_t *mutex) {
  omp_lock_t *lock = (omp_lock_t *)mutex;
  omp_destroy_lock(lock);
  return 0;
}
/* ------ conservative approx to r/w lock support ------ */
int pthread_rwlock_init(pthread_rwlock_t *mutex,
		       const pthread_rwlockattr_t *attr) {
  return pthread_mutex_init((pthread_mutex_t*)mutex,
			    (pthread_mutexattr_t *)attr);
}
int pthread_rwlock_rdlock(pthread_rwlock_t *mutex) {
  return pthread_mutex_lock((pthread_mutex_t*)mutex);
}
int pthread_rwlock_wrlock(pthread_rwlock_t *mutex) {
  return pthread_mutex_lock((pthread_mutex_t*)mutex);
}
int pthread_rwlock_tryrdlock(pthread_rwlock_t *mutex) {
  return pthread_mutex_trylock((pthread_mutex_t*)mutex);
}
int pthread_rwlock_trywrlock(pthread_rwlock_t *mutex) {
  return pthread_mutex_trylock((pthread_mutex_t*)mutex);
}
int pthread_rwlock_unlock(pthread_rwlock_t *mutex) {
  return pthread_mutex_unlock((pthread_mutex_t*)mutex);
}
int pthread_rwlock_destroy(pthread_rwlock_t *mutex) {
  return pthread_mutex_destroy((pthread_mutex_t*)mutex);
}

/* ---------------- Stubs ----------------------- */
int pthread_getschedparam(pthread_t thread, int *opolicy,
			  struct sched_param *oparam) {
  *opolicy = 0;
  oparam->sched_priority = 0;
  return 0;
}
int pthread_setschedparam(pthread_t thread, int policy,
			  const struct sched_param *param) {
  /* ignore */
  return 0;
}
int pthread_attr_setschedparam(pthread_attr_t *attr,
			       const struct sched_param *param) {
  return 0;
}
int pthread_attr_getschedparam(const pthread_attr_t *attr,
			       struct sched_param *oparam) {
  oparam->sched_priority = 0;
  return 0;
}

int pthread_cond_init(pthread_cond_t    *cond,
		      const pthread_condattr_t *cond_attr) {
  return 0;
}
int pthread_cond_signal(pthread_cond_t *cond) {
  return 0;
}
int pthread_cond_broadcast(pthread_cond_t *cond) {
  return 0;
}
int pthread_cond_wait(pthread_cond_t *cond, pthread_mutex_t *mutex) {
  assert(0);
  return 0;
}
int   pthread_cond_timedwait(pthread_cond_t   *cond,
			     pthread_mutex_t *mutex,
			     const struct timespec *abstime) {
  assert(0);
  return 0;
}
int pthread_cond_destroy(pthread_cond_t *cond) {
  return 0;
}
