#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include "Effects.h"
#include "Role.h"
#include "Method.h"
#define MAXREPSEQ 5

void addarraypath(struct heap_state *hs, struct hashtable * ht, long long obj, long long dstobj) {
  struct method *method=hs->methodlist;
  int newpath=0;
  while(method!=NULL) {
    struct hashtable * pathtable=method->pathtable;

    if (!contains(pathtable, dstobj)) {
      struct path * path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=obj;
      path->classname=copystr(((struct heap_object *)gettable(ht, obj))->class);
      /*Array dereferences are uniquely classified by array class*/
      path->fielddesc=NULL;
      
      path->fieldname=copystr("[ARRAY]");
      path->paramnum=-1;
      puttable(pathtable, dstobj, path);
      newpath=1;

    }
    
    method=method->caller;
  }
  if(newpath)
    addeffect(hs, -1, NULL, dstobj);
}

void addpath(struct heap_state *hs, long long obj, char * class, char * field, char * fielddesc, long long dstobj) {
  struct method *method=hs->methodlist;
  int newpath=0;
  while(method!=NULL) {
    struct hashtable * pathtable=method->pathtable;

    if (!contains(pathtable, dstobj)) {
      struct path * path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=obj;
      path->classname=copystr(class);
      path->fieldname=copystr(field);
      path->fielddesc=copystr(fielddesc);
      path->paramnum=-1;
      puttable(pathtable, dstobj, path);
      newpath=1;
    }
    
    method=method->caller;
  }
  if (newpath)
    addeffect(hs, -1, NULL, dstobj);
}

void addnewobjpath(struct heap_state *hs, long long obj) {
  struct method *method=hs->methodlist;
  int newpath=0;
  while(method!=NULL) {
    struct path * path=(struct path *) calloc(1,sizeof(struct path));
    struct hashtable * pathtable=method->pathtable;
    path->paramnum=-1;
    path->prev_obj=-1;
    puttable(pathtable, obj, path);
    method=method->caller;
    newpath=1;
  }
  if (newpath)
    addeffect(hs, -1, NULL, obj);
}

void freeeffects(struct path * pth) {
  free(pth->fieldname);
  free(pth->fielddesc);
  free(pth->globalname);
  free(pth->classname);
  free(pth);
}

void initializepaths(struct heap_state *hs) {
  struct globallist *gl=hs->gl;
  hs->methodlist->pathtable=allocatehashtable();
  while(gl!=NULL) {
    long long uid=gl->object->uid;
    if(!contains(hs->methodlist->pathtable,uid)) {
      struct path * initpth=(struct path *)calloc(1, sizeof(struct path));
      initpth->prev_obj=-1;
      initpth->paramnum=-1;
      initpth->classname=copystr(gl->classname);
      initpth->globalname=copystr(gl->fieldname);
      puttable(hs->methodlist->pathtable, uid,initpth);
    }
    gl=gl->next;
  }
}



void addeffect(struct heap_state *heap, long long suid, char * fieldname, long long duid) {
  struct method * method=heap->methodlist;
  while(method!=NULL) {
    struct hashtable *pathtable=method->pathtable;
    struct effectregexpr* srcexpr=NULL;
    if (suid!=-1)
      srcexpr=buildregexpr(pathtable, suid);
    else printf("expr\n");
    printf("Initial effects for method:\n");
    printeffectlist(method->effects);
    {
      struct effectregexpr* dstexpr=buildregexpr(pathtable, duid);
      struct effectlist * efflist=(struct effectlist *) calloc(1,sizeof(struct effectlist));
      efflist->fieldname=copystr(fieldname);
      efflist->src=srcexpr;
      efflist->dst=dstexpr;
      printf("Effect List Entry:\n");
      printeffectlist(efflist);
      {
	struct effectlist *effptr=method->effects;
	struct effectlist *oldptr=NULL;
	while(effptr!=NULL) {
	  struct effectlist *merged=mergeeffectlist(efflist, effptr);
	  if (merged!=NULL) {
	    freeeffectlist(efflist);
	    if(oldptr!=NULL) {
	      /*splice in merged effect*/
	      oldptr->next=merged;
	      merged->next=effptr->next;
	    } else {
	      /*splice in merged effect at beginning of list*/
	      method->effects=merged;
	      merged->next=effptr->next;
	    }
	    effptr->next=NULL;
	    freeeffectlist(effptr);
	    break;
	  }
	  oldptr=effptr;
	  effptr=effptr->next;
	}
	if (effptr==NULL) {
	  efflist->next=method->effects;
	  method->effects=efflist;
	}
      }
    }
    method=method->caller;
  }
}

