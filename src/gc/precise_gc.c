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
#ifdef MARKSWEEP
  marksweep_gc_init();
#else
  copying_gc_init();
#endif
}

/* saved_registers[13] <- lr (use to walk stack) */
#ifdef WITH_PRECISE_C_BACKEND

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
#else /* when no threads, do nothing. */
void halt_for_GC() {}
void setup_for_threaded_GC() {}
void cleanup_after_threaded_GC() {}
#endif /* WITH_THREADED_GC */

void *precise_malloc (size_t size_in_bytes)
#else
void *precise_malloc_int (size_t size_in_bytes, void *saved_registers[])
#endif
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
#ifdef WITH_PRECISE_C_BACKEND
#ifdef MARKSWEEP
  return marksweep_malloc(size_in_bytes);
#else
  return copying_malloc(size_in_bytes);
#endif
#else
  return copying_malloc(size_in_bytes, saved_registers);
#endif
}

size_t trace_array(struct aarray *arr) {
  ptroff_t containsPointers = arr->obj.claz->gc_info.bitmap;
  assert(containsPointers == 0 || containsPointers == 1);
  if (!containsPointers)
    /* array of non-pointers */
    return aligned_size_of_np_array(arr);
  { /* contains pointers */
    size_t arr_length = arr->length;
    size_t element_size = align(arr->obj.claz->size);
    jobject_unwrapped *element = ((jobject_unwrapped *)arr)+HEADERSZ;
    while(arr_length > 0) { /* continue until done w/ entire array */
      if ((*element) != NULL) {
	error_gc("    array element", "");
	handle_nonroot(element);
      }
      arr_length--;
      element++;
    }
    return aligned_size_of_p_array(arr); /* done */
  }
}

size_t trace_object(jobject_unwrapped obj) {
  size_t obj_size = obj->claz->size;
  ptroff_t bitmap;
  ptroff_t mask = 0;
  int bitmap_position = 0;
  int num_bitmaps =
    (obj_size + COMPACT_ENCODING_SIZE - 1)/COMPACT_ENCODING_SIZE;
  assert(num_bitmaps > 0);
  if (num_bitmaps > 1)
    bitmap = *(obj->claz->gc_info.ptr);
  else
    bitmap = obj->claz->gc_info.bitmap;  
  while(num_bitmaps > 0) { /* continue until done w/ all masks */
    while(bitmap & (~mask)) { /* continue until done w/ this mask */
      /* update mask */
      mask |= 1 << bitmap_position;
      /* check if the current bit is set */
      if (bitmap & (1 << bitmap_position)) {
	/* field is pointer */
	jobject_unwrapped *field = (jobject_unwrapped *)obj + bitmap_position;
	if ((*field) != NULL) {
	  error_gc("    field", "");
	  handle_nonroot(field);
	}
      } /* current bit is set */
      bitmap_position++;
    }
    bitmap = *((&bitmap)+1); /* go to next bitmap */
    num_bitmaps--;
    mask = 0;  
  }
  return align(obj_size); /* done */
}
#endif /* WITH_PRECISE_GC */





