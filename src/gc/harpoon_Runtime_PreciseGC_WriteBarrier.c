#include "jni.h"
#include "jni-private.h"
#include "jni-gc.h"
#include "config.h"
#include <assert.h>
#ifdef WITH_MARKSWEEP_GC
# include "free_list.h"
#endif

FLEX_MUTEX_DECLARE_STATIC(times_called_mutex);
#ifdef WITH_MARKSWEEP_GC
static int *total_barriers;
static int *dont_need;
static int *need;
static int *assigned_null;
extern int num_write_barriers;
#else
static int times_called = 0;
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
# ifdef WITH_MARKSWEEP_GC
  assert(0 /* should not get here */);
# else
  times_called++;
# endif
  FLEX_MUTEX_UNLOCK(&times_called_mutex);
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
#ifdef WITH_MARKSWEEP_GC
  {
    struct block *from, *to;
    // check that we're not writing out of bounds
    assert(id >= 0 && id < num_write_barriers);
    total_barriers[id]++;
    // get the block that we're writing into
    from = (void *)FNI_UNWRAP(obj) - BLOCK_HEADER_SIZE;
    if (FNI_UNWRAP(val) != NULL)
      {
	// compare with the ptr we're writing
	to = (void *)FNI_UNWRAP(val) - BLOCK_HEADER_SIZE;
	if (from->time >= to->time)
	  {
	    //printf("FSC from: %d vs to: %d\n", from->time, to->time); 
	    dont_need[id]++;
	  }
	else
	  need[id]++;
      }
    else
      // for assignments to null,
      // we can't get a time-stamp
      assigned_null[id]++;
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
  total_barriers[id]++;
#ifdef WITH_MARKSWEEP_GC
  {
    struct block *from, *to;
    assert(id >= 0 && id < num_write_barriers);
    from = (void *)FNI_UNWRAP(obj) - BLOCK_HEADER_SIZE;
    if (FNI_UNWRAP(val) != NULL)
      {
	to = (void *)FNI_UNWRAP(val) - BLOCK_HEADER_SIZE;
	if (from->time >= to->time)
	  {
	    //printf("ASC from: %d vs to: %d\n", from->time, to->time); 
	    dont_need[id]++;
	  }
	else
	  need[id]++;
      } 
    else 
      assigned_null[id]++;
  }
#endif
  FLEX_MUTEX_UNLOCK(&times_called_mutex);
#endif
#ifdef WITH_GENERATIONAL_GC
  generational_write_barrier(ptr);
#endif
}


#if defined(WITH_MARKSWEEP_GC) && defined(WITH_STATS_GC)
void init_statistics()
{
  int i;
  // initialize arrays
  dont_need = (int*) malloc(num_write_barriers*sizeof(int)); 
  need = (int*) malloc(num_write_barriers*sizeof(int));
  assigned_null = (int*) malloc(num_write_barriers*sizeof(int));
  total_barriers = (int*) malloc(num_write_barriers*sizeof(int));

  for(i = 0; i < num_write_barriers; i++)
    {
      dont_need[i] = 0;
      need[i] = 0;
      assigned_null[i] = 0;
      total_barriers[i] = 0;
    }
}
#endif


