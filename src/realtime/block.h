/* block.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __BLOCK_H__
#define __BLOCK_H__

#include <assert.h>
#include "RTJconfig.h"
#include "flexthread.h"
#include "asm/atomicity.h"
#include "RTJfinalize.h"

#ifndef BDW_CONSERVATIVE_GC
#include <unistd.h>
#include <sys/mman.h>
#include <fcntl.h>
#endif

#ifdef WITH_PRECISE_GC
#include "fni-wrap.h"
#include "precisec.h"
#include "jni-private.h"
#include <stdlib.h>
#include "../gc/precise_gc.h"
#endif

/* When does it start to be faster to unmap everything than rezero? */
#define START_MMAP 350000

struct Block {
  void* begin;
  void* end;
  void* free;
};

inline struct Block* Block_new(size_t size);
inline void* Block_alloc(struct Block* block, size_t size);
inline void Block_free(struct Block* block);
inline void Block_reset(struct Block* block);
inline void Block_finalize(struct Block* block);

#ifdef WITH_GC_STATS
extern struct timespec total_finalize_time;
#endif


#endif /* __BLOCK_H__ */
