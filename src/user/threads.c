#include "config.h"
#include <assert.h>
#ifdef WITH_USER_THREADS
#include "threads.h"
#include <errno.h>
#include "memstats.h"

#ifdef WITH_REALTIME_THREADS
#include <jni.h>
#include <jni-private.h>
#include "../realtime/threads.h"
#include "../realtime/qcheck.h"
#include <setjmp.h>
/* jump point for the final jump back to main - defined in startup.c */
extern jmp_buf main_return_jump;
#endif

struct thread_list *gtl,*ioptr;
#ifdef WITH_REALTIME_THREADS
struct thread_queue_struct *currentThread; //pointer to the current thread
#endif
void *oldstack;

void startnext();
int * getFDsintSEL(int);
void wakeacondthread(user_cond_t *x);
void wakeallcondthread(user_cond_t *x);

void restorethread() {
  /* 
   * Restore the threads state.
   */
  machdep_restore_float_state();
  machdep_restore_state();
}

/* when using RealtimeThreads, this function will be called with the */
/* thread to switch to */
#ifndef WITH_REALTIME_THREADS
void transfer()
#else
void transfer(struct thread_queue_struct * mthread)
#endif
{
  struct thread_list *tl;

#ifndef WITH_REALTIME_THREADS
  machdep_save_float_state(&(gtl->mthread));
  if (machdep_save_state()) {
    return;
  }
#else
  /* if the transfer thread is the current thread, just return */
  if(mthread == currentThread)
    return;

  /* if there is currently a thread running, save it's state */
  if(currentThread != NULL) {
    machdep_save_float_state(currentThread->mthread);
    if (machdep_save_state()) {
      return;
    }
  }
#endif

#ifndef WITH_REALTIME_THREADS
  tl=gtl;
  tl->prev->next=tl->next;
  tl->next->prev=tl->prev;
  if (tl!=tl->next) {
    gtl=gtl->next;
  } else {
    gtl=NULL;
  }
  
  if (ioptr!=NULL) {
    tl->next=ioptr;
    tl->prev=ioptr->prev;
    tl->next->prev=tl;
    tl->prev->next=tl;
  } else {
    ioptr=tl;
    tl->next=tl;
    tl->prev=tl;
  }
  startnext();
#else
  currentThread = mthread; //set the transfer thread to be the current thread
  restorethread(); //switch to it#
#endif
}

void context_switch() {
  machdep_save_float_state(&(gtl->mthread));
  if (_setjmp(gtl->mthread.machdep_state)) {
    return;
  }
  if (ioptr!=NULL)
    {
      doFDs();
    }
  gtl=gtl->next;

  startnext();
}

void startnext() {
#ifndef WITH_REALTIME_THREADS
  /* Moved threads...*/
  /*  if (gtl==NULL) */
  static int count=0;

  if ((gtl==NULL)&&(ioptr==NULL))
    exit(0);
  count++;
  while(1) {
#ifdef WITH_EVENT_DRIVEN
    if ((gtl==NULL)||(ioptr!=NULL&count==10)) {
      count=0;
      doFDs();
    }
#endif
    if (gtl!=NULL)
      restorethread();
  }
#endif
}

#ifdef WITH_EVENT_DRIVEN

void SchedulerAddRead(int fd) {
  /* Move current thread to IO queue */
  /* Lock on GTL */
  Java_java_io_NativeIO_registerRead(NULL,NULL, fd);
  gtl->syncrdwr=1;
  gtl->fd=fd;
#ifndef WITH_REALTIME_THREADS  
  transfer();
#else
  transfer(currentThread); //just transfer to self
#endif
}

void SchedulerAddWrite(int fd) {
  /* Move current thread to IO queue */
  /* Lock on GTL */
  Java_java_io_NativeIO_registerWrite(NULL,NULL, fd);
  gtl->syncrdwr=2;
  gtl->fd=fd;
#ifndef WITH_REALTIME_THREADS  
  transfer();
#else
  transfer(currentThread); //just transfer to self
#endif
}

