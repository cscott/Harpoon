#ifndef TMAP_H
#define TMAP_H
#include "classlist.h"

class typemap {
 public:
  typemap(model *);
  ~typemap();
  void allocate(void *, int);
  void deallocate(void *);
  bool asserttype(void *ptr, structure *structure);
  bool istype(void *ptr, structure *structure);
  void reset();
 private:
  bool checktype(bool doaction,void *ptr, structure *structure);
  bool checktype(bool doaction, void *low, void *high,structure *structure, struct rbtree *ttree);
  structure * findoffsetstructure(structure *s, int offset);
  model *globalmodel;
  struct rbtree *alloctree;
  struct rbtree *typetree;
};

class structuremap {
  public:
  structuremap(structure *s);
  ~structuremap();
  structure *str;
  struct rbtree *typetree;
};
#endif