struct effectlist * mergeeffectlist(struct effectlist * el1, struct effectlist *el2) {
  struct effectregexpr * mergedsrc=NULL, * mergeddst=NULL;
  if (!equivalentstrings(el1->fieldname, el2->fieldname)) return NULL;
  if (el1->src!=NULL&&el2->src!=NULL) {
    mergedsrc=mergeeffectregexpr(el1->src, el2->src);
    if (mergedsrc==NULL) return NULL;
  } else if (el1->src!=NULL||el2->src!=NULL)
    return NULL;

  if (el1->dst!=NULL&&el2->dst!=NULL) {
    mergeddst=mergeeffectregexpr(el1->dst, el2->dst);
    if (mergeddst==NULL) {
      freeeffectregexpr(mergedsrc);
      return NULL;
    }
  } else if (el1->dst!=NULL||el2->dst!=NULL) {
    freeeffectregexpr(mergedsrc);
    return NULL;
  }
  {
    struct effectlist * retel=(struct effectlist *)calloc(1, sizeof(struct effectlist));
    retel->src=mergedsrc;
    retel->dst=mergeddst;
    retel->fieldname=copystr(el1->fieldname);
    return retel;
  }
}

void updateroleeffects(struct heap_state *heap) {
  struct effectlist *mergedlist=mergemultipleeffectlist(heap->methodlist->effects, heap->methodlist->rm->effects);
  printf("Incoming method effectlist:\n");
  printeffectlist(heap->methodlist->effects);
  printf("Old rolemethod effectlist:\n");
  printeffectlist(heap->methodlist->rm->effects);
  printf("New rolemethod effectlist:\n");
  printeffectlist(mergedlist);
  freeeffectlist(heap->methodlist->rm->effects);
  heap->methodlist->rm->effects=mergedlist;

}

struct effectlist * mergemultipleeffectlist(struct effectlist *el1, struct effectlist *el2) {
  struct epointerlist * listofeffectlist=NULL;
  struct effectlist *mergedeffects=NULL;
  while(el1!=NULL || el2!=NULL) {
    if (el1!=NULL) {
      struct epointerlist *ptr=(struct epointerlist *)calloc(1,sizeof(struct epointerlist));
      ptr->next=listofeffectlist;
      ptr->object=el1;
      listofeffectlist=ptr;
      el1=el1->next;
    }
    if (el2!=NULL) {
      struct epointerlist *ptr=(struct epointerlist *)calloc(1,sizeof(struct epointerlist));
      ptr->next=listofeffectlist;
      ptr->object=el2;
      listofeffectlist=ptr;
      el2=el2->next;
    }
  }
  /* Build up effectlist set*/
  while(listofeffectlist!=NULL) {
    struct epointerlist *ptr2;
    for(ptr2=listofeffectlist;ptr2->next!=NULL;ptr2=ptr2->next) {
      struct effectlist * ptreff=mergeeffectlist((struct effectlist *)listofeffectlist->object,(struct effectlist *)ptr2->next->object);
      if (ptreff!=NULL) {
	ptreff->next=mergedeffects;
	mergedeffects=ptreff;
	break;
      }
    }
    if(ptr2->next!=NULL) {
      struct epointerlist *tmp=ptr2->next;
      ptr2->next=ptr2->next->next;
      free(tmp);
    } else {
      struct effectlist *copy=mergeeffectlist((struct effectlist *)listofeffectlist->object, (struct effectlist *)listofeffectlist->object);
      copy->next=mergedeffects;
      mergedeffects=copy;
    }
    {
      struct epointerlist *tmp=listofeffectlist->next;
      free(listofeffectlist);
      listofeffectlist=tmp;
    }
  }
  return mergedeffects;
}

