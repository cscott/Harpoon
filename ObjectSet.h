#ifndef ObjectSet
#define ObjectSet
#include "RoleInference.h"

#define OSHASHSIZE 10000


struct objectset {
  struct objectlist * ol[OSHASHSIZE];
  struct objectlist * head;
};

struct ositerator {
  struct objectlist * ol;
};

struct objectlist {
  struct heap_object *object;
  struct objectlist * next;
  struct objectlist * binnext;
};

struct ositerator * getIterator(struct objectset *os);
void freeIterator(struct ositerator * it);
struct heap_object * nextobject(struct ositerator *it);
struct objectset * createobjectset();
struct heap_object * removeobject(struct objectset *os);
void addobject(struct objectset *os, struct heap_object *ho);
long int oshashfunction(struct heap_object *ho);
void freeobjectset(struct objectset *os);
int hasNext(struct ositerator *it);
#endif
