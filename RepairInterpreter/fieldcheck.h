#ifndef Fieldcheck_H
#define Fieldcheck_H
#include "classlist.h"
#include "element.h"

class FieldCheck {
 public:
  FieldCheck(model *m);
  void analyze();
  void buildmaps();
  int getindexthatprovides(char *set, char *relation);
 private:
  bool setok(Constraint *c, char *set);
  bool testsatisfy(WorkSet *satisfies, FieldTuple *ft);
  WorkSet * requireswhat(Constraint *c,CoercePredicate *cp);
  void processee(WorkSet *ws, Constraint *c, Elementexpr *ee);
  FieldTuple * provideswhat(Constraint *c,CoercePredicate *cp);
  model *globalmodel;
  Hashtable *cptoreqs;
  Hashtable *propertytonf;
  Hashtable *nftoprovides;
};

char *getset(Constraint *c, char *var);

class FieldTuple:public ElementWrapper {
 public:
  FieldTuple(FieldTuple *o);
  unsigned int hashCode();
  bool equals(ElementWrapper *other);
  int type();

  FieldTuple(char * r, char *s);
  char * relation;
  char * set;
};
#endif

