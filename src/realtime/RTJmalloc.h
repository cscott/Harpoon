/* RTJmalloc.h, created by wbeebee 
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu> 
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __RTJmalloc_h__
#define __RTJmalloc_h__

#include "MemBlock.h"
#include "jni-private.h"
#include "RTJconfig.h"

inline void RTJ_preinit();
inline void RTJ_init();
inline void* RTJ_malloc( size_t size);
inline void* RTJ_malloc_block( size_t size, 
			      struct MemBlock* memBlock);
inline struct MemBlock* MemBlock_currentMemBlock();
inline void MemBlock_setCurrentMemBlock(JNIEnv* env, 
					 jobject realtimeThread,
					struct MemBlock* memBlock);


#endif /* RTJmalloc_h__ */
