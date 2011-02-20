#ifndef PROCESSCONCRETE_H
#define PROCESSCONCRETE_H
#include "classlist.h"

class processconcrete {
 public:
  processconcrete(model *m);
  void processrule(Rule *r);
  ~processconcrete();
  Element * evaluateexpr(CAElementexpr *ee, Hashtable *env);
  void printstats();
 private:
  Element * evaluateexpr(Expr *e, Hashtable *env);
  bool evaluatestatementa(Statementa *sa, Hashtable *env);
  void satisfystatementb(CStatementb *sb, Hashtable *env);
  model * globalmodel;
  bitwriter * bw;
  bitreader * br;
};


#endif
