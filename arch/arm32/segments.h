/* machine-specific segment info */
#ifdef arm32
/* these defines cribbed out of the CodeGen.spec file */
# define CLASS_SEGMENT		.data 1 @ .section class
# define CODE_SEGMENT		.text 0 @ .section code
# define GC_SEGMENT		.data 2 @ .section gc
# define INIT_DATA_SEGMENT	.data 3 @ .section init_data
# define STATIC_OBJECTS_SEGMENT	.data 4 @ .section static_objects
# define STATIC_PRIMITIVES_SEGMENT .data 5 @ .section static_primitives
# define STRING_CONSTANTS_SEGMENT .data 6 @ .section string_constants
# define STRING_DATA_SEGMENT	.data 7 @ .section string_data
# define REFLECTION_OBJECTS_SEGMENT .data 8 @ .section reflection_data
# define REFLECTION_DATA_SEGMENT .data 9 @ .section reflection_data
# define TEXT_SEGMENT		.text   @ .section text
# define ZERO_DATA_SEGMENT	.bss    @ .section zero
# define FIXUP_SEGMENT		.text 10 @ .section fixup
#endif
