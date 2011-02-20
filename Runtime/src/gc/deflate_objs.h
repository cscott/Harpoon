#ifndef INCLUDED_DEFLATE_OBJS_H
#define INCLUDED_DEFLATE_OBJS_H

#include "jni-types.h"
#include "jni-private.h"
#include "obj_list.h"
#include "cp_heap.h"

void deflate_freed_objs (struct copying_heap *h);

void register_inflated_obj (jobject_unwrapped obj, struct copying_heap *h);

#endif
