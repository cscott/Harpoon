// evaluates model definition rules

#ifndef PROCESSABSTRACT_H
#define PROCESSABSTRACT_H
#include "classlist.h"


Element * evaluateexpr(model *m, AElementexpr *ee, Hashtable *env,bool enforcetyping,bool compute);



class processabstract {
 public:
  processabstract(model *m);
  void processrule(Rule *r);
  void setclean();
  bool dirtyflagstatus();
  ~processabstract();
  void processrule(Rule *r, Element *ele, char *set);
 private:
  bool evaluatestatementa(Statementa *sa, Hashtable *env);
  void satisfystatementb(Statementb *sb, Hashtable *env);
  model * globalmodel;
  bool dirtyflag;
  bitreader * br;
};




/* A RelationSet keeps the current state of a quantifier. 
   There are three types of RelationSet's:
   TYPE_SET: "for left in set"
   TYPE_RELATION: "for <left,right> in set"
   TYPE_RANGE: "for left=lower..upper */
#define TYPE_SET 1
#define TYPE_RELATION 2
#define TYPE_RANGE 3

class RelationSet {
 public:
  RelationSet(Set *s, char *l,Type *tl);
  RelationSet(Set *s,char *l, Type *tl,char *r,Type *tr);
  RelationSet(char *l,AElementexpr *lower,AElementexpr*upper);
  int gettype();
  bool incrementassignment(bitreader *br,Hashtable *env, model *m);
  bool incrementassignment(Hashtable *env, model *m);
  bool incrementassignment(processconcrete *pc,Hashtable *env, model *m);
  void resetassignment(Hashtable *env);

  AElementexpr *lower,*upper;
  char *left,*right;
  Type *tleft,*tright;
  /* char's are not the responsibility of this class to dispose of*/
  int type;
  Set *set;
};




// Keeps the current state of the quantifiers of a given rule or constraint
class State {
 public:
  State(Rule *r, Hashtable *h);
  State(Constraint *c, Hashtable *h);
  ~State();
  bool initializestate(bitreader *br, model *m);
  bool increment(bitreader *br, model *m);
  bool initializestate(model *m);
  bool increment(model *m);

  bool initializestate(processconcrete*, model *m);
  bool increment(processconcrete*, model *m);
  
  Hashtable *env;
  RelationSet **relset;
  int numrelset;
  
};
#endif
