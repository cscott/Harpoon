/* ==== machdep.c ============================================================
 * Copyright (c) 1993, 1994 Chris Provenzano, proven@athena.mit.edu
 *
 * Description : Machine dependent functions for Linux-1.0 on i386
 *
 *	1.00 93/08/04 proven
 *      -Started coding this file.
 */
#include <assert.h>
#include "config.h"
#ifdef WITH_USER_THREADS
#ifndef lint
static const char rcsid[] = "$Id: engine-i386-linux-1.0.c,v 1.9 2002-08-28 18:21:21 wbeebee Exp $";
#endif

#include "config.h"
#include <errno.h>
#include <stdlib.h>
#include <sys/uio.h>
#include <sys/types.h>
#include <sys/param.h> /* for OPEN_MAX */
#include <sys/socket.h>
#if defined __GLIBC__
//#include <linux/net.h>//CSA: caused my build to break on lesser-magoo.
#endif
#ifdef WITH_REALTIME_JAVA
#include "../realtime/RTJconfig.h"
#endif
#include "engine-i386-linux-1.0.h"
#include "threads.h"
#include "memstats.h"

/* ==========================================================================
 * machdep_save_state()
 */
int machdep_save_state(void)
{
#ifndef WITH_REALTIME_THREADS
  return(_setjmp(gtl->mthread.machdep_state));
#else
  // puts(">>>>>>>>>>>>>>>>>>>>>>>>>>>>machdep_save_state");
  return(_setjmp(currentThread->mthread->machdep_state));
  //set jump in currentThread
#endif
}

/* ==========================================================================
 * machdep_restore_state()
 */
void machdep_restore_state(void)
{
  //  puts(">>>>>>>>>>>>>>>>>>>>>>>>>>>>machdep_restore_state");
  longjmp(
#ifndef WITH_REALTIME_THREADS
	  gtl->mthread.machdep_state,
#else
	 currentThread->mthread->machdep_state, //jump to currentThread
#endif
	  1);
}

/* ==========================================================================
 * machdep_save_float_state()
 */
int machdep_save_float_state(struct machdep_pthread * mthread)
{
  char * fdata = (char *)mthread->machdep_float_state;
#ifdef RTJ_DEBUG_THREADS
  printf("\nSaving float state to %p, mthread is %p, currentthread is %p (%d)",
	 fdata, mthread, currentThread, currentThread->threadID);
#endif
  __asm__ ("fsave %0"::"m" (*fdata));
}

/* ==========================================================================
 * machdep_restore_float_state()
 */

int machdep_restore_float_state(void)
{

#ifndef WITH_REALTIME_THREADS
  char * fdata = (char *)gtl->mthread.machdep_float_state;
#else
  /* restore currentThread's float state */
  char * fdata = (char *)currentThread->mthread->machdep_float_state;
#endif
#ifdef RTJ_DEBUG_THREADS
  printf("\nRestoring float state from %p, currentthread is %p (%d)",
  	 fdata, currentThread, currentThread->threadID);
#endif
  __asm__ ("frstor %0"::"m" (*fdata));
}

/* ==========================================================================
 * machdep_pthread_cleanup()
 */
void *machdep_pthread_cleanup(struct machdep_pthread *machdep_pthread)
{
    return(machdep_pthread->machdep_stack);
}

/* ==========================================================================
 * machdep_pthread_start()
 */
void machdep_pthread_start(void)
{
  /* Run current threads start routine with argument */
  /*pthread_exit*/
  /*Stash the arg value on the stack*/
#ifndef WITH_REALTIME_THREADS  
  exitthread(gtl->mthread.start_routine(gtl->mthread.start_argument));
#else
  /* call startup routine of current thread */
  exitthread(currentThread->mthread->start_routine(currentThread->mthread->start_argument));
#endif
}

/* ==========================================================================
 * __machdep_stack_free()
 */
void __machdep_stack_free(void * stack)
{  
  /*DECREMENT_MEM_STATS(STACKSIZE);*/
#ifdef WITH_REALTIME_JAVA
     RTJ_FREE(stack);
#else
     free(stack);
#endif
}

/* ==========================================================================
 * __machdep_stack_alloc()
 */
void * __machdep_stack_alloc(size_t size)
{
    void * stack;
    /*    INCREMENT_MEM_STATS(STACKSIZE);*/
#ifdef WITH_REALTIME_JAVA
    return RTJ_MALLOC_UNCOLLECTABLE(size);
#else
    return(malloc(size));
#endif
}

/* ==========================================================================
 * __machdep_pthread_create()
 */
void __machdep_pthread_create(struct machdep_pthread *machdep_pthread,
  void *(* start_routine)(), void *start_argument, 
  long stack_size, long nsec, long flag)
{
#ifdef RTJ_DEBUG_THREADS
    printf("\nCreating thread: %p", machdep_pthread);
#endif
    machdep_pthread->start_routine = start_routine;
    machdep_pthread->start_argument = start_argument;

    machdep_pthread->machdep_timer.it_value.tv_sec = 0;
    machdep_pthread->machdep_timer.it_interval.tv_sec = 0;
    machdep_pthread->machdep_timer.it_interval.tv_usec = 0;
    machdep_pthread->machdep_timer.it_value.tv_usec = nsec / 1000;

    setjmp(machdep_pthread->machdep_state);
    machdep_save_float_state(machdep_pthread);
    /*
     * Set up new stact frame so that it looks like it
     * returned from a longjmp() to the beginning of
     * machdep_pthread_start().
     */
#if defined __GLIBC__
    machdep_pthread->machdep_state->__jmpbuf[JB_PC] = 
                                        (int)machdep_pthread_start;
    machdep_pthread->machdep_state->__jmpbuf[JB_BP] = 0;/* So the backtrace
                                                      * is sensible (mevans) *

    /* Stack starts high and builds down. */
    machdep_pthread->machdep_state->__jmpbuf[JB_SP] =
      (int)machdep_pthread->machdep_stack + stack_size;
    machdep_pthread->hiptr=
      (char *)machdep_pthread->machdep_stack + stack_size;
#else
    machdep_pthread->machdep_state->__pc = (char *)machdep_pthread_start;
    machdep_pthread->machdep_state->__bp = (char *)0;/* So the backtrace
                                                      * is sensible (mevans) *

    /* Stack starts high and builds down. */
    machdep_pthread->machdep_state->__sp =
      (char *)machdep_pthread->machdep_stack + stack_size;
    machdep_pthread->hiptr=
      (char *)machdep_pthread->machdep_stack + stack_size;
#endif
}


/* ==========================================================================
 * Linux Socket calls are a bit different
 * ==========================================================================
 * machdep_sys_socket()
 */

#endif
