#ifndef Action_H
#define Action_H
#include "classlist.h"
class Action {
 public:
  virtual void repair(Hashtable *env, CoercePredicate *p)=0;
  virtual bool conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2)=0;
  virtual bool canrepair(CoercePredicate *p)=0;

 protected:
  char * getset(Constraint *c, char *v);
  char * calculatebound(Constraint *c, Label *clabel);
  bool comparepredicates(Constraint *c1, CoercePredicate *cp1, Constraint *c2, CoercePredicate *cp2);
  bool comparesetexpr(Setexpr *s1,char *v1,Setexpr *s2,char *v2);
  bool compareee(Elementexpr *ee1,char *v1,Elementexpr *ee2,char *v2);
  bool testforconflict(char *setv, char *seta, char *rel, Constraint *c, CoercePredicate *p);
  bool testforconflictremove(char *setv, char *seta, char *rel, Constraint *c, CoercePredicate *p);
  DomainRelation *domrelation;
  model * globalmodel;
  bool possiblysameset(char *v1, Constraint *c1, char *v2, Constraint *c2);
  bool possiblysameset(char *set1, char *v2, Constraint *c2);
  bool possiblysameset(char *set1, char *set2);
  bool conflictwithaddtoset(char *set, Constraint *c, CoercePredicate *p);
  bool conflictwithremovefromset(WorkSet *,char *set, Constraint *c, CoercePredicate *p);
};
#endif
