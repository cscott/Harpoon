#ifndef INCLUDED_WRITE_BARRIER_H
#define INCLUDED_WRITE_BARRIER_H

//#define JOLDEN_WRITE_BARRIER 1

#ifdef JOLDEN_WRITE_BARRIER
#include "../src/gc/ms_heap.h"

/*
extern struct marksweep_heap old_gen;
extern jobject_unwrapped **intergen;
extern int intergen_next;
//extern jobject_unwrapped **intergen_next;

extern inline void generational_write_barrier(jobject_unwrapped *ref)
{
  // if (IN_MARKSWEEP_HEAP(ref, old_gen))
  intergen[intergen_next++] = ref;
  //*intergen_next = ref;
  //intergen_next++;
  //assert(intergen_next < INTERGEN_LENGTH);
}
*/
#endif
#endif
