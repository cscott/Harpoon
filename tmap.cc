#include "tmap.h"
extern "C" {
#include "redblack.h"
}
#include <stdio.h>
#include "tmodel.h"
#include "model.h"
#include "processabstract.h"
#include "element.h"

typemap::typemap(model *m) {
  alloctree=rbinit();
  typetree=rbinit();
  globalmodel=m;
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
}

structuremap::structuremap(structure *s) {
  str=s;
  typetree=rbinit();
}

structuremap::~structuremap() {
  rbdestroy(typetree,freefunction);
}

bool typemap::asserttype(void *ptr, structure *s) {
  bool b=checktype(true,ptr,s);
  if (!b) {
    printf("Assertion failure\n");
    bool b=checktype(true,ptr,s);
  }
  return b;
}

bool typemap::istype(void *ptr, structure *s) {
  bool b=checktype(false,ptr,s);
  if (!b) {
    printf("Verify failure\n");
    bool b=checktype(false,ptr,s);
  }
  return b;
}

void typemap::allocate(void *ptr, int size) {
  void *low=ptr;
  void *high=((char *)ptr)+size;
  int val=rbinsert(low,high,NULL,alloctree);
  if (val==0)
    printf("Error\n");
}

structure * typemap::findoffsetstructure(structure *s, int offset) {
  int count=0;
  for(int i=0;i<s->getnumfields();i++) {
    int mult=1;
    ttype *ttype=s->getfield(i)->gettype();
    if (ttype->getsize()!=NULL) {
      Element * number=evaluateexpr(globalmodel,ttype->getsize(),globalmodel->gethashtable(),true,false);
      mult=number->intvalue();
      delete(number);
    }
    int increment=ttype->basesize(globalmodel->getbitreader(),globalmodel,globalmodel->gethashtable());

    int delt=offset-count;
    if (delt<mult*increment) {
      if (delt%increment==0) {
	return globalmodel->getstructure(ttype->getname());
      } else
	return NULL;
    }

    count+=mult*increment;
  }
  
  return NULL;
}

void typemap::deallocate(void *ptr) {
  if (rbdelete(ptr,alloctree)==NULL)
    printf("Freeing unallocated memory\n");
}

bool typemap::checktype(bool doaction,void *ptr, structure *structure) {
  int size=structure->getsize(globalmodel->getbitreader(),globalmodel,globalmodel->gethashtable());
  void *low=ptr;
  void *high=((char *)low)+size;
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


bool typemap::checktype(bool doaction, void *low, void *high,structure *structre, struct rbtree *ttree) {
  struct pair typep=rbfind(low,high,ttree);
  structuremap *smap=(structuremap *)rblookup(low,high,ttree);
  
  if (typep.low==low&&typep.high==high) {
    /* Recast */
    if (globalmodel->subtypeof(structre,smap->str)) {
      /* narrowing cast */
      if (!doaction)
	return true;
      smap->str=structre;
      return true;
    } else if (globalmodel->subtypeof(smap->str,structre)) {
      /* widening cast */
      return true;
    } else
      return false; /* incompatible types */
  } else if (typep.low<=low&&typep.high>=high) {
    /* See if it matches up with structure inside typep */
    if (rbsearch(low,high,smap->typetree)) {
      /* recurse */
      return checktype(doaction,low,high, structre, smap->typetree);
    } else {
      /* check to see if data lines up correctly */
      int offset=((char *)low)-((char *)typep.low);
      structure * st=findoffsetstructure(smap->str,offset);
      if (st==NULL)
	return false;
      if (globalmodel->subtypeof(structre,st)) {
	if (!doaction)
	  return true;
	structuremap *newsm=new structuremap(structre);
	int flag=rbinsert(low, high, newsm, smap->typetree);
	return (flag==1);
      } else if (globalmodel->subtypeof(st,structre)) {
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
