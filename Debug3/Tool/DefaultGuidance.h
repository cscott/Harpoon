// for CIV

#ifndef DefGuidance_h
#define DefGuidance_h
#include "classlist.h"
#include "Guidance.h"

class DefGuidance:public Guidance {
  /* This class tells the analysis stuff */
  /* For each set:
     1. Source for atoms if the set is too small - can be another set or function call (assumed to be no set)
     2. Source for atoms if relation requires atom of this set - can be another set or function call (assumed to be no set)
     3. Removal from set - where to insert objects from this set
     4. Insertion into set - which subset to put objects in
  */
 public:
  DefGuidance(model *m);
  Source sourceforsetsize(char *set);
  Source sourceforrelation(char *set);
  char * removefromset(char * set);
  char * insertiontoset(char *set);
 private:
  model * globalmodel;
};

Element * allocatebytes(structure * st, model *m);
#endif
