#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include "CalculateDominators.h"

#ifdef MDEBUG
#include <dmalloc.h>
#endif

#define NOBORINGDOM 1

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

struct genhashtable * builddominatormappings(struct heap_state *heap, int includecurrent) {
  struct genhashtable * mindominator=genallocatehashtable(NULL,NULL);
  struct method *m=heap->methodlist;

  if ((includecurrent==0)&&(m!=NULL)) {
    struct localvars *lv=m->lv;
    while(lv!=NULL) {
      int * i=(int *)malloc(sizeof(int));
      *i=0;
      genputtable(mindominator, lv, i);
      lv=lv->next;
    }
    m=m->caller;
  }

  while(m!=NULL) {
    struct localvars *lv=m->lv;
    while(lv!=NULL) {
      genputtable(mindominator, lv,minimaldominatorset(lv,NULL, heap, includecurrent));
      lv=lv->next;
    }
    m=m->caller;
  }

  {
    struct globallist *globals=heap->gl;
    while(globals!=NULL) {
      genputtable(mindominator, globals,minimaldominatorset(NULL,globals,heap,includecurrent));
      globals=globals->next;
    }
  }
  return mindominator;
}

int * minimaldominatorset(struct localvars * lv, struct globallist *gl, struct heap_state *heap, int includecurrent) {
  struct heap_object * ho=(lv!=NULL)?lv->object:gl->object;
  struct referencelist *rl2=ho->rl;
  int * in=(int *) malloc(sizeof(int));

#ifdef NOBORINGDOM
  if (lv!=NULL&&isboring(lv)) {
    *in=0;
    return in;
  }
#endif
      
  while (rl2!=NULL) {
    if((rl2->lv!=NULL)&&(includecurrent==0)&&(rl2->lv->m==heap->methodlist))
      ;
    else if(dominates(rl2->lv,rl2->gl,lv,gl))
      break;
    rl2=rl2->next;
  }
  if (rl2==NULL)
    *in=1;
  else
    *in=0;
  return in;
}

int isboring(struct localvars *lv) {
  if ((lv->name[0]=='s') &&(lv->name[1]=='t')&&(lv->name[2]=='k'))
    return 1;
  else
    return 0;
}

int dominates(struct localvars *lv1, struct globallist *gl1, struct localvars *lv2, struct globallist *gl2) {
  struct heap_object * destobj,* srcobj;
  int age1,age2;

#ifdef NOBORINGDOM
  if(lv1!=NULL&&isboring(lv1))
    return 0;
#endif

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
    else if (age1<age2)
      return 1;
    else return 0;
  }
  
}
