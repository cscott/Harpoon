#ifndef INCLUDED_MS_HEAP_H
#define INCLUDED_MS_HEAP_H

#include "free_list.h"
#include "precise_gc.h"
#include "ms_heap_struct.h"

void *allocate_in_marksweep_heap(size_t size, struct marksweep_heap *h);

int expand_marksweep_heap(size_t size, struct marksweep_heap *h);

void free_unreachable_blocks(struct marksweep_heap *h);

void init_marksweep_heap(void *mapped, 
			 size_t heap_size, 
			 size_t mapped_size, 
			 struct marksweep_heap *h);

int move_to_marksweep_heap(jobject_unwrapped *ref, struct marksweep_heap *h);

#endif
