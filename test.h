/* Defines interfaces for the applications and exports function calls that  
   the applications should use instead of the standard ones. */

#ifndef TEST_H
#define TEST_H
void initializeanalysis();
unsigned long benchmark();  // do analysis
void doanalysis();  // do analysis
void doanalysis2(); // break the specs and do analysis
void doanalysis3(); // insert errors and do analysis
void resetanalysis();
void addmapping(char *, void *,char *);
void addintmapping(char *key, int value);
void alloc(void *ptr,int size);
void dealloc(void *ptr);
void *ourcalloc(size_t nmemb, size_t size);
void *ourmalloc(size_t size);
void ourfree(void *ptr);
void *ourrealloc(void *ptr, size_t size);
#endif
