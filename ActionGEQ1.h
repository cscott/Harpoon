#ifndef ActionGEQ1_H
#define ActionGEQ1_H
#include "classlist.h"
#include "Action.h"
class ActionGEQ1:public Action {
 public:
  ActionGEQ1() {
  }
  ActionGEQ1(DomainRelation *drel, model *);
  void repair(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepair(CoercePredicate *p);
 protected:
};
#endif
