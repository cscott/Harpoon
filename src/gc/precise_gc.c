#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#include "gc_typed.h"
#include <assert.h>
#endif
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"

#ifdef WITH_PRECISE_GC

/* Only set GC_EVERY_TIME for debugging, since this is VERY slow. */
// #define GC_EVERY_TIME

int print_gc_index = 0;
int check_gc_index = 1;

#ifdef HAVE_STACK_TRACE_FUNCTIONS
#include "asm/stack.h" /* snarf in the stack trace functions. */
#endif /* HAVE_STACK_TRACE_FUNCTIONS */

/* saved_registers[13] <- lr (use to walk stack) */
#ifdef WITH_PRECISE_C_BACKEND
void *precise_malloc (size_t size_in_bytes)
#else
void *precise_malloc_int (size_t size_in_bytes, void *saved_registers[])
#endif
{
  /*Frame fp; */
  struct gc_index *found;

#ifdef GC_EVERY_TIME /* Explicitly trigger a full, world-stop collection. */
# ifdef WITH_PRECISE_C_BACKEND
  collect(/* heap not expanded */0);
# endif
#endif
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
  return copying_malloc(size_in_bytes);
#else
  return copying_malloc(size_in_bytes, saved_registers);
#endif
}

#endif /* WITH_PRECISE_GC */





