#include <assert.h>
#include <unistd.h>
#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
# include "gc.h"
# include "gc_typed.h"
#endif
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"
#include "precise_gc.h"
#include "gc-data.h"
#include "system_page_size.h"
#ifdef WITH_THREADED_GC
# include "flexthread.h"
# include "jni-gcthreads.h"
#endif

#ifdef WITH_PRECISE_GC

static int print_gc_index = 0;
static int check_gc_index = 1;

size_t SYSTEM_PAGE_SIZE = 0;
size_t PAGE_MASK = 0;

#ifdef HAVE_STACK_TRACE_FUNCTIONS
# include "asm/stack.h" /* snarf in the stack trace functions. */
#endif /* HAVE_STACK_TRACE_FUNCTIONS */


#ifdef DEBUG_GC
/* effects: verifies the integrity of the given object w/ simple checks */
void debug_verify_object (jobject_unwrapped obj)
{
  // check that this is probably a pointer
  assert(!((ptroff_t)obj & 1));

  // check that the claz ptr is correct
  assert(CLAZ_OKAY(obj));
}
#endif


void precise_gc_init()
{
  // find out the system page size and pre-calculate some constants
  SYSTEM_PAGE_SIZE = getpagesize();
  PAGE_MASK = SYSTEM_PAGE_SIZE - 1;

  // do initialization for gc data
  gc_data_init();

  // do initialization for the specific collector
  internal_gc_init();

#ifdef WITH_STATS_GC
  // register exit function
  atexit(precise_gc_print_stats);
#endif
}


/* effects: registers inflated obj so its resources are freed after GC */
void precise_register_inflated_obj(jobject_unwrapped obj,
				   void (*deflate_fcn)(struct oobj *obj, 
						       ptroff_t client_data))
{
  INFLATED_MASK(obj->hashunion.inflated)->precise_deflate_obj = deflate_fcn;
  internal_register_inflated_obj(obj);
}

/* effects: forces garbage collection to occur */
inline void precise_collect()
{
  internal_collect();
}


/* returns: amt of free memory available */
inline jlong precise_free_memory()
{
  return internal_free_memory();
}


/* returns: size of heap */
inline jlong precise_get_heap_size()
{
  return internal_get_heap_size();
}

#ifndef WITH_STATS_GC
# define COLLECT_NOPTR_STATS()
# define COLLECT_LRGOBJ_STATS() 
#else
/* object statistics */
static int no_pointers = 0;
static int large_objects = 0;

# define COLLECT_NOPTR_STATS() ({ no_pointers++; })
# define COLLECT_LRGOBJ_STATS() ({ large_objects++; })

/* effects: prints out statistics */
void precise_gc_print_stats()
{
  printf("no_pointers = %d\n", no_pointers);
  printf("large_objects = %d\n", large_objects);
  internal_print_stats();
}
#endif


#ifdef WITH_PRECISE_C_BACKEND
inline void *precise_malloc (size_t size_in_bytes)
{
  return TAG_HEAP_PTR(internal_malloc(size_in_bytes));
  /*
  void *result = TAG_HEAP_PTR(internal_malloc(size_in_bytes)); 
  error_gc("Returning tagged heap ptr: %p\n", result);
  return result;
  */
}
#else
/* for StrongARM backend only
   saved_registers[13] <- lr (use to walk stack) */
