/* 
 * Copyright 1988, 1989 Hans-J. Boehm, Alan J. Demers
 * Copyright (c) 1991-1994 by Xerox Corporation.  All rights reserved.
 * Copyright (c) 1996-1999 by Silicon Graphics.  All rights reserved.
 * Copyright (c) 1999 by Hewlett-Packard Company. All rights reserved.
 *
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

#ifndef GC_LOCKS_H
#define GC_LOCKS_H

/*
 * Mutual exclusion between allocator/collector routines.
 * Needed if there is more than one allocator thread.
 * FASTLOCK() is assumed to try to acquire the lock in a cheap and
 * dirty way that is acceptable for a few instructions, e.g. by
 * inhibiting preemption.  This is assumed to have succeeded only
 * if a subsequent call to FASTLOCK_SUCCEEDED() returns TRUE.
 * FASTUNLOCK() is called whether or not FASTLOCK_SUCCEEDED().
 * If signals cannot be tolerated with the FASTLOCK held, then
 * FASTLOCK should disable signals.  The code executed under
 * FASTLOCK is otherwise immune to interruption, provided it is
 * not restarted.
 * DCL_LOCK_STATE declares any local variables needed by LOCK and UNLOCK
 * and/or DISABLE_SIGNALS and ENABLE_SIGNALS and/or FASTLOCK.
 * (There is currently no equivalent for FASTLOCK.)
 *
 * In the PARALLEL_MARK case, we also need to define a number of
 * other inline finctions here:
 *   GC_bool GC_compare_and_exchange( volatile GC_word *addr,
 *   				      GC_word old, GC_word new )
 *   GC_word GC_atomic_add( volatile GC_word *addr, GC_word how_much )
 *   void GC_memory_barrier( )
 *   
 */  
# ifdef THREADS
#  ifdef PCR_OBSOLETE	/* Faster, but broken with multiple lwp's	*/
#    include  "th/PCR_Th.h"
#    include  "th/PCR_ThCrSec.h"
     extern struct PCR_Th_MLRep GC_allocate_ml;
#    define DCL_LOCK_STATE  PCR_sigset_t GC_old_sig_mask
#    define LOCK() PCR_Th_ML_Acquire(&GC_allocate_ml) 
#    define UNLOCK() PCR_Th_ML_Release(&GC_allocate_ml)
#    define UNLOCK() PCR_Th_ML_Release(&GC_allocate_ml)
#    define FASTLOCK() PCR_ThCrSec_EnterSys()
     /* Here we cheat (a lot): */
#        define FASTLOCK_SUCCEEDED() (*(int *)(&GC_allocate_ml) == 0)
		/* TRUE if nobody currently holds the lock */
#    define FASTUNLOCK() PCR_ThCrSec_ExitSys()
#  endif
#  ifdef PCR
#    include <base/PCR_Base.h>
#    include <th/PCR_Th.h>
     extern PCR_Th_ML GC_allocate_ml;
#    define DCL_LOCK_STATE \
	 PCR_ERes GC_fastLockRes; PCR_sigset_t GC_old_sig_mask
#    define LOCK() PCR_Th_ML_Acquire(&GC_allocate_ml)
#    define UNLOCK() PCR_Th_ML_Release(&GC_allocate_ml)
#    define FASTLOCK() (GC_fastLockRes = PCR_Th_ML_Try(&GC_allocate_ml))
#    define FASTLOCK_SUCCEEDED() (GC_fastLockRes == PCR_ERes_okay)
#    define FASTUNLOCK()  {\
        if( FASTLOCK_SUCCEEDED() ) PCR_Th_ML_Release(&GC_allocate_ml); }
#  endif
#  ifdef SRC_M3
     extern GC_word RT0u__inCritical;
#    define LOCK() RT0u__inCritical++
#    define UNLOCK() RT0u__inCritical--
#  endif
#  ifdef SOLARIS_THREADS
#    include <thread.h>
#    include <signal.h>
     extern mutex_t GC_allocate_ml;
#    define LOCK() mutex_lock(&GC_allocate_ml);
#    define UNLOCK() mutex_unlock(&GC_allocate_ml);
#  endif
#  if defined(LINUX_THREADS) || defined(GC_OSF1_THREADS)
#   define NO_THREAD (pthread_t)(-1)
#   if defined(I386)|| defined(POWERPC) || defined(ALPHA) || defined(IA64) \
    || defined(M68K) || defined(SPARC)
