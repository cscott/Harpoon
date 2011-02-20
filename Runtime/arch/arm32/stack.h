/* stack traversal/fiddling code */
#include <config.h>

/* gcc stack layout:
 *    [fp, #-0]  = start_of_function + 16
 *    [fp, #-4]  = return address.
 *    [fp, #-8]  = parent's stack ptr (points to last value on parent's stack)
 *    [fp, #-12] = parent's frame ptr (points to first value on parent's stack)
 *    ...         parent's 'n' registers r10-r4 that need to be saved,
 *                r10 highest, r4 lowest.
 *    [fp, #-16-(4*n)] = space for this method's r3 (caller save)
 *    [fp, #-20-(4*n)] = space for this method's r2 (caller save)
 *    [fp, #-24-(4*n)] = space for this method's r1 (caller save)
[sp]= [fp, #-28-(4*n)] = space for this method's r0 (caller save)
 */

/* return address is in [fp, #-4] */
#define get_retaddr() \
({ void *__pc; \
   asm("ldr %0, [fp, #-4]" : "=r" (__pc) :: "memory"); \
   __pc; })
#define set_retaddr(__pc) \
   asm("str %0, [fp, #-4]" : : "r" (__pc) : "memory")

/* parent's frame pointer is in [fp, #-12] */
#define get_frameptr() \
({ void *__fp; \
   asm("ldr %0, [fp, #-12]" : "=r" (__fp) :: "memory"); \
   __fp; })
/* parent's stack pointer is in [fp, #-8] */
#define get_stackptr() \
({ void *__sp; \
   asm("ldr %0, [fp, #-8]" : "=r" (__sp) :: "memory"); \
   __sp; })
#define set_stackptr(__sp) \
   asm("str %0, [fp, #-8]" : : "r" (__sp) : "memory")

/* Do lookup & update of this function's return address, in order to
 * throw an exception. */

#ifdef NO_UNDERSCORES
#define LOOKUP_HANDLER "_lookup_handler"
#else
#define LOOKUP_HANDLER "__lookup_handler"
#endif

#define lookup() \
asm("bl " LOOKUP_HANDLER \
    : /* no outputs */ \
    : /* no inputs */ \
    : "r1", "r2", "r3", "lr", "cc", "memory");

/*------------------------------- STACK TRACING ------------------------*/
/* This is the stuff that HAVE_STACK_TRACE_FUNCTIONS says we've got. */

struct _Frame {
  void *start_of_function; /* start_of_function + 16 */
};
typedef struct _Frame *Frame;

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

/* saved_registers has the following format:
   at index:   0   r0
               1   r1
             ...  ...
              10  r10
              11  r11 (fp)
              12  r12 (ip)
              13  r13 (sp)
              14  r14 (lr)
 */

/* get the frame pointer from the registers saved using precise_malloc.S */
#define get_fp_from_saved_registers(__saved) \
  ((void **)__saved)[11]

/* get the return address from the registers saved using precise_malloc.S */
#define get_retaddr_from_saved_registers(__saved) \
  ((void **)__saved)[13]

#define NUM_REGS 16
