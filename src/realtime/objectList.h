/* objectList.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __OBJECT_LIST_H__
#define __OBJECT_LIST_H__
#include <stdio.h>
#include <string.h>
#include "RTJconfig.h"
#include "flexthread.h"
#include <jni.h>
#include "RTJfinalize.h"

#ifdef WITH_PRECISE_GC
#include "fni-wrap.h"
#include "precisec.h"
#include "jni-private.h"
#include "../gc/precise_gc.h"
#endif

/* Small (but not smaller than the average object size) prime number that's */
/* one more than a multiple of 8. */
#define PROBE_INC 17

/* Should be templated, but C doesn't support templates */
typedef struct oobj* Object; 
typedef int Probe;

#define Probe_init 0
#define Object_null (Object)0
#define HASH(obj) (int)(PTRMASK(obj))

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
			     void visitor (Object*));
inline void ObjectList_scan(struct ObjectList* ol);

#endif /* __OBJECT_LIST_H__ */
