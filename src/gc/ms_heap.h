#ifndef INCLUDED_MS_HEAP_H
#define INCLUDED_MS_HEAP_H

#include "free_list.h"
#include "precise_gc.h"

/* data structure for a mark-and-sweep heap */
struct marksweep_heap
{
  size_t heap_size;
  size_t free_memory;
  void *heap_begin;
  void *heap_end;
  void *mapped_end;
  float avg_occupancy;
  struct block *free_list;
  struct block *small_blocks[NUM_SMALL_BLOCK_SIZES];
};

#define IN_MARKSWEEP_HEAP(x,h) \
((void *)(x) > (h).heap_begin && (void *)(x) < (h).heap_end)

void *allocate_in_marksweep_heap(size_t size, struct marksweep_heap *h);

int expand_marksweep_heap(size_t size, struct marksweep_heap *h);

void free_unreachable_blocks(struct marksweep_heap *h);

void init_marksweep_heap(void *mapped, 
			 size_t heap_size, 
			 size_t mapped_size, 
			 struct marksweep_heap *h);

int move_to_marksweep_heap(jobject_unwrapped *ref, struct marksweep_heap *h);

#endif
