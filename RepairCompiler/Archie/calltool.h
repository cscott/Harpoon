#ifndef _CALLTOOL
#define _CALLTOOL
unsigned long calltool(void* map);

void assertvalidmemory(int low, int high);
void initializeanalysis();
void doanalysisfordebugging(char* msg);
void addmapping(char *key, void * address, char *type);
void resetanalysis();
void alloc(void *ptr,int size);
void dealloc(void *ptr);

#endif
