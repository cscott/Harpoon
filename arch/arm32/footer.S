/* mark segment ends */
#include "segments.h"

	GC_SEGMENT
.global _gc_end
_gc_end:
	
	STATIC_OBJECTS_SEGMENT
.global _static_objects_end
_static_objects_end:

	STRING_CONSTANTS_SEGMENT
.global _string_constants_end
_string_constants_end:
	
	FIXUP_SEGMENT
.global _fixup_end
_fixup_end:

	CODE_SEGMENT
.global _code_end
_code_end:

	TEXT_SEGMENT
.global _text_end
_text_end:
