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

#ifdef RTJ_DEBUG_REF
#define RTJ_malloc(size) RTJ_malloc_leap(__FILE__, __LINE__, size);
inline void* RTJ_malloc_leap(const char *file, const int line, 
			     size_t size);
#else
inline void* RTJ_malloc(size_t size);
#endif

void* RTJ_jmalloc(jsize size);

inline struct MemBlock* MemBlock_currentMemBlock();
inline void MemBlock_setCurrentMemBlock(JNIEnv* env, 
					 jobject realtimeThread,
					struct MemBlock* memBlock);


#endif /* RTJmalloc_h__ */
