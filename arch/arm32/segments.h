#include "config.h"

/* machine-specific segment info */
#ifndef arm32
# error Wrong segment information included.
#endif

/* the aoutelf selects *either* an aout-format section declaration
 * (ugh, backwards compatibility) or an elf-format section declaration (yay!)
 */
#if defined(AOUT_SEGMENTS)
# define aoutelf(aout,elf) aout
#elif defined(ELF_SEGMENTS)
# define aoutelf(aout,elf) elf
#else
# error what kind of output format is this, anyhow?
#endif

/* these defines cribbed out of the CodeGen.spec file */
# define CLASS_SEGMENT		aoutelf(.data 1, .section .flex.class)
# define CODE_SEGMENT		aoutelf(.text 0, .section .flex.code)
# define GC_SEGMENT		aoutelf(.data 2, .section .flex.gc)
# define INIT_DATA_SEGMENT	aoutelf(.data 3, .section .flex.init_data)
# define STATIC_OBJECTS_SEGMENT	aoutelf(.data 4, .section .flex.static_objects)
# define STATIC_PRIMITIVES_SEGMENT aoutelf(.data 5, .section .flex.static_primitives)
# define STRING_CONSTANTS_SEGMENT aoutelf(.data 6, .section .flex.string_constants)
# define STRING_DATA_SEGMENT	aoutelf(.data 7, .section .flex.string_data)
# define REFLECTION_OBJECTS_SEGMENT aoutelf(.data 8, .section .flex.reflection_objects)
# define REFLECTION_DATA_SEGMENT aoutelf(.data 9, .section .flex.reflection_data)
# define TEXT_SEGMENT		aoutelf(.text, .section .text)
# define ZERO_DATA_SEGMENT	aoutelf(.bss, .section .flex.zero)
# define FIXUP_SEGMENT		aoutelf(.text 10, .section .flex.fixup)
# define GC_INDEX_SEGMENT       aoutelf(.data 10, .section .flex.gc_index)
