#ifndef BITWRITER_H
#define BITWRITER_H
#include "classlist.h"

class bitwriter {
 public:
  bitwriter(model *m, Hashtable *env);
  void writefieldorarray(Element *element, Field *field, Element *index, Element *);
 private:
  model *globalmodel;
  bitreader * bitread;
  Hashtable *env;
};

#endif