#    include <pthread.h>
#    if defined(PARALLEL_MARK) 
      /* We need compare-and-swap to update mark bits, where it's	*/
      /* performance critical.  If USE_MARK_BYTES is defined, it is	*/
      /* no longer needed for this purpose.  However we use it in	*/
      /* either case to implement atomic fetch-and-add, though that's	*/
      /* less performance critical, and could perhaps be done with	*/
      /* a lock.							*/
#     if defined(GENERIC_COMPARE_AND_SWAP)
	/* Probably not useful, except for debugging.	*/
	extern pthread_mutex_t GC_compare_and_swap_lock;

	static GC_bool GC_compare_and_exchange(volatile GC_word *addr,
					       GC_word old, GC_word new_val)
	{
	  GC_bool result;
	  pthread_mutex_lock(&GC_compare_and_swap_lock);
	  if (*addr == old) {
	    *addr = new_val;
	    result = TRUE;
	  } else {
	    result = FALSE;
	  }
	  pthread_mutex_unlock(&GC_compare_and_swap_lock);
	  return result;
	}
#     endif /* GENERIC_COMPARE_AND_SWAP */
#     if defined(I386)
#      if !defined(GENERIC_COMPARE_AND_SWAP)
         /* Returns TRUE if the comparison succeeded. */
         inline static GC_bool GC_compare_and_exchange(volatile GC_word *addr,
		  				       GC_word old,
						       GC_word new_val) 
         {
	   char result;
	   __asm__ __volatile__("lock; cmpxchgl %2, %0; setz %1"
	    	: "=m"(*(addr)), "=r"(result)
		: "r" (new_val), "0"(*(addr)), "a"(old) : "memory");
	   return (GC_bool) result;
         }
#      endif /* !GENERIC_COMPARE_AND_SWAP */
       inline static void GC_memory_barrier()
       {
	 /* We believe the processor ensures at least processor	*/
	 /* consistent ordering.  Thus a compiler barrier	*/
	 /* should suffice.					*/
         __asm__ __volatile__("" : : : "memory");
       }
#     endif
#     if defined(IA64)
#      if !defined(GENERIC_COMPARE_AND_SWAP)
         inline static GC_bool GC_compare_and_exchange(volatile GC_word *addr,
						       GC_word old, GC_word new_val) 
	 {
	  unsigned long oldval;
	  __asm__ __volatile__("mov ar.ccv=%4 ;; cmpxchg8.rel %0=%1,%2,ar.ccv"
		: "=r"(oldval), "=m"(*addr)
		: "r"(new_val), "1"(*addr), "r"(old) : "memory");
	  return (oldval == old);
         }
#      endif /* !GENERIC_COMPARE_AND_SWAP */
       inline static void GC_memory_barrier()
       {
         __asm__ __volatile__("mf" : : : "memory");
       }
#     endif /* IA64 */
      /* Returns the original value of *addr.	*/
      inline static GC_word GC_atomic_add(volatile GC_word *addr, GC_word how_much)
      {
	GC_word old;
	do {
	  old = *addr;
	} while (!GC_compare_and_exchange(addr, old, old+how_much));
        return old;
      }
#    endif /* PARALLEL_MARK */
#    ifndef THREAD_LOCAL_ALLOC
      /* In the THREAD_LOCAL_ALLOC case, the allocation lock tends to	*/
      /* be held for long periods, if it is held at all.  Thus spinning	*/
      /* and sleeping for fixed periods are likely to result in 	*/
      /* significant wasted time.  We thus rely mostly on queued locks. */
#     define USE_SPIN_LOCK
#     if defined(I386)
       inline static int GC_test_and_set(volatile unsigned int *addr) {
	  int oldval;
	  /* Note: the "xchg" instruction does not need a "lock" prefix */
	  __asm__ __volatile__("xchgl %0, %1"
		: "=r"(oldval), "=m"(*(addr))
		: "0"(1), "m"(*(addr)) : "memory");
	  return oldval;
       }
