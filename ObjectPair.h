#ifndef ObjectPair
#define ObjectPair
#include "RoleInference.h"

#define OPHASHSIZE 100000


struct objectpair {
  struct objectpairlist * ol[OPHASHSIZE];
  struct objectpairlist * head;
};

struct objectpairlist {
  /* 3 Options */

  /* Object field */
  struct heap_object *srcobj;

  /* Local Variable */
  struct localvars *srcvar;

  /* Global Variable */
  struct globallist *srcglb;

  struct heap_object *dst;

  struct objectpairlist * next;
  struct objectpairlist * binnext;
};

struct objectpair * createobjectpair();
struct objectpairlist * removeobjectpair(struct objectpair *os);
void addobjectpair(struct objectpair *os, struct heap_object *src, struct localvars *lv, struct globallist *glb, struct heap_object *dst);
long int ophashfunction(struct heap_object *srcobj , void *lv, struct heap_object *dst);
void freeobjectpair(struct objectpair *os);
void freeobjectpairlist(struct objectpairlist *opl);
struct heap_object * objectpairsrcobj(struct objectpairlist *os);
struct localvars * objectpairsrcvar(struct objectpairlist *os);
struct globallist * objectpairsrcglb(struct objectpairlist *os);
struct heap_object * objectpairdst(struct objectpairlist *os);
int isEmptyOP(struct objectpair *op);
#endif
