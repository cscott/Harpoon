#ifndef CONTAINER
#define CONTAINER
#include "RoleInference.h"

void examineheap(struct heap_state *hs, struct hashtable *ht);
void opencontainerfile(struct heap_state *hs);
void closecontainerfile(struct heap_state *hs);
void recordcontainer(struct heap_state *hs, struct heap_object *ho);
void openreadcontainer(struct heap_state *hs);

#endif
