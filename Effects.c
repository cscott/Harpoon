#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include "Effects.h"

void addpath(struct heap_state *hs, long long obj, char * field, long long dstobj) {
  struct method *method=hs->methodlist;
  while(method!=NULL) {
    struct hashtable * pathtable=method->pathtable;

    if (!contains(pathtable, dstobj)) {
      struct path * path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=obj;
      path->fieldname=copystr(field);
      puttable(pathtable, dstobj, path);
    }

    method=method->caller;
  }
}

void freeeffects(struct path * pth) {
  free(pth->fieldname);
  free(pth);
}
