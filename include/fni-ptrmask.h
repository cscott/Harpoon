/* ----- bit-masking for stuffing extra info into low bits of pointers ----- */

#include "config.h"

#ifndef PTRMASK
#ifdef WITH_MASKED_POINTERS
# define PTRMASK(x) ((void*) (((ptroff_t)x) & (~3)))
#else
# define PTRMASK(x) ((void *) (x))
#endif
#endif
