#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#include "gc_typed.h"
#endif
#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"
#include "precise_gc.h"
#ifdef WITH_THREADED_GC
#include "flexthread.h"
#include "gc-data.h"
#include "jni-gcthreads.h"
#endif

#ifdef WITH_PRECISE_GC

static int print_gc_index = 0;
static int check_gc_index = 1;

#ifdef HAVE_STACK_TRACE_FUNCTIONS
#include "asm/stack.h" /* snarf in the stack trace functions. */
#endif /* HAVE_STACK_TRACE_FUNCTIONS */

void precise_gc_init() {
#ifdef WITH_HEAVY_THREADS
    error_gc("Heavy threads\n", "");
#endif
#ifdef WITH_PTH_THREADS
    error_gc("Pth threads\n", "");
#endif
#ifdef WITH_USER_THREADS
    error_gc("User threads\n", "");
#endif
#ifdef WITH_THREADED_GC
  gc_data_init();
#endif
  internal_gc_init();
}

#ifdef WITH_PRECISE_C_BACKEND
inline void *precise_malloc (size_t size_in_bytes)
{
  return internal_malloc(size_in_bytes);
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
  pthread_mutex_lock(&gc_thread_mutex);
  pthread_mutex_lock(&running_threads_mutex);
  error_gc("%d threads are running.\n", num_running_threads);
  if (num_running_threads > 1) {
    pthread_barrier_init(&before, 0, num_running_threads);
    pthread_barrier_init(&after, 0, num_running_threads);
    halt_for_GC_flag = 1;
    // grab done_mutex before any thread gets past the first barruer
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
}
#endif

void trace_array(struct aarray *arr)
{
  ptroff_t containsPointers = arr->obj.claz->gc_info.bitmap;
  // this hack is for Wes' stuff; will need to fix properly later
  // basically, in anything other than standard Java, java.lang.Object
  // may have fields. since arrays inherit from java.lang.Object,
  // they have to contend with pointers in the fields.
#ifdef WITH_REALTIME_JAVA
  int j;
  jobject_unwrapped *fields = (jobject_unwrapped *)(arr->_padding_);
  for(j = 0; j < OBJECT_PADDING; j++)
    {
      error_gc("    array field at %p ", fields[j]);
      handle_reference(&fields[j]);
    }
#endif
  assert(containsPointers == 0 || containsPointers == 1);
  if (containsPointers)
    {
      int i;
      jobject_unwrapped *elements = (jobject_unwrapped *)(arr->element_start);
      // iterate through all the elements of the array, ignoring null ptrs
      for (i = 0; i < arr->length; i++)
	{
	  if (elements[i] != NULL)
	    {
	      error_gc("    array element at %p ", elements[i]);
	      handle_reference(&elements[i]);
	    }
	}
    }
}

/* prints given bitmap */
#ifndef DEBUG_GC
#define print_bitmap(bitmap)
#else
void print_bitmap(ptroff_t bitmap)
{
  int i, j;

  printf("BITMAP ");

  // start from the high bit
  for (i = SIZEOF_VOID_P*8 - 1; i >= 0; i--)
    {
      // print a 1 for every set bit,
      // a 0 for every cleared bit
      printf("%d", ((bitmap & (1 << i)) != 0));
    }
  printf("\n");
}
#endif

/* GC bitmaps for objects whose size (minus the object header) is 
   less than COMPACT_ENCODING_SIZE fits inside the claz object. */
#define COMPACT_ENCODING_SIZE (SIZEOF_VOID_P*SIZEOF_VOID_P*8)

/* trace_object takes a jobject_unwrapped that points to a non-array object. */
void trace_object(jobject_unwrapped obj)
{
  size_t obj_size_minus_header;
  int num_bitmaps, i;
  ptroff_t *bitmap_ptr;
  jobject_unwrapped *field_ptrs;

  // obj must point to a non-array object
  assert(obj->claz->component_claz == NULL);

  // if the object size minus the object header is bigger than 
  // COMPACT_ENCODING_SIZE, then the GC bitmap is not inlined.
  // here we use some clever integer divide roundup thing.
  // we may want to be dumber but more efficient in the future
  // by borrowing the low bit to indicate whether the bitmap
  // is inline or not.
  obj_size_minus_header = obj->claz->size - sizeof(struct oobj);
  num_bitmaps = (obj_size_minus_header + COMPACT_ENCODING_SIZE - 1)/COMPACT_ENCODING_SIZE;
  error_gc("object size = %d bytes\n", obj->claz->size);
  error_gc("header size = %d bytes\n", sizeof(struct oobj));
  error_gc("obj_size_minus_header = %d bytes\n", obj_size_minus_header);
  error_gc("COMPACT_ENCODING_SIZE = %d bytes\n", COMPACT_ENCODING_SIZE);
  error_gc("num_bitmaps = %d\n", num_bitmaps);
  assert(num_bitmaps >= 0);

  if (num_bitmaps > 1)
    bitmap_ptr = obj->claz->gc_info.ptr;
  else
    bitmap_ptr = &(obj->claz->gc_info.bitmap);

  field_ptrs = (jobject_unwrapped *)(obj->field_start);

  // the outer loop iterates through the bitmaps
  // the inner loop iterates through the bits
  for (i = 0; i < num_bitmaps; i++)
    {
      int j;
      ptroff_t bitmap = bitmap_ptr[i];
      print_bitmap(bitmap);

      // as we examine each bit in the bitmap, starting
      // from bit zero, we clear it. since we are only
      // interested in bits that are set, we can stop
      // examining a bitmap when all the remaining bits
      // are clear. that's why the loop termination
      // condition in this for loop is a bit strange.
      for (j = i*SIZEOF_VOID_P*8; bitmap != 0; j++)
	{
	  // if current bit is set, then handle
	  // corresponding reference in object, if any.
	  // the current bit is always the low bit 
	  // since we shift the bitmap right at each 
	  // iteration.
	  if ((bitmap & 1) && field_ptrs[j] != NULL)
	    {
	      error_gc("    field at %p ", field_ptrs[j]);
	      handle_reference(&field_ptrs[j]);
	    }

	  // shift bitmap one right; this should always shift
	  // in a zero since ptroff_t is always unsigned
	  bitmap = bitmap >> 1;
	}
    }
}

/* requires: if the array being pointed to by arr is in the middle
             of being examined, last_index gives the previous array 
	     index at which there was a pointer. otherwise,
	     last_index should be 0.
             new is a boolean that, if 0, indicates the array being
	     pointed to by arr is in the middle of being examined,
	     and 1 otherwise. no other values of new are valid.
   returns:  0 if there are no more pointers in the array, or 1+ the 
             index of the next array element that contains a pointer.
*/
ptroff_t next_array_index(struct aarray *arr, ptroff_t last_index, int new)
{
  ptroff_t containsPointers = arr->obj.claz->gc_info.bitmap;

  printf("%p is an array.\n", arr);		  
  fflush(stdout);

  if (new == 1)
    {
      // we have never seen this array before
      // it may or may not contain pointers
      assert(containsPointers == 0 || containsPointers == 1);
      if (containsPointers && arr->length > 0)
	{
	  // this array contains pointers
	  printf("%p contains pointers.\n", arr);
	  fflush(stdout);
	  return (INDEX_OFFSET+0);
	}
      else
	return NO_POINTERS;
    }
  else
    {
      ptroff_t next_index = last_index+1;

      assert(new == 0);

      // the last_index must be >= 0, and since some index in the 
      // array is a pointer, the array must contain pointers.
      assert(last_index >= 0 && containsPointers == 1);
      if (next_index < arr->length)
	return (INDEX_OFFSET+next_index);
      else
	return NO_POINTERS;
    }
}


/* requires: if the object being pointed to by obj is in the middle
             of being examined, last_index gives the previous field 
	     index at which there was a pointer. otherwise,
	     field_index should be 0.
             new is a boolean that, if 0, indicates the object being
	     pointed to by obj is in the middle of being examined,
	     and 1 otherwise. no other values of new are valid.
   returns:  0 if there are no more pointers in the object, or 1+ the 
             index of the next field that contains a pointer.
*/
ptroff_t next_field_index(jobject_unwrapped obj, ptroff_t last_index, int new)
{
  size_t obj_size_minus_header;
  int i, num_bitmaps;
  ptroff_t *bitmap_ptr;
  ptroff_t next_index;

  //  we have a non-array object
  printf("%p is an object.\n", obj);
  fflush(stdout);

  // if the object size minus the object header is bigger than 
  // COMPACT_ENCODING_SIZE, then the GC bitmap is not inlined.
  // here we use some clever integer divide roundup thing.
  obj_size_minus_header = obj->claz->size - sizeof(struct oobj);
  num_bitmaps = (obj_size_minus_header + COMPACT_ENCODING_SIZE - 1)/COMPACT_ENCODING_SIZE;
  assert(num_bitmaps >= 0);
  
  if (num_bitmaps > 1)
    bitmap_ptr = obj->claz->gc_info.ptr;
  else
    bitmap_ptr = &(obj->claz->gc_info.bitmap);
  
  assert(new == 0 || new == 1);

  // if we've already started looking at the object, 
  // next_index and i will be initialized differently
  if (new)
    {
      next_index = 0;
      i = 0;
    } 
  else
    {
      next_index = last_index + 1;
      i = next_index/(SIZEOF_VOID_P*8);
    }

  // after the first time around the outer loop, next_index
  // needs to be initialized relative to i
  for ( ; i < num_bitmaps; i++, next_index = i*SIZEOF_VOID_P*8)
    {
      int j;
      ptroff_t bitmap = bitmap_ptr[i];
      print_bitmap(bitmap);

      // if next_index is in the middle of a bitmap
      // we need to shift the bitmap over
      bitmap = bitmap >> (next_index - i*SIZEOF_VOID_P*8);

      for ( ; bitmap != 0; next_index++) {
	// stop when we get to the first set bit
	if (bitmap & 1)
	  return (INDEX_OFFSET + next_index);

	// as we examine each bit in the bitmap, we 
	// shift the bitmap right.
	bitmap = bitmap >> 1;
      }
    }
  
  // if we got this far, then we didn't find a pointer
  return NO_POINTERS;
}
#endif /* WITH_PRECISE_GC */
