// Interface for reading structures

#ifndef BITREADER_H
#define BITREADER_H
#include "classlist.h"

class bitreader {
 public:
  bitreader(model *m, Hashtable *env);
  Element * readfieldorarray(Element *element, Field *field, Element *index);
 private:
  model *globalmodel;
  Hashtable *env;
};

#endif
