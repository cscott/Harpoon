#ifndef RRELATION
#define RRELATION
#include "Names.h"
#include "RoleInference.h"

struct rolerelation {
  int srcrole;
  struct fieldname * field;
  int dstrole;
};

int rolerelationhashcode(struct rolerelation *rr);
int equivalentrolerelations(struct rolerelation *rr1, struct rolerelation *rr2);
void addrolerelation(struct heap_state *heap, struct heap_object *src, struct fieldname *field, struct heap_object *dst);
void outputrolerelations(struct heap_state *heap);
#endif
