#ifndef CMEMORY_H
#define CMEMORY_H
#include "test.h"
#define malloc(size) ourmalloc(size)
#define calloc(memb,size) ourcalloc(memb,size)
#define realloc(ptr,size) ourrealloc(ptr,size)
#define free(size) ourfree(size)
#endif
