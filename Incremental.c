#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "RoleInference.h"
#include "CalculateDominators.h"
#include "Role.h"
#include "Method.h"
#include "Effects.h"
#include "dot.h"
#include "Container.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

void doincrementalreachability(struct heap_state *heap, struct hashtable *ht, int enterexit) {
  struct objectset * changedset;

  changedset=dokills(heap,ht);
  donews(heap, changedset,ht);

  {
    /* Kill dead reference structures*/
    struct referencelist * rl=heap->freelist;
    heap->freelist=NULL;
    while (rl!=NULL) {
      struct referencelist * nxtrl=rl->next;
      if (rl->lv!=NULL)
	free(rl->lv);
      else if(rl->gl!=NULL) {
	free(rl->gl);
      }
      free(rl);
      rl=nxtrl;
    }
  }

  /* Lets do our GC now...*/
  while(1) {
    struct heap_object * possiblegarbage=removeobject(changedset,NULL);
    if (possiblegarbage==NULL)
      break;
    if ((possiblegarbage->rl==NULL)&&((possiblegarbage->reachable&REACHABLEMASK)==0)) {
      /*We've got real garbage!!!*/
      struct fieldlist *fl;
      struct arraylist *al;
      /* Record role change to garbage role */
      rolechange(heap,NULL, possiblegarbage,"GARB",2+enterexit);
      removeobject(heap->changedset, possiblegarbage);

      
      if (heap->options&OPTION_FCONTAINERS) {
	int policy=1;
	if(gencontains(heap->policytable, possiblegarbage->class)) {
	  struct policyobject *po=(struct policyobject *) gengettable(heap->policytable, possiblegarbage->class);
	  policy=po->policy;
	} else if (heap->options&OPTION_DEFAULTONEATTIME)
	  policy=2;
	switch(policy) {
	case 0:
	  break;
	case 1:
	case 2:
	  if ((possiblegarbage->reachable&FIRSTREF)&&
	      !(possiblegarbage->reachable&NOTCONTAINER))
	    recordcontainer(heap,possiblegarbage);
	  break;
	case 3:
	  recordcontainer(heap,possiblegarbage);
	  break;
	}
      }

      

      /* Have to remove references to ourself first*/
      fl=possiblegarbage->reversefield;
      al=possiblegarbage->reversearray;
      while(fl!=NULL) {
	struct fieldlist *tmp=fl->dstnext;
	removeforwardfieldreference(fl);
	free(fl);
	fl=tmp;
      }
      while(al!=NULL) {
	struct arraylist *tmp=al->dstnext;
	removeforwardarrayreference(al);
	free(al);
	al=tmp;
      }

      /* Now remove references we own*/
      fl=possiblegarbage->fl;
      al=possiblegarbage->al;
      while(fl!=NULL) {
	struct fieldlist *tmp=fl->next;
	removereversefieldreference(fl);
	free(fl);
	fl=tmp;
      }
      while(al!=NULL) {
	struct arraylist *tmp=al->next;
	removereversearrayreference(al);
	free(al);
	al=tmp;
      }
      free(possiblegarbage->methodscalled);
      /*printf("Freeing Key %lld\n",possiblegarbage->uid);*/
      freekey(ht,possiblegarbage->uid);
      free(possiblegarbage);
    } else {
      /* Possible role change due to reachability*/
      addobject(heap->changedset,possiblegarbage);
    }
  }

  freeobjectset(changedset);
}

struct objectset * dokills(struct heap_state *heap, struct hashtable *ht) {
  /* Flush out old reachability information */
  /* Remove K set */
  struct killtuplelist * kptr=NULL;
  struct objectpair * op=heap->K;
  struct objectset *os=createobjectset();
  
