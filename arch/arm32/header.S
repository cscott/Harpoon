/* mark segment beginnings */
#include <config.h>
#include "segments.h"

/* quickie convenience macro for tracking down memory errors in
 * FLEX-generated code. */
.macro MEMORY_DEBUG
#if defined(DEBUG) && defined(WITH_DMALLOC)
	stmfd sp!, {r0, r1, r2, r3}
	mov r0, #0
	bl _dmalloc_verify
	ldmfd sp!, {r0, r1, r2, r3}
#endif /* DEBUG && WITH_DMALLOC */
.endm

	GC_SEGMENT
	.align 4
.global _gc_start
_gc_start:

	GC_INDEX_SEGMENT
	.align 4
.global _gc_index_start
_gc_index_start:

	STATIC_OBJECTS_SEGMENT
	.align 4
.global _static_objects_start
_static_objects_start:

	STRING_CONSTANTS_SEGMENT
	.align 4
.global _string_constants_start
_string_constants_start:
	
	FIXUP_SEGMENT
	.align 8 @ makes the math work out nicer.
.global _fixup_start
_fixup_start:

	CODE_SEGMENT
.global _code_start
_code_start:

	TEXT_SEGMENT
.global _text_start
_text_start:

	REFLECTION_DATA_SEGMENT
	.align 4
	REFLECTION_OBJECTS_SEGMENT
	.align 4