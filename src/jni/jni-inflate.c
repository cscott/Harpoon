#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#include "flexthread.h"
#include <stdlib.h>

/* lock for inflating locks */
FLEX_MUTEX_DECLARE_STATIC(global_inflate_mutex);

#ifdef BDW_CONSERVATIVE_GC
static void deflate_object(GC_PTR obj, GC_PTR client_data);
#endif

void FNI_InflateObject(JNIEnv *env, jobject wrapped_obj) {
  struct oobj *obj = FNI_UNWRAP(wrapped_obj);
  FLEX_MUTEX_LOCK(&global_inflate_mutex);
  /* be careful in case someone inflates this guy while our back is turned */
  if (obj->hashunion.hashcode & 1) {
    /* all data in inflated_oobj is managed manually, so we can use malloc */
    struct inflated_oobj *infl = 
#if defined(WITH_TRANSACTIONS) && defined(BDW_CONSERVATIVE_GC)
	GC_malloc_uncollectable /* transactions stores version info here */
#else
	malloc
#endif
	(sizeof(*infl));
    /* initialize infl */
    memset(infl, 0, sizeof(*infl));
    infl->hashcode = obj->hashunion.hashcode;
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
    obj->hashunion.inflated = infl;
    assert(FNI_IS_INFLATED(wrapped_obj));
#ifdef BDW_CONSERVATIVE_GC
    /* register finalizer to deallocate inflated_oobj on gc */
    if (GC_base(obj)!=NULL) // skip if this is not a heap-allocated object
        GC_register_finalizer(GC_base(obj), deflate_object,
			      (GC_PTR) ((void*)obj-(void*)GC_base(obj)),
			      &(infl->old_finalizer),
			      &(infl->old_client_data));
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
#ifdef BDW_CONSERVATIVE_GC
static void deflate_object(GC_PTR obj, GC_PTR client_data) {
    struct oobj *oobj = (struct oobj *) ((void*)obj+(ptroff_t)client_data);
    struct inflated_oobj *infl = oobj->hashunion.inflated;
    /*printf("Deflating object %p (clazz %p)\n", oobj, oobj->claz);*/
    /* okay, first invoke java finalizer.  afterwards this object
     * *should* be dead, but the java finalizer might resurrect it.
     * we don't behave well in this case. */
    if (infl->old_finalizer)
	(infl->old_finalizer)(obj, infl->old_client_data);
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
    oobj->hashunion.hashcode = infl->hashcode;
#if defined(WITH_TRANSACTIONS) && defined(BDW_CONSERVATIVE_GC)
    GC_free
#else
    free
#endif
	(infl);
}
#endif

