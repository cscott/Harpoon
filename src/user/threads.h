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
  struct JNIEnv * jnienv;
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
#endif /*U_THREADS*/






