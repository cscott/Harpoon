#ifndef REPAIR_H
#define REPAIR_H
#include "classlist.h"
class Repair {
 public:
  Repair(model *m);
  void repairconstraint(Constraint *c, processobject *po, Hashtable *env);
  Action * findrepairaction(CoercePredicate *cp);
  bool analyzetermination();


 private:
  void repaireleexpr(Constraint *c, processobject *po, Elementexpr *ee, Hashtable *env);
  void buildmap(WorkRelation *wr);
  void checkpredicate(Action *repair,WorkRelation *wr, Constraint *c,CoercePredicate *cp);
  bool checkforcycles(WorkSet *removededges, WorkRelation *wr);
  bool checkcycles(CoerceSentence *cs,WorkSet *removededges, WorkSet *searchset,WorkRelation *wr);
  bool breakcycles(WorkSet *removeedge, int number, WorkSet *cyclelinks, WorkRelation *wr);
  void detectcycles(CoerceSentence *cs,WorkSet *searchset,WorkSet *cycleset, WorkRelation *wr);
  WorkSet * searchcycles(WorkRelation *wr);
  model * globalmodel;

  Action ** repairactions;  // the available repair actions
  int numactions;

  WorkSet *removedsentences;
};
#endif

