#ifndef EFFECTS_H
#define EFFECTS_H

#include "RoleInference.h"

void addpath(struct heap_state *hs, long long obj, char * class, char * field, char * fielddesc, long long dstobj);
void addarraypath(struct heap_state *hs, struct hashtable * ht, long long obj, long long dstobj);

struct path {
  int paramnum;
  long long prev_obj;
  char * classname;
  char * fieldname;
  char * fielddesc;
  char * globalname;
};

struct epointerlist {
  void * object;
  struct epointerlist * next;
};

struct effectlist {
  struct effectregexpr *src;
  char *fieldname;
  struct effectregexpr *dst;
  struct effectlist * next;
};

struct effectregexpr {
  char flag; /* 0=Normal, 1=NEW, 2=Native reached object*/
  int paramnum;
  char *classname;
  char *globalname;
  struct regexprlist *expr;
};

struct regexprlist {
  char multiplicity; /* 0=1, 1=many */
  char *classname; /* Either the fields or the subtree of regexpr's must */
  char *fielddesc; /* be defined in classname and end in a pointer of type fielddesc*/
  struct regfieldlist * fields;
  struct listofregexprlist * subtree;
  struct regexprlist * nextreg;
};

struct listofregexprlist {
  struct regexprlist *expr;
  struct listofregexprlist * nextlist;
};

struct regfieldlist {
  char *classname;
  char *fielddesc;
  char *fieldname;
  struct regfieldlist * nextfld;
};

struct effectregexpr * buildregexpr(struct hashtable *pathtable, long long uid);
struct effectlist * mergeeffectlist(struct effectlist * el1, struct effectlist *el2);
void freeeffectlist(struct effectlist *el);
void freeeffectregexpr(struct effectregexpr *ere);
void freeeffects(struct path *);
void initializepaths(struct heap_state *hs);
void addeffect(struct heap_state *heap, long long suid, char * fieldname, long long duid);
void initloopstructures();
void freeregexprlist(struct regexprlist *tofree);
struct effectregexpr * mergeeffectregexpr(struct effectregexpr * ere1, struct effectregexpr * ere2);
struct regexprlist * mergeregexprlist(struct regexprlist * rel1, struct regexprlist *rel2);
void printregexprlist(struct regexprlist *rel);
void printeffectregexpr(struct effectregexpr *ere);
void printeffectlist(struct effectlist *el);
void addnewobjpath(struct heap_state *hs, long long obj);
struct effectlist * mergemultipleeffectlist(struct effectlist *el1, struct effectlist *el2);
void updateroleeffects(struct heap_state *heap);
#endif
