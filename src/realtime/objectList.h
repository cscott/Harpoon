/* objectList.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "RTJconfig.h"
#ifndef WITH_THREADS
# error Realtime Java is turned on, but threads are not turned on
#endif

#ifndef __OBJECT_LIST_H__
#define __OBJECT_LIST_H__
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "config.h"
#include "flexthread.h"

/* Small (but not too small) prime number */
#define PROBE_INC 13  

/* Should be templated, but C doesn't support templates */
typedef void* Object; 
typedef int Probe;

#define Probe_init 0
#define Object_null (Object)0
#define HASH(obj) (int)obj

struct ObjectList {
  size_t size;
  size_t used;
  Object* objects;
  flex_mutex_t lock;
};

inline struct ObjectList* ObjectList_new(size_t initSize);
void ObjectList_insert(struct ObjectList* ol, Object object);
inline void ObjectList_delete(struct ObjectList* ol, Object object);
inline int ObjectList_contains(struct ObjectList* ol, Object object);
inline Object* ObjectList_packedContents(struct ObjectList* ol);
inline void ObjectList_clear(struct ObjectList* ol, size_t newSize);
inline void ObjectList_freeRefs(struct ObjectList* ol);
inline void ObjectList_free(struct ObjectList* ol);
inline void ObjectList_visit(struct ObjectList* ol,
			     void visitor (Object));

#endif /* __OBJECT_LIST_H__ */