void doFDs() {
  int * fd=(gtl==NULL)?getFDsintSEL(1):getFDsintSEL(0);/*1 for no timeout*/
  struct thread_list *tl=ioptr,*tmp;
  int start=0;
  while((tl!=ioptr)||(start==0)) {
    int filestat=(tl->syncrdwr>0)?fd[tl->fd]:0;
    start=1;
    if (((filestat&1)&&(tl->syncrdwr==1))||
      ((filestat&2)&&(tl->syncrdwr==2))) {
      /*Make it active!*/
      /*Remove from io queue*/
      tmp=tl->next;
      tl->next->prev=tl->prev;
      tl->prev->next=tmp;

      /* Add to execute queue */
      if (gtl==NULL) {
	tl->next=tl;
	tl->prev=tl;
	gtl=tl;
      } else {
	tl->next=gtl;
	tl->prev=gtl->prev;
	tl->next->prev=tl;
	tl->prev->next=tl;
      }
     
      if (ioptr==tl) {
	if (ioptr==tmp) {
	  /*Only element removed*/
	  ioptr=NULL;
	  break;
	} else {
	  /*First element removed*/
	  ioptr=tmp;
	  start=0;
	}
      }
      tl=tmp;
    } else
      tl=tl->next;
  }
  free(fd);
}
#endif

void exitthread() {
#ifndef WITH_REALTIME_THREADS
  struct thread_list *tl;
  /*LOCK ON GTL*/
  if (gtl!=gtl->next) {
    FNI_DestroyThreadState(gtl->jnienv);
    tl=gtl;
    gtl = gtl->next;
    gtl->prev=tl->prev;
    gtl->prev->next=gtl;

    /*perhaps free structures if we didn't have GC*/

    /*NEED LOCK AROUND THIS*/
    if (oldstack!=NULL) {
      __machdep_stack_free(oldstack);
      oldstack=NULL;
    }
    oldstack=tl->mthread.machdep_stack;

    free(tl);
    DECREMENT_MEM_STATS(sizeof (struct thread_list));

    restorethread();
    return;
  } else {
    FNI_DestroyThreadState(gtl->jnienv);
    if (oldstack!=NULL) {
      __machdep_stack_free(oldstack);
      oldstack=NULL;
    }
    oldstack=gtl->mthread.machdep_stack;

    free(gtl);

    DECREMENT_MEM_STATS(sizeof (struct thread_list));
    gtl=NULL;
    startnext();
  }

#else // WITH_REALTIME_THREADS
  /* if no new thread has been set (by the end of thread_startup_routine) */
  //  puts("----------EXITING-------------");
  //  printf("CurrentThread is %p\n", currentThread);
  if(currentThread == NULL)
    longjmp(main_return_jump, 1); //jump back to main to clean up and end
  else
    restorethread(); //otherwise, restore the new thread
#endif

}

void inituser(int *bottom) {
  void * stack;
  struct thread_list * tl = 
    (struct thread_list *)malloc(sizeof(struct thread_list));
  
#ifndef WITH_REALTIME_THREADS
  INCREMENT_MEM_STATS(sizeof(struct thread_list));
  /*build stack and stash it*/
  __machdep_pthread_create(&(tl->mthread), NULL, NULL,STACKSIZE, 0,0);
  /*stash hiptr*/
  tl->mthread.hiptr=bottom;
#else
  /*build stack and stash it*/
  currentThread = 
    (struct thread_queue_struct *)malloc(sizeof(struct thread_queue_struct));
  INCREMENT_MEM_STATS(sizeof(struct thread_queue_struct));
  currentThread->mthread =
    (struct machdep_pthread*)malloc(sizeof(struct machdep_pthread));
  __machdep_pthread_create(currentThread->mthread, NULL, NULL,STACKSIZE, 0,0);
  /*stash hiptr*/
  currentThread->mthread->hiptr=bottom;
#endif
  gtl=tl;
  tl->next=tl;
  tl->prev=tl;
  ioptr=NULL; /* Initialize ioptr */
  oldstack=NULL;

#ifdef WITH_EVENT_DRIVEN
  Java_java_io_NativeIO_initScheduler(NULL,NULL,/*MOD_SELECT*/0);
#endif
}