struct effectregexpr * mergeeffectregexpr(struct effectregexpr * ere1, struct effectregexpr * ere2) {
  if ((ere1->paramnum==ere2->paramnum)&&equivalentstrings(ere1->classname, ere2->classname)&&equivalentstrings(ere1->globalname, ere2->globalname)&&(ere1->flag==ere2->flag)) {
    /* Okay...now all the remains is to check compatibility of the regular expression list */
    struct regexprlist * rel1=ere1->expr;
    struct regexprlist * rel2=ere2->expr;
    struct regexprlist *merged=NULL;
    if (rel1!=NULL&&rel2!=NULL) {
      merged=mergeregexprlist(rel1,rel2);
      if(merged==NULL)
	return NULL;
    } else if ((rel1!=NULL)||(rel2!=NULL)) {
      return NULL;
    }
    {
      struct effectregexpr * ere=(struct effectregexpr *)calloc(1,sizeof(struct effectregexpr));
      ere->paramnum=ere1->paramnum;
      ere->flag=ere1->flag;
      ere->classname=copystr(ere1->classname);
      ere->globalname=copystr(ere1->globalname);
      ere->expr=merged;
      return ere;
    }
  } else return NULL;
}

struct regexprlist * mergeregexprlist(struct regexprlist * rel1, struct regexprlist *rel2) {
  struct regexprlist * rel=NULL;
  while(rel1!=NULL&&rel2!=NULL) {
    struct regexprlist * copy=(struct regexprlist *)calloc(1, sizeof(struct regexprlist));
    copy->multiplicity=rel1->multiplicity|rel2->multiplicity;
    if (equivalentstrings(rel1->classname, rel2->classname)&&
	  equivalentstrings(rel1->fielddesc, rel2->fielddesc)) {
      copy->classname=copystr(rel1->classname);
      copy->fielddesc=copystr(rel1->fielddesc);
      /*Merge fields*/
      {
	/* Copy the first list*/
	struct regfieldlist *copyfieldptr=rel1->fields;
	while(copyfieldptr!=NULL) {
	  struct regfieldlist *copyfld=(struct regfieldlist *) calloc(1, sizeof(struct regfieldlist));
	  copyfld->classname=copystr(copyfieldptr->classname);
	  copyfld->fieldname=copystr(copyfieldptr->fieldname);
	  copyfld->fielddesc=copystr(copyfieldptr->fielddesc);
	  copyfld->nextfld=copy->fields;
	  copy->fields=copyfld;
	  copyfieldptr=copyfieldptr->nextfld;
	}
      }
      {
      	struct regfieldlist *copyfieldptr=rel2->fields;
	while(copyfieldptr!=NULL) {
	  struct regfieldlist *searchptr=rel1->fields;
	  while(searchptr!=NULL) {
	    if (equivalentstrings(searchptr->classname, copyfieldptr->classname)&&equivalentstrings(searchptr->fieldname, copyfieldptr->fieldname)&&equivalentstrings(searchptr->fielddesc, copyfieldptr->fielddesc))
	      break;
	    searchptr=searchptr->nextfld;
	  }
	  if(searchptr==NULL) {
	    struct regfieldlist *copyfld=(struct regfieldlist *) calloc(1, sizeof(struct regfieldlist));
	    copyfld->classname=copystr(copyfieldptr->classname);
	    copyfld->fieldname=copystr(copyfieldptr->fieldname);
	    copyfld->fielddesc=copystr(copyfieldptr->fielddesc);
	    copyfld->nextfld=copy->fields;
	    copy->fields=copyfld;
	  }
	  copyfieldptr=copyfieldptr->nextfld;
	}
      }
      
      /*Merge subtree*/
      {
	struct listofregexprlist *allregexpr=NULL;
	struct listofregexprlist *indexptr=NULL;
	struct listofregexprlist *newlistptr=NULL;
      	struct listofregexprlist *copyregptr=rel2->subtree;
	while(copyregptr!=NULL) {
	  struct listofregexprlist *tmp=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
	  tmp->expr=copyregptr->expr;
	  tmp->nextlist=allregexpr;
	  allregexpr=tmp;
	  copyregptr=copyregptr->nextlist;
	}
	copyregptr=rel1->subtree;
	while(copyregptr!=NULL) {
	  struct listofregexprlist *tmp=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
	  tmp->expr=copyregptr->expr;
	  tmp->nextlist=allregexpr;
	  allregexpr=tmp;
	  copyregptr=copyregptr->nextlist;
	}
	while(allregexpr!=NULL) {
	  indexptr=allregexpr;
	  while(indexptr->nextlist!=NULL) {
	    struct regexprlist *merge=mergeregexprlist(indexptr->nextlist->expr, allregexpr->expr);
	    if (merge!=NULL) {
	      struct listofregexprlist *tmp=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
	      tmp->expr=merge;
	      tmp->nextlist=newlistptr;
	      newlistptr=tmp;
	      tmp=allregexpr->nextlist;
	      /* Free the merged expr list*/
	      free(allregexpr);
	      allregexpr=tmp;
	      tmp=indexptr->nextlist;
	      indexptr->nextlist=tmp->nextlist;
	      free(tmp);
	      break;
	    }
	    indexptr=indexptr->nextlist;
	  }
	  if (indexptr->nextlist==NULL) {
	      struct listofregexprlist *tmp=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
	      tmp->expr=mergeregexprlist(allregexpr->expr,allregexpr->expr);
	      tmp->nextlist=newlistptr;
	      newlistptr=tmp;
	      tmp=allregexpr->nextlist;
	      free(allregexpr);
	      allregexpr=tmp;
	  }
	  allregexpr=allregexpr->nextlist;
	}
      }
      copy->nextreg=rel;
      rel=copy;
    } else {
      free(copy);
      freeregexprlist(rel);
      return NULL;
    }
    rel1=rel1->nextreg;
    rel2=rel2->nextreg;
  }
  if((rel1!=NULL)||(rel2!=NULL)) {
    freeregexprlist(rel);
    return NULL;
  } else
    return rel;
}

