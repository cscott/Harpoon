#ifndef INCLUDED_MS_HEAP_H
#define INCLUDED_MS_HEAP_H

#include "free_list.h"
#include "precise_gc.h"

/* data structure for a page of memory */
struct page
{
  void *page_end;
  void *mapped_end;
  struct page *next;
  struct block blocks[0];
};


/* data structure for a mark-and-sweep heap */
struct marksweep_heap
{
  size_t heap_size;
  float avg_occupancy;
  struct page *allocated_pages;
  struct block *free_list;
  struct block *small_blocks[NUM_SMALL_BLOCK_SIZES];
};

void *allocate_in_marksweep_heap(size_t size, struct marksweep_heap *h);

int expand_marksweep_heap(size_t size, struct marksweep_heap *h);

void free_unreachable_blocks(struct marksweep_heap *h);

int in_marksweep_heap(jobject_unwrapped obj, struct marksweep_heap *h);

void init_marksweep_heap(void *mapped, 
			 size_t heap_size, 
			 size_t mapped_size, 
			 struct marksweep_heap *h);

#endif
