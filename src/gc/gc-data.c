#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#include "gc_typed.h"
#include "gcconfig.h"
#endif
#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"
#include "fni-threadstate.h"
#include "gc-data.h"
#include "precise_gc.h"
#ifdef WITH_THREADED_GC
#include "jni-gcthreads.h"
#endif

#define kDEBUG  0
#define DESCSZ  8  /* number of bits needed for descriptor */
#define JINTSZ (sizeof(jint)*8) /* number of bits in a jint */
#define DEBUG   0  /* 1 turns on status reporting; 0 turns off */

/* --------- garbage collection data types --------- */

/* kludge for boolean in C */
enum boolean { FALSE, TRUE }; /* FALSE = 0, TRUE = 1 */

enum loctype { REG, STACK };

enum sign { PLUS, MINUS, NONE };

enum masks { NO_LIVE_REGISTERS             =   01, 
	     NO_CHANGE_IN_REGISTERS        =   02,
	     NO_LIVE_STACK_LOCATIONS       =   04,
	     NO_CHANGE_IN_STACK_LOCATIONS  =  010,
	     NO_LIVE_DERIVED_POINTERS      =  020,
	     NO_CHANGE_IN_DERIVED_POINTERS =  040,
	     NO_CALLEE_SAVED_REGISTERS     = 0100,
	     NO_CHANGE_IN_CALLEE_SAVED_REGISTERS = 0200 };

/* index entry */
struct _gc_index_entry {
    ptroff_t gc_pt;            /* address of GC point in instruction stream */
    struct _gc_data *gc_data;  /* address of GC data in GC segment */
    struct _basetable *gc_bt;  /* address of base table for that GC point */
};
typedef struct _gc_index_entry *gc_index_ptr;

struct _gc_regs {
    int num_regs;        /* number of registers */
    enum boolean *regs;  /* array of booleans: TRUE => live */
};
typedef struct _gc_regs *gc_regs_ptr;

struct _gc_stack {
    int num_stack;    /* number of live stack offsets */
    jint *stack;      /* array of live stack offsets */
};
typedef struct _gc_stack *gc_stack_ptr;

struct _gc_derivs {
    jint num_derivs;              /* number of live derived pointers */ 
    struct _gc_derived *derived;  /* array of derivations */
};
typedef struct _gc_derivs *gc_derivs_ptr;    /* derivations */

struct _gc_loc {
    enum loctype _loctype;  /* type of location: REG or STACK */
    jint offset_or_index;   /* offset if STACK, index if REG */
    enum sign _sign;        /* sign of base pointer, NONE if derived pointer */
};
typedef struct _gc_loc *gc_loc_ptr;

struct _gc_derived {
    struct _gc_loc loc;      /* location of derived pointer */
    jint num_base;           /* number of base pointers in the derivation */
    struct _gc_loc *base;    /* array of base pointers */
};
typedef struct _gc_derived *gc_derived_ptr;  /* derived pointers */

/* gc data */
struct _gc_data {
    jint data[0];
};

/* base table */
struct _basetable {
    jint num_entries;    /* number of entries */
    jint offsets[0];     /* first of entries */
};

/* -------------- externally-defined --------------- */

// extern JNIEnv *FNI_JNIEnv;

/* --------- garbage collection functions ---------- */

/* effects: finds static fields that are objects and
            adds each to root set using find_root_set */
void find_static_fields();

/* given the address (PC) of the GC point, returns a gc_index_ptr
   which can be used to obtain information about live pointers
   at the particular GC point */
gc_index_ptr find_gc_data(ptroff_t);

/* given a gc_data_ptr and the number of registers in the
   given architecture, returns a regs object, which contains
   information about which registers contain live base pointers */
gc_regs_ptr get_live_in_regs(gc_index_ptr, int);

/* given a gc_data_ptr and the number of registers in the
   given architecture, returns a stack object, which contains
   information about which offsets on the stack contain live
   base pointers */
gc_stack_ptr get_live_in_stack(gc_index_ptr, int);

