/* do a flex-native method invocation.  This helps patch together
 * flex-native and JNI exception handling. */
#include <regdef.h>
#include <config.h>
#include <sym.h>
#include "segments.h"

	CODE_SEGMENT
.align 4

	/* jvalue FNI_dispatch(void (*method_pointer)(), int narg_words,
	 *		       void *argptr, jobject_unwrapped *exception)
	 * calls the java method pointed to by method_pointer, with nargs
	 * words of arguments pointed to by argptr. [Note that the words
	 * are already packed as per native calling convention; int, for
	 * example, takes one word, not two (as it would in a jvalue array).
	 * Also, the argument words should be *un*wrapped.
	 * FNI_dispatch leaves *exception untouched if the method returns
	 * successfully, or sets it to the (unwrapped) exception object
	 * caught if an exception is thrown.
	 * [this is a thunk from C code to Java code]
     * There is an extra argument, so if narg_words = 0, there are no
     * args.  But if it is 1 there are 2 args
	 */
#define excptr  a3

/* declarations of FNI_Dispatch_<type> for different return types. */
#define DECLARE_FNI_Dispatch(type) \
csymbol(FNI_Dispatch_##type):	.globl	csymbol(FNI_Dispatch_##type) 

DECLARE_FNI_Dispatch(Void)
DECLARE_FNI_Dispatch(Object)
DECLARE_FNI_Dispatch(Boolean)
DECLARE_FNI_Dispatch(Byte)
DECLARE_FNI_Dispatch(Char)
DECLARE_FNI_Dispatch(Short)
DECLARE_FNI_Dispatch(Int)
DECLARE_FNI_Dispatch(Long)

/* need to use a different calling convention for float unless -msoft-float */
DECLARE_FNI_Dispatch(Float)
DECLARE_FNI_Dispatch(Double)
    addiu sp, -16       # set up a frame
    sw    ra,  12(sp)
    sw    fp,   8(sp)
    sw    excptr, 4(sp) # Put the exception pointer on the stack for
                        # possible later use 
    move  fp, sp
    move  t4, a0        # jump via t4, we cant use t9, the regular mips
                        # .so convention because ypekoe uses that reg
    # Now copy arguments onto the stack
    sll   a1, 2        /* a1 = narg*4 */
    add   t0, a2, a1   /* t0 = endptr  */
    sub   t0, 4        /* make endptr valid and start pushing with it*/
    /* Copy args onto stack.  Unroll the first four iterations */
    /* and add code to copy args into argument registers */
    /* Start pushing with the highest numbered argument */
    /* for( int* t1 = argptr ; t1 < endptr ; ++t1,++sp ) { */
    /*    *stack = *t1;                                    */

1:  
    ble   a1, 16, 2f   #  iterations that also need to load arg regs
    lw    t1, 0(t0)
    sw    t1, 0(sp)
    subu  sp, 4
    subu  t0, 4
    subu  a1, 4
    b     1b
2:
    la    t1, 8f      # Jump table to go to one of 3f-7f based on
                      # how many more args we have to push
    subu  sp, 16      # leave space on the stack in case caller wants
    addu  a1, t1      # to save args
    lw    t1, 0(a1)
    jr    t1
    /* Store arguments 0-3 */

3:      
    lw    a3, 0(t0)
    subu  t0, 4
4:  
    lw    a2, 0(t0)
    subu  t0, 4    
5:  
    lw    a1, 0(t0)
    subu  t0, 4
6:  
    lw    a0, 0(t0)
7:      
    jalr  t4       # invoke the method
return_point:
    move  sp, fp   # restore the stack pointer to the allocated frame
    lw    fp, 8(sp)
    lw    ra, 12(sp)
    addu sp, sp, 16   # takedown frame
    j     ra    

	FIXUP_SEGMENT
	.word return_point, exception_handler # (retaddr, handler)
    
	CODE_SEGMENT
.align 4    
exception_handler:	# exception handler code.
	# let the caller wrap the exceptional value
	# we just stash it away.
	lw  excptr, 4(fp)  # load address to store exception into.
    sw  v0, 0(excptr)  # put the exception object away
    li  v0, 0          # zero out the 'return value'
    li  v1, 0
	b return_point     # and return.

    # Jump table
.data
8:
    .word 7b
    .word 6b
    .word 5b
    .word 4b
    .word 3b
    
	# done!

    
