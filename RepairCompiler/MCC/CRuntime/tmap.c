#include <stdio.h>
#include "tmap.h"
#include "size.h"
#include "stack.h"
#include <stdlib.h>

#define CHECKTYPE
#define CHECKMEMORY

struct typemap * allocatetypemap() {
  struct typemap *thisvar=(struct typemap *) malloc(sizeof(struct typemap));
  thisvar->alloctree=rbinit();
  thisvar->typetree=rbinit();
  thisvar->low=GC_linux_stack_base();
  return thisvar;
}

void freefunction(void *ptr) {
  if(ptr!=NULL) {
    free((struct structuremap *)ptr);
  }
}

void freetypemap(struct typemap * ptr) {
  rbdestroy(ptr->typetree,freefunction);
  rbdestroy(ptr->alloctree,freefunction);
  free(ptr);
}

void typemapreset(struct typemap *ptr) {
  rbdestroy(ptr->typetree,freefunction);
  ptr->typetree=rbinit();
  if (ptr->low<ptr->high)
    rbdelete(ptr->low,ptr->alloctree);
  else
    rbdelete(ptr->high,ptr->alloctree);
}

void initializetypemapstack(struct typemap * ptr, void *high) {
  ptr->high=high;
  if (ptr->low<ptr->high)
    rbinsert(ptr->low,ptr->high,NULL,ptr->alloctree);
  else
    rbinsert(ptr->high,ptr->low,NULL,ptr->alloctree);
}

struct structuremap * allocatestructuremap(int s) {
  struct structuremap *ptr=(struct structuremap *)malloc(sizeof(struct structuremap));
  ptr->str=s;
  ptr->typetree=rbinit();
  return ptr;
}

void freestructuremap(struct structuremap *ptr) {
  rbdestroy(ptr->typetree,freefunction);
  free(ptr);
}

bool typemapasserttype(struct typemap *thisvar, void *ptr, int s) {
  int toadd=sizeBytes(s);
  return typemapasserttypeB(thisvar, ptr,((char *) ptr)+toadd,s);
}

bool typemapasserttypeB(struct typemap * thisvar, void *ptr, void *high, int s) {
#ifdef CHECKTYPE
  bool b=typemapchecktype(thisvar,true,ptr,s);
  if (!b) {
    printf("Assertion failure\n");
    {
      bool testb=typemapchecktype(thisvar,true,ptr,s);
    }
  }
  return b;
#endif
  return typemapassertvalidmemoryB(thisvar,ptr, high);
}

bool typemapassertvalidmemory(struct typemap * thisvar, void* low, int s) {
  int toadd=sizeBytes(s);
  return typemapassertvalidmemoryB(thisvar, low,((char *)low)+toadd);
}

bool typemapassertvalidmemoryB(struct typemap * thisvar, void* low, void* high) {
#ifdef CHECKMEMORY
  return typemapcheckmemory(thisvar, low, high);
#endif
  return true;
}

bool typemapistype(struct typemap *thisvar, void *ptr, void *high, int s) {
#ifdef CHECKTYPE
  bool b=typemapchecktype(thisvar, false,ptr,s);
  if (!b) {
    printf("Verify failure\n");
    {
      bool testb=typemapchecktype(thisvar, false,ptr,s);
    }
  }
  return b;
#endif
  return typemapassertvalidmemoryB(thisvar, ptr, high);
}

void typemapallocate(struct typemap *thisvar,void *ptr, int size) {
  void *low=ptr;
  void *high=((char *)ptr)+size;
  int val=rbinsert(low,high,NULL,thisvar->alloctree);
  if (val==0)
    printf("Error\n");
}

inline int sizeinbytes(unsigned int bits) {
  int bytes=bits>>3;
  if (bits %8)
    bytes++;
  return bytes;
}

int typemapfindoffsetstructure(struct typemap * thisvar, int s, int offset) {
  int count=0;
  int i;
  int increment;
  for(i=0;i<getnumfields(s);i++) {
    int mult=1;
    int ttype=getfield(s,i);
    if (isArray(s,i)) {
      mult=numElements(s,i);
    }
    increment=size(ttype);
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

void typemapdeallocate(struct typemap * thisvar,void *ptr) {
  if (rbdelete(ptr,thisvar->alloctree)==NULL)
    printf("Freeing unallocated memory\n");
}

bool typemapcheckmemory(struct typemap *thisvar, void* low, void* high) {
  struct pair allocp=rbfind(low,high,thisvar->alloctree);
  if (allocp.low == NULL) {
    return false;
  } else if ((allocp.low > low) || (allocp.high < high)) { /* make sure this block is used */
    return false;
  } else {
    return true;
  }
}


bool typemapchecktype(struct typemap *thisvar, bool doaction,void *ptr, int structure) {
  int ssize=sizeBytes(structure);
  void *low=ptr;
  void *high=((char *)low)+ssize;
  struct pair allocp=rbfind(low,high,thisvar->alloctree);
  if (allocp.low==NULL)
    return false;
  if (allocp.low>low||allocp.high<high) /* make sure this block is used */
    return false;
  {
    struct pair typep=rbfind(low,high,thisvar->typetree);
    struct structuremap *smap=(struct structuremap *)rblookup(low,high,thisvar->typetree);
    if (typep.low==NULL) {
      if(!doaction)
        return true;
      {
        struct structuremap *sm=allocatestructuremap(structure);
        int flag=rbinsert(low, high, sm, thisvar->typetree);
        if (flag==0) {
          printf("Error in asserttype\n");
          return false;
        } else
          return true;
      }
    }
    return typemapchecktypeB(thisvar, doaction, low,high, structure, thisvar->typetree);
  }
}

bool typemapchecktypeB(struct typemap *thisvar, bool doaction, void *low, void *high, int structure, struct rbtree *ttree) {
  struct pair typep=rbfind(low,high,ttree);
  struct structuremap *smap=(struct structuremap *)rblookup(low,high,ttree);
  if (typep.low==low&&typep.high==high) {
    /* Recast */
    if (issubtype(structure,smap->str)) {
      /* narrowing cast */
      if (!doaction)
	return true;
      smap->str=structure;
      return true;
    } else if (issubtype(smap->str,structure)) {
      /* widening cast */
      return true;
    } else
      return false; /* incompatible types */
  } else if (typep.low<=low&&typep.high>=high) {
    /* See if it matches up with structure inside typep */
    if (rbsearch(low,high,smap->typetree)) {
      /* recurse */
      return typemapchecktypeB(thisvar,doaction,low,high, structure, smap->typetree);
    } else {
      /* check to see if data lines up correctly */
      int offset=((char *)low)-((char *)typep.low);
      int st=typemapfindoffsetstructure(thisvar, smap->str,offset);
      if (st==-1)
	return false;
      if (issubtype(structure,st)) {
	if (!doaction)
	  return true;
        {
          struct structuremap *newsm=allocatestructuremap(structure);
          int flag=rbinsert(low, high, newsm, smap->typetree);
          return (flag==1);
        }
      } else if (issubtype(st,structure)) {
	if (!doaction)
	  return true;
        {
          struct structuremap *newsm=allocatestructuremap(st);
          int flag=rbinsert(low, high, newsm, smap->typetree);
          return (flag==1);
        }
      } else
	return false;
    }
  } else
    return false;
}