#     endif
#     if defined(IA64)
       inline static int GC_test_and_set(volatile unsigned int *addr) {
	  long oldval, n = 1;
	  __asm__ __volatile__("xchg4 %0=%1,%2"
		: "=r"(oldval), "=m"(*addr)
		: "r"(n), "1"(*addr) : "memory");
	  return oldval;
       }
       inline static void GC_clear(volatile unsigned int *addr) {
	 __asm__ __volatile__("st4.rel %0=r0" : "=m" (*addr) : : "memory");
       }
#      define GC_CLEAR_DEFINED
#     endif
#     ifdef SPARC
       inline static int GC_test_and_set(volatile unsigned int *addr) {
	 int oldval;

	 __asm__ __volatile__("ldstub %1,%0"
	 : "=r"(oldval), "=m"(*addr)
	 : "m"(*addr) : "memory");
	 return oldval;
       }
#     endif
#     ifdef M68K
       /* Contributed by Tony Mantler.  I'm not sure how well it was	*/
       /* tested.							*/
       inline static int GC_test_and_set(volatile unsigned int *addr) {
          char oldval; /* this must be no longer than 8 bits */

          /* The return value is semi-phony. */
          /* 'tas' sets bit 7 while the return */
          /* value pretends bit 0 was set */
          __asm__ __volatile__(
                 "tas %1@; sne %0; negb %0"
                 : "=d" (oldval)
                 : "a" (addr) : "memory");
          return oldval;
       }
#     endif
#     if defined(POWERPC)
        inline static int GC_test_and_set(volatile unsigned int *addr) {
          int oldval;
          int temp = 1; // locked value

          __asm__ __volatile__(
               "1:\tlwarx %0,0,%3\n"   // load and reserve
               "\tcmpwi %0, 0\n"       // if load is
               "\tbne 2f\n"            //   non-zero, return already set
               "\tstwcx. %2,0,%1\n"    // else store conditional
               "\tbne- 1b\n"           // retry if lost reservation
               "2:\t\n"                // oldval is zero if we set
              : "=&r"(oldval), "=p"(addr)
              : "r"(temp), "1"(addr)
              : "memory");
          return (int)oldval;
        }
        inline static void GC_clear(volatile unsigned int *addr) {
	  __asm__ __volatile__("eieio" ::: "memory");
          *(addr) = 0;
        }
#       define GC_CLEAR_DEFINED
#     endif
#     if defined(ALPHA) 
#      if defined(LINUX_THREADS) || defined(__GNUC__)
        inline static int GC_test_and_set(volatile unsigned int * addr)
        {
          unsigned long oldvalue;
          unsigned long temp;

          __asm__ __volatile__(
                             "1:     ldl_l %0,%1\n"
                             "       and %0,%3,%2\n"
                             "       bne %2,2f\n"
                             "       xor %0,%3,%0\n"
                             "       stl_c %0,%1\n"
                             "       beq %0,3f\n"
                             "       mb\n"
                             "2:\n"
                             ".section .text2,\"ax\"\n"
                             "3:     br 1b\n"
                             ".previous"
                             :"=&r" (temp), "=m" (*addr), "=&r" (oldvalue)
                             :"Ir" (1), "m" (*addr)
			     :"memory");

          return oldvalue;
        }
        /* Should probably also define GC_clear, since it needs	*/
        /* a memory barrier ??					*/
#      else /* ALPHA, not GCC, presumably OSF1 */
#	ifndef GC_OSF1_THREADS
	  --> How did we get here
#	else
#         define GC_test_and_set(addr) __cxx_test_and_set_atomic(addr, 1)
#       endif
#      endif
#     endif /* ALPHA */
#     ifdef ARM32
        inline static int GC_test_and_set(volatile unsigned int *addr) {
          int oldval;
          /* SWP on ARM is very similar to XCHG on x86.  Doesn't lock the
           * bus because there are no SMP ARM machines.  If/when there are,
           * this code will likely need to be updated. */
          /* See linuxthreads/sysdeps/arm/pt-machine.h in glibc-2.1 */
          __asm__ __volatile__("swp %0, %1, [%2]"
      		  	     : "=r"(oldval)
      			     : "r"(1), "r"(addr)
			     : "memory");
          return oldval;
        }