void freeeffectlist(struct effectlist *el) {
  while (el!=NULL) {
    struct effectlist *eltmp=el->next;
    freeeffectregexpr(el->src);
    free(el->fieldname);
    freeeffectregexpr(el->dst);
    free(el);
    el=eltmp;
  }
}

void freeeffectregexpr(struct effectregexpr *ere) {
  if (ere!=NULL) {
    struct regexprlist *tofree=ere->expr;
    freeregexprlist(tofree);
    free(ere->classname);
    free(ere->globalname);
    free(ere);
  }
}

void freeregexprlist(struct regexprlist *tofree) {
  while(tofree!=NULL) {
    struct regexprlist *tmpptr=tofree->nextreg;
    struct regfieldlist *rfl=tofree->fields;
    struct listofregexprlist *lrel=tofree->subtree;
    while(rfl!=NULL) {
      struct regfieldlist *tmpfldptr=rfl->nextfld;
      free(rfl->fieldname);
      free(rfl->classname);
      free(rfl->fielddesc);
      free(rfl);
      rfl=tmpfldptr;
    }
    while(lrel!=NULL) {
      freeregexprlist(lrel->expr);
      lrel=lrel->nextlist;
    }
    free(tofree->classname);
    free(tofree->fielddesc);
    free(tofree);
    tofree=tmpptr;
  }
}

static struct regexprlist ** ringbuffers[MAXREPSEQ-1];
static int loopcount[MAXREPSEQ-1];

void initloopstructures() {
  int i;
  for(i=2;i<=MAXREPSEQ;i++)
    ringbuffers[i-2]=(struct regexprlist **) malloc(sizeof(struct regexprlist *)*i);
}

