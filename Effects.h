#ifndef EFFECTS_H
#define EFFECTS_H

#include "RoleInference.h"

void addpath(struct heap_state *hs, long long obj, char * class, char * field, long long dstobj);

struct path {
  int paramnum;
  long long prev_obj;
  char * classname;
  char * fieldname;
  char * globalname;
};

struct effectlist {
  struct effectregexpr *src;
  char *fieldname;
  struct effectregexpre *dst;
  struct effectlist * next;
};

struct effectregexpr {
  int paramnum;
  char *classname;
  char *globalname;
  struct regexprlist *expr;
};

struct regexprlist {
  char multiplicity; /* 0=1, 1=many */
  char *classname;
  struct regfieldlist * fields;
  struct regexprlist * nextreg;
};

struct regfieldlist {
  char *fieldname;
  struct regfieldlist * nextfld;
};

void freeeffectregexpr(struct effectregexpr *ere);
void freeeffects(struct path *);
void initializepaths(struct heap_state *hs);
void addeffect(struct heap_state *heap, long long suid, char * fieldname, long long duid);
#endif
