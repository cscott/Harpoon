#ifndef U_THREADS
#define U_THREADS

#ifdef __arm
#include "engine-arm32-linux-1.0.h"
#else
#include "engine-i386-linux-1.0.h"
#endif

struct thread_list {
  struct thread_list *prev;
  struct machdep_pthread mthread;
  const struct JNINativeInterface **jnienv; /* see jni-types.h for typedef */
  struct thread_list *next;
  struct thread_list *lnext;
  int syncrdwr;
  int fd;
};

extern struct thread_list *gtl,*ioptr;
extern void *oldstack;
#define USER_MUTEX_INITIALIZER {SEMAPHORE_CLEAR,NULL}
#define STACKSIZE 0x8000

typedef struct user_mutex {
  semaphore mutex;
  struct thread_list* tl;
}  user_mutex_t;

void inituser();

int user_mutex_init(user_mutex_t *x, void *);
int user_mutex_lock(user_mutex_t *x);
int user_mutex_trylock(user_mutex_t *x);
int user_mutex_unlock(user_mutex_t *x);
int user_mutex_destroy(user_mutex_t *x);

typedef struct user_cond {
  long counter;
  struct thread_list *tl;
} user_cond_t;

#define USER_COND_INIT {0,NULL}
int user_cond_init(user_cond_t *x, void *);
int user_cond_broadcast(user_cond_t *x);
int user_cond_signal(user_cond_t *x);
int user_cond_wait(user_cond_t *x, user_mutex_t *y);
int user_cond_timedwait(user_cond_t *cond,
			   user_mutex_t *mutex, 
			   const struct timespec *abstime);

void SchedulerAddRead(int fd);
void SchedulerAddWrite(int fd);
void doFDs();

/* ------- pthreads compatibility ------------- */
#ifdef USER_THREADS_COMPATIBILITY

#define pthread_self()		gtl
#define pthread_t               struct thread_list *

#define PTHREAD_MUTEX_INITIALIZER  USER_MUTEX_INITIALIZER
#define pthread_mutex_t		user_mutex_t
#define pthread_mutex_init(x,a)	user_mutex_init((x), NULL)
#define pthread_mutex_lock	user_mutex_lock
#define pthread_mutex_unlock	user_mutex_unlock
#define pthread_mutex_destroy	user_mutex_destroy
#define pthread_mutex_trylock   user_mutex_trylock

#define PTHREAD_COND_INITIALIZER	USER_COND_INIT
#define pthread_cond_t			user_cond_t
#define pthread_cond_init		user_cond_init
#define pthread_cond_broadcast	        user_cond_broadcast
#define pthread_cond_signal		user_cond_signal
#define pthread_cond_wait		user_cond_wait
#define pthread_cond_destroy		user_cond_destroy
#define pthread_cond_timedwait          user_cond_timedwait

#endif /* USER_THREADS_COMPATIBILITY */

#endif /*U_THREADS*/
