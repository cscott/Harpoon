#ifndef ROLE
#define ROLE
#include "RoleInference.h"
#include "CalculateDominators.h"

void printrole(struct genhashtable * dommapping,struct heap_object *ho);

struct identity_relation * find_identities(struct heap_object *ho);
void free_identities(struct identity_relation *irptr);
void print_identities(struct identity_relation *irptr);

#endif
