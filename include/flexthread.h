/* FLEX thread definitions */
#ifndef INCLUDED_FLEXTHREAD_H
#define INCLUDED_FLEXTHREAD_H
#include "config.h"

#ifdef WITH_HEAVY_THREADS
#include <pthread.h>

#ifndef HAVE_PTHREAD_RWLOCK_T
/* work-around for missing read/write lock. */
/* a mutex is a conservative approximation to a read/write lock. */
#define pthread_rwlock_t	pthread_mutex_t
#define pthread_rwlock_init	pthread_mutex_init
#define pthread_rwlock_rdlock	pthread_mutex_lock
#define pthread_rwlock_wrlock	pthread_mutex_lock
#define pthread_rwlock_unlock	pthread_mutex_unlock
#define pthread_rwlock_destroy	pthread_mutex_destroy
#endif /* !HAVE_PTHREAD_RWLOCK_T */

#endif /* WITH_HEAVY_THREADS */

#ifdef WITH_PTH_THREADS
#define PTH_SYSCALL_SOFT 1
#include <pth.h>

#define pthread_t			pth_t

#define PTHREAD_MUTEX_INITIALIZER	PTH_MUTEX_INIT
#define pthread_mutex_t			pth_mutex_t
#define pthread_mutex_init(m, a)	pth_mutex_init((m))
#define pthread_mutex_lock(m)		pth_mutex_acquire((m), FALSE, NULL)
#define pthread_mutex_trylock(m)	pth_mutex_acquire((m), TRUE, NULL)
#define pthread_mutex_unlock(m)		pth_mutex_release((m))
#define pthread_mutex_destroy(m)	/* do nothing */

#define PTHREAD_RWLOCK_INITIALIZER	PTH_RWLOCK_INIT
#define pthread_rwlock_t		pth_rwlock_t
#define pthread_rwlock_init(l, a)	pth_rwlock_init((l))
#define pthread_rwlock_rdlock(l)	pth_rwlock_acquire((l), PTH_RWLOCK_RD,\
							   FALSE, NULL)
#define pthread_rwlock_tryrdlock(l)	pth_rwlock_acquire((l), PTH_RWLOCK_RD,\
							   TRUE, NULL)
#define pthread_rwlock_wrlock(l)	pth_rwlock_acquire((l), PTH_RWLOCK_RW,\
							   FALSE, NULL)
#define pthread_rwlock_trywrlock(l)	pth_rwlock_acquire((l), PTH_RWLOCK_RW,\
							   TRUE, NULL)
#define pthread_rwlock_unlock(l)	pth_rwlock_release((l))
#define pthread_rwlock_destroy(l)	/* do nothing */
#endif /* WITH_PTH_THREADS */


/* define flex_mutex_t ops. */
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS

#define FLEX_MUTEX_INITIALIZER	PTHREAD_MUTEX_INITIALIZER
#define flex_mutex_t		pthread_mutex_t
#define flex_mutex_init(x)	pthread_mutex_init((x), NULL)
#define flex_mutex_lock		pthread_mutex_lock
#define flex_mutex_unlock	pthread_mutex_unlock
#define flex_mutex_destroy	pthread_mutex_destroy

#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS */

#endif /* INCLUDED_FLEXTHREAD_H */
