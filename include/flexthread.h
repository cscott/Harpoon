/* FLEX thread definitions */
#ifndef INCLUDED_FLEXTHREAD_H
#define INCLUDED_FLEXTHREAD_H
#include "config.h"

#ifndef HAVE_PTHREAD_BARRIER_T
# define NEED_PTHREAD_BARRIER_T
#endif /* HAVE_PTHREAD_BARRIER_T */

#ifdef WITH_HEAVY_THREADS
#include <pthread.h>
#include <sched.h>	/* for sched_yield */
#define pthread_yield_np	sched_yield

#ifndef HAVE_PTHREAD_RWLOCK_T
# define NEED_PTHREAD_RWLOCK_T
#endif /* HAVE_PTHREAD_RWLOCK_T */

/* Make sure the BDW collector has a chance to redefine 
 * pthread_create/sigmask/join for its own nefarious purposes. */
#ifndef FLEXTHREAD_TYPEDEFS_ONLY/*sometimes we don't want to pull all this in*/
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#endif /* FLEXTHREAD_TYPEDEFS_ONLY */

#endif /* WITH_HEAVY_THREADS */

#ifdef WITH_PTH_THREADS
#define PTH_SYSCALL_SOFT 1
#include <pth.h>
#include <errno.h>

#ifdef BDW_CONSERVATIVE_GC
# error GC wants to redefine pthread_create/pthread_sigmask/pthread_join
#endif

#define PTHERR(x) ((x)?0:errno)

#define pthread_t			pth_t
#define pthread_self			pth_self
#define pthread_equal(x,y)		((x) == (y))
#define pthread_yield_np()		({ pth_yield(NULL); 0; })

#define pthread_create(thread,attr,start,arg)				\
    ({ pth_attr_t *_na = (attr);					\
       *(thread)=pth_spawn(_na?*_na:PTH_ATTR_DEFAULT,start, arg);	\
       *(thread) ? 0 : EAGAIN; })
#define pthread_attr_t			pth_attr_t
#define pthread_attr_init(attr)	\
    ({ *(attr)=pth_attr_new(); *(attr) ? 0 : errno; })
#define pthread_attr_destroy(attr) \
    ({ pth_attr_destroy(*(attr)); *(attr)=NULL; 0; })
#define PTHREAD_CREATE_DETACHED 0
#define PTHREAD_CREATE_JOINABLE 1
#define pthead_attr_setdetachstate(attr, state) \
    PTHERR(pth_attr_set(*(attr), PTH_ATTR_JOINABLE, state))

#define PTHREAD_MUTEX_INITIALIZER	PTH_MUTEX_INIT
#define pthread_mutex_t			pth_mutex_t
#define pthread_mutex_init(m, a)	PTHERR(pth_mutex_init((m)))
#define pthread_mutex_lock(m)	    PTHERR(pth_mutex_acquire((m), FALSE, NULL))
#define pthread_mutex_trylock(m)    PTHERR(pth_mutex_acquire((m), TRUE, NULL))
#define pthread_mutex_unlock(m)		PTHERR(pth_mutex_release((m)))
#define pthread_mutex_destroy(m)	(/* do nothing */0)

#define PTHREAD_RWLOCK_INITIALIZER	PTH_RWLOCK_INIT
#define pthread_rwlock_t		pth_rwlock_t
#define pthread_rwlock_init(l, a)	PTHERR(pth_rwlock_init((l)))
#define pthread_rwlock_rdlock(l)	PTHERR(pth_rwlock_acquire((l), \
					       PTH_RWLOCK_RD, FALSE, NULL))
#define pthread_rwlock_tryrdlock(l)	PTHERR(pth_rwlock_acquire((l), \
					       PTH_RWLOCK_RD, TRUE, NULL))
#define pthread_rwlock_wrlock(l)	PTHERR(pth_rwlock_acquire((l), \
                                               PTH_RWLOCK_RW, FALSE, NULL))
#define pthread_rwlock_trywrlock(l)	PTHERR(pth_rwlock_acquire((l), \
					       PTH_RWLOCK_RW, TRUE, NULL))
#define pthread_rwlock_unlock(l)	PTHERR(pth_rwlock_release((l)))
#define pthread_rwlock_destroy(l)	(/* do nothing */0)

#define PTHREAD_COND_INITIALIZER	PTH_COND_INIT
#define pthread_cond_t			pth_cond_t
#define pthread_cond_init(c, a)		PTHERR(pth_cond_init((c)))
#define pthread_cond_broadcast(c)	PTHERR(pth_cond_notify((c), TRUE))
#define pthread_cond_signal(c)		PTHERR(pth_cond_notify((c), FALSE))
#define pthread_cond_wait(c, m)		PTHERR(pth_cond_await((c), (m), NULL))
#define pthread_cond_destroy(c)		(/* do nothing */0)
#define pthread_cond_timedwait(c, m, abstime)			\
({ const struct timespec *_abstime = (abstime);			\
   pth_event_t _ev = pth_event(PTH_EVENT_TIME|PTH_MODE_STATIC,	\
			       &flex_timedwait_key,		\
			       pth_time(_abstime->tv_sec,	\
					_abstime->tv_nsec/1000));\
   (!pth_cond_await((c), (m), _ev)) ? errno :			\
   pth_event_occurred(_ev) ? ETIMEDOUT : 0; })
extern pth_key_t flex_timedwait_key; /* defined in java_lang_Thread.c */

