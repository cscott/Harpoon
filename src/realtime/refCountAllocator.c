/* linkedListAllocator.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "refCountAllocator.h"

inline RefCountAllocator RefCountAllocator_new() {
  RefCountAllocator rc = (RefCountAllocator)
    RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct refCountAllocator), 1);
#ifdef RTJ_DEBUG
  checkException();
  printf("0x%08x = RefCountAllocator_new()\n", rc);
#endif  
  rc->scan = &(rc->in_use);
  return rc;
}

inline void* RefCountAllocator_alloc(RefCountAllocator rc, size_t size, int clear) {
  struct refCons* head = NULL;
#ifdef RTJ_DEBUG
  printf("RefCountAllocator_alloc(0x%08x, %d)\n", rc, size);
  checkException();
#endif
  RefCountAllocator_INC(rc);
  if (clear) {
    head = (struct refCons*)RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct refCons)+size, 1);
  } else {
    head = (struct refCons*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct refCons)+size);
    ((struct oobj*)(&(head->obj)))->claz = NULL;
    head->finalize = NULL;
    head->nextFree = NULL;
  }
  head->refCount = 1;
  while (!compare_and_swap((long int*)(&(rc->in_use)), 
			   (long int)(head->next = rc->in_use),
			   (long int)head)) {}
  RefCountAllocator_DEC(rc);
#ifdef RTJ_DEBUG
  checkException();
  printf("  = 0x%08x\n", &(head->obj));
#endif
  return (void*)(&(head->obj));
}

/* Not thread-safe - call when you know everything's done. */
inline void RefCountAllocator_free(RefCountAllocator rc) {
  struct refCons* current;
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCountAllocator_free(0x%08x)\n", rc);
#endif
  current = rc->in_use;
  rc->in_use = NULL;
  while (current) {
    struct refCons* old = current;
    current = current->next;
    if (old->finalize) old->finalize(rc, (void*)(&(old->obj)));
    RTJ_FREE(old);
  }
  current = rc->freeList;
  rc->freeList = NULL;
  while (current) {
    struct refCons* old = current;
    current = current->next;
    if (old->finalize) old->finalize(rc, (void*)(&(old->obj)));
    RTJ_FREE(old);
  }
#ifdef RTJ_DEBUG
  checkException();
  printf("  done!\n");
#endif
}

inline void cleanup(RefCountAllocator rc) {
  int i;
#ifdef RTJ_DEBUG
  checkException();
  printf("    RefCountAllocator_cleanup(0x%08x)\n", rc);
#endif
  printf("     ");
  if (!(rc->refCount)) {
    compare_and_swap((long int*)(&(rc->cleaning)), 0, 1);
    if (rc->cleaning == 1) {
      if (rc->refCount) {
	compare_and_swap((long int*)(&(rc->cleaning)), 1, 0);
      } else {
	struct refCons* freeList = rc->freeList;
	if (freeList) {
	  rc->collectable = freeList;
	}
	compare_and_swap((long int*)(&(rc->cleaning)), 1, 2);
      }
    }
    if (rc->cleaning == 2) {
      rc->freeList = NULL;
      compare_and_swap((long int*)(&(rc->cleaning)), 2, 3);
    }
    if (!(rc->refCount)) {
      while (rc->collectable) {
	struct refCons* current;
	while ((current = rc->collectable) && 
	       (!compare_and_swap((long int*)(&(rc->collectable)),
				  (long int)current, (long int)(current->nextFree)))) {}
	if (current) {
#ifdef RTJ_DEBUG
	  printf(" 0x%08x (free),", &(current->obj));
#endif
	  if (current->finalize) current->finalize(rc, (void*)(&(current->obj)));
	  RTJ_FREE(current);
	}
      }
    }
  }

  if ((!(rc->scan))||(!(*(rc->scan)))) {
    rc->scan = &(rc->in_use);
  } 

  for(i = 0; (i<10)&&(rc->scan)&&(*(rc->scan))&&(!(rc->refCount)); i++) {
    struct refCons **currentRef, *current;
    
    while ((!((current = (*(currentRef = rc->scan)))->refCount))&&
	   (!compare_and_swap((long int*)(rc->scan), (long int)current,
			      (long int)(current->next)))) {}
    if (current->refCount) {
      compare_and_swap((long int*)(&(rc->scan)), (long int)currentRef,
		       (long int)(&(current->next)));
#ifdef RTJ_DEBUG
      printf(" 0x%08x,", &(current->obj));
#endif
    } else {
      while (!compare_and_swap((long int*)(&(rc->freeList)), 
			       (long int)(current->nextFree = rc->freeList),
			       (long int)current)) {}
#ifdef RTJ_DEBUG
      printf(" 0x%08x (dead),", &(current->obj));
#endif
    }
  }
  printf("\n");  
}

inline void RefCountAllocator_INC(RefCountAllocator rc) {
#ifdef RTJ_DEBUG 
  checkException();
  printf("  RefCountAllocator_INC(0x%08x) was %d\n", rc, rc->refCount);
#endif
  cleanup(rc);
  atomic_add(&(rc->refCount), 1);
  rc->cleaning = 0;
}

inline void RefCountAllocator_DEC(RefCountAllocator rc) {
#ifdef RTJ_DEBUG
  checkException();
  printf("  RefCountAllocator_DEC(0x%08x) was %d\n", rc, rc->refCount);
#endif
  atomic_add(&(rc->refCount), -1);
  cleanup(rc);
}

