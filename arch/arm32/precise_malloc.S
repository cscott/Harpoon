#include <config.h>
#include <sym.h>
#include "segments.h"

.text
	.align 4

	/* void *precise_gc(size_t n)
	 *
	 * returns a pointer to n bytes of uninitialized storage, or
	 * NULL if the request cannot be satisfied. Saves away the
	 * contents of r0 - r10 on the stack.
	 */

	.global csymbol(precise_malloc)
csymbol(precise_malloc):
	
	mov	ip, sp
	stmfd	sp!, {r0 - r10, fp, ip, lr, pc} @ save register contents
	sub	fp, ip, #4
	sub     r1, fp, #56         @ calculate ptr to saved registers
	bl	precise_malloc_int  @ call precise_malloc_int with 2 arguments
	                            @   r0 contains the number of bytes
	                            @   r1 contains a ptr to saved registers
	mov	r2, r0
	mov	r3, r2
	mov	r0, r3
	ldmea	fp, {r1 - r10, fp, sp, pc}