/* given a gc_data_ptr and the number of registers in the
   given architecture, returns a derivs object, which contains
   information about the derivations that are live */
gc_derivs_ptr get_live_derivs(gc_index_ptr, int);

/* given a gc_regs_ptr and a register index, returns whether
   that register contains a live base pointer */
enum boolean is_live_reg(gc_regs_ptr, int);

/* given a gc_stack_ptr, returns how many stack offsets
   contain a live base pointer */
int num_live_stack_offsets(gc_stack_ptr);

/* given a gc_stack_ptr and an index n, returns the nth
   live stack offset */
jint live_stack_offset_at(gc_stack_ptr, int);

/* given a gc_derivs_ptr, returns the number of live derived 
   pointers */
jint num_live_derivs(gc_derivs_ptr);

/* given a gc_derivs_ptr and an index n, returns the nth
   derived pointer */
gc_derived_ptr live_derived_ptr_at(gc_derivs_ptr, int);

/* given a gc_derived_ptr, returns the location where the
   derived pointer is stored */
gc_loc_ptr location_at(gc_derived_ptr);

/* given a gc_derived_ptr, returns the number of base pointers
   making up this derived pointer */
jint num_base_ptrs(gc_derived_ptr);

/* given a gc_derived_ptr, returns the location where the nth   
   base pointer is stored */
gc_loc_ptr base_ptr_at(gc_derived_ptr, int);

/* given a gc_loc_ptr, returns whether the location is a
   stack offset or a register index */
enum loctype get_loc_type(gc_loc_ptr);

/* given a gc_loc_ptr, returns either the stack offset or the 
   register index, depending on which type of location it is */
jint get_loc(gc_loc_ptr);

/* given a gc_loc_ptr, returns the sign (PLUS or MINUS) if the
   location is a base pointer, or NONE if the location is the
   derived pointer */
enum sign get_sign(gc_loc_ptr);

/* use free when done with all the data associated with this
   gc_index_ptr */
void free(gc_index_ptr);

/* cleanup should be invoked at the end of a garbage collection
   to make sure that all the memory that was allocated to
   store information about various GC points have been freed */
void cleanup();

void report(char *);

#ifndef WITH_PRECISE_C_BACKEND
# ifdef HAVE_STACK_TRACE_FUNCTIONS
# include "asm/stack.h" /* snarf in the stack trace functions. */

/*
  static jobject_unwrapped *callee_saved[NUM_REGS]; */
static jobject_unwrapped *callee_saved[NUM_REGS];

/* linked list unit for representing derived pointers */
struct dlist {
  int sign;
  jobject_unwrapped *ptr_to_base; /* address of base pointer */
  struct dlist *next;
};

struct derivation {
  union {
    jobject_unwrapped *ptr_to_deriv; /* address of derived pointer */
    ptroff_t *ptr_to_constant;       /* address of calculated constant */
  } derivunion;
  struct dlist *head;
};

/* linked list unit for lists of derived pointers */
struct derived_ptrs {
  struct derivation deriv;
  struct derived_ptrs *next;
};

/* returns negative if keyval is less than datum->retaddr,
   zero if equal, and postive if greater */
int gc_index_cmp(const void *keyval, const void *datum) {
  void *entry = ((struct gc_index *)datum)->retaddr;
  return (keyval < entry) ? -1 : (keyval > entry); 
}

/* specific to what is done in precise_gc.S for the given architecture */
void fill_in_callee_saved(void *saved_registers[]) {
  int i = 0;
  for( ; i < 13 /* sp */; i++)
    callee_saved[i] = (jobject_unwrapped *)(&(saved_registers[i]));
  for( ; i < NUM_REGS; i++)
    callee_saved[i] = (jobject_unwrapped *)(&(saved_registers[i-1]));
  for(i = 0; i < NUM_REGS; i++)
    printf("r%d %p %p\n", i, callee_saved[i], *(callee_saved[i]));
}

