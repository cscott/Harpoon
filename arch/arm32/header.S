/* mark segment beginnings */
#include <config.h>
#include <sym.h>
#include "segments.h"

/* quickie convenience macro for tracking down memory errors in
 * FLEX-generated code. */
.macro MEMORY_DEBUG
#if defined(DEBUG) && defined(WITH_DMALLOC)
	stmfd sp!, {r0, r1, r2, r3}
	mov r0, #0
	bl csymbol(dmalloc_verify)
	ldmfd sp!, {r0, r1, r2, r3}
#endif /* DEBUG && WITH_DMALLOC */
.endm

#ifdef AOUT_SEGMENTS
	GC_SEGMENT
	.align 4
.global csymbol(gc_start)
csymbol(gc_start):

	GC_INDEX_SEGMENT
	.align 4
.global csymbol(gc_index_start)
csymbol(gc_index_start):

	STATIC_OBJECTS_SEGMENT
	.align 4
.global csymbol(static_objects_start)
csymbol(static_objects_start):

	STRING_CONSTANTS_SEGMENT
	.align 4
.global csymbol(string_constants_start)
csymbol(string_constants_start):
	
	FIXUP_SEGMENT
	.align 8 @ makes the math work out nicer.
.global csymbol(fixup_start)
csymbol(fixup_start):

	CODE_SEGMENT
.global csymbol(code_start)
csymbol(code_start):

	TEXT_SEGMENT
.global csymbol(text_start)
csymbol(text_start):

	REFLECTION_DATA_SEGMENT
	.align 4
	REFLECTION_OBJECTS_SEGMENT
	.align 4

#endif
