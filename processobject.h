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
  bool processconstraint(Constraint *c);
  void setclean();
  ~processobject();
  int processpredicate(Predicate *p, Hashtable *env);
 private:
  Repair * repair;
  int processstatement(Statement *s, Hashtable *env);
  model * globalmodel;
};

Element * evaluateexpr(Elementexpr *ee, Hashtable *env, model *m);
Element * evaluatevalueexpr(Valueexpr *ve, Hashtable *env, model *m);
#endif
