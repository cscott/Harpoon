#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#include "gc_typed.h"
#endif
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"

#ifdef WITH_PRECISE_GC

/* Only set GC_EVERY_TIME for debugging, since this is VERY slow. */
#define GC_EVERY_TIME            0

int print_gc_index = 0;
int check_gc_index = 1;

struct _Frame {
  void *start_of_function; /* start_of_function + 16 */
};
typedef struct _Frame *Frame;

/* returns negative if keyval is less than datum->retaddr,
   zero if equal, and postive if greater */
int gc_index_cmp(const void *keyval, const void *datum) {
  void *entry = ((struct gc_index *)datum)->retaddr;
  int result = (keyval < entry) ? -1 : (keyval > entry); 
  return result;
}

/* saved_registers[13] <- lr (use to walk stack) */
void *precise_malloc_int (size_t size_in_bytes, void *saved_registers[])
{
  void *result;
  Frame fp; 
  struct gc_index *found;
  /* Explicitly trigger a full, world-stop collection. */
  /* if (GC_EVERY_TIME) GC_gcollect(); */
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
  /* find corresponding gc data */
  found = (struct gc_index *)bsearch(saved_registers[13], 
				     gc_index_start, 
				     (gc_index_end - gc_index_start),
				     sizeof(struct gc_index),
				     gc_index_cmp); 
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
  result = (void *)copying_malloc(size_in_bytes);
  if (result != NULL) {
    printf(":");
    return result;
  }
  collect(saved_registers);
  result = (void *)malloc(size_in_bytes);
  printf(".");
  /* printf("Allocate with malloc: %p\n", result); */
  return result;
}

#endif