/* thread-specific key creation */
#define pthread_key_t			pth_key_t
#define pthread_key_create(k,d)		PTHERR(pth_key_create((k),(d)))
#define pthread_key_delete(k)		PTHERR(pth_key_delete((k)))
#define pthread_setspecific(k,v)	PTHERR(pth_key_setdata((k),(v)))
#define pthread_getspecific(k)		pth_key_getdata((k))

#endif /* WITH_PTH_THREADS */

#if WITH_USER_THREADS
#define USER_THREADS_COMPATIBILITY
#include "../src/user/threads.h"
#define NEED_PTHREAD_RWLOCK_T
#endif /* WITH_USER_THREADS */

/* some pthreads-compatible libraries don't have an implementation of
 * pthread_rwlock_t. */
#ifdef NEED_PTHREAD_RWLOCK_T
/* work-around for missing read/write lock. */
/* a mutex is a conservative approximation to a read/write lock. */
#define pthread_rwlock_t	pthread_mutex_t
#define pthread_rwlock_init	pthread_mutex_init
#define pthread_rwlock_rdlock	pthread_mutex_lock
#define pthread_rwlock_wrlock	pthread_mutex_lock
#define pthread_rwlock_unlock	pthread_mutex_unlock
#define pthread_rwlock_destroy	pthread_mutex_destroy
#endif /* !NEED_PTHREAD_RWLOCK_T */

/* some pthreads-compatible libraries don't have an implementation of
 * pthread_barrier_t. */
#ifdef NEED_PTHREAD_BARRIER_T
#include <errno.h> /* for EBUSY, EINVAL */
/* uses mutexes and condition variables */
#define pthread_barrierattr_t int
#define pthread_barrier_t struct { pthread_mutex_t _m; \
                                   pthread_cond_t _cv; \
                                   unsigned int _n; \
                                   unsigned int _countw; \
                                   unsigned int _countu; }
#define pthread_barrier_destroy(pb) \
    ({ (pthread_mutex_trylock(&(pb)->_m) || (pb)->_countw) ? EBUSY : \
       pthread_cond_destroy(&(pb)->_cv) ? EINVAL : \
       (pthread_mutex_unlock(&(pb)->_m), \
       pthread_mutex_destroy(&(pb)->_m)) ? EINVAL : 0; })
#define pthread_barrier_init(pb,pattr,n) \
    ({ (n <= 0) ? EINVAL: \
       (pthread_mutex_init(&(pb)->_m,NULL), \
        pthread_cond_init(&(pb)->_cv,NULL), \
        (pb)->_n = n, (pb)->_countw = (pb)->_countu = 0); })
#define pthread_barrier_wait(pb) \
    ({ pthread_mutex_lock(&(pb)->_m); \
       assert((pb)->_countu == 0); \
       (pb)->_countw++; \
       if ((pb)->_countw == (pb)->_n) { /* _n threads have called wait */ \
         (pb)->_countu++; \
         if ((pb)->_countu == (pb)->_n) { /* _n = 1 */ \
           (pb)->_countw = (pb)->_countu = 0; \
         } else { pthread_cond_broadcast(&(pb)->_cv); } \
           pthread_mutex_unlock(&(pb)->_m); \
       } else { \
         do { pthread_cond_wait(&(pb)->_cv, &(pb)->_m); } \
           while ((pb)->_countw != (pb)->_n); \
         (pb)->_countu++; \
         if ((pb)->_countu == (pb)->_n) /* last thread to unblock */ \
            (pb)->_countw = (pb)->_countu = 0; \
         pthread_mutex_unlock(&(pb)->_m); \
      } })
#define pthread_barrierattr_destroy(pattr) (0 /* do nothing */)
#define pthread_barrierattr_getpshared(pattr,psh) assert(0 /* unimplemented */)
#define pthread_barrierattr_init(pattr) (0 /* do nothing */)
#define pthread_barrierattr_setpshared(pattr,psh) assert(0 /* unimplemented */)
#endif /* !NEED_PTHREAD_BARRIER_T */

/* define flex_mutex_t ops. */
#if WITH_HEAVY_THREADS || WITH_USER_THREADS || WITH_PTH_THREADS

#define FLEX_MUTEX_INITIALIZER	PTHREAD_MUTEX_INITIALIZER
#define flex_mutex_t		pthread_mutex_t
#define flex_mutex_init(x)	pthread_mutex_init((x), NULL)
#define flex_mutex_lock		pthread_mutex_lock
#define flex_mutex_unlock	pthread_mutex_unlock
#define flex_mutex_destroy	pthread_mutex_destroy

#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS */

/* simply declarations to avoid lots of tedious #ifdef WITH_THREAD'ing. */
/* declare nop-variants of mutex ops if WITH_THREADS not defined */
#ifdef WITH_THREADS
#define FLEX_MUTEX_DECLARE_STATIC(name) \
	static flex_mutex_t name = FLEX_MUTEX_INITIALIZER
#define FLEX_MUTEX_LOCK flex_mutex_lock
#define FLEX_MUTEX_UNLOCK flex_mutex_unlock
#else /* if WITH_THREADS not defined, then mutex lock/unlock does nothing. */
#define FLEX_MUTEX_DECLARE_STATIC(name)
#define FLEX_MUTEX_LOCK(x) ((void)0)
#define FLEX_MUTEX_UNLOCK(x) ((void)0)
#endif /* WITH_THREADS */

#endif /* INCLUDED_FLEXTHREAD_H */