int compareregexprlist(struct regexprlist *rel1, struct regexprlist * rel2) {
  if ((equivalentstrings(rel1->classname, rel2->classname))&&
      (equivalentstrings(rel1->fielddesc, rel2->fielddesc))&&
      (rel1->multiplicity==rel2->multiplicity))
    return 1;
  else return 0;
}

struct effectregexpr * buildregexpr(struct hashtable *pathtable, long long uid) {
  struct regexprlist *rel=NULL;
  struct path * path=NULL;
  /* lastloopsize gives size of last loop*/
  /* lastloopindex gives index of last loop*/
  int lastloopsize=0;
  int lastloopindex=0;
  int i,index=0;
  if (uid==-1) return NULL;

  while(1) {
    if (!contains(pathtable, uid)) {
      struct effectregexpr *src=(struct effectregexpr *) calloc(1, sizeof(struct effectregexpr));
      printf("Didn't find uid %lld\n",uid);
      src->paramnum=-1;
      src->flag=2;
      src->expr=rel;
      return src;
    }
    path=gettable(pathtable, uid);
    if (path->prev_obj!=-1) {
      if ((rel==NULL)||(!equivalentstrings(rel->classname, path->classname)&&!equivalentstrings(rel->fielddesc, path->fielddesc))) {
	struct regexprlist *ere=(struct regexprlist *) calloc(1, sizeof(struct regexprlist));
	struct regfieldlist *rfl=(struct regfieldlist *) calloc(1, sizeof(struct regfieldlist));
	ere->multiplicity=0;
	ere->classname=copystr(path->classname);
	ere->fielddesc=copystr(path->fielddesc);
	ere->fields=rfl;
	ere->nextreg=rel;
	rel=ere;
	rfl->fieldname=copystr(path->fieldname);
	rfl->classname=copystr(path->classname);
	rfl->fielddesc=copystr(path->fielddesc);
	/* Now check for loopies */
	for(i=2;i<=MAXREPSEQ;i++) {
	  int countmodulo=index % (i);
	  int ringindex=i-2;
	  if (index>i) {
	    /* Ring full...do comparison and increment counter*/
	    struct regexprlist *oldere=ringbuffers[ringindex][countmodulo];
	    if ((oldere!=NULL)&&compareregexprlist(oldere, ere)) {
	      if ((++loopcount[ringindex])==i) {
		/*We've got a loop...*/
		int j;
		if((lastloopsize+lastloopindex)==index) {
		  /* We've already got a loop node!!!*/
		} else {
		  /* Build loop node */
		  struct regexprlist * relloop=(struct regexprlist *) calloc(1, sizeof(struct regexprlist));
		  /* Get 2 regexprlists */
		  struct regexprlist * list1=ere;
		  struct regexprlist * list2=oldere;
		  struct regexprlist * mergedlist=NULL;
		  int j;
		  for(j=1;j<i;j++) {
		    list1=list1->nextreg;
		    list2=list2->nextreg;
		  }
		  list1->nextreg=NULL;
		  relloop->nextreg=list2->nextreg;
		  list2->nextreg=NULL;
		  list1=ere;list2=oldere;
		  /*Got them in list1 and list2*/
		  mergedlist=mergeregexprlist(list1,list2);
		  if (mergedlist==NULL) {
		    struct listofregexprlist * loel1=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
		    struct listofregexprlist * loel2=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
		    loel1->expr=list1;
		    loel2->expr=list2;
		    loel1->nextlist=loel1;
		    relloop->subtree=loel1;
		  } else {
		    struct listofregexprlist * loel=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
		    loel->expr=mergedlist;
		    relloop->subtree=loel;
		    freeregexprlist(list1);
		    freeregexprlist(list2);
		  }

		  relloop->multiplicity=1;
		  relloop->classname=copystr(ringbuffers[ringindex][(countmodulo+1)%i]->classname);
		  relloop->fielddesc=copystr(ere->fielddesc);
		  

		  rel=relloop;
		}
		/* We've got a real loop...only the exact same sequence can
		   match the old data...*/
		for(j=2;j<=MAXREPSEQ;j++) {
		  int k;
		  loopcount[j-2]=0;
		  if (j!=i)
		    for(k=0;k<j;k++)
		      ringbuffers[j-2][k]=NULL;
		}
		lastloopindex=index;
		lastloopsize=i;
	      } else {
		ringbuffers[ringindex][countmodulo]=ere;
	      }
	    } else {
	      loopcount[ringindex]=0;
	      ringbuffers[ringindex][countmodulo]=ere;
	    }
	  } else {
	    loopcount[ringindex]=0; /* Ring not full...zero counter */
	    ringbuffers[ringindex][countmodulo]=ere;
	  }
	}
	index++;
      } else {
	/* Class exists, simply add field */
	struct regfieldlist *rflptr=rel->fields;
	rel->multiplicity=1;
	while(rflptr!=NULL) {
	  if (strcmp(rflptr->fieldname, path->fieldname)==0)
	    break;
	  rflptr=rflptr->nextfld;
	}
	if (rflptr==NULL) {
	  struct regfieldlist *newfield=(struct regfieldlist *)calloc(1, sizeof(struct regfieldlist));
	  newfield->fieldname=copystr(path->fieldname);
	  newfield->fielddesc=copystr(path->fielddesc);
	  newfield->nextfld=rel->fields;
	  newfield->classname=copystr(path->classname);
	  rel->fields=newfield;
	}
      }
    } else if(path->paramnum==-1&&path->classname==NULL&&path->fieldname==NULL&&path->fielddesc==NULL&&path->globalname==NULL) {
      struct effectregexpr * ere=(struct effectregexpr *)calloc(1, sizeof(struct effectregexpr));
      ere->flag=1;
      ere->paramnum=-1;
      if(rel!=NULL)
	printf("ERROR:  New object part of path\n");
      return ere; /* object created after procedure call...not in original heap*/
    } else {
      /* At top level...*/
      struct effectregexpr *src=(struct effectregexpr *) calloc(1, sizeof(struct effectregexpr));
      src->paramnum=path->paramnum;
      src->classname=copystr(path->classname);
      src->globalname=copystr(path->globalname);
      src->expr=rel;
      return src;
    }
    uid=path->prev_obj;
  }
}