void add_register_locations(struct gc_index *index)
{
  int i;
  jint *bitmap_ptr = &(index->gc_data->descriptor);
  for(i = 0; i < NUM_REGS; i++) {
    int x = (DESCSZ + i) / JINTSZ;
    int y = (DESCSZ + i) % JINTSZ;
    if(*(bitmap_ptr+x) & (1 << y)) {
      printf("bit %d (%p) (%p) on\n", i, *(callee_saved[i]), callee_saved[i]);
      add_to_root_set(callee_saved[i]);
    }
  }
}

void add_stack_locations(struct gc_index *index, Frame fp)
{
  int i, num_entries, offset;
  jint *bitmap_ptr;
  assert(index->bt_ptr != NULL); /* make sure we have a base table */
  num_entries = index->bt_ptr->bt[0];
  printf("\nBase Table (%d): ", num_entries);
  for(i = 0; i < num_entries; i++)
    printf("%d ", index->bt_ptr->bt[i+1]);
  printf("\n");
  bitmap_ptr = &(index->gc_data->descriptor);
  offset = (index->gc_data->descriptor & NO_LIVE_REGISTERS) ?  
    DESCSZ : DESCSZ + NUM_REGS;
  for(i = 0; i < num_entries; i++) {
    int x = (offset+i) / JINTSZ;
    int y = (offset+i) % JINTSZ;
    printf("x: %d y: %d offset: %d JINTSZ: %d bitmap: %p\n", 
	   x, y, offset, JINTSZ, *bitmap_ptr);
    if(*(bitmap_ptr+x) & (1 << y)) {
      printf("base table entry is: %d\n", index->bt_ptr->bt[i+1]);
      printf("fp: %p parent's fp: %p\n", fp, get_parent_fp(fp));
      printf("bit %d on (%p at %p)\n", i, 
	     *(((Frame)get_parent_fp(fp))-index->bt_ptr->bt[i+1]),
	     ((Frame)get_parent_fp(fp))-index->bt_ptr->bt[i+1]);
    }
  }
}

void find_other_roots(void *saved_registers[]) {
  void *retaddr = get_retaddr_from_saved_registers(saved_registers);
  Frame fp = (Frame)(get_fp_from_saved_registers(saved_registers));
  Frame top = (Frame)(((struct FNI_Thread_State *)FNI_GetJNIEnv())->stack_top);

  fill_in_callee_saved(saved_registers);
  
  printf("\ntop: %p\n", top); 
  do {
    struct gc_index *found = 
      (struct gc_index *)bsearch(retaddr, gc_index_start, 
				 (gc_index_end - gc_index_start),
				 sizeof(struct gc_index), gc_index_cmp); 

    printf("lr: %p fp: %p\n", retaddr, fp);
    if (found != NULL) {
      jint *bitmap_ptr = &(found->gc_data->descriptor);
      int offset = DESCSZ;
      printf("gc_index: %p %p %p %p\n", found, 
	     found->retaddr, found->gc_data, found->bt_ptr);
      if (!(found->gc_data->descriptor & NO_LIVE_REGISTERS)) {
	/* live registers, may or may not be same ones as previous gc point */
	struct gc_index *curr = found;
	while(curr != NULL &&
	      curr->gc_data->descriptor & NO_CHANGE_IN_REGISTERS)
	  curr--;
	assert(curr != NULL);
	add_register_locations(curr);
      }
      if (!(found->gc_data->descriptor & NO_LIVE_STACK_LOCATIONS)) {
	struct gc_index *curr = found;
	while(curr != NULL &&
	      curr->gc_data->descriptor & NO_CHANGE_IN_STACK_LOCATIONS)
	  curr--;
	/* same base table for all gc points in a method */
	assert(curr != NULL && curr->bt_ptr == found->bt_ptr); 
	add_stack_locations(curr, fp);
      }
      if (found->gc_data->descriptor & NO_LIVE_DERIVED_POINTERS)
	printf("D: none ");
      else if (found->gc_data->descriptor & NO_CHANGE_IN_DERIVED_POINTERS)
	printf("D: same ");
      else printf("D: ? ");
      if (found->gc_data->descriptor & NO_CALLEE_SAVED_REGISTERS)
	printf("C: none ");
      else if (found->gc_data->descriptor & NO_CHANGE_IN_CALLEE_SAVED_REGISTERS)
	printf("C: same ");
      else printf("C: ? ");
      printf("\n");
      
    }
    fp = get_parent_fp(fp);
    retaddr = get_my_retaddr(fp);
  } while (fp < top);
}
# endif /* HAVE_STACK_TRACE_FUNCTIONS */
#endif /* WITH_PRECISE_C_BACKEND */

