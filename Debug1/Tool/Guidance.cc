#include "Guidance.h"
#include <stdlib.h>

Source::Source(Element * (*fptr)(structure *,model *)) {
  functionptr=fptr;
  setname=NULL;
}

Source::Source(char *sname) {
  functionptr=NULL;
  setname=sname;
}
