#ifndef TMAP_H
#define TMAP_H
#include "classlist.h"

class typemap {
 public:
  typemap(typeobject *);
  ~typemap();
  void allocate(void *, int);
  void deallocate(void *);
  bool assertvalidmemory(void* low, void* high);
  bool asserttype(void *ptr, void *high, int structure);
  bool assertvalidmemory(void* low, int structure);
  bool asserttype(void *ptr, int structure);
  bool istype(void *ptr, void *high, int structure);
  void reset();
  typeobject *size;
 private:
  bool checkmemory(void* low, void* high);
  bool checktype(bool doaction,void *ptr, int structure);
  bool checktype(bool doaction, void *low, void *high,int structure, struct rbtree *ttree);
  int findoffsetstructure(int s, int offset);
  struct rbtree *alloctree;
  struct rbtree *typetree;
};

class structuremap {
  public:
  structuremap(int s);
  ~structuremap();
  int str;
  struct rbtree *typetree;
};
#endif
