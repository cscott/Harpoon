// handles prediate of the following forms: VE<E, VE<=E, VE=E, VE>=E, VE>E


#ifndef ActionAssign_H
#define ActionAssign_H
#include "classlist.h"
#include "Action.h"
class ActionAssign:public Action {
 public:
  ActionAssign(DomainRelation *drel, model *);
  void repairpredicate(Hashtable *env, CoercePredicate *p);
  void breakpredicate(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepairpredicate(CoercePredicate *p);
 private:
  char * gettype(Constraint *c,Elementexpr *ee);
};
#endif