#     endif /* ARM32 */
#     ifndef GC_CLEAR_DEFINED
         inline static void GC_clear(volatile unsigned int *addr) {
	  /* Try to discourage gcc from moving anything past this. */
	  __asm__ __volatile__(" " : : : "memory");
          *(addr) = 0;
         }
#     endif /* !GC_CLEAR_DEFINED */

      extern volatile unsigned int GC_allocate_lock;
      extern void GC_lock(void);
	/* Allocation lock holder.  Only set if acquired by client through */
	/* GC_call_with_alloc_lock.					   */
#     ifdef GC_ASSERTIONS
#        define LOCK() \
		{ if (GC_test_and_set(&GC_allocate_lock)) GC_lock(); \
		  SET_LOCK_HOLDER(); }
#        define UNLOCK() \
		{ GC_ASSERT(I_HOLD_LOCK()); UNSET_LOCK_HOLDER(); \
	          GC_clear(&GC_allocate_lock); }
#     else
#        define LOCK() \
		{ if (GC_test_and_set(&GC_allocate_lock)) GC_lock(); }
#        define UNLOCK() \
		GC_clear(&GC_allocate_lock)
#     endif /* !GC_ASSERTIONS */
#     if 0
	/* Another alternative for OSF1 might be:		*/
#       include <sys/mman.h>
        extern msemaphore GC_allocate_semaphore;
#       define LOCK() { if (msem_lock(&GC_allocate_semaphore, MSEM_IF_NOWAIT) \
 			    != 0) GC_lock(); else GC_allocate_lock = 1; }
        /* The following is INCORRECT, since the memory model is too weak. */
	/* Is this true?  Presumably msem_unlock has the right semantics?  */
	/*		- HB						   */
#       define UNLOCK() { GC_allocate_lock = 0; \
                          msem_unlock(&GC_allocate_semaphore, 0); }
#     endif /* 0 */
#    else /* THREAD_LOCAL_ALLOC */
#      define USE_PTHREAD_LOCKS
#    endif /* THREAD_LOCAL_ALLOC */
#   else /* LINUX_THREADS on hardware for which we don't know how	*/
	 /* to do test and set.						*/
#      define USE_PTHREAD_LOCKS
#   endif /* ! known hardware */
#   ifdef USE_PTHREAD_LOCKS
#      include <pthread.h>
       extern pthread_mutex_t GC_allocate_ml;
#      ifdef GC_ASSERTIONS
#        define LOCK() \
		{ GC_lock(); \
		  SET_LOCK_HOLDER(); }
#        define UNLOCK() \
		{ GC_ASSERT(I_HOLD_LOCK()); UNSET_LOCK_HOLDER(); \
	          pthread_mutex_unlock(&GC_allocate_ml); }
#      else /* !GC_ASSERTIONS */
#        define LOCK() \
	   { if (0 != pthread_mutex_trylock(&GC_allocate_ml)) GC_lock(); }
#        define UNLOCK() pthread_mutex_unlock(&GC_allocate_ml)
#      endif /* !GC_ASSERTIONS */
#   endif /* USE_PTHREAD_LOCKS */
#   define SET_LOCK_HOLDER() GC_lock_holder = pthread_self()
#   define UNSET_LOCK_HOLDER() GC_lock_holder = NO_THREAD
#   define I_HOLD_LOCK() (pthread_equal(GC_lock_holder, pthread_self()))
    extern VOLATILE GC_bool GC_collecting;
#   define ENTER_GC() GC_collecting = 1;
#   define EXIT_GC() GC_collecting = 0;
    extern void GC_lock(void);
    extern pthread_t GC_lock_holder;
#   ifdef GC_ASSERTIONS
      extern pthread_t GC_mark_lock_holder;
#   endif
#  endif /* LINUX_THREADS || GC_OSF1_THREADS */
#  if defined(HPUX_THREADS)
#    include <pthread.h>
     extern pthread_mutex_t GC_allocate_ml;
#    define NO_THREAD (pthread_t)(-1)
#    define LOCK() pthread_mutex_lock(&GC_allocate_ml)
#    define UNLOCK() pthread_mutex_unlock(&GC_allocate_ml)
#  endif
#  if defined(IRIX_THREADS)
     /* This may also eventually be appropriate for HPUX_THREADS */
#    include <pthread.h>
#    ifndef HPUX_THREADS
	/* This probably should never be included, but I can't test	*/
	/* on Irix anymore.						*/
