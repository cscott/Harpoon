#ifndef HASHTABLE
#define HASHTABLE
#define numbins 10000

struct hashtable {
  struct pointerlist * bins[numbins];
};

#include "RoleInference.h"

struct pointerlist {
  long long uid;
  void * object;
  struct pointerlist * next;
};


int puttable(struct hashtable *, long long key, void * object);
void * gettable(struct hashtable *, long long key);
int hashfunction(long long key);
struct hashtable * allocatehashtable();
void freehashtable(struct hashtable * ht);
int contains(struct hashtable *ht, long long key);
void freekey(struct hashtable *ht, long long key);
void freedatahashtable(struct hashtable * ht, void (*freefunction)(void *));
#endif
