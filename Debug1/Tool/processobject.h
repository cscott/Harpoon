// evaluates constraints in the ICL


#ifndef PROCESSOBJECT_H
#define PROCESSOBJECT_H
#include "classlist.h"

#define PTRUE 1
#define PFALSE 0
#define PFAIL -1

class processobject {
 public:
  processobject(model *m);
  int processpredicate(Predicate *p, Hashtable *env);
  bool issatisfied(Constraint *c); // returns true iff c is satisfied
  bool processconstraint(Constraint *c); // evaluates c and if it's not satisfied, calls the repair alg.
  void breakconstraint(Constraint *c);   // breaks the given constraint by invalidating each of its satisfied sentences
  void modifyconstraint(Constraint *c);  // modifies the given constraint
  void setclean();
  ~processobject();
  
 private:
  Repair * repair;
  int processstatement(Statement *s, Hashtable *env);
  model * globalmodel;
};

Element * evaluateexpr(Elementexpr *ee, Hashtable *env, model *m);
Element * evaluatevalueexpr(Valueexpr *ve, Hashtable *env, model *m);
#endif
