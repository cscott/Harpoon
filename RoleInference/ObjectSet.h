#ifndef ObjectSet
#define ObjectSet
#include "RoleInference.h"

#define OSINITIALSIZE 10


struct objectset {
  struct objectlist ** ol;
  struct objectlist * head;
  int objectsetsize;
  long counter;
};

struct ositerator {
  struct objectlist * ol;
};

struct objectlist {
  struct heap_object *object;
  struct objectlist * next;
  struct objectlist * prev;
  struct objectlist * binnext;
};

struct ositerator * getIterator(struct objectset *os);
void freeIterator(struct ositerator * it);
struct heap_object * nextobject(struct ositerator *it);
struct objectset * createobjectset();
struct heap_object * removeobject(struct objectset *os, struct heap_object *ho);
void addobject(struct objectset *os, struct heap_object *ho);
long int oshashfunction(struct objectset *os, struct heap_object *ho);
void freeobjectset(struct objectset *os);
int hasNext(struct ositerator *it);
int setisempty(struct objectset *os);
#endif
