#ifndef AbstractParser_H
#define AbstractParser_H

#include "common.h"
#include <iostream.h>
#include <stdio.h>
#include "classlist.h"

class AParser {
 public:
  AParser(Reader *r);
  Rule * parserule();
  AParser() {}
  
 protected:
  Type * parsetype();
  TypeEle * parsetypeele();
  AElementexpr * checkdot(AElementexpr * incoming);
  Rule * parsequantifiers();
  AQuantifier * parsequantifierfor();
  AQuantifier * parsequantifier();
  Set * parseset();
  Setexpr * parsesetexpr();
  Statementa * parsestatementa(bool);
  virtual Statementb * parsestatementb();
  virtual AElementexpr * parseaelementexpr(bool);
  void skiptoken();
  void needtoken(int);
  void error();
  Reader *reader;
};
#endif
