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
#ifdef EFFECTS
  struct effectlist *effects;
#endif
};

struct rolereturnstate {
  char **paramroles;
  char *returnrole;
  struct rolereturnstate *next;
};
int methodhashcode(struct rolemethod * method);

void methodassignhashcode(struct rolemethod * method);

int comparerolemethods(struct rolemethod * m1, struct rolemethod *m2);

struct rolemethod * methodaddtable(struct heap_state * heap , struct rolemethod *method);

void methodfree(struct rolemethod *method);

void addrolereturn(struct rolemethod * method, struct rolereturnstate *rrs);

int equivalentrolereturnstate(int numobjectargs, struct rolereturnstate *rrs1, struct rolereturnstate *rrs2);

void freerolereturnstate(int numobjectargs, struct rolereturnstate * rrs);

void printrolemethod(struct rolemethod *method);
#endif