int user_mutex_init(user_mutex_t *x, void * v) {
  x->mutex=SEMAPHORE_CLEAR;
#ifndef WITH_REALTIME_THREADS
  x->tl=NULL;
#else
  //RealtimeThreads makes this a thread, not a list
  x->queue = x->endQueue = NULL;
#endif
  return 0;
}

void addwaitthread(user_mutex_t *x) {
#ifndef WITH_REALTIME_THREADS  
  gtl->syncrdwr=0;
  gtl->lnext=x->tl;
  x->tl=gtl;
  transfer();
#else
  //  CheckQuanta(0, 0, 1);
  if(currentThread->threadID != 0) {
    print_queue(thread_queue, "BEG addwaitthread queue");
    print_queue(x->queue, "mutex");

    if(currentThread->queue_state == IN_ACTIVE_QUEUE) {
      if(currentThread->prev != NULL)
	currentThread->prev->next = currentThread->next;
      else
	thread_queue = currentThread->next;
      if(currentThread->next != NULL)
	currentThread->next->prev = currentThread->prev;
      else
	end_thread_queue = currentThread->prev;

      enqueue(&x->queue, &x->endQueue, currentThread);
    }

    DisableThread(currentThread);

    print_queue(x->queue, "mutex");
    print_queue(thread_queue, "END addwaitthread queue");
    CheckQuanta(1, 0, 1);
  }
#endif
}

void wakewaitthread(user_mutex_t *x) {
#ifndef WITH_REALTIME_THREADS
  struct thread_list *tl;

  tl=x->tl;
  if (tl!=NULL) {
    x->tl=tl->lnext;
    /*Remove from previous list*/
    tl->prev->next=tl->next;
    tl->next->prev=tl->prev;
    if (ioptr==tl) {
      if (tl->next==tl)
	ioptr=NULL;
      else
	ioptr=tl->next;
    }
    tl->next=gtl->next;
    tl->prev=gtl;
    tl->prev->next=tl;
    tl->next->prev=tl;
  }
#else
  if(x->queue != NULL) {
    print_queue(thread_queue, "BEG wakewaitthread queue");
    print_queue(x->queue, "mutex");
    EnableThread(x->queue);

    if(thread_queue == NULL) {
      thread_queue = end_thread_queue = x->queue;
      thread_queue->prev = NULL;
    }
    else {
      end_thread_queue->next = x->queue;
      x->queue->prev = end_thread_queue;
      end_thread_queue = x->queue;
    }
    x->queue = x->queue->next;
    end_thread_queue->next = NULL;

    print_queue(x->queue, "mutex");
    print_queue(thread_queue, "END wakewaitthread queue");
  }
#endif  
}

int user_mutex_lock(user_mutex_t *x) {
  while(SEMAPHORE_TEST_AND_SET(&(x->mutex))==SEMAPHORE_SET) {
    addwaitthread(x);
  }
  return 0;
}

int user_mutex_trylock(user_mutex_t *x) {
  semaphore test= SEMAPHORE_TEST_AND_SET(&(x->mutex));
  if (test==SEMAPHORE_CLEAR)
    return 0;
  else {
#ifndef WITH_REALTIME_THREADS
    context_switch();
#else
    CheckQuanta(0, 0, 1);
#endif
    return EBUSY;
  }
}

int user_mutex_unlock(user_mutex_t *x) {
  SEMAPHORE_RESET(&(x->mutex));
  wakewaitthread(x);
  return 0;
}

int user_mutex_destroy(user_mutex_t *x) {
  return 0;
}

int user_cond_init(user_cond_t *x, void * v) {
  x->counter=0;
  return 0;
}

int user_cond_broadcast(user_cond_t *x) {
  wakeallcondthread(x);
  return 0;
}

int user_cond_signal(user_cond_t *x) {
  wakeacondthread(x);
  return 0;
}