/* effects: adds global references to root set using add_to_root_set */
void find_global_refs() {
  struct _jobject_globalref *jobj =  FNI_globalrefs.next;
  while(jobj != NULL) {
    assert(jobj->jobject.obj != NULL);
    error_gc("Global reference (%p) ", jobj);
    error_gc("at %p\n", jobj->jobject.obj);
    add_to_root_set(&(jobj->jobject.obj));
    error_gc("    New address is %p\n", jobj->jobject.obj);
    jobj = jobj->next;
  }
}

#ifdef WITH_THREADED_GC
/* struct FNI_Thread_State ptr for main thread */
static struct FNI_Thread_State *main_thrstate;
void gc_data_init() {
  main_thrstate = (struct FNI_Thread_State *)FNI_GetJNIEnv();
  error_gc("FNI_Thread_State for main thread: %p\n", main_thrstate);
}
#endif

/* effects: adds thread-local references to root set using add_to_root_set */
void find_thread_local_refs() {
  struct FNI_Thread_State *curr_thrstate =
    (struct FNI_Thread_State *)FNI_GetJNIEnv();
  error_gc("Current thread (%p)\n", curr_thrstate);
  handle_local_refs_for_thread(curr_thrstate);
#ifdef WITH_THREADED_GC
  // if the current thread is the main thread, don't repeat
  if (main_thrstate != curr_thrstate) {
    error_gc("Main thread (%p)\n", main_thrstate);
    handle_local_refs_for_thread(main_thrstate);
  }
  // deal with other threads
  find_other_thread_local_refs(curr_thrstate);
#endif
}

void handle_local_refs_for_thread(struct FNI_Thread_State *thread_state_ptr) {
  struct _jobject *top = thread_state_ptr->localrefs_next;
  struct _jobject *jobj = thread_state_ptr->localrefs_stack;
  while(jobj < top) {
    error_gc("Thread local reference (%p) ", jobj);
    error_gc("at %p\n", jobj->obj);
    if (jobj->obj != NULL) {
      add_to_root_set(&(jobj->obj));
      error_gc("    New address is %p\n", jobj->obj); 
    }
    jobj++;
  }
}

/* effects: finds root set and adds each element using add_to_root_set */
#ifdef WITH_PRECISE_C_BACKEND
void find_root_set()
{
  find_static_fields();
  find_global_refs();
  find_thread_local_refs();
#ifdef WITH_GENERATIONAL_GC
  find_generational_refs();
#endif
}
#else
void find_root_set(void *saved_registers[])
{
  find_other_roots(saved_registers);
  find_static_fields();
  find_global_refs();
  find_thread_local_refs();
}
#endif

/* effects: finds static fields that are objects and
            adds each to root set using find_root_set */
void find_static_fields() {
  jobject_unwrapped *obj;
  error_gc("Static objects from %p ", static_objects_start);
  error_gc("to %p\n", static_objects_end);
  /* adds static objects to root set */
  for(obj = static_objects_start; obj < static_objects_end; obj++) {
    error_gc("Static object (%p) ", obj);
    error_gc("at %p\n", (*obj));
    if ((*obj) != NULL) {
      add_to_root_set(obj);
      error_gc("    New address is %p\n", (*obj));
    }
  }
}


/* given the address of an instruction that is a GC point,
   returns a gc_data_ptr to the corresponding GC data */