  while(!isEmptyOP(op)) {
    struct objectpairlist *opl=removeobjectpair(op);
    struct heap_object *dst=objectpairdst(opl);
    struct heap_object *srcobj=objectpairsrcobj(opl);
    struct globallist *srcglb=objectpairsrcglb(opl);
    struct localvars *srcvar=objectpairsrcvar(opl);
    

    if (srcvar!=NULL) {
      struct killtuplelist *ktpl=(struct killtuplelist *) calloc(1,sizeof(struct killtuplelist));
      struct referencelist *rl=(struct referencelist *) calloc(1,sizeof(struct referencelist));
      rl->lv=srcvar;
      ktpl->rl=rl;
      ktpl->ho=dst;
      ktpl->next=kptr;
      kptr=ktpl;
    } else if(srcglb!=NULL) {
      struct killtuplelist *ktpl=(struct killtuplelist *) calloc(1,sizeof(struct killtuplelist));
      struct referencelist *rl=(struct referencelist *) calloc(1,sizeof(struct referencelist));
      rl->gl=srcglb;
      ktpl->rl=rl;
      ktpl->ho=dst;
      ktpl->next=kptr;
      kptr=ktpl;
    } else if(srcobj!=NULL) {
      struct killtuplelist *ktpl=(struct killtuplelist *) calloc(1,sizeof(struct killtuplelist));
      struct referencelist *rtmp=srcobj->rl;
      if (rtmp!=NULL) {
	ktpl->ho=dst;
	if ((srcobj->reachable&REACHABLEMASK)!=0)
	  ktpl->reachable=1;
	ktpl->next=kptr;
	kptr=ktpl;
	/*loop through src*/
	while(rtmp!=NULL) {
	  if (((rtmp->lv!=NULL)&&(rtmp->lv->invalid==0)&&(rtmp->lv->object==dst))||((rtmp->gl!=NULL)&&(rtmp->gl->invalid==0)&&(rtmp->gl->object==dst))) {
	    /*Don't propagate kills to object being pointed to by live localpointer*/
	  } else {
	    struct referencelist *rkl=(struct referencelist *) calloc(1,sizeof(struct referencelist));
	    rkl->gl=rtmp->gl;
	    rkl->lv=rtmp->lv;
	    rkl->next=ktpl->rl;
	    ktpl->rl=rkl;
	  }
	  rtmp=rtmp->next;
	}
      }
    } else printf("XXXXXXXXXX: ERROR in dokills");
    freeobjectpairlist(opl);
  }
  while(kptr!=NULL) {
    struct killtuplelist * tuple=kptr;
    struct heap_object * ho=tuple->ho;
    struct fieldlist * fl=ho->fl;
    struct arraylist * al=ho->al;
    struct referencelist * orl=ho->rl;

    kptr=kptr->next;
    /*    addobject(os, tuple->ho);*/
    {
      /*Add all incoming nodes to set...*/
      struct arraylist *ral=ho->reversearray;
      struct fieldlist *rfl=ho->reversefield;
      while(ral!=NULL) {
	addobject(os,ral->src);
	ral=ral->dstnext;
      }
      while(rfl!=NULL) {
	addobject(os,rfl->src);
	rfl=rfl->dstnext;
      }
      addobject(os,ho); /*Allow gc of ho if everything is unreachable*/
    }
    
    
    /*cycle through the lists*/
    /*adding only if R(t) intersect S!={}*/
    while(fl!=NULL) {
#ifdef EFFECTS
      if (!(heap->options&OPTION_NOEFFECTS)) {
	addpath(heap, ho->uid, fl->fieldname, fl->object->uid);
      }
#endif
      if (matchlist(fl->object->rl, tuple->rl)||(((fl->object->reachable&REACHABLEMASK)==1)&&(tuple->reachable==1))) {
	struct killtuplelist *ktpl=(struct killtuplelist *) calloc(1,sizeof(struct killtuplelist));
	struct referencelist *rtmp=tuple->rl;
	if ((fl->object->reachable&REACHABLEMASK)==1)
	  ktpl->reachable=tuple->reachable;
	if (rtmp!=NULL) {
	  ktpl->ho=fl->object;
	  ktpl->next=kptr;
	  kptr=ktpl;
	  /*loop through src*/
	  while(rtmp!=NULL) {
	    if (((rtmp->lv!=NULL)&&(rtmp->lv->invalid==0)&&(rtmp->lv->object==fl->object))||((rtmp->gl!=NULL)&&(rtmp->gl->invalid==0)&&(rtmp->gl->object==fl->object))) {
	    /*Don't propagate kills to object being pointed to by live localpointer*/
	    } else {
	      struct referencelist *rkl=(struct referencelist *) calloc(1,sizeof(struct referencelist));
	      rkl->gl=rtmp->gl;
	      rkl->lv=rtmp->lv;
	      rkl->next=ktpl->rl;
	      ktpl->rl=rkl;
	    }
	    rtmp=rtmp->next;
	  }
	} else printf("WEIRDNESS\n");
      }
      fl=fl->next;
    }
    
    /*cycle through the lists*/
    /*adding only if R(t) intersect S!={}*/
    while(al!=NULL) {
#ifdef EFFECTS
      if (!(heap->options&OPTION_NOEFFECTS)) {
	addarraypath(heap, ht, ho->uid, al->object->uid);
      }
#endif
      if (matchlist(al->object->rl, tuple->rl)||(((al->object->reachable&REACHABLEMASK)==1)&&(tuple->reachable==1))) {
	struct killtuplelist *ktpl=(struct killtuplelist *) calloc(1,sizeof(struct killtuplelist));
	struct referencelist *rtmp=tuple->rl;
	if (rtmp!=NULL) {
	  ktpl->ho=al->object;
	  ktpl->next=kptr;
	  kptr=ktpl;
	  if ((al->object->reachable&REACHABLEMASK)==1)
	    ktpl->reachable=tuple->reachable;
	  /*loop through src*/
	  while(rtmp!=NULL) {
	    if (((rtmp->lv!=NULL)&&(rtmp->lv->invalid==0)&&(rtmp->lv->object==al->object))||((rtmp->gl!=NULL)&&(rtmp->gl->invalid==0)&&(rtmp->gl->object==al->object))) {
	      /*Don't propagate kills to object being pointed to by live localpointer*/
	    } else {
	      struct referencelist *rkl=(struct referencelist *) calloc(1,sizeof(struct referencelist));
	      rkl->gl=rtmp->gl;
	      rkl->lv=rtmp->lv;
	      rkl->next=ktpl->rl;
	      ktpl->rl=rkl;
	    }
	    rtmp=rtmp->next;
	  }
	} else printf("WEIRDNESS\n");
      }
      al=al->next;
    }
    
    /*fix our object*/
    
    if (((ho->reachable&REACHABLEMASK)==1)&&(tuple->reachable==1))
      ho->reachable&=OTHERMASK;
    
    while((orl!=NULL)&&(matchrl(orl,tuple->rl))) {
      ho->rl=orl->next;
      free(orl);
      orl=ho->rl;
    }
    
    if(orl!=NULL)
      while(orl->next!=NULL) {
	if(matchrl(orl->next, tuple->rl)) {
	  struct referencelist *tmpptr=orl->next;
	  orl->next=orl->next->next;
	  free(tmpptr);
	} else
	  orl=orl->next;
      }
    freekilltuplelist(tuple);
  }


