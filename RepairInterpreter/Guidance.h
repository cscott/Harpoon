#ifndef Guidance_h
#define Guidance_h
#include "classlist.h"

class Guidance {
  /* This class tells the analysis stuff */
  /* For each set:
     1. Source for atoms if the set is too small - can be another set or function call (assumed to be no set)
     2. Source for atoms if relation requires atom of this set - can be another set or function call (assumed to be no set)
     3. Removal from set - where to insert objects from this set
     4. Insertion into set - which subset to put objects in
  */
 public:
  virtual Source sourceforsetsize(char *set)=0;
  virtual Source sourceforrelation(char *set)=0;
  virtual char * removefromset(char * set)=0;
  virtual char * insertiontoset(char *set)=0;
};


class Source {
 public:
  Source(Element * (*fptr)(structure *, model *));
  Source(char *sname);

  Element * (*functionptr)(structure *,model *);
  char * setname;
};

#endif
