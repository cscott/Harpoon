#include "jni.h"

#define FNIDISPATCHPROTO(name, type) \
   extern type FNI_Dispatch_##name(void (*method_pointer)(), \
				   int narg_words, void * argptr) \
   __attribute__ ((/*weak,*/ alias ("FNI_Dispatch")));
FORALLTYPES(FNIDISPATCHPROTO)
