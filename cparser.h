#ifndef CParser_H
#define CParser_H

#include "common.h"
#include <iostream.h>
#include <stdio.h>
#include "classlist.h"
#include "aparser.h"

class CParser:public AParser {
 public:
  CParser(Reader *r);
 protected:
  Expr * parseexpr();
  AElementexpr * parseaelementexpr(bool);
  CAElementexpr * checkdot(CAElementexpr * incoming);
  Setexpr * parsesetexpr();
  Statementb * parsestatementb();  
};
#endif
