/* structures for gc */

#ifndef INCLUDED_JNI_GC_H
#define INCLUDED_JNI_GC_H

#include <stdio.h>
#include "jni-types.h"
#include "jni-private.h"

/* --------- new garbage collection stuff ---------- */

typedef union { 
  jobject_unwrapped unwrapped_obj; 
} pointer;

void add_to_root_set(pointer);

void find_root_set();

/* --------- garbage collection data types --------- */

/* kludge for boolean in C */
enum boolean { FALSE, TRUE }; /* FALSE = 0, TRUE = 1 */

enum loctype { REG, STACK };

enum sign { PLUS, MINUS, NONE };

typedef struct _gc_index_entry *gc_index_ptr;

typedef struct _gc_regs *gc_regs_ptr;

typedef struct _gc_stack *gc_stack_ptr;

typedef struct _gc_derivs *gc_derivs_ptr;    /* derivations */

typedef struct _gc_derived *gc_derived_ptr;  /* derived pointers */

typedef struct _gc_loc *gc_loc_ptr;

/* --------- garbage collection functions ---------- */

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

#endif /* INCLUDED_JNI_GC_H */




