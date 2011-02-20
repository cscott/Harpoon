#ifndef INCLUDED_FREE_LIST_CONSTS_H
#define INCLUDED_FREE_LIST_CONSTS_H

#include "precise_gc_consts.h"

#define BLOCK_HEADER_SIZE     (sizeof(struct block))
#define SMALL_BLOCK_SIZE      256
#define SMALL_OBJ_SIZE        (SMALL_BLOCK_SIZE - BLOCK_HEADER_SIZE)
#define MIN_BLOCK_SIZE        (BLOCK_HEADER_SIZE + sizeof(struct oobj))
#define NUM_SMALL_BLOCK_SIZES (SMALL_BLOCK_SIZE/ALIGN_TO + 1)

#endif // INCLUDED_FREE_LIST_CONSTS_H
