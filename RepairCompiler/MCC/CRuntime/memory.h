#ifndef MEMORY_H
#define MEMORY_H
#include "instrument.h"

#define malloc(size) ourmalloc(size)
#define calloc(memb,size) ourcalloc(memb,size)
#define realloc(ptr,size) ourrealloc(ptr,size)
#define strdup(str) ourstrdup(str)
#define free(size) ourfree(size)
#endif