gc_index_ptr find_gc_data(ptroff_t addr) {
    gc_index_ptr left=(gc_index_ptr)gc_index_start;
    gc_index_ptr right=(gc_index_ptr)gc_index_end;
    while(right >= left) {
	gc_index_ptr x = left + ((right-left)/2);
	if (addr < x->gc_pt) right = x-1; else left = x+1;
	if (addr == x->gc_pt) return x;
    }
    return NULL;
}

/* given a gc_data_ptr and the number of registers in the
   given architecture, returns a bitmap of the same size,
   indicating which registers contain live base pointers */
gc_regs_ptr get_live_in_regs(gc_index_ptr ptr, int num) {
  struct _gc_data *gc_data = ptr->gc_data;
  jint desc = gc_data->data[0];

  /* handle case of no live base pointers in registers */
  {
    jint no_live_ptrs = desc /* & REGS_ZERO */;
      if (no_live_ptrs) {
	  int index;
	  gc_regs_ptr result = (gc_regs_ptr)malloc(sizeof(struct _gc_regs));
	  if (result == NULL) report("ERROR: Out of memory");
	  result->num_regs = num;
	  result->regs = (enum boolean *)malloc(num * sizeof(enum boolean)); 
	  if (result->regs == NULL) report("ERROR: Out of memory");
	  /* initialize result array */
	  for (index = 0; index < num; index++)
	      *(result->regs+index) = FALSE;
	  return result;
      }
  }

  /* handle case of live base pointers in registers same
     as at previous GC point */
  {
    jint same_as_prev = desc/* & REGS_PREV*/;
      if (same_as_prev)
	  return get_live_in_regs(ptr-1, num);
  }

  /* handle remaining case */
  {
      int index;
      gc_regs_ptr result = (gc_regs_ptr)malloc(sizeof(struct _gc_regs));
      if (result == NULL) report("ERROR: Out of memory");
      result->num_regs = num;
      result->regs = (enum boolean *)malloc(num * sizeof(enum boolean));
      if (result->regs == NULL) report("ERROR: Out of memory");
      for (index = 0; index < num; index++) {
	  int i = (DESCSZ+index) / JINTSZ;
	  int j = (DESCSZ+index) % JINTSZ;
	  jint mask = 1 << (JINTSZ - j - 1);
	  jint reg_live = *(&desc+i) & mask;
	  *(result->regs+index) = reg_live ? TRUE : FALSE;
      }
      return result;
  }
}

/* given a gc_index_ptr and the number of registers in the
   given architecture, returns a gc_stack_ptr which contains
   information about which stack offsets contain live base pointers */
gc_stack_ptr get_live_in_stack(gc_index_ptr ptr, int num) {
  struct _gc_data *gc_data = ptr->gc_data;
  jint desc = gc_data->data[0];
  
  /* handle case of no live base pointers in stack */
  {
    jint no_live_ptrs = desc/* & STACK_ZERO*/;
      if (no_live_ptrs) {
	  gc_stack_ptr result = (gc_stack_ptr)malloc(sizeof(struct _gc_stack));
	  if (result == NULL) report("ERROR: Out of memory");
	  result->num_stack = 0;
	  result->stack = NULL;
	  return result;
      }
  }

  /* handle case of live base pointers in stack same
     as at previous GC point */
  {
    jint same_as_prev = desc/* & STACK_PREV*/;
      if (same_as_prev)
	  return get_live_in_stack(ptr-1, num);
  }

  /* handle remaining case */
  {
      jint *data = &desc;
      struct _basetable *bt = ptr->gc_bt; 
      jint no_reg_data = desc /*& (REGS_ZERO | REGS_PREV)*/;
      int index, live_count = 0, offset = no_reg_data ? 0 : num;
      jint num_offsets = bt->num_entries;
      jint tmp_store[num_offsets];
      gc_stack_ptr result = (gc_stack_ptr)malloc(sizeof(struct _gc_stack));
      if (result == NULL) report("ERROR: Out of memory");
      for (index = 0; index < num_offsets; index++) {
	  int i = (DESCSZ+offset+index) / JINTSZ;
	  int j = (DESCSZ+offset+index) % JINTSZ;
	  jint mask = 1 << (JINTSZ - j - 1);
	  jint stack_live = *(data+i) & mask;
	  if (stack_live)
	      tmp_store[live_count++] = *(bt->offsets+index);
      }
      result->num_stack = live_count;
      result->stack = (jint *)malloc(live_count * sizeof(jint));
      /* copy over to heap memory */
      for (index = 0; index < live_count; index++)
	  *(result->stack+index) = tmp_store[index];
      return result;
  }
}

