#ifndef ActionNormal_H
#define ActionNormal_H
#include "classlist.h"
#include "Action.h"
class ActionNormal:public Action {
 public:
  ActionNormal(model *);
  void repair(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepair(CoercePredicate *p);
 private:
};
#endif