#ifdef WITH_STATS_GC
void print_write_barrier_stats()
{
#ifdef WITH_MARKSWEEP_GC
  int i;
  int top_10_overall[10] = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
  int top_10_elim[10] = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
  int total = 0, needed = 0, not_needed = 0, nulls = 0;
  int removable = 0, stransformable = 0;
  int transformable = 0, remaining = 0;
  int top_10_removed = 0, top_10_total = 0;
  
  printf("\n");
  printf("WRITE BARRIERS CALLED\n");
  for(i = 0; i < num_write_barriers; i++)
    {
      int j, shift;
      
      // check math
      assert(total_barriers[i] == 
	     dont_need[i] + need[i] + assigned_null[i]);

      total += total_barriers[i];
      not_needed += dont_need[i];
      needed += need[i];
      nulls += assigned_null[i];

      if (total_barriers[i] == 0) 
	continue;

      printf("ID %9d\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", i,
	     dont_need[i], need[i], assigned_null[i], total_barriers[i]);

      shift = i;

      // see if part of top 10 overall
      for(j = 0; j < 10; j++)
	{
	  int curr = top_10_overall[j];
	  // if empty, fill in
	  if (curr == -1)
	    {
	      top_10_overall[j] = shift;
	      break;
	    }
	  if (total_barriers[curr] < total_barriers[shift])
	    {
	      top_10_overall[j] = shift;
	      shift = curr;
	    }
	}

      // some needed write barriers
      if (need[i] != 0) {
	if (dont_need[i] == 0) {
	  // may be transformable
	  transformable += total_barriers[i];
	  stransformable ++;
	} else {
	  // not transformable
	  remaining += total_barriers[i];
	}
	continue;
      }

      // no needed write barriers
      removable++;
	  
      shift = i;

      // see if part of top 10 removable
      for(j = 0; j < 10; j++)
	{
	  int curr = top_10_elim[j];
	  // if empty, fill in
	  if (curr == -1)
	    {
	      top_10_elim[j] = shift;
	      break;
	    }
	  if (dont_need[curr] < dont_need[shift])
	    {
	      top_10_elim[j] = shift;
	      shift = curr;
	    }
	}
    }
  printf("-----------------------------------------");
  printf("--------------------------------------\n");
  printf("   %9d\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", 
	 num_write_barriers, not_needed, needed, nulls, total);

  not_needed = 0;
  needed = 0;
  nulls = 0;

  printf("\n");
  printf("TOP 10 TOTAL WRITE BARRIERS\n");
  for(i = 0; i < 10; i++)
    {
      int j = top_10_overall[i];
      if (j == -1)
	break;
      top_10_total += total_barriers[j];
      not_needed += dont_need[j];
      needed += need[j];
      nulls += assigned_null[j];
      
      printf("ID %9d\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", j,
	     dont_need[j], need[j], assigned_null[j], total_barriers[j]);
    }
  printf("-----------------------------------------");
  printf("--------------------------------------\n");
  printf("\t\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", 
	 not_needed, needed, nulls, top_10_total);

  printf("\n");
  printf("TOP 10 REMOVABLE WRITE BARRIERS\n");
  for(i = 0; i < 10; i++)
    {
      int j = top_10_elim[i];
      if (j == -1)
	break;
      top_10_removed += total_barriers[j];
      printf("ID %9d\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", j,
	     dont_need[j], need[j], assigned_null[j], total_barriers[j]);
    }
  printf("-----------------------------------------");
  printf("--------------------------------------\n");
  printf("\t\tUNNEC %9d\tREQD %9d\tNULL %9d\tTOTAL %9d\n", 
	 top_10_removed, 0, 0, top_10_removed);

  printf("\n");
  printf("WRITE BARRIERS THAT MAY BE REMOVABLE:\t%8d of %8d (%4f)\n",
	 removable, num_write_barriers, 
	 (float)removable/(float)num_write_barriers);
  printf("IF ALL REMOVED, CALLS ELIMINATED:\t%8d of %8d (%4f)\n",
	 total - remaining - transformable, total, 
	 (float)(total-remaining-transformable)/(float)total);
  printf("IF TOP 10 REMOVED, CALLS ELIMINATED:\t%8d of %8d (%4f)\n",
	 top_10_removed, total, (float)top_10_removed/(float)total);
  printf("\n");
  printf("DYNAMIC REMOVABLE     = %d\n", total - remaining - transformable);
  printf("DYNAMIC TRANSFORMABLE = %d\n", transformable);
  printf("DYNAMIC REMAINING     = %d\n", remaining);
  printf("STATIC REMOVABLE     = %d\n", removable);
  printf("STATIC TRANSFORMABLE = %d\n", stransformable);
  printf("STATIC REMAINING     = %d\n", 
	 num_write_barriers - removable - stransformable);
#else
  printf("Write barrier called %d times.\n", times_called);
#endif
}
#endif

