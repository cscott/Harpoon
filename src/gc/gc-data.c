#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#include "../../Contrib/gc/gc_typed.h"
#include "../../Contrib/gc/gcconfig.h"
#endif
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"

#define DESC_SIZE 6  /* number of bits needed for descriptor */
#define JINT_SIZE 32 /* number of bits in a jint */
#define DEBUG 0      /* 1 turns on status reporting; 0 turns off */

/* --------- garbage collection data types --------- */

/* kludge for boolean in C */
enum boolean { FALSE, TRUE }; /* FALSE = 0, TRUE = 1 */

enum loctype { REG, STACK };

enum sign { PLUS, MINUS, NONE };

enum masks { REGS_ZERO   = 0x80000000U, 
	     REGS_PREV   = 0x40000000U,
	     STACK_ZERO  = 0x20000000U,
	     STACK_PREV  = 0x10000000U,
	     DERIVS_ZERO = 0x08000000U,
	     DERIVS_PREV = 0x04000000U,
	     CSAVED_ZERO = 0x02000000U,
	     CSAVED_PREV = 0x01000000U };

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

extern JNIEnv *FNI_JNIEnv;

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

/* effects: adds global references to root set using find_root_set */
void find_global_refs() {
  struct _jobject *jobj =  &FNI_globalrefs;
  /* printf("\nAdding global refs: "); */
  while(jobj != NULL) {
    if (jobj->obj != NULL) {
      /* printf("%p ", jobj->obj); */
      add_to_root_set(&(jobj->obj));
    }
    jobj = jobj->next;
  }
  /* printf("\n"); */
}

/* effects: adds thread-local references to root set using find_root_set */
void find_thread_local_refs() {
  struct FNI_Thread_State *thread_state_ptr =
    (struct FNI_Thread_State *)FNI_GetJNIEnv();
  struct _jobject *jobj =  &(thread_state_ptr->localrefs);
  /* printf("\nAdding local refs: "); */
  while(jobj != NULL) {
    if (jobj->obj != NULL) {
      /* printf("%p ", jobj->obj); */
      add_to_root_set(&(jobj->obj));
    }
    jobj = jobj->next;
  }
  /* printf("\n"); */
}

/* effects: finds root set and adds each element using find_root_set */
void find_root_set() {
  report("Entering find_root_set.");
  find_static_fields();
  find_global_refs();
  find_thread_local_refs();
  report("Leaving find_root_set.");
}

/* effects: finds static fields that are objects and
            adds each to root set using find_root_set */
void find_static_fields() {
  jobject_unwrapped *obj;
  report("Entering find_static_fields.");
  /* printf("STATIC OBJECTS START: %p\n", static_objects_start);
     printf("STATIC OBJECTS END: %p\n", static_objects_end); */
  /* adds static objects to root set */
  for(obj = static_objects_start; obj < static_objects_end; obj++) {
    if ((*obj) != NULL) {
      /*printf("Adding object to root set: %p\n", (*obj)); */
      add_to_root_set(obj);
    }
  }
  report("Leaving find_static_fields.");
}


/* given the address of an instruction that is a GC point,
   returns a gc_data_ptr to the corresponding GC data */
gc_index_ptr find_gc_data(ptroff_t addr) {
    gc_index_ptr left=gc_index_start, right=gc_index_end;
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
      jint no_live_ptrs = desc & REGS_ZERO;
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
      jint same_as_prev = desc & REGS_PREV;
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
	  int i = (DESC_SIZE+index) / JINT_SIZE;
	  int j = (DESC_SIZE+index) % JINT_SIZE;
	  jint mask = 1 << (JINT_SIZE - j - 1);
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
      jint no_live_ptrs = desc & STACK_ZERO;
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
      jint same_as_prev = desc & STACK_PREV;
      if (same_as_prev)
	  return get_live_in_stack(ptr-1, num);
  }

  /* handle remaining case */
  {
      jint *data = &desc;
      struct _basetable *bt = ptr->gc_bt; 
      jint no_reg_data = desc & (REGS_ZERO | REGS_PREV);
      int index, live_count = 0, offset = no_reg_data ? 0 : num;
      jint num_offsets = bt->num_entries;
      jint tmp_store[num_offsets];
      gc_stack_ptr result = (gc_stack_ptr)malloc(sizeof(struct _gc_stack));
      if (result == NULL) report("ERROR: Out of memory");
      for (index = 0; index < num_offsets; index++) {
	  int i = (DESC_SIZE+offset+index) / JINT_SIZE;
	  int j = (DESC_SIZE+offset+index) % JINT_SIZE;
	  jint mask = 1 << (JINT_SIZE - j - 1);
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
      jint no_live_ptrs = desc & DERIVS_ZERO;
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
      jint same_as_prev = desc & DERIVS_PREV;
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
	  jint no_reg_data = desc & (REGS_ZERO | REGS_PREV);
	  offset_in_bits += (no_reg_data ? 0 : num);
      }
      {
	  /* offset due to stack data */
	  struct _basetable *bt = ptr->gc_bt;
	  jint no_stack_data = desc & (STACK_ZERO | STACK_PREV);
	  offset_in_bits += (no_stack_data ? 0 : bt->num_entries);
      }
      /* ceiling trick, get data to point to derivations */
      data += (offset_in_bits + JINT_SIZE - 1)/JINT_SIZE;

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
	      int i = (2 * regindex) / JINT_SIZE;
	      int j = (2 * regindex) % JINT_SIZE;
	      jint mask_live = 1 << (JINT_SIZE - j - 1);
	      jint mask_sign = 1 << (JINT_SIZE - j - 2);
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
	  data += (num + JINT_SIZE - 1)/JINT_SIZE;

	  /* baseindex initially equals number of base pointers in registers */
	  for (stackindex = baseindex; stackindex < derived->num_base; 
	       stackindex++) {
	      (derived->base+stackindex)->offset_or_index = *(data++);
	      (derived->base+stackindex)->_loctype = STACK;
	  }
	  for (stackindex = baseindex ; stackindex < derived->num_base; 
	       stackindex++) {
	      int i = stackindex / JINT_SIZE;
	      int j = stackindex % JINT_SIZE;
	      jint mask_sign = 1 << (JINT_SIZE - j - 2);
	      jint stack_sign = *(data+i) & mask_sign;
	      (derived->base+stackindex)->_sign = stack_sign ? PLUS : MINUS;
	  }
	  /* increment data pointer */
	  data += (derived->num_base + JINT_SIZE - 1)/JINT_SIZE;
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
    return NULL;
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



