#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#ifdef WITH_HEAVY_THREADS
#include <pthread.h>
#endif
#include <stdlib.h>

#ifdef WITH_HEAVY_THREADS
/* lock for inflating locks */
static pthread_mutex_t global_inflate_mutex = PTHREAD_MUTEX_INITIALIZER;
#endif

#ifdef BDW_CONSERVATIVE_GC
static void deflate_object(GC_PTR obj, GC_PTR client_data);
#endif

void FNI_InflateObject(JNIEnv *env, jobject wrapped_obj) {
  struct oobj *obj = FNI_UNWRAP(wrapped_obj);
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_lock(&global_inflate_mutex);
#endif
  /* be careful in case someone inflates this guy while our back is turned */
  if (obj->hashunion.hashcode & 1) {
    /* all data in inflated_oobj is managed manually, so we can use malloc */
    struct inflated_oobj *infl = malloc(sizeof(*infl));
    /* initialize infl */
    infl->hashcode = obj->hashunion.hashcode;
    infl->jni_data = NULL;
    infl->jni_cleanup_func = NULL;
#ifdef WITH_HEAVY_THREADS
    infl->tid = 0;
    infl->nesting_depth = 0;
    pthread_mutex_init(&(infl->mutex), NULL);
    pthread_cond_init(&(infl->cond), NULL);
    pthread_rwlock_init(&(infl->jni_data_lock), NULL);
#endif
    obj->hashunion.inflated = infl;
    assert(FNI_IS_INFLATED(wrapped_obj));
#ifdef BDW_CONSERVATIVE_GC
    /* register finalizer to deallocate inflated_oobj on gc */
    if (GC_base(obj)!=NULL) // skip if this is not a heap-allocated object
	GC_register_finalizer(obj, deflate_object, NULL,
			      &(infl->old_finalizer),
			      &(infl->old_client_data));
#endif
  }
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_unlock(&global_inflate_mutex);
#endif
}

/* here's the deallocation function.  warning: this *may* misbehave if
 * the java finalizer resurrects the object, and then inflation is done
 * on the resurrected object.  we might be able to use a two-pass approach
 * (call the java finalizer and register a new (second-stage) finalizer;
 *  when the second-stage finalizer is invoked, the object is *really*
 *  dead).  we're punting on the potential problem for now. */
#ifdef BDW_CONSERVATIVE_GC
static void deflate_object(GC_PTR obj, GC_PTR client_data) {
    struct oobj *oobj = (struct oobj *) obj;
    struct inflated_oobj *infl = oobj->hashunion.inflated;
    printf("Deflating object %p\n", oobj);
    /* okay, first invoke java finalizer.  afterwards this object
     * *should* be dead, but the java finalizer might resurrect it.
     * we don't behave well in this case. */
    if (infl->old_finalizer)
	(infl->old_finalizer)(obj, infl->old_client_data);
    /* okay, java finalization's taken care of.  Let's clean up the
     * JNI data */
    if (infl->jni_cleanup_func)
	(infl->jni_cleanup_func)(infl->jni_data);
    /* okay deallocate mutexes, etc. */
#ifdef WITH_HEAVY_THREADS
    pthread_mutex_destroy(&(infl->mutex));
    pthread_cond_destroy(&(infl->cond));
#endif
    /* wow, all done. */
    oobj->hashunion.hashcode = infl->hashcode;
    free(infl);
}
#endif