void *precise_malloc_int (size_t size_in_bytes, void *saved_registers[])
{
  /*Frame fp; */
  struct gc_index *found;

  if (print_gc_index) {
    struct gc_index *entry;
    print_gc_index = 0;
    for(entry = gc_index_start; entry < gc_index_end; entry++)
      printf("%p %p %p\n", entry, entry->retaddr, entry->gc_data);
    /*	printf("%p %p\n", entry->retaddr, entry->gc_data); */
    printf("Code    : %p -> %p\n", &code_start, &code_end);
    printf("GC index: %p -> %p\n", gc_index_start, gc_index_end);
    printf("GC      : %p -> %p\n", gc_start, gc_end);
  }
  if (check_gc_index) {
    struct gc_index *entry;
    void *prev = NULL;
    for(entry = gc_index_start; entry < gc_index_end; entry++) {
      if (prev >= entry->retaddr)
	  printf("FAIL: entries in the GC index must be "
		 "ordered and unique: %p %p\n", prev, entry->retaddr);
	prev = entry->retaddr;
    }
    check_gc_index = 0;
  }
  /*
  printf("lr: %p\t", saved_registers[13]);
  fp = (Frame)(saved_registers+14);
  printf("fp: %p\t", fp);
  if (found == NULL)
    printf("NOT FOUND\n");
  else
    printf("%p\n", found->gc_data);
  */
  /*  printf("%d:------------------- %p\n", size_in_bytes, saved_registers);
      for(i = 0; i < 16; i++)
      printf("r%d: %p\n", i, saved_registers[i]); */

  return copying_malloc(size_in_bytes, saved_registers);
}
#endif

/* simply declarations to avoid lots of tedious #ifdef'ing. */
/* declare nop-variants of ops if WITH_THREADED_GC not defined */
#ifdef WITH_THREADED_GC
pthread_barrier_t before;
pthread_barrier_t after;

pthread_cond_t done_cond = PTHREAD_COND_INITIALIZER;
pthread_mutex_t done_mutex = PTHREAD_MUTEX_INITIALIZER;
int done_count;

jint halt_for_GC_flag = 0;

#ifdef WITH_REALTIME_THREADS
int GC_in_progress = 0;
#endif

