#ifndef INCLUDED_WRITE_BARRIER_H
#define INCLUDED_WRITE_BARRIER_H

#define JOLDEN_WRITE_BARRIER 1

//#define ARRAY_WB
//#define PTR_WB
#define WITH_GENERATION_CHECK

//#ifdef JOLDEN_WRITE_BARRIER

FLEX_MUTEX_DECLARE_STATIC(intergen_mutex);

#ifdef WITH_GENERATION_CHECK
#include "../src/gc/ms_heap_struct.h"
extern struct marksweep_heap old_gen;
#endif

// array of intergenerational references
extern jobject_unwrapped **intergen;

#ifdef ARRAY_WB 
extern int intergen_next;
#else // !ARRAY_WB
extern jobject_unwrapped **intergen_next;
#endif // !ARRAY_WB

static inline void generational_write_barrier(jobject_unwrapped *ref)
#ifdef ARRAY_WB
{
#ifdef WITH_GENERATION_CHECK
  if (IN_MARKSWEEP_HEAP(ref, old_gen))
#endif
    {
      FLEX_MUTEX_LOCK(&intergen_mutex);
      intergen[intergen_next++] = ref;
      //assert(intergen_next < INTERGEN_LENGTH);
      FLEX_MUTEX_UNLOCK(&intergen_mutex);
    }
}
#elif defined(PTR_WB)
{
#ifdef WITH_GENERATION_CHECK
  if (IN_MARKSWEEP_HEAP(ref, old_gen))
#endif
    {
      FLEX_MUTEX_LOCK(&intergen_mutex);
      *intergen_next++ = ref;
      FLEX_MUTEX_UNLOCK(&intergen_mutex);
    }
}
#else
{
  jobject_unwrapped **t;
#ifdef WITH_GENERATION_CHECK
  if (IN_MARKSWEEP_HEAP(ref, old_gen))
#endif
    {
      FLEX_MUTEX_LOCK(&intergen_mutex);
      t = intergen_next;
      *t = ref;
      t++;
      intergen_next = t;
      FLEX_MUTEX_UNLOCK(&intergen_mutex);
    }
}
#endif

#endif // JOLDEN_WRITE_BARRIER
#endif // INCLUDED_WRITE_BARRIER_H
