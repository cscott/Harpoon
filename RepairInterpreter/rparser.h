#ifndef RangeFileParser_H
#define RangeFileParser_H

#include "common.h"
#include <iostream.h>
#include <stdio.h>
#include "classlist.h"

class RParser {
 public:
  RParser(Reader *r);
  char* parserelation();
  WorkSet* parseworkset();
  
 private:
  void skiptoken();
  void needtoken(int);
  void error();
  Reader *reader;
};
#endif
