#include "jni.h"
#include "jni-private.h"
#include "jni-gc.h"
#include "config.h"
#include <assert.h>
#ifdef WITH_MARKSWEEP_GC
# include "free_list.h"
#endif

FLEX_MUTEX_DECLARE_STATIC(times_called_mutex);
static int times_called = 0;
#ifdef WITH_MARKSWEEP_GC
static int eliminatable = 0;
static int nulls = 0;
#endif

/*
 * Class:     harpoon_Runtime_PreciseGC_WriteBarrier
 * Method:    storeCheck
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_harpoon_Runtime_PreciseGC_WriteBarrier_storeCheck
  (JNIEnv *env, jclass cls, jobject obj) {
#ifdef WITH_STATS_GC
  FLEX_MUTEX_LOCK(&times_called_mutex);
  times_called++;
  FLEX_MUTEX_UNLOCK(&times_called_mutex);
#ifdef WITH_MARKSWEEP_GC
  assert(0 /* should not get here */);
#endif
#endif
#ifdef WITH_GENERATIONAL_GC
  generational_write_barrier(obj);
#endif
}


/*
 * Class:     harpoon_Runtime_PreciseGC_WriteBarrier
 * Method:    fsc
 * Signature: (Ljava/lang/Object;Ljava/lang/reflect/Field;Ljava/lang/Object;I)V
 */
JNIEXPORT void JNICALL Java_harpoon_Runtime_PreciseGC_WriteBarrier_fsc
  (JNIEnv *env, jclass cls, jobject obj, jobject field, jobject val, jint id)
{
  void *ptr = 
    FNI_UNWRAP(obj)->field_start + FNI_GetFieldInfo(field)->fieldID->offset;
#ifdef WITH_STATS_GC
  FLEX_MUTEX_LOCK(&times_called_mutex);
  times_called++;
#ifdef WITH_MARKSWEEP_GC
  {
    struct block *from, *to;
    from = (void *)FNI_UNWRAP(obj) - BLOCK_HEADER_SIZE;
    if (FNI_UNWRAP(val) != NULL) {
      to = (void *)FNI_UNWRAP(val) - BLOCK_HEADER_SIZE;
      //printf("from: %d, to: %d\n", from->time, to->time);
      if (from->time > to->time)
	eliminatable++;
    } else nulls++;
  }
#endif
  FLEX_MUTEX_UNLOCK(&times_called_mutex);
#endif
#ifdef WITH_GENERATIONAL_GC
  generational_write_barrier(ptr);
#endif
}


/*
 * Class:     harpoon_Runtime_PreciseGC_WriteBarrier
 * Method:    asc
 * Signature: (Ljava/lang/Object;ILjava/lang/Object;I)V
 */
JNIEXPORT void JNICALL Java_harpoon_Runtime_PreciseGC_WriteBarrier_asc 
  (JNIEnv *env, jclass cls, jobject obj, jint index, jobject val, jint id) 
{
  void *ptr = 
    ((struct aarray *)FNI_UNWRAP(obj))->element_start + index*SIZEOF_VOID_P;
#ifdef WITH_STATS_GC
  FLEX_MUTEX_LOCK(&times_called_mutex);
  times_called++;
#ifdef WITH_MARKSWEEP_GC
  {
    struct block *from, *to;
    from = (void *)FNI_UNWRAP(obj) - BLOCK_HEADER_SIZE;
    if (FNI_UNWRAP(val) != NULL) {
      to = (void *)FNI_UNWRAP(val) - BLOCK_HEADER_SIZE;
      //printf("from: %d, to: %d\n", from->time, to->time);
      if (from->time < to->time)
	eliminatable++;
    } else nulls++;
  }
#endif
  FLEX_MUTEX_UNLOCK(&times_called_mutex);
#endif
#ifdef WITH_GENERATIONAL_GC
  generational_write_barrier(ptr);
#endif
}


void print_write_barrier_stats()
{
#ifdef WITH_STATS_GC
  printf("Write barrier called %d times.\n", times_called);
#ifdef WITH_MARKSWEEP_GC
  printf("Write barrier avoidable %d times.\n", eliminatable);
  printf("Write barrier nulls %d times.\n", nulls);
#endif
#endif
}

