#ifndef ActionEQ1_H
#define ActionEQ1_H
#include "classlist.h"
#include "ActionGEQ1.h"
class ActionEQ1:public ActionGEQ1 {
 public:
  ActionEQ1(DomainRelation *drel,model *);
  void repairpredicate(Hashtable *env, CoercePredicate *p);
  void breakpredicate(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepairpredicate(CoercePredicate *p);
 protected:
};
#endif
