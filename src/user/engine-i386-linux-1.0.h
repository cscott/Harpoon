/* ==== machdep.h ============================================================
 * Copyright (c) 1993 Chris Provenzano, proven@athena.mit.edu
 *
 * $Id: engine-i386-linux-1.0.h,v 1.6 2002-10-22 19:40:23 wbeebee Exp $
 */
#ifndef MACHDEP
#define MACHDEP

#include <unistd.h>
#include <setjmp.h>
#include <sys/time.h>
#include <limits.h>

/*
 * The first machine dependent functions are the SEMAPHORES
 * needing the test and set instruction.
 */
#define SEMAPHORE_CLEAR 0
#define SEMAPHORE_SET   1

#define SEMAPHORE_TEST_AND_SET(lock)    \
({										\
volatile long temp = SEMAPHORE_SET;     \
										\
__asm__("xchgl %0,(%2)"                 \
        :"=r" (temp)                    \
        :"0" (temp),"r" (lock):"memory");        \
temp;                                   \
})

#define SEMAPHORE_RESET(lock)           *lock = SEMAPHORE_CLEAR

/*
 * New types
 */
typedef long    semaphore;

#define SIGMAX	31

/*
 * New Strutures
 */
struct machdep_pthread {
  void        		*(*start_routine)(void *);
  void        		*start_argument;
  void        		*machdep_stack;
  void                  *hiptr;
  struct itimerval	machdep_timer;
#ifdef WITH_REALTIME_THREADS
  sigjmp_buf            machdep_state;
#else
  jmp_buf     		machdep_state;
#endif
  char 	    		machdep_float_state[108];
  int                   started;
};

/*
 * Static machdep_pthread initialization values.
 * For initial thread only.
 */
#define MACHDEP_PTHREAD_INIT    \
{ NULL, NULL, NULL, { { 0, 0 }, { 0, 100000 } }, 0 }

/*
 * Minimum stack size
 */
#ifndef PTHREAD_STACK_MIN
#define PTHREAD_STACK_MIN	1024
#endif

/*
 * sigset_t macros
 */
#define	SIG_ANY(sig)		(sig)

/*
 * Some fd flag defines that are necessary to distinguish between posix
 * behavior and bsd4.3 behavior.
 */
#define __FD_NONBLOCK 		O_NONBLOCK

/*
 * New functions
 */

__BEGIN_DECLS


#define __machdep_stack_get(x)      (x)->machdep_stack
#define __machdep_stack_set(x, y)   (x)->machdep_stack = y
#define __machdep_stack_repl(x, y)                          \
{                                                           \
    if (stack = __machdep_stack_get(x)) {                   \
        __machdep_stack_free(stack);                        \
    }                                                       \
    __machdep_stack_set(x, y);                              \
}

void *  __machdep_stack_alloc       __P((size_t));
void    __machdep_stack_free        __P((void *));

// The function that calls machdep_save_state must never return.
// This function is a macro, so that you don't end up creating
// an extra stack frame that can be trashed by the next function
// that the function that calls machdep_save_state calls before
// restoring state back to the location saved by machdep_save_state.
#ifndef WITH_REALTIME_THREADS
#define machdep_save_state(mthread) _setjmp((mthread)->machdep_state)
#else
#define machdep_save_state(mthread) sigsetjmp((mthread)->machdep_state, 1)
#endif

void machdep_restore_state();

void __machdep_pthread_create(struct machdep_pthread *machdep_pthread,
			      void *(* start_routine)(), void *start_argument,
			      long stack_size, long nsec, long flag);

void machdep_restore_float_state();
void machdep_save_float_state(struct machdep_pthread *mthread);

__END_DECLS
#endif /*MACHDEP*/

