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

    .globl FNI_Dispatch_Void
FNI_Dispatch_Void:
    .globl FNI_Dispatch_Object
FNI_Dispatch_Object:    
    .globl FNI_Dispatch_Boolean
FNI_Dispatch_Boolean:
    .globl FNI_Dispatch_Byte
FNI_Dispatch_Byte:  
    .globl FNI_Dispatch_Char
FNI_Dispatch_Char:  
    .globl FNI_Dispatch_Short
FNI_Dispatch_Short: 
    .globl FNI_Dispatch_Int
FNI_Dispatch_Int:   
    .globl FNI_Dispatch_Long
FNI_Dispatch_Long:  
    .globl FNI_Dispatch_Float
FNI_Dispatch_Float:
    .globl FNI_Dispatch_Double
FNI_Dispatch_Double:    
     lw $0, 1($0)    