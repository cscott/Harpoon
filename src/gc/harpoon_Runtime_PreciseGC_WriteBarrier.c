#include "jni.h"
#include "jni-private.h"
#include "jni-gc.h"
#include "config.h"
#include <assert.h>
#ifdef WITH_MARKSWEEP_GC
# include "free_list.h"
#elif WITH_DYNAMIC_WB
# ifndef WITH_GENERATIONAL_GC
#   error dynamic write barriers require generational GC
# endif
# include "generational.h"
#endif

FLEX_MUTEX_DECLARE_STATIC(times_called_mutex);

#ifdef WITH_STATS_GC
#ifdef WITH_MARKSWEEP_GC
extern int num_write_barriers;
static int *times_called_array;
static int *old_to_young_array;
static int *not_old_to_young_array;
static int *null_assign_array;
void collect_stats(jobject obj, jobject val, jint id);
#elif defined(WITH_DYNAMIC_WB)
extern int num_write_barriers;
static int *times_called_array;
static int *old_to_young_0_array;
static int *old_to_young_1_array;
static int *old_to_other_0_array;
static int *old_to_other_1_array;
static int *young_to_any_0_array;
static int *young_to_any_1_array;
void collect_stats(jobject obj, jobject val, jint id);
#else // !WITH_MARKSWEEP_GC && !WITH_DYNAMIC_WB
static int times_called = 0;
#endif
#endif // WITH_STATS_GC

/*
 * Class:     harpoon_Runtime_PreciseGC_WriteBarrier
 * Method:    storeCheck
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_harpoon_Runtime_PreciseGC_WriteBarrier_storeCheck
  (JNIEnv *env, jclass cls, jobject obj) {
#ifdef WITH_STATS_GC
  FLEX_MUTEX_LOCK(&times_called_mutex);
# if defined(WITH_MARKSWEEP_GC) || defined(WITH_DYNAMIC_WB)
  assert(0 /* should not get here */);
# else
  times_called++;
# endif
  FLEX_MUTEX_UNLOCK(&times_called_mutex);
#endif // WITH_STATS_GC
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
    void *ptr = (void *)((ptroff_t) FNI_UNWRAP_MASKED(obj) + 
	FNI_GetFieldInfo(field)->fieldID->offset);
#ifdef WITH_STATS_GC
  FLEX_MUTEX_LOCK(&times_called_mutex);
  collect_stats(obj, val, id);
  FLEX_MUTEX_UNLOCK(&times_called_mutex);
#endif // WITH_STATS_GC
#ifdef WITH_GENERATIONAL_GC
  generational_write_barrier(ptr);
#endif // WITH_GENERATIONAL_GC
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
      ((char *) &((struct aarray *)FNI_UNWRAP_MASKED(obj))->element_start) + 
      index*SIZEOF_VOID_P;
#ifdef WITH_STATS_GC
  FLEX_MUTEX_LOCK(&times_called_mutex);
  collect_stats(obj, val, id);
  FLEX_MUTEX_UNLOCK(&times_called_mutex);
#endif // WITH_STATS_GC
#ifdef WITH_GENERATIONAL_GC
  generational_write_barrier(ptr);
#endif // WITH_GENERATIONAL_GC
}

