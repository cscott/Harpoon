#ifndef ActionNotInSet_H
#define ActionNotInSet_H
#include "classlist.h"
#include "Action.h"
class ActionNotInSet:public Action {
 public:
  ActionNotInSet(DomainRelation *drel, model *);
  void repair(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepair(CoercePredicate *p);
 private:
};
#endif
