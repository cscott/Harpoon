#ifndef ActionAssign_H
#define ActionAssign_H
#include "classlist.h"
#include "Action.h"
class ActionAssign:public Action {
 public:
  ActionAssign(DomainRelation *drel, model *);
  void repair(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepair(CoercePredicate *p);
 private:
  char * gettype(Constraint *c,Elementexpr *ee);
};
#endif
