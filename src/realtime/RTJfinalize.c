/* RTJfinalize.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "RTJfinalize.h"

inline void RTJ_register_finalizer(jobject wrapped_obj,
				   void (*finalizer)(void* obj,
						     void* client_data)) {
  /* All RTJ finalizable objects must first be inflated. */
  assert(FNI_IS_INFLATED(wrapped_obj));
  
  /* The finalizer must handle deflating the object. */
  FNI_INFLATED(wrapped_obj)->RTJ_finalizer = finalizer;
}
