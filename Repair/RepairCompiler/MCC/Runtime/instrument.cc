/* Defines interfaces for the applications and exports function calls that  
   the applications should use instead of the standard ones. */

#include <stdlib.h>
#include <sys/time.h>
extern "C" {
#include "instrument.h"
}
#include <stdio.h>
#include "tmap.h"
#include "size.h"

typemap * memmap;

void *ourcalloc(size_t nmemb, size_t size) {
  void *oc=calloc(nmemb,size);
  memmap->allocate(oc,size*nmemb);
  return oc;
}

void *ourmalloc(size_t size) {
  void *oc=malloc(size);
  memmap->allocate(oc,size);
  return oc;
}

void ourfree(void *ptr) {
  memmap->deallocate(ptr);
  free(ptr);
}

void *ourrealloc(void *ptr, size_t size) {
  void *orr=realloc(ptr,size);
  if (size==0) {
    memmap->deallocate(ptr);
    return orr;
  }
  if (orr==NULL) {
    return orr;
  }
  memmap->deallocate(ptr);
  memmap->allocate(ptr,size);
}

void alloc(void *ptr,int size) {
  memmap->allocate(ptr,size);
}

void dealloc(void *ptr) {
  memmap->deallocate(ptr);
}

void initializemmap() {
  typeobject *to=new typeobject();
  memmap=new typemap(to);
}

typeobject * gettypeobject() {
  return memmap->size;
}

void resettypemap() {
  memmap->reset();
}

bool assertvalidtype(int ptr, int structure) {
  return memmap->asserttype((void *)ptr, structure);
}
bool assertvalidmemory(int ptr, int structure) {
  return memmap->assertvalidmemory((void *)ptr, structure);
}

void initializestack(void *high) {
  memmap->initializestack(high);
}
