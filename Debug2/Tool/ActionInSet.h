#ifndef ActionInSet_H
#define ActionInSet_H
#include "classlist.h"
#include "Action.h"
class ActionInSet:public Action {
 public:
  ActionInSet(DomainRelation *drel,model *);
  void repairpredicate(Hashtable *env, CoercePredicate *p);
  void breakpredicate(Hashtable *env, CoercePredicate *p);
  bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2);
  bool canrepairpredicate(CoercePredicate *p);
 private:
  bool testforinvconflictwithinvsetsize(Constraint *c1,CoercePredicate *cp1, Constraint *c2,CoercePredicate *cp2);
  bool testforinvconflictwithsetsize(Constraint *c1,CoercePredicate *cp1, Constraint *c2,CoercePredicate *cp2);
  bool testforinvconflictwithinvrelset(Constraint *c1,CoercePredicate *cp1, Constraint *c2,CoercePredicate *cp2);
  bool testforinvconflictwithrelset(Constraint *c1,CoercePredicate *cp1, Constraint *c2,CoercePredicate *cp2);
  bool testforconflictwithsetsize(Constraint *c1,CoercePredicate *cp1, Constraint *c2,CoercePredicate *cp2);
  bool testforconflictwithinvsetsize(Constraint *c1,CoercePredicate *cp1, Constraint *c2,CoercePredicate *cp2);
  bool testforconflictwithrelset(Constraint *c1,CoercePredicate *cp1, Constraint *c2,CoercePredicate *cp2);
  bool testforconflictwithinvrelset(Constraint *c1,CoercePredicate *cp1, Constraint *c2,CoercePredicate *cp2);
  bool testforconflictwithvalueexpr(Constraint *c1, CoercePredicate *cp1,Constraint *c2, CoercePredicate *cp2);
  bool testforinvconflictwithvalueexpr(Constraint *c1, CoercePredicate *cp1,Constraint *c2, CoercePredicate *cp2);
};
#endif
