/* do a flex-native method invocation.  This helps patch together
 * flex-native and JNI exception handling. */
#include <regdef.h>
#include "segments.h"

CODE_SEGMENT
.align 2
/* declarations of FNI_Dispatch_<type> for different return types. */
#define DECLARE_FNI_Dispatch(type) \
.globl	FNI_Dispatch_##type \
    FNI_Dispatch_##type:	

DECLARE_FNI_Dispatch(Void)
DECLARE_FNI_Dispatch(Object)
DECLARE_FNI_Dispatch(Boolean)
DECLARE_FNI_Dispatch(Byte)
DECLARE_FNI_Dispatch(Char)
DECLARE_FNI_Dispatch(Short)
DECLARE_FNI_Dispatch(Int)
DECLARE_FNI_Dispatch(Long)
DECLARE_FNI_Dispatch(Float)
DECLARE_FNI_Dispatch(Double)
.entry: 
     lw $0, 1($0)    