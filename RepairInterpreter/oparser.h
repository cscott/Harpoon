#ifndef ObjectModelParser_H
#define ObjectModelParser_H

#include "common.h"
#include <iostream.h>
#include <stdio.h>
#include "classlist.h"

class Parser {
 public:
  Parser(Reader *r);
  Constraint * parseconstraint();
  
 private:
  Elementexpr * checkdot(Elementexpr * incoming);
  Constraint * parsequantifiers();
  Quantifier * parsequantifier();
  Set * parseset();
  Setexpr * parsesetexpr();
  Statement * parsestatement(bool);
  Elementexpr * parseelementexpr();
  Predicate * parsepredicate();
  void skiptoken();
  void needtoken(int);
  void error();
  Reader *reader;
};
#endif
