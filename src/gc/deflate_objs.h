#ifndef INCLUDED_DEFLATE_OBJS_H
#define INCLUDED_DEFLATE_OBJS_H

#include "jni-types.h"
#include "jni-private.h"
#include "cp_heap.h"

/* data structures for list of inflated objects */
struct obj_list {
  jobject_unwrapped obj;
  struct obj_list *next;
};

void deflate_freed_objs (struct copying_heap *h);

void register_inflated_obj (jobject_unwrapped obj, struct copying_heap *h);

#endif
