#include "config.h"
#ifdef WITH_HASHLOCK_SHRINK
# define GC_I_HIDE_POINTERS /* we need HIDE_POINTER from gc.h */
#endif /* WITH_HASHLOCK_SHRINK */

#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#include "flexthread.h"
#include <stdlib.h>
#if defined(WITH_REALTIME_JAVA) || defined(WITH_REALTIME_JAVA_STUBS)
#include "../realtime/RTJconfig.h"
#endif
#include "memstats.h"

#ifdef WITH_REALTIME_JAVA
#include "../realtime/RTJfinalize.h" /* for RTJ_register_finalizer */
#endif

#ifdef WITH_HASHLOCK_SHRINK
/* hashtable mapping obj to inflated obj */
# ifndef BDW_CONSERVATIVE_GC
#  error WITH_HASHLOCK_SHRINK only works with BDW_CONSERVATIVE_GC
# endif
# define MAKE_POINTER_VERSION
# define TYPE struct inflated_oobj *
# define TABLE infl_table
# define TABLE_ELEMENT infl_table_el
# define GET infl_table_get
# define SET infl_table_set
# define REMOVE infl_table_remove
# include "hashimpl.h"
static char *INFL_LOCK="lock";
struct inflated_oobj *FNI_infl_lookup(struct oobj *oobj) {
  return infl_table_get(INFL_LOCK, oobj, NULL);
}
#endif /* WITH_HASHLOCK_SHRINK */

/* lock for inflating locks */
FLEX_MUTEX_DECLARE_STATIC(global_inflate_mutex);

#ifdef WITH_REALTIME_JAVA
static void deflate_object(void* obj, void* client_data);
#elif defined(BDW_CONSERVATIVE_GC)
static void deflate_object(GC_PTR obj, GC_PTR client_data);
#elif defined(WITH_PRECISE_GC)
static void deflate_object(struct oobj *obj, ptroff_t client_data);
#endif

void FNI_InflateObject(JNIEnv *env, jobject wrapped_obj) {
  struct oobj *obj = FNI_UNWRAP_MASKED(wrapped_obj);
  FLEX_MUTEX_LOCK(&global_inflate_mutex);
  /* be careful in case someone inflates this guy while our back is turned */
  if (!FNI_IS_INFLATED(wrapped_obj)) {
    /* all data in inflated_oobj is managed manually, so we can use malloc */
    struct inflated_oobj *infl = 
#if defined(WITH_TRANSACTIONS) && defined(BDW_CONSERVATIVE_GC)
#ifdef WITH_GC_STATS
      GC_malloc_uncollectable_stats
#else
      GC_malloc_uncollectable /* transactions stores version info here */
#endif
#else
	malloc
#endif
      (sizeof(*infl));
#if (!defined(WITH_TRANSACTIONS)) || (!defined(BDW_CONSERVATIVE_GC))
    INCREMENT_MEM_STATS(sizeof(*infl));
#endif
    /* initialize infl */
    memset(infl, 0, sizeof(*infl));
#ifndef WITH_HASHLOCK_SHRINK
    infl->hashcode = HASHCODE_MASK(obj->hashunion.hashcode);
#endif /* !WITH_HASHLOCK_SHRINK */
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
# ifdef ERROR_CHECKING_LOCKS
    /* error checking locks are slower, but catch more bugs (maybe) */
    { pthread_mutexattr_t attr; pthread_mutexattr_init(&attr);
      pthread_mutexattr_setkind_np(&attr, PTHREAD_MUTEX_ERRORCHECK_NP);
      pthread_mutex_init(&(infl->mutex), &attr);
      pthread_mutexattr_destroy(&attr);
    }
# else /* !ERROR_CHECKING_LOCKS */
    pthread_mutex_init(&(infl->mutex), NULL);
# endif /* ERROR_CHECKING_LOCKS || !ERROR_CHECKING_LOCKS */
    pthread_cond_init(&(infl->cond), NULL);
    pthread_rwlock_init(&(infl->jni_data_lock), NULL);
#endif
#ifndef WITH_HASHLOCK_SHRINK
#ifndef WITH_DYNAMIC_WB
    obj->hashunion.inflated = infl;
#else
    obj->hashunion.inflated = (ptroff_t) infl | (obj->hashunion.hashcode & 2);
#endif
#else
    infl_table_set(INFL_LOCK, obj, infl, NULL);
#endif /* WITH_HASHLOCK_SHRINK */
    assert(FNI_IS_INFLATED(wrapped_obj));
#ifdef WITH_PRECISE_GC 
#if defined(WITH_REALTIME_JAVA) && defined(WITH_NOHEAP_SUPPORT)
    /* Can't inflate a heap reference in a NoHeapRealtimeThread */
    assert((!(((ptroff_t)FNI_UNWRAP(wrapped_obj))&1))||
	   (!((struct FNI_Thread_State*)env)->noheap));
    if (((ptroff_t)FNI_UNWRAP(wrapped_obj))&1)  /* register only if in heap */
#endif
      precise_register_inflated_obj(obj, deflate_object);
#elif defined(BDW_CONSERVATIVE_GC)
    /* register finalizer to deallocate inflated_oobj on gc */
    if (GC_base(obj)!=NULL) {// skip if this is not a heap-allocated object
        GC_register_finalizer_no_order(GC_base(obj), deflate_object,
			      (GC_PTR) ((void*)obj-(void*)GC_base(obj)),
			      &(infl->old_finalizer),
			      &(infl->old_client_data));
    } 
#endif
#ifdef WITH_REALTIME_JAVA
    RTJ_register_finalizer(wrapped_obj, deflate_object); 
#endif
  }
  FLEX_MUTEX_UNLOCK(&global_inflate_mutex);
}

