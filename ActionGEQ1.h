#ifndef ActionGEQ1_H
#define ActionGEQ1_H
#include "classlist.h"
#include "Action.h"
class ActionGEQ1:public Action {
 public:
  ActionGEQ1() {
  }
  ActionGEQ1(DomainRelation *drel, model *);
  void repairpredicate(Hashtable *env, CoercePredicate *p);
  void breakpredicate(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepairpredicate(CoercePredicate *p);
 protected:
};
#endif
