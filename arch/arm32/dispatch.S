/* do a flex-native method invocation.  This helps patch together
 * flex-native and JNI exception handling. */
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
	 */
#define methptr	r0
#define nargs	r1
#define argend	r1
#define argptr	r2
#define excptr  r3

/* declarations of FNI_Dispatch_<type> for different return types. */
#define DECLARE_FNI_Dispatch(type) \
csymbol(FNI_Dispatch_##type):	.global	csymbol(FNI_Dispatch_##type)
	
DECLARE_FNI_Dispatch(Void)
DECLARE_FNI_Dispatch(Object)
DECLARE_FNI_Dispatch(Boolean)
DECLARE_FNI_Dispatch(Byte)
DECLARE_FNI_Dispatch(Char)
DECLARE_FNI_Dispatch(Short)
DECLARE_FNI_Dispatch(Int)
DECLARE_FNI_Dispatch(Long)
/* need to use a different calling convention for float unless -msoft-float */
#ifdef WITH_SOFT_FLOAT
DECLARE_FNI_Dispatch(Float)
DECLARE_FNI_Dispatch(Double)
#endif /* WITH_SOFT_FLOAT */
.entry:
	mov ip, sp			@ ip gets out sp
	stmfd sp!, { methptr, excptr, fp, ip, lr, pc } @ save caller''s state
	add fp, sp, #20			@ make frame pointer point at pc.
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
	ldr pc, [fp, #-20]		@ go go gadget method invocation.

3:					@ we''re back!
	ldmdb fp, { fp, sp, pc }	@ go home, james.

	FIXUP_SEGMENT
	.word 3b, 4f @ (retaddr, handler)

	CODE_SEGMENT
4:	@ exception handler code.
	@ let the caller wrap the exceptional value@ 
	@ we just stash it away.
	ldr excptr, [fp, #-16]	@ load address to store exception into.
	str r0, [excptr]	@ put the exception object away
	mov r0, #0	@ zero out the 'return value'
	mov r1, #0	@ ...
	b 3b		@ and return.

	@ done!

/* When compiling on a -mhard-float platform, floating-point retvals are
 * passed in f0, according to the C calling convention.  Our java calling
 * convention always uses the -msoft-float style of retval (returned in
 * r0, r1), so we need to fix things up when calling from C to Java.
 * [CSA, 4-apr-2000]
 */
#ifndef WITH_SOFT_FLOAT
DECLARE_FNI_Dispatch(Float)
	str lr, [sp, #-4]!	@ push retaddr on stack.
	bl .entry		@ do the standard FNI_Dispatch stuff,
				@ but return here before going back to C.
	str r0, [sp, #-4]!	@ push retval on stack.
	ldfs f0, [sp], #4	@ reload where C expects to find it.
	ldr pc, [sp], #4	@ return.
DECLARE_FNI_Dispatch(Double)
	str lr, [sp, #-4]!	@ push retaddr on stack.
	bl .entry		@ do the standard FNI_Dispatch stuff,
				@ but return here before going back to C.
	stmfd sp!, {r0, r1}	@ push retval on stack.
	ldfd  f0, [sp], #8	@ reload where C expects to find it.
	ldr pc, [sp], #4	@ return.
#endif /* ! WITH_SOFT_FLOAT */