#ifdef WITH_DYNAMIC_WB
#ifdef WITH_THREADS
# error no thread support in clearBit
#endif
/*
 * Class:     harpoon_Runtime_PreciseGC_WriteBarrier
 * Method:    clearBit
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_harpoon_Runtime_PreciseGC_WriteBarrier_clearBit
    (JNIEnv *env, jclass cls, jobject obj)
{
  DYNAMIC_WB_CLEAR(FNI_UNWRAP_MASKED(obj));
}
#endif


#ifdef WITH_STATS_GC
void collect_stats(jobject obj, jobject val, jint id)
#ifdef WITH_MARKSWEEP_GC
{
  struct block *from, *to;
  // check that we're not writing out of bounds
  assert(id >= 0 && id < num_write_barriers);
  times_called_array[id]++;
  // get the block that we're writing into
  from = (void *)FNI_UNWRAP_MASKED(obj) - BLOCK_HEADER_SIZE;
  if (FNI_UNWRAP_MASKED(val) != NULL) {
    // compare with the ptr we're writing
    to = (void *)FNI_UNWRAP_MASKED(val) - BLOCK_HEADER_SIZE;
    if (from->time < to->time)
      old_to_young_array[id]++;
    else
      not_old_to_young_array[id]++;
  } else {
    // for assignments to null,
    // we can't get a time-stamp
    null_assign_array[id]++;
  }
}
#elif defined(WITH_DYNAMIC_WB)
{
  jobject_unwrapped from = FNI_UNWRAP_MASKED(obj);
  jobject_unwrapped to = FNI_UNWRAP_MASKED(val);
  // check that we're not writing out of bounds
  assert(id >= 0 && id < num_write_barriers);
  times_called_array[id]++;
  // check if creating a reference from object in 
  // old generation to object in young generation
  if (in_old_gen(from)) {
      if (to != NULL && in_young_gen(to)) {
	  if (DYNAMIC_WB_ON(from))
	      old_to_young_1_array[id]++;
	  else
	      old_to_young_0_array[id]++;
      } else {
	  assert(to == NULL || in_old_gen(to));
	  if (DYNAMIC_WB_ON(from))
	      old_to_other_1_array[id]++;
	  else
	      old_to_other_0_array[id]++;
      }
  } else { 
      assert((in_young_gen(from) && 
	      (to == NULL || in_old_gen(to) || in_young_gen(to))));
      if (DYNAMIC_WB_ON(from))
	  young_to_any_1_array[id]++;
      else
	  young_to_any_0_array[id]++;
  }
}
#endif

void init_statistics()
#ifdef WITH_MARKSWEEP_GC
{
  times_called_array = (int*) calloc(num_write_barriers, sizeof(int));
  old_to_young_array = (int*) calloc(num_write_barriers, sizeof(int));
  not_old_to_young_array = (int*) calloc(num_write_barriers, sizeof(int)); 
  null_assign_array = (int*) calloc(num_write_barriers, sizeof(int));
}
#elif defined(WITH_DYNAMIC_WB)
{
  times_called_array = (int*) calloc(num_write_barriers, sizeof(int));
  old_to_young_0_array = (int*) calloc(num_write_barriers, sizeof(int));
  old_to_young_1_array = (int*) calloc(num_write_barriers, sizeof(int));
  old_to_other_0_array = (int*) calloc(num_write_barriers, sizeof(int));
  old_to_other_1_array = (int*) calloc(num_write_barriers, sizeof(int));
  young_to_any_0_array = (int*) calloc(num_write_barriers, sizeof(int)); 
  young_to_any_1_array = (int*) calloc(num_write_barriers, sizeof(int)); 
}
#endif

void print_write_barrier_stats()
#ifdef WITH_MARKSWEEP_GC
{
  int i;
  int top_10_overall[10] = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
  int top_10_removable[10] = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
  int total_times_called = 0;
  int total_old_to_young = 0;
  int total_not_old_to_young = 0;
  int total_null_assigns = 0;
  int total_transformable = 0;
  int num_transformable = 0;
  int total_untransformable = 0;
  int num_not_transformable = 0;
  int total_removable = 0;
  int num_removable = 0;
  
  printf("\nWRITE BARRIERS CALLED\n");
  for(i = 0; i < num_write_barriers; i++)
    {
      // check math
      assert(times_called_array[i] == old_to_young_array[i] + 
	     not_old_to_young_array[i] + null_assigns_array[i]);
      
      total_times_called += times_called_array[i];
      total_old_to_young += old_to_young_array[i];
      total_not_old_to_young += not_old_to_young_array[i];
      total_null_assigns += null_assigns_array[i];

      if (times_called_array[i] == 0) 
        continue;

      printf("ID %9d\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", 
	     i, not_old_to_young_array[i], old_to_young_array[i], 
	     null_assigns_array[i], times_called_array[i]);

      // see if part of top 10 overall
      {
        int j, shift = i;
	  
	for(j = 0; j < 10; j++)
	  {
	    int curr = top_10_overall[j];
	    // if empty, fill in
	    if (curr == -1)
	      {
	        top_10_overall[j] = shift;
		break;
	      }
	    if (times_called_array[curr] < times_called_array[shift])
              {
	        top_10_overall[j] = shift;
		shift = curr;
	      }
	  }
      }

      // some old to young write barriers
      if (old_to_young_array[i] != 0) {
	if (not_old_to_young_array[i] == 0) {
	  // may be transformable
	  total_transformable += times_called_array[i];
	  num_transformable++;
	} else {
	  // not transformable
	  total_untransformable += times_called_array[i];
	  num_not_transformable++;
	}
      } else {
	// no old to young write barriers
	total_removable += times_called_array[i];
	num_removable++;
      
	// see if part of top 10 removable
	{
	  int j, shift = i;

	  for(j = 0; j < 10; j++)
	  {
	    int curr = top_10_removable[j];
	    // if empty, fill in
	    if (curr == -1)
	    {
	      top_10_removable[j] = shift;
	      break;
	    }
	    if (not_old_to_young_array[curr] < not_old_to_young_array[shift])
	    {
	      top_10_removable[j] = shift;
	      shift = curr;
	    }
	  }
	}
      }
    }

  printf("-----------------------------------------");
  printf("--------------------------------------\n");
  printf("   %9d\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", 
	 num_write_barriers, total_not_old_to_young, total_old_to_young,
	 total_null_assigns, total_times_called);

  {
    int top_10_not_old_to_young = 0;
    int top_10_old_to_young = 0;
    int top_10_null_assigns = 0;
    int top_10_times_called = 0;

    printf("\nTOP 10 TOTAL WRITE BARRIERS\n");
    for (i = 0; i < 10; i++)
    {
      int j = top_10_overall[i];
      if (j != -1)
      {
	top_10_not_old_to_young += not_old_to_young_array[j];
	top_10_old_to_young += old_to_young_array[j];
	top_10_null_assigns += null_assigns_array[j];
	top_10_times_called += times_called_array[j];
      
	printf("ID %9d\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n",
	       j, not_old_to_young_array[j], old_to_young_array[j], 
	       null_assigns_array[j], times_called_array[j]);
      }
    }

    printf("-----------------------------------------");
    printf("--------------------------------------\n");
    printf("\t\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", 
	   top_10_not_old_to_young, top_10_old_to_young, top_10_null_assigns,
	   top_10_times_called);
  }
  {
    int top_10_removable_not_old_to_young = 0;
    int top_10_removable_old_to_young = 0;
    int top_10_removable_null_assigns = 0;
    int top_10_removable_times_called = 0;

    printf("\nTOP 10 REMOVABLE WRITE BARRIERS\n");
    for(i = 0; i < 10; i++)
    {
      int j = top_10_removable[i];
      if (j != -1)
      {
	top_10_removable_not_old_to_young += not_old_to_young_array[j];
	top_10_removable_old_to_young += old_to_young_array[j];
	top_10_removable_null_assigns += null_assigns_array[j];
        top_10_removable_times_called += times_called_array[j];
	printf("ID %9d\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n",
	       j, not_old_to_young_array[j], old_to_young_array[j], 
	       null_assigns_array[j], times_called_array[j]);
      }
    }
    printf("-----------------------------------------");
    printf("--------------------------------------\n");
    printf("\t\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", 
	   top_10_removable_not_old_to_young, top_10_removable_old_to_young,
	   top_10_removable_null_assigns, top_10_removable_times_called);
  }

  assert(num_write_barriers == num_removable + num_transformable +
	 num_untransformable);

  printf("\nWRITE BARRIERS THAT MAY BE REMOVABLE:\t%8d of %8d (%4f)\n",
	 num_removable, num_write_barriers, 
	 (float)num_removable/(float)num_write_barriers);

  assert(total_times_called == total_removable + total_transformable +
	 total_untransformable);

  printf("IF ALL REMOVED, CALLS ELIMINATED:\t%8d of %8d (%4f)\n",
	 total_removable, total_times_called,
	 (float)total_removable/(float)total_times_called);

  printf("IF TOP 10 REMOVED, CALLS ELIMINATED:\t%8d of %8d (%4f)\n\n",
	 top_10_removable, total_times_called,
	 (float)top_10_removable/(float)total_times_called);

  printf("\tREMOVABLE\tTRANSFORMABLE\tUNTRANSFORMABLE\n");
  printf("EXCEL\t%d\t%d\t%d\n", total_removable, total_transformable,
	 total_untransformable);
}
#elif defined(WITH_DYNAMIC_WB)
{
  int i;
  int total_times_called = 0;
  int total_old_to_young_0 = 0;
  int total_old_to_young_1 = 0;
  int total_old_to_other_0 = 0;
  int total_old_to_other_1 = 0;
  int total_young_to_any_0 = 0;
  int total_young_to_any_1 = 0;

  printf("   ID|       OLD-TO-YOUNG|       OLD-TO-OTHER|       YOUNG-TO-ANY|    TOTAL (%)\n");
  printf("     |    CLEAR|      SET|    CLEAR|      SET|    CLEAR|      SET|\n");

  for (i = 0; i < num_write_barriers; i++) {
    // check math
    assert(times_called_array[i] == old_to_young_0_array[i] +
	   old_to_young_1_array[i] + old_to_other_0_array[i] +
	   old_to_other_1_array[i] + young_to_any_0_array[i] +
	   young_to_any_1_array[i]);
    
    total_times_called += times_called_array[i];
    total_old_to_young_0 += old_to_young_0_array[i];
    total_old_to_young_1 += old_to_young_1_array[i];
    total_old_to_other_0 += old_to_other_0_array[i];
    total_old_to_other_1 += old_to_other_1_array[i];
    total_young_to_any_0 += young_to_any_0_array[i];
    total_young_to_any_1 += young_to_any_1_array[i];
  }

  for (i = 0; i < num_write_barriers; i++)
    if (times_called_array[i] != 0)
      printf("%5d|%9d|%9d|%9d|%9d|%9d|%9d|%9d (%3.1lf\%)\n", i, 
	     old_to_young_0_array[i], old_to_young_1_array[i], 
	     old_to_other_0_array[i], old_to_other_1_array[i],
	     young_to_any_0_array[i], young_to_any_1_array[i],
	     times_called_array[i],
	     100*(double)times_called_array[i]/(double)total_times_called);

  printf("----------------------------------------");
  printf("----------------------------------------\n");
  printf("%5d|%9d|%9d|%9d|%9d|%9d|%9d|%9d (%3.1lf\%)\n", num_write_barriers, 
	 total_old_to_young_0, total_old_to_young_1, 
	 total_old_to_other_0, total_old_to_other_1,
	 total_young_to_any_0, total_young_to_any_1, total_times_called,
	 100*(double)total_times_called/(double)total_times_called);

  printf("\nEXCEL\t REMOVED+\t REMOVED-\tREMAINING+\tREMAINING-\n");
  printf("EXCEL\t%9d\t%9d\t%9d\t%9d\n", total_young_to_any_1, 
	 (total_old_to_young_1+total_old_to_other_1),
	 (total_old_to_young_0+total_old_to_other_0), total_young_to_any_0);
  printf("EXCEL\t    %4.1lf\%\t    %4.1lf\%\t    %4.1lf\%\t    %4.1lf\%\n", 
	 100*(double)total_young_to_any_1/(double)total_times_called, 
	 100*(double)(total_old_to_young_1+total_old_to_other_1)/(double)total_times_called, 
	 100*(double)(total_old_to_young_0+total_old_to_other_0)/(double)total_times_called, 
	 100*(double)total_young_to_any_0/total_times_called);
}
#else
{
  printf("Write barrier called %d times.\n", times_called);
}
#endif

#endif // WITH_STATS_GC
