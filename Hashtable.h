#ifndef HASHTABLE
#define HASHTABLE
#define initialnumbins 100

struct hashtable {
  struct pointerlist ** bins;
  long counter;
  int currentsize;
};

#include "RoleInference.h"

struct pointerlist {
  long long uid;
  void * object;
  struct pointerlist * next;
};


int puttable(struct hashtable *, long long key, void * object);
void * gettable(struct hashtable *, long long key);
int hashfunction(struct hashtable *,long long key);
struct hashtable * allocatehashtable();
void freehashtable(struct hashtable * ht);
int contains(struct hashtable *ht, long long key);
void freekey(struct hashtable *ht, long long key);
void freedatahashtable(struct hashtable * ht, void (*freefunction)(void *));
#endif
