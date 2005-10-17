#ifndef TMAP_H
#define TMAP_H
#include "classlist.h"
#include "redblack.h"

#ifndef bool
#define bool int
#endif

#ifndef true
#define true 1
#endif

#ifndef false
#define false 0
#endif

struct typemap * allocatetypemap();
void freetypemap(struct typemap *);
void typemapreset(struct typemap *);
void initializetypemapstack(struct typemap *, void *high);

void typemapallocate(struct typemap *, void *, int);
void typemapdeallocate(struct typemap *, void *);
bool typemapassertvalidmemoryB(struct typemap *, void* low, void* high);
bool typemapasserttypeB(struct typemap *, void *ptr, void *high, int structure);
bool typemapassertvalidmemory(struct typemap *, void* low, int structure);
bool typemapassertexactmemory(struct typemap *, void* low, int structure);
bool typemapasserttype(struct typemap *, void *ptr, int structure);
bool typemapistype(struct typemap *, void *ptr, void *high, int structure);
bool typemapcheckmemory(struct typemap *, void* low, void* high);
void * typemapgetendofblock(struct typemap *thisvar, void* low);
bool typemapchecktype(struct typemap *, bool doaction,void *ptr, int structure);
bool typemapchecktypeB(struct typemap *, bool doaction, void *low, void *high,int structure, struct rbtree *ttree);
int typemapfindoffsetstructure(struct typemap *, int s, int offset);


struct typemap {
  void *low;
  void *high;
  struct rbtree *alloctree;
  struct rbtree *typetree;
};

struct structuremap * allocatestructuremap(int s);
void freestructuremap(struct structuremap *);

struct structuremap {
  int str;
  struct rbtree *typetree;
};
#endif
