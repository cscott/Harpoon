#ifndef ActionNormal_H
#define ActionNormal_H
#include "classlist.h"
#include "Action.h"
class ActionNormal:public Action {
 public:
  ActionNormal(model *);
  void repairpredicate(Hashtable *env, CoercePredicate *p);
  void breakpredicate(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepairpredicate(CoercePredicate *p);
 private:
};
#endif
