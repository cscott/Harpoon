#include <jni.h>
#include <stdlib.h>
#include "bfd.h"
#include <jni-private.h> /* for FNI_Thread_State */
#include "java_lang_Throwable.h"
#ifdef arm32
# include "../../include/asm/stack.h"
#endif

char *name_of_binary;

struct _StackTrace {
  void *retaddr;
  struct _StackTrace *next;
};
typedef struct _StackTrace *StackTrace;

struct _Frame {
  void *start_of_function; /* start_of_function + 16 */
};
typedef struct _Frame *Frame;

typedef struct _symtab_entry {
  symvalue value;
  CONST char *name;  /* Symbol name.  */  
} symtab_entry;

/* frame pointer */
#define get_my_fp() \
({ Frame __fp; \
   asm("mov %0, fp" : "=r" (__fp)); \
   __fp; })

/* [fp, #-4] = return address */
#define get_my_retaddr(__fp) \
  *(&(((Frame)__fp)->start_of_function)-1)

/* [fp, #-8] = parent's stack ptr (points to last value on parent's stack */
#define get_parent_sp(__fp) \
  *(&(((Frame)__fp)->start_of_function)-2)

/* [fp, #-12] = parent's frame ptr (points to first value on parent's stack */
#define get_parent_fp(__fp) \
  *(&(((Frame)__fp)->start_of_function)-3)

/* free memory */
void free_stacktrace(void *stacktrace) {
  StackTrace tr = (StackTrace)stacktrace;
  StackTrace next = NULL;
  while(tr != NULL) {
    next = tr->next; /* keep pointer to next */
    free(tr); /* free current */
    tr = next; /* loop */
  }
}

int compare_symtab_entry(const void *a, const void *b) {
  symtab_entry *sa, *sb;
  sa = (symtab_entry *)a;
  sb = (symtab_entry *)b;
  return (sa->value < sb->value) ? -1 : (sa->value > sb->value);
}

symtab_entry *
bsearch_symtab(symvalue key, symtab_entry *symtab, size_t size) {
  symtab_entry *curr = symtab;
  int curr_size = size;
  while(curr_size > 1) {
    int mid = curr_size/2;
    if (key >= (curr+mid)->value) {
      curr += mid; curr_size -= mid;
    } else {
      curr_size = mid;
    }
  }
  if (key >= curr->value) return curr; else return NULL;
}

static int printStackTrace(StackTrace tr) {
  bfd *abfd;
  long storage_needed;
  asymbol **symbol_table;
  symtab_entry *symtab;
  long number_of_symbols;
  long number_of_text_symbols;
  long i, j;
  StackTrace curr;
    
  bfd_init();
  abfd = bfd_openr(name_of_binary, "default");
  bfd_set_format(abfd, bfd_object);
  
  if (!bfd_check_format(abfd, bfd_object)) {
    printf("Error(1) in printStackTrace\n"); return(1);
  }
  
  storage_needed = bfd_get_symtab_upper_bound(abfd);
    
  if (storage_needed < 0) {
    printf("Error(2) in printStackTrace\n"); return(2);
  }
  if (storage_needed == 0) {
    printf("Error(3) no symbols\n"); return(3);
  }

  symbol_table = (asymbol **) xmalloc (storage_needed);
  number_of_symbols = bfd_canonicalize_symtab (abfd, symbol_table);

  if (number_of_symbols < 0) {
    printf("Error(4) in printStackTrace\n"); return(4);
  } 

  /* count number of symbols in text segment */
  for (i = number_of_text_symbols = 0; i < number_of_symbols; i++)
    if (bfd_decode_symclass(symbol_table[i]) == 'T') 
      number_of_text_symbols++;

  if (number_of_text_symbols < 0) {
    printf("Error(5) in printStackTrace\n"); return(5);
  }
  if (number_of_text_symbols == 0) {
    printf("Error(6) No symbols in text segment.\n"); return(6);
  }

  symtab = (symtab_entry *) 
    malloc(number_of_text_symbols * sizeof(symtab_entry));
  
  for (i = j = 0; i < number_of_symbols; i++) {
    symbol_info si;
    bfd_symbol_info(symbol_table[i], &si);
    if (bfd_decode_symclass(symbol_table[i]) == 'T') {
      if (j >= number_of_text_symbols) {
	printf("Error(7) in printStackTrace\n"); return(7);
      }
      (symtab+j)->value = (unsigned long)si.value;
      (symtab+j)->name  = si.name;
      j++;
    }
  }

  qsort(symtab, number_of_text_symbols, sizeof(symtab_entry),
	compare_symtab_entry);
  
  curr = tr;
  fprintf(stderr, "Exception in thread\n");

  while(curr != NULL) {
    symtab_entry *found = 
      bsearch_symtab((symvalue)curr->retaddr, symtab, number_of_text_symbols);
    if (found != NULL && 
	found->value >= (symvalue)(&code_start) && 
	found->value < (symvalue)(&code_end))
      /* make sure we only print methods not in the runtime */
      fprintf(stderr, "        at %s\n", found->name);
    else
      fprintf(stderr, "        at %p\n", curr->retaddr);
    curr = curr->next;
  }

  /* clean up */
  free(symtab);
  bfd_close(abfd);
}

