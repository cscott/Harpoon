#ifndef INCLUDED_FNI_PTROFF_H
#define INCLUDED_FNI_PTROFF_H

/* defines an integral type equal in size to void * */

#include "config.h"
#if SIZEOF_VOID_P==4
  typedef u_int32_t ptroff_t;
#else
# if SIZEOF_VOID_P==8
   typedef u_int64_t ptroff_t;
# else
#  error unsupported pointer size.
# endif
#endif

#endif /* INCLUDED_FNI_PTROFF_H */