inline long int RefCountAllocator_INCREF(void* obj) {
  if (!obj) return -1;
#ifdef RTJ_DEBUG
  checkException();
  printf("  RefCountAllocator_INCREF(0x%08x) was %d\n", obj, 
	 REF_HEADER(obj)->refCount);
#endif
  return exchange_and_add(&(REF_HEADER(obj)->refCount), 1);
}

inline void RefCountAllocator_DECREF(RefCountAllocator rc, void* obj) {
  if (!obj) return;
#ifdef RTJ_DEBUG
  checkException();
  printf("  RefCountAllocator_DECREF(0x%08x, 0x%08x) was %d\n", rc, obj,
	 REF_HEADER(obj)->refCount);
#endif
  exchange_and_add(&(REF_HEADER(obj)->refCount), -1);
}

inline void* RefCountAllocator_accumulate(RefCountAllocator rc,
					  void (*visitor)(void* obj,
							  struct accum* acc)) {
  struct accum accu = {NULL, 0};
  struct refCons* current = rc->in_use;
  struct refCons* old;
#ifdef RTJ_DEBUG
  checkException();
  printf("  RefCountAllocator_accumulate(%08x, %08x)\n", rc, visitor);
#endif
  RefCountAllocator_INC(rc);
  current = rc->in_use;
  while (current&&(!(accu.stop))) {
    if (RefCountAllocator_INCREF(&(current->obj))) 
      visitor((void*)(&(current->obj)), &accu);
    current = (old = current)->next;
    RefCountAllocator_DECREF(rc, &(old->obj));
  }
  RefCountAllocator_DEC(rc);
#ifdef RTJ_DEBUG
  checkException();
  printf("\n    Done!\n");
#endif
  return accu.acc;
}

#ifdef WITH_PRECISE_GC
inline void RefCountAllocator_gc_visit(void* obj, struct accum* accum) {
#ifdef RTJ_DEBUG
  checkException();
  printf("0x%08x ", obj);
#endif
  if (((struct oobj*)obj)->claz) trace(((struct oobj*)obj));
}

inline void RefCountAllocator_gc(RefCountAllocator rc) {
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCountAllocator_gc(0x%08x)\n", rc);
#endif
  RefCountAllocator_accumulate(rc, RefCountAllocator_gc_visit);
}
#endif

inline void RefCountAllocator_register_finalizer(void* obj, 
						 void (*finalize)
						 (RefCountAllocator rc, void* obj)) {
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCountAllocator_register_finalizer(0x%08x, 0x%08x)\n", obj, finalize);
#endif
  REF_HEADER(obj)->finalize = finalize;
}

#define error_gc(str, foo) 0
#define print_bitmap(str) 0
#define COLLECT_NOPTR_STATS() 0
#define COLLECT_LRGOBJ_STATS() 0

#define traceFunc(obj) RefCountAllocator_oobj_trace(RefCountAllocator rc, obj)
#define handleFunc(obj) RefCountAllocator_oobj_handle_reference(rc, obj)
#define func_proto traceFunc
#define handle_ref handleFunc
#include "../gc/trace.c"
#undef func_proto
#undef handle_ref
#undef traceFunc
#undef handleFunc

#undef error_gc
#undef print_bitmap
#undef COLLECT_NOPTR_STATS
#undef COLLECT_LRGOBJ_STATS

inline void RefCountAllocator_oobj_finalizer(RefCountAllocator rc, void* obj) {
  struct oobj* oobj = (struct oobj*)obj;
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCountAllocator_oobj_finalizer(0x%08x): %s\n", obj, classNameUnwrap(oobj));
#endif
  if (oobj->claz) RTJ_finalize(oobj);
  RefCountAllocator_oobj_trace(rc, oobj);
}

static jfieldID memoryAreaID = NULL;
static struct oobj* refCountInstance = NULL;

inline void RefCountAllocator_oobj_handle_reference(RefCountAllocator rc,
						    struct oobj** oobj) {
  struct _jobject obj;
#ifdef RTJ_DEBUG
  printf("RefCountAllocator_oobj_handle_reference(0x%08x, 0x%08x)\n", rc, *oobj);
#endif
  obj.obj = *oobj;
  if (!refCountInstance) {  
    JNIEnv* env = FNI_GetJNIEnv();
    jclass refCountAreaClass;
    jmethodID instanceID;
#ifdef RTJ_DEBUG
    checkException();
#endif
    refCountAreaClass = (*env)->FindClass(env, "javax/realtime/RefCountArea");
#ifdef RTJ_DEBUG
    checkException();
#endif
    instanceID = (*env)->GetStaticMethodID(env, refCountAreaClass, "refInstance",
					   "()Ljavax/realtime/RefCountArea;");
#ifdef RTJ_DEBUG
    checkException();
#endif
    refCountInstance = 
      FNI_UNWRAP_MASKED((*env)->CallStaticObjectMethod(env, refCountAreaClass, 
						       instanceID, NULL));
#ifdef RTJ_DEBUG
    checkException();
#endif
    memoryAreaID = (*env)->GetFieldID(env, refCountAreaClass, "memoryArea",
				      "Ljavax/realtime/MemoryArea;");
#ifdef RTJ_DEBUG
    checkException();
#endif
  } 
  if ((!(((ptroff_t)(*oobj))&1)) && refCountInstance &&
      ((FNI_UNWRAP_MASKED(FNI_GetObjectField(FNI_GetJNIEnv(), &obj, memoryAreaID)))
       == refCountInstance)) {
#ifdef RTJ_DEBUG
    checkException();
#endif
    RefCountAllocator_DECREF(rc, *oobj);
  }
}
