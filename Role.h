#ifndef ROLE
#define ROLE
#include "RoleInference.h"
#include "CalculateDominators.h"

struct role {
  int hashcode;
  struct classname * class;
  struct rolereferencelist * dominatingroots;
  struct rolefieldlist * pointedtofl;
  struct rolearraylist * pointedtoal;
  struct identity_relation * identities;
  struct rolefieldlist * nonnullfields;
  struct rolearraylist * nonnullarrays;
  int * methodscalled;
};

struct rolereferencelist {
  struct fieldname * globalname;
  struct methodname * methodname;
  char * lvname;
  char * sourcename;
  int linenumber;
  struct rolereferencelist *next;
};

struct rolearraylist {
  struct classname * class;
  int role;
  int duplicates;
  struct rolearraylist * next;
};

struct rolechange {
  char *origrole;
  long long uid;
  char *newrole;
};

struct rolefieldlist {
  struct fieldname * field;
  int role;
  int duplicates;
  struct rolefieldlist * next;
};

void printrole(struct heap_state *heap,struct role * r, char * rolename);
void freerole(struct role * r);
struct role * calculaterole(struct heap_state *heap, struct genhashtable * dommapping,struct heap_object *ho, int enterexit);
struct identity_relation * find_identities(struct heap_state *heap, struct heap_object *ho);
void free_identities(struct identity_relation *irptr);
void print_identities(struct heap_state *heap,struct identity_relation *irptr);

void sortidentities(struct role *role);
int comparedomroots(struct rolereferencelist *r1, struct rolereferencelist *r2);
void insertdomroot(struct role * role, struct rolereferencelist * domroots);
void insertnonfl(struct role * role, struct rolefieldlist * domroots);
void insertnonal(struct role * role, struct rolearraylist * domroots);
void insertrfl(struct role * role, struct rolefieldlist * domroots);
void insertral(struct role * role, struct rolearraylist * domroots);
int fieldcompare(struct rolefieldlist *field1, struct rolefieldlist *field2);
int arraycompare(struct rolearraylist *array1, struct rolearraylist *array2);
int compareidentity(struct identity_relation *ir1, struct identity_relation *ir2);
int equivalentroles(struct role *role1, struct role *role2);
void assignhashcode(struct role * role);
int rolehashcode(struct role *role);
int hashstring(char *strptr);
char * findrolestring(struct heap_state * heap, struct genhashtable * dommapping,struct heap_object *ho, int enterexit);
int equivalentstrings(char *str1, char *str2);
void setheapstate(struct heap_state *hs);
int rchashcode(struct rolechange *rc);
int equivalentrc(struct rolechange *rc1, struct rolechange *rc2);
void rolechange(struct heap_state *hs, struct genhashtable * dommapping, struct heap_object *ho, char *newrole,int enterexit);
void printrolechange(struct heap_state * hs, struct rolechange *rc);
int parserolestring(char * input);
#endif
