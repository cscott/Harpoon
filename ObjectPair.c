#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include "ObjectPair.h"
/*#include <dmalloc.h>*/

struct objectpair * createobjectpair() {
  return (struct objectpair *) calloc(1,sizeof(struct objectpair));
}

struct heap_object * objectpairsrcobj(struct objectpairlist *os) {
  return os->srcobj;
}

struct localvars * objectpairsrcvar(struct objectpairlist *os) {
  return os->srcvar;
}

struct globallist * objectpairsrcglb(struct objectpairlist *os) {
  return os->srcglb;
}

struct heap_object * objectpairdst(struct objectpairlist *os) {
  return os->dst;
}

struct objectpairlist * removeobjectpair(struct objectpair *os) {
  if (os->head!=NULL) {
    struct objectpairlist * olptr=os->head;
    long int bin=ophashfunction(olptr->srcobj, (olptr->srcvar==NULL)?((void *)olptr->srcglb):((void *)olptr->srcvar), olptr->dst);
    /* remove from masterlist */
    os->head=os->head->next;

    /* remove from bin */
    if (os->ol[bin]==olptr) {
      os->ol[bin]=olptr->binnext;
    } else {
      struct objectpairlist * binhead=os->ol[bin];

      /* This case should not happen...but we'll handle it anyways*/
      while(binhead->binnext!=NULL) {
	if(binhead->binnext==olptr) {
	  binhead->binnext=olptr->binnext;
	  break;
	}
	binhead=binhead->binnext;
      }
      if (binhead->binnext==NULL)
	printf("XXXXX: Failed remove from bin\n");
    }
    olptr->next=NULL;
    olptr->binnext=NULL;
    return olptr;
  } else 
    return NULL;
}

int isEmptyOP(struct objectpair *op) {
  return (op->head==NULL);
}

void addobjectpair(struct objectpair *os, struct heap_object *src, struct localvars *lv, struct globallist *glb, struct heap_object *dst) {
  long int bin=ophashfunction(src, (lv==NULL)?((void *)glb):((void *)lv), dst);

  struct objectpairlist *binhead=os->ol[bin];
  
  if (binhead!=NULL) {
    /*Search bin */
    while(binhead!=NULL) {
      /* If already in list, we are done */
      if((binhead->dst==dst)&&(binhead->srcvar==lv)
	 &&(binhead->srcglb==glb)&&(binhead->srcobj==src))
	return;
      binhead=binhead->binnext;
    }
  }
  {
    struct objectpairlist *ol=(struct objectpairlist *)calloc(1,sizeof(struct objectpairlist));
    ol->srcobj=src;
    ol->dst=dst;
    ol->srcvar=lv;
    ol->srcglb=glb;
    /* Add to list of objects */
    ol->next=os->head;
    os->head=ol;
    
    /* Add into bin */
    /* Check to see if bin has item*/
    ol->binnext=os->ol[bin];
    os->ol[bin]=ol;

    return;
  }
}

long int ophashfunction(struct heap_object *src, void *lv, struct heap_object *dst) {
  return ((((long int)src)<<2)^(((long int) lv)<<1)^((long int)dst))%OPHASHSIZE;
}

void freeobjectpairlist(struct objectpairlist *opl) {
  free(opl);
}

void freeobjectpair(struct objectpair *os) {
  struct objectpairlist *olptr=os->head;
  while(olptr!=NULL) {
    struct objectpairlist *nxtptr=olptr->next;
    free(olptr);
    olptr=nxtptr;
  }
  free(os);
}
