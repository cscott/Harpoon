#ifndef Relation_H
#define Relation_h
#include "classlist.h"

class Tuple {
 public:
  Tuple(void *l,void *r);
  Tuple();
  bool isnull();
  void *left,*right;
};



#define WRELATION_SINGDOMAIN 0x1
#define WRELATION_MANYDOMAIN 0x2
#define WRELATION_SINGRANGE 0x10
#define WRELATION_MANYRANGE 0x20

class WorkRelation {
 public:
  WorkRelation();
  WorkRelation(bool);
  bool contains(void *key, void*object);
  Tuple firstelement();
  Tuple getnextelement(void *left,void *right);
  void put(void *key, void*object);
  void remove(void *key, void *object);
  WorkSet* getset(void *key);
  void* getobj(void *key);
  WorkSet* invgetset(void *key);
  void* invgetobj(void *key);
  ~WorkRelation();
  void print();

 private:
  void destroyer(struct genhashtable *d);
  bool flag;
  struct genhashtable *forward, *inverse;
};
#endif



