#include <stdlib.h>
#include "Guidance.h"

Source::Source(Element * (*fptr)(structure *,model *)) {
  functionptr=fptr;
  setname=NULL;
}

Source::Source(char *sname) {
  functionptr=NULL;
  setname=sname;
}
