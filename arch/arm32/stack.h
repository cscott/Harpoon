/* stack traversal/fiddling code */


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

/* Do lookup & update of this function's return address, in order to
 * throw an exception. */

#define lookup() \
asm("bl __lookup_handler" \
    : /* no outputs */ \
    : /* no inputs */ \
    : "r1", "r2", "r3", "lr", "cc", "memory");
