#ifndef MEMORY_H
#define MEMORY_H
extern "C" {
#include "instrument.h"
}
#define malloc(size) ourmalloc(size)
#define calloc(memb,size) ourcalloc(memb,size)
#define realloc(ptr,size) ourrealloc(ptr,size)
#define free(size) ourfree(size)
#endif