/* given a gc_data_ptr and the number of registers in the
   given architecture, returns a derivs object, which contains
   information about the derivations that are live */
gc_derivs_ptr get_live_derivs(gc_index_ptr ptr, int num) {
  struct _gc_data *gc_data = ptr->gc_data;
  jint desc = gc_data->data[0];
  
  /* handle case of no live derived pointers */
  {
    jint no_live_ptrs = desc /*& DERIVS_ZERO*/;
      if (no_live_ptrs) {
	  gc_derivs_ptr result = 
	      (gc_derivs_ptr)malloc(sizeof(struct _gc_derivs));
	  if (result == NULL) report("ERROR: Out of memory");
	  result->num_derivs = 0;
	  result->derived = NULL;
	  return result;
      }
  }

  /* handle case of live derived pointers same as at previous GC point */
  {
    jint same_as_prev = desc /*& DERIVS_PREV*/;
      if (same_as_prev)
	  return get_live_derivs(ptr-1, num);
  }

  /* handle remaining case */
  {
      jint numRegDerivs = 0, numStackDerivs = 0;
      jint *data = &desc; 
      int index, offset = 0, offset_in_bits = 0;
      gc_derivs_ptr result = (gc_derivs_ptr)malloc(sizeof(struct _gc_derivs));
      if (result == NULL) report("ERROR: Out of memory");

      {  
	  /* offset due to register data */
	jint no_reg_data = desc/* & (REGS_ZERO | REGS_PREV)*/;
	  offset_in_bits += (no_reg_data ? 0 : num);
      }
      {
	  /* offset due to stack data */
	  struct _basetable *bt = ptr->gc_bt;
	  jint no_stack_data = desc/* & (STACK_ZERO | STACK_PREV)*/;
	  offset_in_bits += (no_stack_data ? 0 : bt->num_entries);
      }
      /* ceiling trick, get data to point to derivations */
      data += (offset_in_bits + JINTSZ - 1)/JINTSZ;

      /* number of derived pointers in registers */
      numRegDerivs = *(data++);
      /* total number of derived pointers in stack */
      result->num_derivs = *(data++) + numRegDerivs;
      /* allocate memory for array of derivations */
      result->derived = (gc_derived_ptr)malloc(result->num_derivs * 
					       sizeof(struct _gc_derived));
      
      for (index = 0; index < result->num_derivs; index++) {
	  int regindex, stackindex, baseindex = 0;
	  struct _gc_derived *derived = result->derived+index;
	  /* location of derived pointer */
	  derived->loc.offset_or_index = *(data++);
	  /* derived pointer has no sign */
	  derived->loc._sign = NONE;
	  /* whether the location is in a register or on the stack */
	  derived->loc._loctype = (index < numRegDerivs) ? REG : STACK;
	  /* number of base pointers in this derivation */
	  derived->num_base = *(data++);
	  derived->base = (gc_loc_ptr)malloc(derived->num_base *
					     sizeof(struct _gc_loc));
	  for (regindex = 0; regindex < num; regindex++) {
	      int i = (2 * regindex) / JINTSZ;
	      int j = (2 * regindex) % JINTSZ;
	      jint mask_live = 1 << (JINTSZ - j - 1);
	      jint mask_sign = 1 << (JINTSZ - j - 2);
	      jint reg_live = *(data+i) & mask_live;
	      jint reg_sign = *(data+i) & mask_sign;
	      if (reg_live) {
		  (derived->base+baseindex)->offset_or_index = (jint)regindex;
		  (derived->base+baseindex)->_loctype = REG;
		  (derived->base+baseindex)->_sign = reg_sign ? PLUS : MINUS;
		  baseindex++;
	      }
	  }
	  /* increment data pointer */
	  data += (num + JINTSZ - 1)/JINTSZ;

	  /* baseindex initially equals number of base pointers in registers */
	  for (stackindex = baseindex; stackindex < derived->num_base; 
	       stackindex++) {
	      (derived->base+stackindex)->offset_or_index = *(data++);
	      (derived->base+stackindex)->_loctype = STACK;
	  }
	  for (stackindex = baseindex ; stackindex < derived->num_base; 
	       stackindex++) {
	      int i = stackindex / JINTSZ;
	      int j = stackindex % JINTSZ;
	      jint mask_sign = 1 << (JINTSZ - j - 2);
	      jint stack_sign = *(data+i) & mask_sign;
	      (derived->base+stackindex)->_sign = stack_sign ? PLUS : MINUS;
	  }
	  /* increment data pointer */
	  data += (derived->num_base + JINTSZ - 1)/JINTSZ;
      }
      return result;
  }
}

