#ifndef METHOD
#define METHOD
#include "Role.h"

struct rolemethod {
  char *classname;
  char *methodname;
  char *signature;
  char **paramroles;
  int numobjectargs;
  short isStatic;
  struct rolereturnstate * returnstates;
}

struct rolereturnstate {
  char **paramroles;
  char *returnrole;
  struct rolereturnstate *next;
}


#endif
