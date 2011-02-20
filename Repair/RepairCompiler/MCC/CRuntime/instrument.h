/* Defines interfaces for the applications and exports function calls that
   the applications should use instead of the standard ones. */

#ifndef INSTRUMENT_H
#define INSTUMENT_H
#include "classlist.h"
#include <stdlib.h>

#ifndef bool
#define bool int
#endif

void alloc(void *ptr,int size);
void dealloc(void *ptr);
void *ourcalloc(size_t nmemb, size_t size);
void *ourmalloc(size_t size);
void ourfree(void *ptr);
void *ourrealloc(void *ptr, size_t size);
void initializemmap();
void resettypemap();
bool assertvalidtype(int ptr, int structure);
bool assertvalidmemory(int ptr, int structure);
bool assertexactmemory(int ptr, int structure);
char *ourstrdup(const char *ptr);
void * getendofblock(int ptr);
void initializestack(void *);
extern struct typemap * memmap;
#endif
