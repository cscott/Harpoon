#ifndef INCLUDED_MS_HEAP_STRUCT_H
#define INCLUDED_MS_HEAP_STRUCT_H

#include "free_list_consts.h"

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

#endif
