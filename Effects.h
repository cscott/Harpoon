#ifndef EFFECTS_H
#define EFFECTS_H

#include "RoleInference.h"

void addpath(struct heap_state *hs, long long obj, char * field, long long dstobj);

struct path {
  long long prev_obj;
  char * fieldname;
};

void freeeffects(struct path *);
#endif
