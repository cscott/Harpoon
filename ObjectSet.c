#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <values.h>
#include "ObjectSet.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

struct objectset * createobjectset() {
  struct objectset *os=(struct objectset *) calloc(1,sizeof(struct objectset));
  os->objectsetsize=OSINITIALSIZE;
  os->ol=(struct objectlist **) calloc(OSINITIALSIZE, sizeof(struct objectlist *));
  return os;
}

struct ositerator * getIterator(struct objectset *os) {
  struct ositerator * it=(struct ositerator *) calloc(1,sizeof(struct ositerator));
  it->ol=os->head;
  return it;
}

void freeIterator(struct ositerator * it) {
  free(it);
}

struct heap_object * nextobject(struct ositerator * it) {
  struct heap_object * ho=(it->ol!=NULL)?it->ol->object:NULL;
  if (it->ol!=NULL) 
    it->ol=it->ol->next;
  return ho;
}

int hasNext(struct ositerator *it) {
  if (it->ol!=NULL)
    return 1;
  else
    return 0;
}

int setisempty(struct objectset *os) {
  return (os->head==NULL);
}

struct heap_object * removeobject(struct objectset *os, struct heap_object *ho) {
  if (os->head!=NULL) {
    struct objectlist * olptr;
    long int bin;

    if (ho==NULL) {
      olptr=os->head;
      ho=olptr->object;
      bin=oshashfunction(os,ho);
      /* remove from masterlist */
      os->head=os->head->next;
      if (os->head!=NULL)
	os->head->prev=NULL;
    } else {
      bin=oshashfunction(os,ho);
      olptr=os->ol[bin];
      while(olptr!=NULL&&olptr->object!=ho)
	olptr=olptr->binnext;
      if (olptr==NULL)
	return NULL;
      /* remove from masterlist */      
      if (olptr->prev!=NULL)
	olptr->prev->next=olptr->next;
      else
	os->head=os->head->next;  /* We must be first */
      if (olptr->next!=NULL)
	olptr->next->prev=olptr->prev;
    }



    /* remove from bin */
    if (os->ol[bin]==olptr) {
      os->ol[bin]=olptr->binnext;
    } else {
      struct objectlist * binhead=os->ol[bin];

      /* This case should not happen...but we'll handle it anyways*/
      while(binhead->binnext!=NULL) {
	if(binhead->binnext==olptr) {
	  binhead->binnext=olptr->binnext;
	  break;
	}
	binhead=binhead->binnext;
      }
    }
    free(olptr);
    os->counter--;
    return ho;
  } else 
    return NULL;
}

void addobject(struct objectset *os, struct heap_object *ho) {
  long int bin=oshashfunction(os, ho);
  struct objectlist *binhead=os->ol[bin];
  
  if (binhead!=NULL) {
    /*Search bin */
    while(binhead!=NULL) {
      /* If already in list, we are done */
      if(binhead->object==ho)
	return;
      binhead=binhead->binnext;
    }
  }
  {
    struct objectlist *ol=(struct objectlist *)calloc(1,sizeof(struct objectlist));
    ol->object=ho;
    /* Add to list of objects */
    ol->next=os->head;
    if (os->head!=NULL)
      os->head->prev=ol;
    os->head=ol;
    os->counter++;
    /* Add into bin */
    /* Check to see if bin has item*/
    if (os->ol[bin]!=NULL) {
      ol->binnext=os->ol[bin];
    }
    os->ol[bin]=ol;
    if (os->counter>os->objectsetsize&&os->objectsetsize!=MAXINT) {
      long newcurrentsize=(os->objectsetsize<(MAXINT/2))?os->objectsetsize*2:MAXINT;
      long oldcurrentsize=os->objectsetsize;
      struct objectlist **newbins=(struct objectlist **)calloc(newcurrentsize, sizeof(struct objectlist *));
      struct objectlist **oldbins=os->ol;
      long i;
      os->objectsetsize=newcurrentsize;
      for(i=0;i<oldcurrentsize;i++) {
	struct objectlist * tmpptr=oldbins[i];
	while(tmpptr!=NULL) {
	  int hashcode=oshashfunction(os, tmpptr->object);
	  struct objectlist *nextptr=tmpptr->binnext;
	  tmpptr->binnext=newbins[hashcode];
	  newbins[hashcode]=tmpptr;
	  tmpptr=nextptr;
	}
      }
      os->ol=newbins;
      free(oldbins);
    }
    return;
  }
}

long int oshashfunction(struct objectset *os, struct heap_object *ho) {
  return ((int)ho->uid) % os->objectsetsize;
}

void freeobjectset(struct objectset *os) {
  struct objectlist *olptr=os->head;
  while(olptr!=NULL) {
    struct objectlist *nxtptr=olptr->next;
    free(olptr);
    olptr=nxtptr;
  }
  free(os->ol);
  free(os);
}
