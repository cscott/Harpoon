#include "cp_heap.h"
#include "precise_gc.h"
#include "system_page_size.h"
#include <assert.h>

#define COPYING_HEAP_DIVISOR 4

#ifdef DEBUG_GC
/* effects: overwrites to-space with recognizable garbage for easy debugging
 */
void debug_overwrite_to_space(struct copying_heap *h)
{
  memset(h->to_begin, 7, h->heap_size/2);
}
#endif


/* requires: all live data should reside in to-space
   effects: flips the semi-spaces so that all the live
   data resides in from-space
*/
void flip_semispaces(struct copying_heap *h)
{
  void *tmp;

  // switch the begin pointers
  tmp = h->from_begin;
  h->from_begin = h->to_begin;
  h->to_begin = tmp;

  // switch the end pointers
  tmp = h->from_end;
  h->from_end = h->to_end;
  h->to_end = tmp;

  // re-assign the free pointers
  // no need to save the free pointer in the from-space
  // since the from-space is empty; the new to-space
  // free pointer therefore points to the beginning of
  // to-space
  h->from_free = h->to_free;
  h->to_free = h->to_begin;
}


/* effects: trys to grow the given heap to accomodate 
   at least the amt requested. if not possible, grows
   the heap to the max possible size
*/
void grow_copying_heap(size_t needed_space, struct copying_heap *h)
{
  size_t bytes;

  bytes = ROUND_TO_NEXT_PAGE(needed_space + 
			     h->heap_size/COPYING_HEAP_DIVISOR);

  // check if we have mapped space to expand into
  if (h->heap_end + bytes > h->mapped_end)
    // if we can't get the amount we need, just
    // expand the heap as much as possible
    bytes = h->mapped_end - h->heap_end;

  // update the heap size
  h->heap_size += 2*bytes;
  
  // update the bounds of the semi-spaces
  h->to_end += bytes;
  h->from_end += bytes;

  assert(((h->from_end - h->from_begin) + 
	  (h->to_end - h->to_begin) == h->heap_size));
}


/* effects: initializes the heap at the given memory with the given sizes */
void init_copying_heap(void *mapped, 
		       size_t heap_size, 
		       size_t mapped_size, 
		       struct copying_heap *h)
{
  h->heap_size = heap_size;
  h->heap_end = mapped + heap_size;
  h->mapped_end = mapped + mapped_size;
  h->avg_occupancy = 0;
  
  // from-space is initially allocated at the beginning of the mapped space
  h->from_begin = mapped;
  h->from_free = mapped;
  h->from_end = mapped + heap_size/2;

  // to-space is initially allocated 1/2 of the way into the mapped space
  mapped += mapped_size/2;
  h->to_begin = mapped;
  h->to_free = mapped;
  h->to_end = mapped + heap_size/2;

  // no objects have survived a collection yet
  h->survived = mapped;
}


/* effects: move object from from-space to to-space, updating
   the given pointer and creating a forwarding reference */
void relocate_to_to_space(jobject_unwrapped *ref, struct copying_heap *h)
{
  jobject_unwrapped obj = PTRMASK((*ref));
  void *forwarding_address;
  size_t obj_size = align(FNI_ObjectSize(obj));

  // relocating objects should not exceed the size of the heap
  assert(h->to_free + obj_size <= h->to_end);

  // copy over to to-space
  forwarding_address = TAG_HEAP_PTR(memcpy(h->to_free, obj, obj_size));
  
  // write forwarding address to previous location
  obj->claz = (struct claz *)forwarding_address;
  
  // update given reference
  (*ref) = forwarding_address;

  // increment free
  h->to_free += obj_size;
}
