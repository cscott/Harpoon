#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include "ObjectSet.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

struct objectset * createobjectset() {
  return (struct objectset *) calloc(1,sizeof(struct objectset));
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

struct heap_object * removeobject(struct objectset *os) {
  if (os->head!=NULL) {
    struct objectlist * olptr=os->head;
    struct heap_object *ho=olptr->object;
    long int bin=oshashfunction(ho);
    /* remove from masterlist */
    os->head=os->head->next;

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
    return ho;
  } else 
    return NULL;
}

void addobject(struct objectset *os, struct heap_object *ho) {
  long int bin=oshashfunction(ho);
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
    os->head=ol;
    
    /* Add into bin */
    /* Check to see if bin has item*/
    if (os->ol[bin]!=NULL) {
      ol->binnext=os->ol[bin];
    }
    os->ol[bin]=ol;

    return;
  }
}

long int oshashfunction(struct heap_object *ho) {
  return (ho->uid) % OSHASHSIZE;
}

void freeobjectset(struct objectset *os) {
  struct objectlist *olptr=os->head;
  while(olptr!=NULL) {
    struct objectlist *nxtptr=olptr->next;
    free(olptr);
    olptr=nxtptr;
  }
  free(os);
}
