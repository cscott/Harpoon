#include <config.h>
#include <sym.h>
#include "segments.h"

@ _lookup: tweak the return address on an exception so that execution
@          resumes at the proper exception handler.
@ effect:  changes the return address of the caller to
@	   the fixed-up value found in the fixup table.
@ clobbers: r1, r2, r3, condition codes. Does not touch r0.
@ [note that this function does not adhere to standard calling convention]
		
fp	.req	r11
ip	.req	r12
sp	.req	r13
lr	.req	r14
pc	.req	r15

	CODE_SEGMENT
	
1:	.word csymbol(fixup_start)	@ first entry in fixup table
	.word csymbol(fixup_end) - 8	@ last entry in fixup table

	.global csymbol(_lookup_handler)
csymbol(_lookup_handler):
	stmfd sp!, {r4,r5,r6}   @ save r4, r5, r6
	ldr r1, [fp, #-4]	@ key (== return address of caller)
	ldr r2, 1b		@ left
	ldr r3, 1b+4		@ right
	mov r6, #0		@ a zero constant is useful.
2:	cmp r2, r3		@ while (r >= l) ...
	add r4, r2, r3		@ x = (l + r) / 2
	mov r4, r4, lsr #4	@   (scaling factor for element size)
	movhi pc, #0		@  [die horrible death if no match found]
	ldr r5, [r6, r4, lsl #3] @ load x->label
	mov r4, r4, lsl #3	@ (fixup r4 while load completes)
	cmp r1, r5		@ compare key with x->label
	subcc r3, r4, #8	@ if (key < x->label) r = x-1;
	addhi r2, r4, #8	@ else l = x+1;
	bne 2b			@ loop if not found.
	ldr r4, [r4, #4]	@ found match!  load x->value.
	str r4, [fp, #-4]	@ and store as return address of caller.
	ldmfd sp!, {r4,r5,r6}   @ restore r4, r5, r6
	mov pc, lr		@ return from _lookup.
