/* block.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

/* FIX ME! */

#ifndef __BLOCK_H__
#define __BLOCK_H__

#define __need_timeval
#include <time.h>
#include <assert.h>
#include "RTJconfig.h"
#include "flexthread.h"
#include "asm/atomicity.h"
#include "RTJfinalize.h"

#ifdef WITH_PRECISE_GC
#include "fni-wrap.h"
#include "precisec.h"
#include "jni-private.h"
#include "../gc/precise_gc.h"
#endif

#define RTJ_ALIGN(ptr) ((((int)ptr)+7)&(~7))

struct Block {
  void* superBlockTag;
#ifdef WITH_NOHEAP_SUPPORT 
  void* oldBegin;
#endif
  void* begin;
  void* end;
  void* free;
  struct Block* next;
  struct Block* prev;
};

inline struct Block* Block_new(void* superBlockTag, size_t size);
#ifdef WITH_NOHEAP_SUPPORT
inline void* Block_alloc(struct Block* block, size_t size, int noheap);
#else
inline void* Block_alloc(struct Block* block, size_t size);
#endif
inline void Block_free(struct Block* block);
inline void Block_reset(struct Block* block);
inline void Block_finalize(struct Block* block);

#endif /* __BLOCK_H__ */