void addcontthread(user_cond_t *x) {
#ifndef WITH_REALTIME_THREADS  
  gtl->syncrdwr=0;
  gtl->lnext=x->tl;
  x->tl=gtl;
  transfer();
#else
  if(currentThread->threadID != 0) {
    print_queue(thread_queue, "BEG addcontthread queue");
    print_queue(x->queue, "cond");

    if(currentThread->queue_state == IN_ACTIVE_QUEUE) {
      if(currentThread->prev != NULL)
	currentThread->prev->next = currentThread->next;
      else
	thread_queue = currentThread->next;
      if(currentThread->next != NULL)
	currentThread->next->prev = currentThread->prev;
      else
	end_thread_queue = currentThread->prev;
      enqueue(&x->queue, &x->endQueue, currentThread);
    }

    DisableThread(currentThread);
    print_queue(x->queue, "cond");
    print_queue(thread_queue, "END addcontthread queue");
    CheckQuanta(1, 0, 1);
  }
#endif
}

void wakeacondthread(user_cond_t *x) {
  struct thread_list *tl;

#ifndef WITH_REALTIME_THREADS
  tl=x->tl;
  if (tl!=NULL) {
    x->tl=tl->lnext;
    /*Remove from previous list*/
    tl->prev->next=tl->next;
    tl->next->prev=tl->prev;
    if (ioptr==tl) {
      if (tl->next==tl)
	ioptr=NULL;
      else
	ioptr=tl->next;
    }

    tl->next=gtl->next;
    tl->prev=gtl;
    tl->prev->next=tl;
    tl->next->prev=tl;
  }
#else
  if(x->queue != NULL) {
    print_queue(thread_queue, "BEG wakeacondthread queue");
    print_queue(x->queue, "cond");

    EnableThread(x->queue);
    if(thread_queue == NULL) {
      thread_queue = end_thread_queue = x->queue;
      thread_queue->prev = NULL;
    }
    else {
      end_thread_queue->next = x->queue;
      x->queue->prev = end_thread_queue;
      end_thread_queue = x->queue;
    }
    x->queue = x->queue->next;
    end_thread_queue->next = NULL;
    print_queue(x->queue, "cond");
    print_queue(thread_queue, "END wakeacondthread queue");
  }
#endif
}

void wakeallcondthread(user_cond_t *x) {
  struct thread_list *tl;
#ifndef WITH_REALTIME_THREADS
  tl=x->tl;
  while (tl!=NULL) {
    x->tl=tl->lnext;
    /*Remove from previous list*/
    tl->prev->next=tl->next;
    tl->next->prev=tl->prev;
    if (ioptr==tl) {
      if (tl->next==tl)
	ioptr=NULL;
      else
	ioptr=tl->next;
    }

    tl->next=gtl->next;
    tl->prev=gtl;
    tl->prev->next=tl;
    tl->next->prev=tl;
    tl=tl->lnext;
  }
#else
  if(x->queue != NULL) {
    print_queue(thread_queue, "BEG wakeallcondthread queue");
    print_queue(x->queue, "cond");

    EnableThreadList(x->queue);
    if(thread_queue == NULL) {
      x->queue->prev = NULL;
      thread_queue = end_thread_queue = x->queue;
    }
    else
    {
      end_thread_queue->next = x->queue;
      x->queue->prev = end_thread_queue;
    }

    while(end_thread_queue->next != NULL) {
      end_thread_queue = end_thread_queue->next;
    }
    print_queue(x->queue, "cond");
    print_queue(thread_queue, "END wakeallcondthread queue");
  }
#endif
}

int user_cond_wait(user_cond_t *cond, user_mutex_t *mutex) {
  /*Only one thread so this will work...*/
  user_mutex_unlock(mutex);
  /*Previous 2 lines need to be atomic!!!!*/
  addcontthread(cond);
  /*grab the lock again*/
  user_mutex_lock(mutex);
}

int user_cond_timedwait(user_cond_t *cond, user_mutex_t *mutex, const struct timespec *abstime) {
  user_cond_wait(cond,mutex);
}

int user_cond_destroy(user_cond_t *cond) {
  return 0;
}
#endif
