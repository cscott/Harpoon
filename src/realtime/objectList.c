/* objectList.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "objectList.h"

inline Object* ObjectList_first(struct ObjectList* ol, Object object) {
  return &(ol->objects[HASH(object) % (ol->size)]);
}

inline Object* ObjectList_next(struct ObjectList* ol, Object object, 
			       Probe* p) {
  return &(ol->objects[(HASH(object) + ((*p) += PROBE_INC)) % ol->size]);
}

inline struct ObjectList* ObjectList_new(size_t initSize) {
  struct ObjectList* ol = (struct ObjectList*)
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc_uncollectable
#else
    malloc
#endif
    (sizeof(struct ObjectList));
  ol->used = 0;
  ol->objects =
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc_uncollectable
#else
    malloc
#endif
    ((ol->size = initSize) * sizeof(Object));
  memset(ol->objects, 0, initSize * sizeof(Object));
  flex_mutex_init(&(ol->lock));
  return ol;
}

void ObjectList_insert(struct ObjectList* ol, Object object) {
  Probe probe;
  Object* index;
  int i;

  flex_mutex_lock(&(ol->lock));
  if (ol->used > (ol->size >> 1)) { 
    Object* oldObjects = ol->objects;
    ol->objects =
#ifdef BDW_CONSERVATIVE_GC
      GC_malloc_uncollectable  
#else      
      malloc
#endif
      ((ol->size <<= 1) * sizeof(Object)); /* Resize */

    for (i = 0; i < (ol->size >> 1); i++) { /* Rehash */
      Object oldObject = oldObjects[i];
      if (oldObject == Object_null) {
	continue;
      }
      probe = Probe_init;
      index = ObjectList_first(ol, oldObject);
      while (*index != Object_null) {
	index = ObjectList_next(ol, oldObject, &probe);
      }
      *index = oldObject;
    }
#ifdef BDW_CONSERVATIVE_GC
    GC_free
#else
      free
#endif
      (oldObjects);
  }

  index = ObjectList_first(ol, object);
  while (*index != object) { /* Do the insert */
    if (*index == Object_null) {
      *index = object;
      ol->used++;
      break;
    }
    index = ObjectList_next(ol, object, &probe); 
  }
  flex_mutex_unlock(&(ol->lock));
}

inline void ObjectList_delete(struct ObjectList* ol, Object object) {
  Probe probe = Probe_init;
  Object* index;
  flex_mutex_lock(&(ol->lock));
  index = ObjectList_first(ol, object);
  while (*index != Object_null) {
    if (*index == object) {
      *index = Object_null;
      ol->used--;
      break;
    }
    index = ObjectList_next(ol, object, &probe);
  }
  flex_mutex_unlock(&(ol->lock));
}

inline int ObjectList_contains(struct ObjectList* ol, Object object) {
  Probe probe = Probe_init;
  Object* index;
  flex_mutex_lock(&(ol->lock));
  index = ObjectList_first(ol, object);
  while (*index != Object_null) {
    if (*index == object) {
      flex_mutex_unlock(&(ol->lock));
      return 1;
    }
    index = ObjectList_next(ol, object, &probe);
  }
  flex_mutex_unlock(&(ol->lock));
  return 0;
}

inline Object* ObjectList_packedContents(struct ObjectList* ol) {
  Object* newContents = (Object*)
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc_uncollectable
#else
    malloc
#endif
    (ol->used * sizeof(Object));
  int index = 0;
  int i;
  flex_mutex_lock(&(ol->lock));
  for (i = 0; i < ol->size; i++) {
    if (ol->objects[i] != Object_null) {
      newContents[index++] = ol->objects[i];
    }
  }
  flex_mutex_unlock(&(ol->lock));
  return newContents;
}

inline void ObjectList_visit(struct ObjectList* ol, 
			     void visitor (Object)) {
  int i;
  flex_mutex_lock(&(ol->lock));
  for (i = 0; i < ol->size; i++) {
    if (ol->objects[i] != Object_null) {
      visitor(ol->objects[i]);
    }
  }
  flex_mutex_unlock(&(ol->lock));
}

inline void ObjectList_clear(struct ObjectList* ol, size_t newSize) {
  flex_mutex_lock(&(ol->lock));
#ifdef BDW_CONSERVATIVE_GC
  GC_free
#else
    free
#endif
    (ol->objects);
  ol->objects = 
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc_uncollectable
#else
    malloc
#endif
    ((ol->size = newSize) * sizeof(Object));
  memset(ol->objects, 0, newSize * sizeof(Object));
  ol->used = 0;
  flex_mutex_unlock(&(ol->lock));
}

inline void ObjectList_freeRefs(struct ObjectList* ol) {
  int i;
  flex_mutex_lock(&(ol->lock));
#ifdef DEBUG
  printf("  ");
#endif
  for (i = 0; i < ol->size; i++) {
    if (ol->objects[i] != Object_null) {
#ifdef DEBUG
      printf("%08x ", ol->objects[i]);
#endif
#ifdef BDW_CONSERVATIVE_GC
      GC_free
#else
	free
#endif
	(ol->objects[i]);
      ol->objects[i] = Object_null;
    }
  }
#ifdef DEBUG
  printf("\n");
#endif
  ol->used = 0;
  flex_mutex_unlock(&(ol->lock));
}

inline void ObjectList_free(struct ObjectList* ol) {
  flex_mutex_destroy(&(ol->lock));
#ifdef DEBUG
  printf("  ol->objects = %08x\n", ol->objects);
#endif
#ifdef BDW_CONSERVATIVE_GC
  GC_free
#else
    free
#endif
    (ol->objects);
#ifdef DEBUG
  printf("  ol = %08x\n", ol);
#endif
#ifdef BDW_CONSERVATIVE_GC
  GC_free
#else
    free
#endif
    (ol);
}
