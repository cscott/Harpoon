#ifndef ActionEQ1_H
#define ActionEQ1_H
#include "classlist.h"
#include "ActionGEQ1.h"
class ActionEQ1:public ActionGEQ1 {
 public:
  ActionEQ1(DomainRelation *drel,model *);
  void repair(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepair(CoercePredicate *p);
 protected:
};
#endif