static void printNumericStackTrace(StackTrace tr) {
  StackTrace curr = tr;
  fprintf(stderr, "Exception in thread\n");
  while(curr != NULL) {
    fprintf(stderr, "        at %p\n", curr->retaddr);
    curr = curr->next;
  }
}

/*
 * Class:     java_lang_Throwable
 * Method:    printStackTrace0
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Throwable_printStackTrace0
  (JNIEnv *env, jobject thisobj, jobject sobj) {
    /* XXX: unimplemented. */
  StackTrace tr;
  tr = (StackTrace)FNI_GetJNIData(env, thisobj);
  if (!printStackTrace(tr))
      printNumericStackTrace(tr);
  return;
}

/*
 * Class:     java_lang_Throwable
 * Method:    fillInStackTrace
 * Signature: ()Ljava/lang/Throwable;
 */
JNIEXPORT jthrowable JNICALL Java_java_lang_Throwable_fillInStackTrace
  (JNIEnv *env, jobject thisobj) {
  Frame fp = (Frame)get_my_fp(), next_fp = NULL;
  Frame top = (Frame)((struct FNI_Thread_State *)(env))->stack_top;
  StackTrace tr, prev = NULL;

  fprintf(stderr, "\ncode_start: %p code_end: %p\n", &code_start, &code_end);

  /*
  fprintf(stderr, "Top of stack is at: %p\n", top);
  fprintf(stderr, "Scott's functions say: \n");
  fprintf(stderr, "get_frameptr: %p.\n", get_frameptr());
  fprintf(stderr, "get_retaddr: %p.\n", get_retaddr());
  */
  while(fp < top) {
    void *retaddr;
    retaddr = get_my_retaddr(fp);

    if (retaddr >= (void *)(&code_start) && 
      retaddr < (void *)(&code_end)) {
      /* filter out return addresses that are part of the runtime */
      tr = (StackTrace)malloc(sizeof(*tr));
      tr->retaddr = retaddr;
      tr->next = prev; /* point to callee */
      prev = tr;       /* setup for next */
    }

    /*
    fprintf(stderr, "%8p: %8p (r15/pc/start_of_function + 16)\n", 
	    &(fp->start_of_function), fp->start_of_function);
    fprintf(stderr, "%8p: %8p (r14/lr/return address)\n", 
	    &(fp->start_of_function)-1, tr->retaddr);
    fprintf(stderr, "%8p: %8p (r12/ip/parent's stack ptr)\n", 
	    &(fp->start_of_function)-2, get_parent_sp(fp));
    fprintf(stderr, "%8p: %8p (r11/fp/parent's frame ptr)\n", 
	    &(fp->start_of_function)-3, get_parent_fp(fp));
    */

    /* setup for next */
    next_fp = (Frame)(get_parent_fp(fp));

    if (next_fp < fp) {
      fprintf(stderr, "STACK ERROR fp: %p, next: %p\n", fp, next_fp); 
      break;
    }

    fp = next_fp;
  }

  FNI_SetJNIData(env, thisobj, tr, free_stacktrace);

  {
    StackTrace test = (StackTrace)FNI_GetJNIData(env, thisobj);
    if (!printStackTrace(test))
      printNumericStackTrace(test);
  }
  /*
  free_stacktrace(tr);
  fprintf(stderr, "Freed stack trace.\n");
  */

  return thisobj;
}