void printeffectlist(struct effectlist *el) {
  while (el!=NULL) {
    if (el->src!=NULL) {
      printeffectregexpr(el->src);
      printf(".%s=", el->fieldname);
    } else if(el->fieldname==NULL)
      printf("Read: ");
    else
      printf("%s=",el->fieldname);
    if (el->dst!=NULL)
      printeffectregexpr(el->dst);
    else printf("NULL");
    printf("\n");
    el=el->next;
  }
}

void printeffectregexpr(struct effectregexpr *ere) {
  if (ere->paramnum==-1&&ere->flag==0)
    printf("[%s.%s]", ere->classname, ere->globalname);
  else if (ere->flag==1)
    printf("NEW");
  else if (ere->flag==2)
    printf("NATIVEREACH");
  else
    printf("[Param %d]",ere->paramnum);
  printregexprlist(ere->expr);
}

void printregexprlist(struct regexprlist *rel) {
  while(rel!=NULL) {
    printf(".");
    if (rel->fields!=NULL) {
      struct regfieldlist * flptr=rel->fields;
      if (flptr->nextfld!=NULL)
	printf("(");
      while(flptr!=NULL) {
	printf("[%s.%s]",flptr->classname,flptr->fieldname);
	if (flptr->nextfld!=NULL)
	  printf("|");
	flptr=flptr->nextfld;
      }
      if (rel->fields->nextfld!=NULL)
	printf(")");
      if (rel->multiplicity==1)
	printf("*");
    } else {
      struct listofregexprlist * flptr=rel->subtree;
      if (flptr->nextlist!=NULL)
	printf("(");
      while(flptr!=NULL) {
	printregexprlist(flptr->expr);
	if (flptr->nextlist!=NULL)
	  printf("|");
	flptr=flptr->nextlist;
      }
      if (rel->subtree->nextlist!=NULL)
	printf(")");
      if (rel->multiplicity==1)
	printf("*");
    }
    rel=rel->nextreg;
  }  
}





