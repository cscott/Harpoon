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

struct Block {
  void* superBlockTag;
  void* begin;
  void* end;
  void* free;
  struct Block* next;
  struct Block* prev;
  flex_mutex_t lock;  
};

inline struct Block* Block_new(void* superBlockTag, size_t size);
inline void* Block_alloc(struct Block* block, size_t size);
inline void* BlockVec_alloc(struct Block* block, size_t size);
inline void Block_free(struct Block* block);
inline void Block_reset(struct Block* block);

#endif /* __BLOCK_H__ */
