/* execute static initializers. */
#include <config.h>
#include "segments.h"

	CODE_SEGMENT
	.align 4
1:	.word _static_inits
	.global	_FNI_do_java_main
	.type _FNI_do_java_main,#function
_FNI_do_java_main:
	@ JNIEnv *env in r0.
	mov ip, sp				@ save registers
	stmfd sp!, {r0, r4, fp, ip, lr, pc}
	sub fp, ip, #4
	
	/* do static initializers */
	ldr r4, 1b				@ r4=&static_inits
2:	mov r0, #0
	ldr r1, [r4], #4
#ifdef DEBUG
	stmfd sp!, {r0, r1}
	adr r0, 4f
	bl _printf
	ldmfd sp!, {r0, r1}
#ifdef WITH_DMALLOC
	stmfd sp!, {r0, r1}
	mov r0, #0
	bl _dmalloc_verify
	ldmfd sp!, {r0, r1}
#endif /* WITH_DMALLOC */
#endif /* DEBUG */
	cmp r0, r1				@ if (*r4++ != NULL) ....
	adrne lr, 2b				@    r4() /* call *r4 */
	movne pc, r1
	FIXUP_SEGMENT
	.word 2b, 3f				@ on exception goto 3f.
	CODE_SEGMENT
	/* done with all the static initializers */

	/* do java.lang.System.initializeSystemClass */
	bl _Flex_java_lang_System_initializeSystemClass__
	FIXUP_SEGMENT
	.word 2f, 3f				@ on exception goto 3f.
	CODE_SEGMENT
2:	/* done with initializeSystemClass */

	/* hit the main method with a null strings array */
	mov r0, #0
	adr lr, 2f
	ldr r1, 5f
	ldr pc, [r1]
	FIXUP_SEGMENT
	.word 2f, 3f				@ on exception goto 3f.
	CODE_SEGMENT
2:	/* done with main. */
		
	ldmea fp, {r4, fp, sp, pc}		@ restore registers & return

3:	/* exception handler */
	bl _FNI_NewLocalRef			@ r0=wrapException(r0)
	mov r1, r0
	ldr r0, [sp]				@ reload JNIEnv * into r0
	bl _FNI_Throw				@ throw ex into the JNI.
	ldmea fp, {r4, fp, sp, pc}		@ restore registers & return

#ifdef DEBUG
4:	.asciz "INIT: %p\n"
#endif

5:	.word _javamain
