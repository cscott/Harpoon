#include "segments.h"

@ little native method implementations.

fp	.req	r11
ip	.req	r12
sp	.req	r13
lr	.req	r14
pc	.req	r15

	CODE_SEGMENT

	.global _Java_java_lang_Object_hashCode__
_Java_java_lang_Object_hashCode__:
	ldr	r0, [r0, #-4]	@ load the hashcode field of the object.
	mov	pc, lr		@ return.

_Java_java_lang_Object_getClass__:
	.global _Java_java_lang_Object_getClass__
	ldr	r0, [r0]	@ load the claz pointer.
	ldr	r0, [r0]	@ load the class object field.
	mov	pc, lr

	.global _Java_java_lang_Float_floatToIntBits__F
	.global _Java_lang_lang_Float_intBitsToFloat__I
_Java_java_lang_Float_floatToIntBits__F:
_Java_lang_lang_Float_intBitsToFloat__I:
	mov	pc, lr		@ no conversion necessary.

	.global _Java_java_lang_Double_doubleToLongBits__D
	.global _Java_java_lang_Double_longBitsToDouble__J
_Java_java_lang_Double_doubleToLongBits__D:
_Java_java_lang_Double_longBitsToDouble__J:
	mov pc, lr		@ no conversion necessary.
