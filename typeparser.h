#ifndef TYPEPARSER_H
#define TYPEPARSER_H

#include "common.h"
#include <iostream.h>
#include <stdio.h>
#include "classlist.h"

class Typeparser {
 public:
  Typeparser(Reader *r);
  structure * parsestructure();
 private:
  void commaorcloseparen();
  AElementexpr *checkdot(AElementexpr* incoming);
  AElementexpr *parseaelementexpr(bool);
  tlabel * parsetlabel();
  tfield * parsetfield();
  ttype * parsettype();
  AElementexpr * parseindex();
  Reader *reader;
  void skiptoken();
  void needtoken(int);
  void error();
};
#endif
