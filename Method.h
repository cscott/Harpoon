#ifndef METHOD
#define METHOD
#include "Role.h"

struct rolemethod {
  int hashcode;
  struct methodname * methodname;
  char **paramroles;
  int numobjectargs;
  short isStatic;
  struct rolereturnstate * returnstates;
  struct genhashtable * rolechanges;
  int numberofcalls;
#ifdef EFFECTS
  struct effectlist *effects;
#endif
};

struct rolechangeheader{
  struct rolechangepath *rcp;
  int inner;
};

struct rolechangesum {
  char *origrole;
  char *newrole;
};

struct rolechangepath {
  struct effectregexpr *expr;
  int exact;
  struct rolechangepath * next;
  char inner;
};

struct rolereturnstate {
  char **paramroles;
  char *returnrole;
  struct rolereturnstate *next;
};

void mergerolechanges(struct heap_state *heap);
int methodhashcode(struct rolemethod * method);
void methodassignhashcode(struct rolemethod * method);
int comparerolemethods(struct rolemethod * m1, struct rolemethod *m2);
struct rolemethod * methodaddtable(struct heap_state * heap , struct rolemethod *method);
void methodfree(struct rolemethod *method);
void addrolereturn(struct rolemethod * method, struct rolereturnstate *rrs);
int equivalentrolereturnstate(int numobjectargs, struct rolereturnstate *rrs1, struct rolereturnstate *rrs2);
void freerolereturnstate(int numobjectargs, struct rolereturnstate * rrs);
void entermethod(struct heap_state * heap, struct hashtable * ht);
void exitmethod(struct heap_state * heap, struct hashtable * ht, long long uid);
void printrolemethod(struct heap_state *heap, struct rolemethod *method);
int rcshashcode(struct rolechangesum *rcs);
int equivalentrcs(struct rolechangesum *rcs1, struct rolechangesum *rcs2);
void printrolechanges(struct heap_state *heap,struct rolemethod *rm);
#endif

