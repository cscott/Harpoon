#ifndef WEB
#define WEB
#include "RoleInference.h"
#include "Role.h"
#include "dot.h"

void outputweb(struct heap_state *heap, struct genhashtable *dotmethodtable);
void printwebrole(FILE *rolefile, struct heap_state *heap,struct role *r, char * rolename);
void printwebmethod(FILE *methodfile, struct heap_state *heap, struct rolemethod *method);
void printwebrolechanges(FILE *methodfile, struct heap_state *heap, struct rolemethod *rm);
void print_webidentities(FILE *rolefile, struct heap_state *heap,struct identity_relation *irptr);
void printwebdot(FILE *dotfile,struct heap_state *heap,struct classname *class, struct dotclass *dotclass);
void outputwebrolerelations(struct heap_state *heap);
#endif
