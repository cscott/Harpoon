#ifndef METHOD
#define METHOD
#include "Role.h"

struct rolemethod {
  int hashcode;
  char *classname;
  char *methodname;
  char *signature;
  char **paramroles;
  int numobjectargs;
  short isStatic;
  struct rolereturnstate * returnstates;
};

struct rolereturnstate {
  char **paramroles;
  char *returnrole;
  struct rolereturnstate *next;
};
int methodhashcode(struct rolemethod * method);

void methodassignhashcode(struct rolemethod * method);

int comparerolemethods(struct rolemethod * m1, struct rolemethod *m2);
#endif
