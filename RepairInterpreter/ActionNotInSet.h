#ifndef ActionNotInSet_H
#define ActionNotInSet_H
#include "classlist.h"
#include "Action.h"
class ActionNotInSet:public Action {
 public:
  ActionNotInSet(DomainRelation *drel, model *);
  void repairpredicate(Hashtable *env, CoercePredicate *p);
  void breakpredicate(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepairpredicate(CoercePredicate *p);
 private:
};
#endif
