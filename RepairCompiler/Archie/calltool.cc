extern "C" {
#include "calltool.h"
}
#include "SimpleHash.h"
#include <stdio.h>
#include <stdlib.h>
int initialized = 0;

void * map_ptr;

void assertvalidmemory(int low, int high) {}

void initializeanalysis() {
  initialized = 1;
}

void doanalysisfordebugging(char* msg) {  
  printf("%s\n",msg);
  if (initialized)
    calltool(map_ptr);
}

void addmapping(char *key, void * address, char *type) {
  map_ptr=address;
}

void resetanalysis() {}
void alloc(void *ptr,int size) {}
void dealloc(void *ptr) {}

unsigned long calltool(void* map) {

#include "specs/freeciv.cc"
    
}