#       include <mutex.h>
#    endif

#    ifndef HPUX_THREADS
#      if __mips < 3 || !(defined (_ABIN32) || defined(_ABI64)) \
	|| !defined(_COMPILER_VERSION) || _COMPILER_VERSION < 700
#        define GC_test_and_set(addr, v) test_and_set(addr,v)
#      else
#	 define GC_test_and_set(addr, v) __test_and_set(addr,v)
#      endif
#    else
       /* I couldn't find a way to do this inline on HP/UX	*/
#    endif
     extern unsigned long GC_allocate_lock;
	/* This is not a mutex because mutexes that obey the (optional) 	*/
	/* POSIX scheduling rules are subject to convoys in high contention	*/
	/* applications.  This is basically a spin lock.			*/
     extern pthread_t GC_lock_holder;
     extern void GC_lock(void);
	/* Allocation lock holder.  Only set if acquired by client through */
	/* GC_call_with_alloc_lock.					   */
#    define SET_LOCK_HOLDER() GC_lock_holder = pthread_self()
#    define NO_THREAD (pthread_t)(-1)
#    define UNSET_LOCK_HOLDER() GC_lock_holder = NO_THREAD
#    define I_HOLD_LOCK() (pthread_equal(GC_lock_holder, pthread_self()))
#    ifdef HPUX_THREADS
#      define LOCK() { if (!GC_test_and_clear(&GC_allocate_lock)) GC_lock(); }
       /* The following is INCORRECT, since the memory model is too weak. */
#      define UNLOCK() { GC_noop1(&GC_allocate_lock); \
			*(volatile unsigned long *)(&GC_allocate_lock) = 1; }
#    else
#      define LOCK() { if (GC_test_and_set(&GC_allocate_lock, 1)) GC_lock(); }
#      if __mips >= 3 && (defined (_ABIN32) || defined(_ABI64)) \
	   && defined(_COMPILER_VERSION) && _COMPILER_VERSION >= 700
#	    define UNLOCK() __lock_release(&GC_allocate_lock)
#      else
	    /* The function call in the following should prevent the	*/
	    /* compiler from moving assignments to below the UNLOCK.	*/
	    /* This is probably not necessary for ucode or gcc 2.8.	*/
	    /* It may be necessary for Ragnarok and future gcc		*/
	    /* versions.						*/
#           define UNLOCK() { GC_noop1(&GC_allocate_lock); \
			*(volatile unsigned long *)(&GC_allocate_lock) = 0; }
#      endif
#    endif
     extern VOLATILE GC_bool GC_collecting;
#    define ENTER_GC() \
		{ \
		    GC_collecting = 1; \
		}
#    define EXIT_GC() GC_collecting = 0;
#  endif /* IRIX_THREADS */
#  ifdef WIN32_THREADS
#    include <windows.h>
     GC_API CRITICAL_SECTION GC_allocate_ml;
#    define LOCK() EnterCriticalSection(&GC_allocate_ml);
#    define UNLOCK() LeaveCriticalSection(&GC_allocate_ml);
#  endif
#  ifndef SET_LOCK_HOLDER
#      define SET_LOCK_HOLDER()
#      define UNSET_LOCK_HOLDER()
#      define I_HOLD_LOCK() FALSE
		/* Used on platforms were locks can be reacquired,	*/
		/* so it doesn't matter if we lie.			*/
#  endif
# else /* !THREADS */
#    define LOCK()
#    define UNLOCK()
# endif /* !THREADS */
# ifndef SET_LOCK_HOLDER
#   define SET_LOCK_HOLDER()
#   define UNSET_LOCK_HOLDER()
#   define I_HOLD_LOCK() FALSE
		/* Used on platforms were locks can be reacquired,	*/
		/* so it doesn't matter if we lie.			*/
# endif
# ifndef ENTER_GC
#   define ENTER_GC()
#   define EXIT_GC()
# endif

# ifndef DCL_LOCK_STATE
#   define DCL_LOCK_STATE
# endif
# ifndef FASTLOCK
#   define FASTLOCK() LOCK()
#   define FASTLOCK_SUCCEEDED() TRUE
#   define FASTUNLOCK() UNLOCK()
# endif

#endif /* GC_LOCKS_H */