/* given a gc_regs_ptr and a register index, returns whether
   that register contains a live base pointer */
enum boolean is_live_reg(gc_regs_ptr ptr, int n) {
    if (n < ptr->num_regs)
	return *(ptr->regs+n);
    report("ERROR: index out of bounds");
    return FALSE;
}

/* given a gc_stack_ptr, returns how many stack offsets
   contain a live base pointer */
int num_live_stack_offsets(gc_stack_ptr ptr) {
    return ptr->num_stack;
}

/* given a gc_stack_ptr and an index n, returns the nth
   live stack offset */
jint live_stack_offset_at(gc_stack_ptr ptr, int n) {
    if (n < ptr->num_stack)
	return *(ptr->stack+n);
    report("ERROR: Index out of bounds");
    return (jint)NULL;
}

/* given a gc_derivs_ptr, returns the number of live derived 
   pointers */
jint num_live_derivs(gc_derivs_ptr ptr) {
    return ptr->num_derivs;
}

/* given a gc_derivs_ptr and an index n, returns the nth
   derived pointer */
gc_derived_ptr live_derived_ptr_at(gc_derivs_ptr ptr, int n) {
    if (n < ptr->num_derivs) 
	return ptr->derived+n; /* returns a pointer, no dereference */
    report("ERROR: Index out of bounds");
    return NULL;
}

/* given a gc_derived_ptr, returns the location where the
   derived pointer is stored */
gc_loc_ptr location_at(gc_derived_ptr ptr) {
  return &(ptr->loc);
}

/* given a gc_derived_ptr, returns the number of base pointers
   making up this derived pointer */
jint num_base_ptrs(gc_derived_ptr ptr) {
    return ptr->num_base;
}

/* given a gc_derived_ptr, returns the location where the nth   
   base pointer is stored */
gc_loc_ptr base_ptr_at(gc_derived_ptr ptr, int n) {
    if (n < ptr->num_base)
	return ptr->base+n;
    report("ERROR: Index out of bounds");
    return NULL;
}

/* given a gc_loc_ptr, returns whether the location is a
   stack offset or a register index */
enum loctype get_loc_type(gc_loc_ptr ptr) {
    return ptr->_loctype;
}

/* given a gc_loc_ptr, returns either the stack offset or the 
   register index, depending on which type of location it is */
jint get_loc(gc_loc_ptr ptr) {
    return ptr->offset_or_index;
}

/* given a gc_loc_ptr, returns the sign (PLUS or MINUS) if the
   location is a base pointer, or NONE if the location is the
   derived pointer */
enum sign get_sign(gc_loc_ptr ptr) {
    return ptr->_sign;
}

/* debugging utility */
void report(char *errmsg) {
    if (DEBUG) printf("%s\n", errmsg);
}



