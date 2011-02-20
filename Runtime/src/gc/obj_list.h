#ifndef INCLUDED_OBJ_LIST_H
#define INCLUDED_OBJ_LIST_H

#include "jni-types.h"
#include "jni-private.h"

/* data structure for list of objects */
struct obj_list {
  jobject_unwrapped obj;
  struct obj_list *next;
};

#endif