  return os;
}



void donews(struct heap_state *heap, struct objectset * os, struct hashtable *ht) {
  struct ositerator *it=getIterator(os);
  struct objectset *N=heap->N;
  while(hasNext(it)) {
    struct heap_object* ho=nextobject(it);
    addobject(N, ho);
  }
  freeIterator(it);
  /*N set now has all elements of interest*/

  /* Add root edges in reachability info*/
  while(heap->newreferences!=NULL) {
    struct referencelist *rl=heap->newreferences;
    struct heap_object *ho=NULL;
    heap->newreferences=rl->next;

    if(rl->lv!=NULL) {
      /* handle lv*/
      if (rl->lv->invalid==0)
	ho=rl->lv->object;
    } else {
      /* handle gl*/
      if (rl->gl->invalid==0)
	ho=rl->gl->object;
    }
    
    if(ho!=NULL) {
      rl->next=ho->rl;
      ho->rl=rl;
    } else
      free(rl);
    /*invalidated pointer case*/
  }

  while(1) {
    struct heap_object *object=removeobject(N,NULL);

    if (object==NULL)
      break; /* We're done!!!! */
    else {
      struct arraylist *al=object->al;
      struct fieldlist *fl=object->fl;

      /* Cycle through all fields/array indexes*/
      while(al!=NULL) {
#ifdef EFFECTS
	if (!(heap->options&OPTION_NOEFFECTS)) {
	  addarraypath(heap, ht, object->uid, al->object->uid);
	}
#endif
	propagaterinfo(N, object, al->object);
	al=al->next;
      }
      while(fl!=NULL) {
#ifdef EFFECTS
	if (!(heap->options&OPTION_NOEFFECTS)) {
	  addpath(heap, object->uid, fl->fieldname, fl->object->uid);
	}
#endif
	propagaterinfo(N, object, fl->object);
	fl=fl->next;
      }
    }
  }
}

void propagaterinfo(struct objectset * set, struct heap_object *src, struct heap_object *dst) {
  int addedsomething=0;
  struct referencelist *srclist=src->rl;
  struct referencelist *dstlist=dst->rl;
  if (((src->reachable&REACHABLEMASK)!=0)&&((dst->reachable&REACHABLEMASK)==0)) {
    dst->reachable|=1;
    addedsomething=1;
  }

  while(srclist!=NULL) {
    if (!matchrl(srclist, dstlist)) {
      struct referencelist *ref=(struct referencelist *)calloc(1,sizeof(struct referencelist));
      ref->lv=srclist->lv;
      ref->gl=srclist->gl;
      ref->next=dst->rl;
      dst->rl=ref;
      addedsomething=1;
    }
    srclist=srclist->next;
  }
  /* Do we need to further propagate changes?*/
  if(addedsomething) 
    addobject(set, dst);
}
