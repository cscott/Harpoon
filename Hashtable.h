#ifndef HASHTABLE
#define HASHTABLE
#define numbins 10000

struct hashtable {
  struct pointerlist * bins[numbins];
};

#include "RoleInference.h"

struct pointerlist {
  long long uid;
  struct heap_object * object;
  struct pointerlist * next;
};


int puttable(struct hashtable *, long long key, struct heap_object * object);
struct heap_object * gettable(struct hashtable *, long long key);
int hashfunction(long long key);
struct hashtable * allocatehashtable();
void freehashtable(struct hashtable * ht);
void freekey(struct hashtable *ht, long long key);
#endif
