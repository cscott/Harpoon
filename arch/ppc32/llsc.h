/* Macros to access the load linked and store conditional instructions
 * on this architecture.
 *  C. Scott Ananian <cananian@alumni.princeton.edu>
 */
#ifndef INCLUDED_LLSC_H
#define INCLUDED_LLSC_H

static inline int32_t load_linked(volatile int32_t *ptr) {
  uint32_t result;
  __asm__ volatile ("lwarx %[result],0,%[ptr]" :
                    [result] "=r"(result) : [ptr] "r"(ptr), "m"(*ptr));
  return result;
}
/* this can/ought to be done so we branch only iff we fail. */
static inline int store_conditional(volatile int32_t *ptr, int32_t val) {
  int result;
  __asm__ volatile ("\
	stwcx. %[val],0,%[ptr]\n\
	li %[result],0\n\
	bne- 0f\n\
	li %[result],1\n\
0:\n\
" : [result] "=r"(result), "=m"(*ptr) : [ptr] "r"(ptr), [val] "r"(val) : "cr0");
  return result;
}

static inline int64_t load_linked_double(volatile int64_t *ptr) {
  uint64_t result;
  __asm__ volatile ("ldarx %[result],0,%[ptr]" :
                    [result] "=r"(result) : [ptr] "r"(ptr), "m"(*ptr));
  return result;
}
/* this can/ought to be done so we branch only iff we fail. */
static inline int store_conditional_double(volatile int64_t *ptr, int64_t val){
  int result;
  __asm__ volatile ("\
	stdcx. %[val],0,%[ptr]\n\
	li %[result],0\n\
	bne- 0f\n\
	li %[result],1\n\
0:\n\
" : [result] "=r"(result), "=m"(*ptr) : [ptr] "r"(ptr), [val] "r"(val) : "cr0");
  return result;
}

static inline void sync(void) {
  __asm__ volatile ("sync" ::: "memory" );
}

#define LL(x) \
({ typeof(x) _ptr =  (x); typeof(*_ptr) _result;			\
   (typeof(_result))							\
     __builtin_choose_expr(sizeof(_result)<=sizeof(int32_t),		\
			   load_linked((volatile int32_t*)_ptr),	\
			   load_linked_double((volatile int64_t*)_ptr));\
})
// __builtin_choose_expr still (as of gcc 3.3) gives warnings on the
// unevaluated portion of the function, which means that a SC() function
// written like the LL() function above gives warnings on every use
// about the pointer-to-integer conversion on the "wrong" side of the
// branch. <grumble>  So let the architecture define SC_PTR appropriately.
#define SC(x,y) store_conditional(x,y)
#define SC_PTR(x,y) \
({ typeof(x) _ptr =  (x); typeof(*_ptr) _val = (y);			\
   store_conditional((volatile int32_t*)_ptr, (int32_t)_val);		\
})
#define SC_D(x,y) store_conditional_double(x,y)

#endif /* INCLUDED_LLSC_H */
