#ifndef NORMALIZER_H
#define NORMALIZER_H
#include "classlist.h"

class NormalForm {
 public:
  NormalForm(Constraint *c);
  CoerceSentence * closestmatch(WorkSet *,processobject *po,Hashtable *env);
  int getnumsentences();
  CoerceSentence *getsentence(int i);
  NormalForm(Rule *r);
 private:
  Constraint *c; /*keep reference for quantifiers */
  CoerceSentence **sentences;
  int length;
};

class SentenceArray {
 public:
  SentenceArray(CoerceSentence **sentences, int l);
  int length;
  CoerceSentence **sentences;
};

class CoerceSentence {
 public:
  CoerceSentence(CoercePredicate **pred, int numpredicates);
  CoercePredicate *getpredicate(int i);
  int getnumpredicates();
  ~CoerceSentence();
  int cost(processobject *po, Hashtable *env);

 private:
  CoercePredicate **predicates;
  int numpreds;
};

class CoercePredicate {
 public:
  CoercePredicate(bool, Predicate *);
  CoercePredicate(char *ts, char *lt, char *rs);
  CoercePredicate(char *ts, char *lt, char *rt,char *rs);
  Predicate * getpredicate();
  bool getcoercebool();
  bool isrule();
  bool istuple();
  char *gettriggerset();
  char *getrelset();
  char *getltype();
  char *getrtype();

 private:
  char *triggerset;
  bool tuple;
  char *ltype;
  char *rtype;
  char *relset;
  bool rule;
  bool coercebool;
  Predicate *predicate;
};

SentenceArray * computesentences(Statement *st,bool stat);
int costfunction(CoercePredicate *p);
char * gettype(char *label, char *set,AElementexpr *ae);
#endif
