/* RTJfinalize.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __RTJfinalize_h__
#define __RTJfinalize_h__

#include "jni.h"
#include "jni-private.h"
#include <assert.h>

inline void RTJ_register_finalizer(jobject wrapped_obj, 
				   void (*finalizer)(void* obj, 
						     void* client_data));
/* defined for speed of scanning. */

#define RTJ_should_finalize(obj) \
((((((struct oobj*)(PTRMASK(obj)))->hashunion.hashcode) & 1) == 0) && \
(((struct oobj*)(PTRMASK(obj)))->hashunion.inflated->RTJ_finalizer))

#define RTJ_finalize(obj) \
if (RTJ_should_finalize(obj)) { \
  ((struct oobj*)(PTRMASK(obj)))->hashunion.inflated->RTJ_finalizer(PTRMASK(obj), (void*)0); \
}

#endif