/* here's the deallocation function.  warning: this *may* misbehave if
 * the java finalizer resurrects the object, and then inflation is done
 * on the resurrected object.  we might be able to use a two-pass approach
 * (call the java finalizer and register a new (second-stage) finalizer;
 *  when the second-stage finalizer is invoked, the object is *really*
 *  dead).  we're punting on the potential problem for now. */
#ifdef WITH_REALTIME_JAVA
static void deflate_object(void* obj, void* client_data) {
#elif defined(BDW_CONSERVATIVE_GC) 
static void deflate_object(GC_PTR obj, GC_PTR client_data) {
#elif defined(WITH_PRECISE_GC)
static void deflate_object(struct oobj *obj, ptroff_t client_data) {
#endif
#if defined(BDW_CONSERVATIVE_GC) || defined(WITH_PRECISE_GC) || defined(WITH_REALTIME_JAVA)
    struct oobj *oobj = (struct oobj *) ((void*)obj+(ptroff_t)client_data);
#ifndef WITH_HASHLOCK_SHRINK
    struct inflated_oobj *infl = INFLATED_MASK(oobj->hashunion.inflated);
#else
    struct inflated_oobj *infl = infl_table_get(INFL_LOCK, oobj, NULL);
#endif
    /*printf("Deflating object %p (clazz %p)\n", oobj, FNI_CLAZ(oobj));*/
    /* okay, first invoke java finalizer.  afterwards this object
     * *should* be dead, but the java finalizer might resurrect it.
     * we don't behave well in this case. */
#if defined(BDW_CONSERVATIVE_GC) && !defined(WITH_REALTIME_JAVA)
        if (infl->old_finalizer)
	 (infl->old_finalizer)(obj, infl->old_client_data);
#endif
    /* okay, java finalization's taken care of.  Let's clean up the
     * JNI data */
    if (infl->jni_cleanup_func)
	(infl->jni_cleanup_func)(infl->jni_data);
    infl->jni_data = NULL; infl->jni_cleanup_func = NULL;
    /* release clustered heap */
#ifdef WITH_CLUSTERED_HEAPS
    /* call release function if non-null */
    if (infl->heap_release)
      (infl->heap_release)(infl->heap);
    infl->heap = NULL; infl->heap_release = NULL;
#endif
    /* okay deallocate mutexes, etc. */
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
    pthread_mutex_destroy(&(infl->mutex));
    pthread_cond_destroy(&(infl->cond));
#endif
    /* wow, all done. */
#ifndef WITH_HASHLOCK_SHRINK
#ifndef WITH_DYNAMIC_WB
    oobj->hashunion.hashcode = infl->hashcode;
#else
    oobj->hashunion.hashcode = 
      infl->hashcode | ((ptroff_t) oobj->hashunion.inflated & 2);
#endif
#else
    infl_table_remove(INFL_LOCK, oobj);
#endif /* WITH_HASHLOCK_SHRINK */
#if defined(WITH_TRANSACTIONS) && defined(BDW_CONSERVATIVE_GC)
    GC_free
#else
    free
#endif
	(infl);
#if (!defined(WITH_TRANSACTIONS)) || (!defined(BDW_CONSERVATIVE_GC))
    DECREMENT_MEM_STATS(sizeof(*infl));
#endif
}
#endif

