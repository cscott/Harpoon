/* 
 * Copyright (c) 1994 by Xerox Corporation.  All rights reserved.
 * Copyright (c) 1996 by Silicon Graphics.  All rights reserved.
 * Copyright (c) 1998 by Fergus Henderson.  All rights reserved.
 * Copyright (c) 2000 by Hewlett-Packard Company.  All rights reserved.
 *
 * THIS MATERIAL IS PROVIDED AS IS, WITH ABSOLUTELY NO WARRANTY EXPRESSED
 * OR IMPLIED.  ANY USE IS AT YOUR OWN RISK.
 *
 * Permission is hereby granted to use or copy this program
 * for any purpose,  provided the above notices are retained on all copies.
 * Permission to modify the code and to distribute modified code is granted,
 * provided the above notices are retained, and a notice that the code was
 * modified is included with the above copyright notice.
 */
/*
 * Support code for LinuxThreads, the clone()-based kernel
 * thread package for Linux which is included in libc6.
 *
 * This code relies on implementation details of LinuxThreads,
 * (i.e. properties not guaranteed by the Pthread standard),
 * though this version now does less of that than the other Pthreads
 * support code.
 *
 * Note that there is a lot of code duplication between linux_threads.c
 * and thread support for some of the other Posix platforms; any changes
 * made here may need to be reflected there too.
 */

/* #define DEBUG_THREADS 1 */

/* ANSI C requires that a compilation unit contains something */

# if defined(GC_USER_THREADS) || defined(USER_THREADS)

# include "gc_priv.h"
# include "specific.h"
# include <stdlib.h>
# include <threads.h>
# include <sched.h>
# include <time.h>
# include <errno.h>
# include <unistd.h>
# include <sys/mman.h>
# include <sys/time.h>
# include <semaphore.h>
# include <signal.h>
# include <sys/types.h>
# include <sys/stat.h>
# include <fcntl.h>

#ifdef GC_USE_LD_WRAP
#   define WRAP_FUNC(f) __wrap_##f
#   define REAL_FUNC(f) __real_##f
#else
#   define WRAP_FUNC(f) GC_##f
#   define REAL_FUNC(f) f
#   undef pthread_create
#   undef pthread_sigmask
#   undef pthread_join
#endif


void GC_thr_init();
/*
To make sure that we're using LinuxThreads and not some other thread
package, we generate a dummy reference to `pthread_kill_other_threads_np'
(was `__pthread_initial_thread_bos' but that disappeared),
which is a symbol defined in LinuxThreads, but (hopefully) not in other
thread packages.
*/

long GC_nprocs = 1;	/* Number of processors.  We may not have	*/
			/* access to all of them, but this is as good	*/
			/* a guess as any ...				*/


static __inline__ void start_mark_threads()
{
}

/*Small world*/
void GC_start_world() {}
void GC_stop_world() {}

volatile unsigned int GC_allocate_lock = 0;

void GC_lock() {
  /* No lock needed*/
}


/* We hold allocation lock.  Should do exactly the right thing if the	*/
/* world is stopped.  Should not fail if it isn't.			*/
void GC_push_all_stacks()
{
  ptr_t sp = GC_approx_sp();
  ptr_t lo, hi;
  /* On IA64, we also need to scan the register backing store. */
  struct thread_list * tptr = gtl;
    
#if DEBUG_THREADS
  GC_printf1("Pushing stacks from thread 0x%lx\n", (unsigned long) me);
#endif
  
  /*Handle self*/

  lo=sp;
  hi=gtl->mthread.hiptr;
  GC_push_all_stack(lo,hi);

  /*Handle active threads*/
  tptr=tptr->next;
  while(tptr!=gtl) {
    lo=tptr->mthread.machdep_state->__jmpbuf[JB_SP];
    hi=tptr->mthread.hiptr;
    GC_push_all_stack(lo,hi);
    tptr=tptr->next;
  }

  /*Handle io waiting threads*/
  if (ioptr!=NULL) {
    lo=ioptr->mthread.machdep_state->__jmpbuf[JB_SP];
    hi=ioptr->mthread.hiptr;
    GC_push_all_stack(lo,hi);
    
    tptr=ioptr->next;
    while(tptr!=ioptr) {
      lo=tptr->mthread.machdep_state->__jmpbuf[JB_SP];
      hi=tptr->mthread.hiptr;
      GC_push_all_stack(lo,hi);
      tptr=tptr->next;
    }
  }
}


VOLATILE GC_bool GC_collecting = 0;
/* A hint that we're in the collector and       */
/* holding the allocation lock for an           */
/* extended period.                             */


/* Return the number of processors, or i<= 0 if it can't be determined.	*/
int GC_get_nprocs()
{
  return 1;
}

# endif /* USER_THREADS */

