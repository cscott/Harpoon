/* do a flex-native method invocation.  This helps patch together
 * flex-native and JNI exception handling. */
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
	 */
#define methptr	r0
#define nargs	r1
#define argend	r1
#define argptr	r2
#define excptr  r3

	.global	_FNI_Dispatch
_FNI_Dispatch:
	stmfd sp!, { methptr, excptr, fp, sp, lr } @ save caller''s state
	add fp, sp, #16			@ make frame pointer point at lr.
	cmp nargs, #2
	stmlsfd sp!, { r1, r2, r3 }	@ write three pad words if necessary
	add argend, argptr, nargs, lsl #2 @ argend points at end of list
	b 2f
1:	ldr r3, [argend, #-4]!		@ load (pre-decrementing) from argend
	str r3, [sp, #-4]!		@ push on stack (last arg first)
2:	cmp argptr, argend
	bcc 1b
	ldmfd sp!, { r0, r1, r2, r3 }	@ first args go in registers
	mov lr, pc			@ set up link register
	ldr pc, [fp, #-16]		@ go go gadget method invocation.

3:					@ we''re back!
	ldmda fp, { fp, sp, pc }	@ go home, james.

	FIXUP_SEGMENT
	.word 3b, 4f @ (retaddr, handler)

	CODE_SEGMENT
4:	@ exception handler code.
	@ let the caller wrap the exceptional value@ 
	@ we just stash it away.
	ldr excptr, [fp, #-12]	@ load address to store exception into.
	str r0, [excptr]	@ put the exception object away
	mov r0, #0	@ zero out the 'return value'
	mov r1, #0	@ ...
	b 3b		@ and return.

	@ done!
