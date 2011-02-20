#include <stdio.h>
#include "tmap.h"
#include "size.h"
extern "C" {
#include "redblack.h"
#include "stack.h"
}

#define CHECKTYPE
#define CHECKMEMORY

typemap::typemap(typeobject * size) {
  alloctree=rbinit();
  typetree=rbinit();
  this->size=size;
  this->low=GC_linux_stack_base();
}

void freefunction(void *ptr) {
  if(ptr!=NULL) {
    delete((structuremap *)ptr);
  }
}

typemap::~typemap() {
  rbdestroy(typetree,freefunction);
  rbdestroy(alloctree,freefunction);
}

void typemap::reset() {
  rbdestroy(typetree,freefunction);
  typetree=rbinit();
  if (low<high) 
    rbdelete(low,alloctree);
  else
    rbdelete(high,alloctree);
}

void typemap::initializestack(void *high) {
  this->high=high;
  if (low<high) 
    rbinsert(low,high,NULL,alloctree);
  else
    rbinsert(high,low,NULL,alloctree);
}

structuremap::structuremap(int s) {
  str=s;
  typetree=rbinit();
}

structuremap::~structuremap() {
  rbdestroy(typetree,freefunction);
}

bool typemap::asserttype(void *ptr, int s) {
  int toadd=size->sizeBytes(s);
  return asserttype(ptr,((char *) ptr)+toadd,s);
}

bool typemap::asserttype(void *ptr, void *high, int s) {
#ifdef CHECKTYPE
  bool b=checktype(true,ptr,s);
  if (!b) {
    printf("Assertion failure\n");
    bool testb=checktype(true,ptr,s);
  }
  return b;
#endif
  return assertvalidmemory(ptr, high);
}

bool typemap::assertvalidmemory(void* low, int s) {
  int toadd=size->sizeBytes(s);
  return assertvalidmemory(low,((char *)low)+toadd);
}

bool typemap::assertvalidmemory(void* low, void* high) {
#ifdef CHECKMEMORY
  return checkmemory(low, high);
#endif
  return true;
}

bool typemap::istype(void *ptr, void *high, int s) {
#ifdef CHECKTYPE
  bool b=checktype(false,ptr,s);
  if (!b) {
    printf("Verify failure\n");
    bool testb=checktype(false,ptr,s);
  }
  return b;
#endif
  return assertvalidmemory(ptr, high);
}

void typemap::allocate(void *ptr, int size) {
  void *low=ptr;
  void *high=((char *)ptr)+size;
  int val=rbinsert(low,high,NULL,alloctree);
  if (val==0)
    printf("Error\n");
}

inline int sizeinbytes(unsigned int bits) {
  int bytes=bits>>3;
  if (bits %8)
    bytes++;
  return bytes;
}

int typemap::findoffsetstructure(int s, int offset) {
  int count=0;
  for(int i=0;i<size->getnumfields(s);i++) {
    int mult=1;
    int ttype=size->getfield(s,i);
    if (size->isArray(s,i)) {
      mult=size->numElements(s,i);
    }
    int increment=size->size(ttype);
    if (increment%8) {
      int delt=offset-count;
      int byteincrement=increment/8;
      if (delt<mult*byteincrement) {
	if (delt%byteincrement==0) {
	  return ttype;
	} else
	  return -1;
      }
    } else {
      if ((count+sizeinbytes(mult*increment))>offset)
	return -1;
    }
    count+=sizeinbytes(mult*increment);
  }
  return -1;
}

void typemap::deallocate(void *ptr) {
  if (rbdelete(ptr,alloctree)==NULL)
    printf("Freeing unallocated memory\n");
}

bool typemap::checkmemory(void* low, void* high) {
  struct pair allocp=rbfind(low,high,alloctree);
  if (allocp.low == NULL) {
    return false; 
  } else if ((allocp.low > low) || (allocp.high < high)) { /* make sure this block is used */
    return false;
  } else {
    return true;
  }
}


bool typemap::checktype(bool doaction,void *ptr, int structure) {
  int ssize=size->sizeBytes(structure);
  void *low=ptr;
  void *high=((char *)low)+ssize;
  struct pair allocp=rbfind(low,high,alloctree);
  if (allocp.low==NULL)
    return false;
  if (allocp.low>low||allocp.high<high) /* make sure this block is used */
    return false;
  struct pair typep=rbfind(low,high,typetree);
  structuremap *smap=(structuremap *)rblookup(low,high,typetree);
  if (typep.low==NULL) {
    if(!doaction)
      return true;
    structuremap *sm=new structuremap(structure);
    int flag=rbinsert(low, high, sm, typetree);
    if (flag==0) {
      printf("Error in asserttype\n");
      return false;
    } else
      return true;
  }
  return checktype(doaction, low,high, structure, typetree);
}

bool typemap::checktype(bool doaction, void *low, void *high, int structure, struct rbtree *ttree) {
  struct pair typep=rbfind(low,high,ttree);
  structuremap *smap=(structuremap *)rblookup(low,high,ttree);
  if (typep.low==low&&typep.high==high) {
    /* Recast */
    if (size->issubtype(structure,smap->str)) {
      /* narrowing cast */
      if (!doaction)
	return true;
      smap->str=structure;
      return true;
    } else if (size->issubtype(smap->str,structure)) {
      /* widening cast */
      return true;
    } else
      return false; /* incompatible types */
  } else if (typep.low<=low&&typep.high>=high) {
    /* See if it matches up with structure inside typep */
    if (rbsearch(low,high,smap->typetree)) {
      /* recurse */
      return checktype(doaction,low,high, structure, smap->typetree);
    } else {
      /* check to see if data lines up correctly */
      int offset=((char *)low)-((char *)typep.low);
      int st=findoffsetstructure(smap->str,offset);
      if (st==-1)
	return false;
      if (size->issubtype(structure,st)) {
	if (!doaction)
	  return true;
	structuremap *newsm=new structuremap(structure);
	int flag=rbinsert(low, high, newsm, smap->typetree);
	return (flag==1);
      } else if (size->issubtype(st,structure)) {
	if (!doaction)
	  return true;
	structuremap *newsm=new structuremap(st);
	int flag=rbinsert(low, high, newsm, smap->typetree);
	return (flag==1);
      } else
	return false;
    }
  } else
    return false;
}
