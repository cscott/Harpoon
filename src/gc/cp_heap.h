#ifndef INCLUDED_CP_HEAP_H
#define INCLUDED_CP_HEAP_H

#include "jni-types.h"
#include "jni-private.h"
#include "deflate_objs.h"

/* data structure for a copying generation */
struct copying_heap
{
  size_t heap_size;
  void *heap_end;
  void *mapped_end;
  float avg_occupancy;
  void *from_begin;
  void *from_free;
  void *from_end;
  void *to_begin;
  void *to_free;
  void *to_end;
  void *survived;
  struct obj_list *inflated_objs; // protected by mutex
};

#define IN_TO_SPACE(x,h) \
((void *)(x) >= (h)->to_begin && (void *)(x) < (h)->to_free)

#define IN_FROM_SPACE(x,h) \
((void *)(x) >= (h)->from_begin && (void *)(x) < (h)->from_free)

void grow_copying_heap(size_t needed_space, struct copying_heap *h);

void init_copying_heap(void *mapped, 
		       size_t heap_size, 
		       size_t mapped_size, 
		       struct copying_heap *h);

#endif
