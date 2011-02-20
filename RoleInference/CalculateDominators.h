#ifndef CALCULATEDOMINATOR
#define CALCULATEDOMINATOR
#include "RoleInference.h"
#include "GenericHashtable.h"


struct referencelist * calculatedominators(struct genhashtable * dommapping,struct heap_object *ho);
struct genhashtable * builddominatormappings(struct heap_state *heap, int includecurrent);
int * minimaldominatorset(struct localvars * lv, struct globallist *gl, struct heap_state *heap, int includecurrent);
int dominates(struct localvars *lv1, struct globallist *gl1, struct localvars *lv2, struct globallist *gl2);
int isboring(struct localvars *lv);
#endif