void halt_for_GC() {
#if defined(WITH_REALTIME_JAVA) && defined(WITH_NOHEAP_SUPPORT)
  if (((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap) return;
#endif
  error_gc("Thread %p ready for GC\n", FNI_GetJNIEnv());
  // ready for GC
  pthread_barrier_wait(&before);
  // wait for GC to finish
  pthread_barrier_wait(&after);
  // let GC know that we're done
  pthread_mutex_lock(&done_mutex);
  done_count++;
  pthread_cond_signal(&done_cond);
  pthread_mutex_unlock(&done_mutex);
}

/* halt threads and acquire necessary locks */
void setup_for_threaded_GC() {
  /* get count of the number of threads */
#ifdef WITH_REALTIME_THREADS
  GC_in_progress = 1;
#endif
  pthread_mutex_lock(&gc_thread_mutex);
  pthread_mutex_lock(&running_threads_mutex);
  error_gc("%d threads are running.\n", num_running_threads);
  if (num_running_threads > 1) {
    pthread_barrier_init(&before, 0, num_running_threads);
    pthread_barrier_init(&after, 0, num_running_threads);
    halt_for_GC_flag = 1;
    // grab done_mutex before any thread gets past the first barrier
    pthread_mutex_lock(&done_mutex);
    pthread_barrier_wait(&before);
    halt_for_GC_flag = 0;
  }
}

/* release locks and allow threads to continue */
void cleanup_after_threaded_GC() {
  if (num_running_threads > 1) {
    pthread_barrier_wait(&after);
    // make sure all threads have restarted before continuing
    done_count = 1;
    while(done_count < num_running_threads)
      pthread_cond_wait(&done_cond, &done_mutex);;
    pthread_mutex_unlock(&done_mutex);
  }
  pthread_mutex_unlock(&gc_thread_mutex);
  pthread_mutex_unlock(&running_threads_mutex);
#ifdef WITH_REALTIME_THREADS
  GC_in_progress = 0;
#endif
}
#endif

/* prints given bitmap */
#ifndef DEBUG_GC
#define print_bitmap(bitmap)
#else
void print_bitmap(ptroff_t bitmap)
{
  int i, j;

  printf("BITMAP ");

  // start from the high bit
  for (i = BITS_IN_GC_BITMAP - 1; i >= 0; i--)
    {
      // print a 1 for every set bit,
      // a 0 for every cleared bit
      printf("%d", ((bitmap & (1 << i)) != 0));
    }
  printf("\n"); fflush(stdout);
}
#endif

#define func_proto trace
#define handle_ref handle_reference
#include "trace.c"
#undef func_proto
#undef handle_ref

#ifdef WITH_POINTER_REVERSAL
/* requires: that obj be an aligned pointer to an object, and next_index
             be the next index in the object that may need to be examined.
   returns:  NO_POINTERS if there are no more pointers in the object, 
             or 1+ the index of the next field or array index that 
	     contains a pointer. for arrays that contain fields, an 
	     array index is offset by the number of fields in the array.
*/
ptroff_t get_next_index(jobject_unwrapped obj, ptroff_t next_index)
{
  int i, bits_needed, bitmaps_needed;
  ptroff_t *bitmap_ptr;

  // should only be called w/ aligned ptrs
  assert(obj == PTRMASK(obj));

  // this object contains no pointers
  if (obj->claz->gc_info.bitmap == 0)
    {
      COLLECT_NOPTR_STATS();
      return NO_POINTERS;
    }

  // we want to initialize i based on where we are in the object
  i = next_index/BITS_IN_GC_BITMAP;

  // we use one bit in the GC bitmap for each pointer-sized 
  // word in the object. if the object size (including header)
  // is too big to be encoded in the in-line bitmap, then it's
  // put in the auxiliary bitmap, and the in-line field
  // contains a pointer to the bitmap
  bits_needed = (obj->claz->size + SIZEOF_VOID_P - 1)/SIZEOF_VOID_P;

  // if we are looking at the elements of the array, 
  // we may not need to examine the bitmap at all
  if (obj->claz->component_claz != NULL)
    {
      struct aarray *arr = (struct aarray *)obj;

      // if we have already started examining array
      // elements, then the array must contain pointers
      if (next_index > bits_needed)
	{
	  if (next_index < (arr->length + bits_needed))
	    return (INDEX_OFFSET + next_index);
	  else
	    return NO_POINTERS;
	}
      else
	// if we are going to look at the bitmap, we
	// need to remember that for arrays, we keep 
	// an extra bit for the array elements
	bits_needed++;
    }

  bitmaps_needed = (bits_needed + BITS_IN_GC_BITMAP - 1)/BITS_IN_GC_BITMAP;
  assert(bitmaps_needed >= 0);
  
  if (bitmaps_needed > 1)
    {
      bitmap_ptr = obj->claz->gc_info.ptr;
      COLLECT_LRGOBJ_STATS();
    }
  else
    bitmap_ptr = &(obj->claz->gc_info.bitmap);
  
  // after the first time around the outer loop, next_index
  // needs to be initialized relative to i
  for ( ; i < bitmaps_needed; i++, next_index = i*BITS_IN_GC_BITMAP)
    {
      ptroff_t bitmap = bitmap_ptr[i];
      print_bitmap(bitmap);

      // if next_index is in the middle of a bitmap
      // we need to shift the bitmap over
      bitmap = bitmap >> (next_index - i*BITS_IN_GC_BITMAP);

      for ( ; bitmap != 0; next_index++) {
	// stop when we get to the first set bit
	if (bitmap & 1)
	  {
	    // need to check for zero-length arrays
	    if (obj->claz->component_claz != NULL)
	      {
		struct aarray *arr = (struct aarray *)obj;

		// check if we are looking at the last bit
		if (next_index == bits_needed - 1 && arr->length == 0)
		  return NO_POINTERS;
	      }
	    return (INDEX_OFFSET + next_index);
	  }

	// as we examine each bit in the bitmap, we 
	// shift the bitmap right.
	bitmap = bitmap >> 1;
      }
    }
  
  // if we got this far, then we didn't find a pointer
  return NO_POINTERS;
}
#endif
#endif /* WITH_PRECISE_GC */
