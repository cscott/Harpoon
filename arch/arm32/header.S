/* mark segment beginnings */
#include "segments.h"

	GC_SEGMENT
	.align 4
.global _gc_start
_gc_start:
	
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