#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include "CalculateDominators.h"

struct referencelist * calculatedominators(struct genhashtable * dommapping,struct heap_object *ho) {
  struct referencelist *rl=ho->rl;
  struct referencelist *dominators=NULL;
  while(rl!=NULL) {
    if ((*((int*)gengettable(dommapping, (rl->lv!=NULL)?((void *)rl->lv):((void *)rl->gl))))==1) {
      struct referencelist * tmpptr=(struct referencelist *)calloc(1, sizeof(struct referencelist));
      tmpptr->lv=rl->lv;
      tmpptr->gl=rl->gl;
      tmpptr->next=dominators;
      dominators=tmpptr;
    }
    rl=rl->next;
  }
  return dominators;
}

struct genhashtable * builddominatormappings(struct heap_state *heap) {
  struct genhashtable * mindominator=genallocatehashtable();
  struct method *m=heap->methodlist;

  while(m!=NULL) {
    struct localvars *lv=m->lv;
    while(lv!=NULL) {
      genputtable(mindominator, lv,minimaldominatorset(lv,NULL));
      lv=lv->next;
    }
    m=m->caller;
  }

  {
    struct globallist *globals=heap->gl;
    while(globals!=NULL) {
      genputtable(mindominator, globals,minimaldominatorset(NULL,globals));
      globals=globals->next;
    }
  }
  return mindominator;
}

int * minimaldominatorset(struct localvars * lv, struct globallist *gl) {
  struct heap_object * ho=(lv!=NULL)?lv->object:gl->object;
  struct referencelist *rl2=ho->rl;
  int * in=(int *) malloc(sizeof(int));

  while (rl2!=NULL) {
    if(dominates(rl2->lv,rl2->gl,lv,gl))
      break;
    rl2=rl2->next;
  }
  if (rl2==NULL)
    *in=1;
  else
    *in=0;
  return in;
}


int dominates(struct localvars *lv1, struct globallist *gl1, struct localvars *lv2, struct globallist *gl2) {
  struct heap_object * destobj,* srcobj;
  int age1,age2;

  if (lv2!=NULL) {
    destobj=lv2->object;
    age2=lv2->age;
  }
  else {
    destobj=gl2->object;
    age2=gl2->age;
  }

  if (lv1!=NULL) {
    srcobj=lv1->object;
    age1=lv1->age;
  } else {
    srcobj=gl1->object;
    age1=gl1->age;
  }
  
  {
    struct referencelist *rl=destobj->rl;

    while(rl!=NULL) {
      if((rl->lv==lv1)&&(rl->gl==gl1))
	break;
      rl=rl->next;
    }
    if (rl==NULL)
      return 0;


    rl=srcobj->rl;

    while(rl!=NULL) {
      if((rl->lv==lv2)&&(rl->gl==gl2))
	break;
      rl=rl->next;
    }
    if (rl==NULL)
      return 1;
    else if (age1>age2)
      return 1;
    else return 0;
  }
  
}
