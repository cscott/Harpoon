/* mark segment ends */
#include <config.h>
#include <sym.h>
#include "segments.h"

#ifdef AOUT_SEGMENTS
	GC_SEGMENT
.global csymbol(gc_end)
csymbol(gc_end):

	GC_INDEX_SEGMENT
.global csymbol(gc_index_end)
csymbol(gc_index_end):
	
	STATIC_OBJECTS_SEGMENT
.global csymbol(static_objects_end)
csymbol(static_objects_end):

	STRING_CONSTANTS_SEGMENT
.global csymbol(string_constants_end)
csymbol(string_constants_end):
	
	FIXUP_SEGMENT
.global csymbol(fixup_end)
csymbol(fixup_end):

	CODE_SEGMENT
.global csymbol(code_end)
csymbol(code_end):

	TEXT_SEGMENT
.global csymbol(text_end)
csymbol(text_end):

#endif
