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

void precise_gc_init()
{
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

/* the number of bits in the in-line gc bitmap is platform-dependent */
#define BITS_IN_GC_BITMAP (SIZEOF_VOID_P*8)

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

/* trace takes a pointer to an object and traces the pointers w/in it */
void trace(jobject_unwrapped unaligned_ptr)
{
  jobject_unwrapped obj;
  size_t obj_size_minus_header;
  int bits_needed, bitmaps_needed, i;
  ptroff_t *bitmap_ptr;
  struct aarray *arr = NULL;
  jobject_unwrapped *fields;
  jobject_unwrapped *elements = NULL;
  struct claz *claz_ptr;

  obj = PTRMASK(unaligned_ptr);

  claz_ptr = obj->claz;

  assert(&claz_start <= claz_ptr && claz_ptr < &claz_end);

  // each word in the object (excluding the header words) is
  // represented by a corresponding bit in the GC bitmap. if
  // the object is too large for the in-line bitmap, then an
  // auxiliary bitmap is used. here we determine whether the
  // bitmap is in-lined based on the size of the object. in
  // future, we may be cleverer by stealing the low bit to
  // determine whether the in-line bitmap is used, since the
  // low 2 bits are free when we use the auxiliary bitmap.
  obj_size_minus_header = claz_ptr->size - OBJ_HEADER_SIZE;
  bits_needed = (obj_size_minus_header + SIZEOF_VOID_P - 1)/SIZEOF_VOID_P;

  if (claz_ptr->component_claz != NULL)
    {
      // for arrays, we keep an extra bit for fields
      bits_needed++;
      // in arrays, fields have a different location
      arr = (struct aarray *)obj;
      fields = (jobject_unwrapped *)(arr->_padding_);
      error_gc("Object is an array.\n", "");
    }
  else
    fields = (jobject_unwrapped *)(obj->field_start);

  bitmaps_needed = (bits_needed + BITS_IN_GC_BITMAP - 1)/BITS_IN_GC_BITMAP;
  error_gc("object size = %d bytes\n", claz_ptr->size);
  error_gc("header size = %d bytes\n", OBJ_HEADER_SIZE);
  error_gc("obj_size_minus_header = %d bytes\n", obj_size_minus_header);
  error_gc("bitmaps_needed = %d\n", bitmaps_needed);
  assert(bitmaps_needed >= 0);

  if (bitmaps_needed > 1)
    bitmap_ptr = claz_ptr->gc_info.ptr;
  else
    bitmap_ptr = &(claz_ptr->gc_info.bitmap);

  // the outer loop iterates through the bitmaps
  // the inner loop iterates through the bits
  for (i = 0; i < bitmaps_needed; i++)
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
      for (j = i*BITS_IN_GC_BITMAP; bitmap != 0; j++)
	{
	  // if current bit is set, then handle
	  // corresponding reference in object, if any.
	  // the current bit is always the low bit 
	  // since we shift the bitmap right at each 
	  // iteration.
	  if (bitmap & 1)
	    {
	      // for arrays, the last bit of the bitmap
	      // is used for the array elements
	      if (arr != NULL && j == (bits_needed - 1))
		{
		  // we should be looking at the last bit
		  assert(bitmap == 1 && i == (bitmaps_needed - 1));
		  error_gc("    array contains pointers.\n", "");
		  elements = (jobject_unwrapped *)(arr->element_start);
		  break;
		}
	      else if (fields[j] != NULL)
		{
		  error_gc("    field at %p ", fields[j]);
		  handle_reference(&fields[j]);
		}
	    }

	  // shift bitmap one right; this should always shift
	  // in a zero since ptroff_t is always unsigned
	  bitmap = bitmap >> 1;
	}
    }
  // handle arrays of pointers here
  if (elements != NULL)
    {
      int k;
      assert(arr != NULL);
      // iterate through all the elements of the array, ignoring null ptrs
      for (k = 0; k < arr->length; k++)
	{
	  if (elements[k] != NULL)
	    {
	      error_gc("    array element at %p ", elements[k]);
	      handle_reference(&elements[k]);
	    }
	} 
    }
}



/* requires: that obj be an aligned pointer to an object, and next_index
             be the next index in the object that may need to be examined.
   returns:  NO_POINTERS if there are no more pointers in the object, 
             or 1+ the index of the next field or array index that 
	     contains a pointer. for arrays that contain fields, an 
	     array index is offset by the number of fields in the array.
*/
ptroff_t get_next_index(jobject_unwrapped obj, ptroff_t next_index)
{
  size_t obj_size_minus_header;
  int i, bits_needed, bitmaps_needed;
  ptroff_t *bitmap_ptr;

  // should only be called w/ aligned ptrs
  assert(obj == PTRMASK(obj));

  // we want to initialize i based on where we are in the object
  i = next_index/BITS_IN_GC_BITMAP;

  // we use one bit in the GC bitmap for each pointer-sized 
  // word in the object. if the object size (minus header)
  // is too big to be encoded in the in-line bitmap, then it's
  // put in the auxiliary bitmap, and the in-line field
  // contains a pointer to the bitmap
  obj_size_minus_header = obj->claz->size - OBJ_HEADER_SIZE;
  bits_needed = (obj_size_minus_header + SIZEOF_VOID_P - 1)/SIZEOF_VOID_P;

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
    bitmap_ptr = obj->claz->gc_info.ptr;
  else
    bitmap_ptr = &(obj->claz->gc_info.bitmap);
  
  // after the first time around the outer loop, next_index
  // needs to be initialized relative to i
  for ( ; i < bitmaps_needed; i++, next_index = i*BITS_IN_GC_BITMAP)
    {
      int j;
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
#endif /* WITH_PRECISE_GC */
