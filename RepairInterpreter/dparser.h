#ifndef DPARSER_H
#define DPARSER_H
#include "common.h"
#include <iostream.h>
#include <stdio.h>
#include "classlist.h"

class Dparser {
 public:
  Dparser(Reader *r);
  DomainRelation * parsesetrelation();
 private:
  DomainSet * parseset();
  DRelation * parserelation();
  Reader *reader;
  void skiptoken();
  void needtoken(int);
  void error();
};
#endif
