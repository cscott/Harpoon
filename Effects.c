#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include "Effects.h"
#include "Role.h"
#include "Method.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

#define MAXREPSEQ 4

void addarraypath(struct heap_state *hs, struct hashtable * ht, long long obj, long long dstobj) {
  struct method *method=hs->methodlist;
  int newpath=0,newpatho=0;
  while(method!=NULL) {
    struct hashtable * pathtable=method->pathtable;

    if (!contains(pathtable, obj)) {
      /*Magically got this object...mark it as soo...*/
      struct path *path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=-1;
      path->paramnum=-2;
      puttable(pathtable, obj, path);
      newpatho=1;
    }

    if (!contains(pathtable, dstobj)) {
      struct path * path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=obj;
      path->fieldname=getfieldc(hs->namer, ((struct heap_object *)gettable(ht, obj))->class,"[]",NULL);
      /*Array dereferences are uniquely classified by array class*/
      path->paramnum=-1;
      puttable(pathtable, dstobj, path);
      newpath=1;
    }
    method=method->caller;
  }
  if(newpath)
    addeffect(hs, -1, NULL, dstobj);
  if(newpatho)
    addeffect(hs, -1, NULL, obj);
}

void checkpath(struct heap_state *hs, long long obj) {
  struct method *method=hs->methodlist;
  int newpatho=0;
  while(method!=NULL) {
    struct hashtable * pathtable=method->pathtable;
    if (!contains(pathtable, obj)) {
      /*Magically got this object...mark it as soo...*/
      struct path *path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=-1;
      path->paramnum=-2;
      puttable(pathtable, obj, path);
      newpatho=1;
    }
   
    method=method->caller;
  }
  if (newpatho)
    addeffect(hs, -1, NULL, obj);
}

void addpath(struct heap_state *hs, long long obj, struct fieldname * field, long long dstobj) {
  struct method *method=hs->methodlist;
  int newpath=0,newpatho=0;
  while(method!=NULL) {
    struct hashtable * pathtable=method->pathtable;

    if (!contains(pathtable, obj)) {
      /*Magically got this object...mark it as soo...*/
      struct path *path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=-1;
      path->paramnum=-2;
      puttable(pathtable, obj, path);
      newpatho=1;
    }

    if (!contains(pathtable, dstobj)) {
      struct path * path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=obj;
      path->fieldname=field;
      path->paramnum=-1;
      puttable(pathtable, dstobj, path);
      newpath=1;
    }
    
    method=method->caller;
  }
  if (newpath)
    addeffect(hs, -1, NULL, dstobj);

  if (newpatho)
    addeffect(hs, -1, NULL, obj);

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
      initpth->fieldname=gl->fieldname;
      puttable(hs->methodlist->pathtable, uid,initpth);
    }
    gl=gl->next;
  }
}


/* if fieldname=NULL, then array dereference*/
void addeffect(struct heap_state *heap, long long suid, struct fieldname  * fieldname, long long duid) {
  struct method * method=heap->methodlist;
  while(method!=NULL) {
    struct hashtable *pathtable=method->pathtable;
    struct effectregexpr* srcexpr=NULL;
    if (suid!=-1)
      srcexpr=buildregexpr(pathtable, suid);
    /*        printf("Initial effects for method:\n");
	      printeffectlist(method->effects);*/
    {
      struct effectregexpr* dstexpr=buildregexpr(pathtable, duid);
      struct effectlist * efflist=(struct effectlist *) calloc(1,sizeof(struct effectlist));
      efflist->fieldname=fieldname;
      efflist->src=srcexpr;
      efflist->dst=dstexpr;
      /*            printf("Effect List Entry:\n");
		    printeffectlist(efflist);*/
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
  if (el1->fieldname!=el2->fieldname) return NULL;
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
    retel->fieldname=el1->fieldname;
    return retel;
  }
}

void updateroleeffects(struct heap_state *heap) {
  struct effectlist *mergedlist=mergemultipleeffectlist(heap->methodlist->effects, heap->methodlist->rm->effects);
  /*    printf("Incoming method effectlist:\n");
  printeffectlist(heap->methodlist->effects);
  printf("Old rolemethod effectlist:\n");
  printeffectlist(heap->methodlist->rm->effects);
  printf("New rolemethod effectlist:\n");
  printeffectlist(mergedlist);*/
  freeeffectlist(heap->methodlist->rm->effects);
  heap->methodlist->rm->effects=mergedlist;

}



struct effectlist * mergemultipleeffectlist(struct effectlist *el1, struct effectlist *el2) {
  struct epointerlist * listofeffectlist=NULL;
  struct effectlist *mergedeffects=NULL;
  while(el1!=NULL||el2!=NULL) {
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
      if (copy==NULL) {
	printf("ERRORXXXXXXX");
	copy=mergeeffectlist((struct effectlist *)listofeffectlist->object, (struct effectlist *)listofeffectlist->object);
      }
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
  if ((ere1->paramnum==ere2->paramnum)&&(ere1->globalname==ere2->globalname)&&(ere1->flag==ere2->flag)) {
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
      ere->globalname=ere1->globalname;
      ere->expr=merged;
      return ere;
    }
  } else return NULL;
}

struct regexprlist * mergeregexprlist(struct regexprlist * rel1, struct regexprlist *rel2) {
  struct regexprlist * rel=NULL, *oldcopy=NULL;
  while(rel1!=NULL&&rel2!=NULL) {
    struct regexprlist * copy=(struct regexprlist *)calloc(1, sizeof(struct regexprlist));
    if(rel==NULL)
      rel=copy;
    if (oldcopy!=NULL)
      oldcopy->nextreg=copy;
    oldcopy=copy;

    copy->multiplicity=rel1->multiplicity|rel2->multiplicity;
    if ((rel1->classname==rel2->classname)&&
	  (rel1->fielddesc==rel2->fielddesc)) {
      copy->classname=rel1->classname;
      copy->fielddesc=rel1->fielddesc;
      /*Merge fields*/
      {
	/* Copy the first list*/
	struct regfieldlist *copyfieldptr=rel1->fields;
	while(copyfieldptr!=NULL) {
	  struct regfieldlist *copyfld=(struct regfieldlist *) calloc(1, sizeof(struct regfieldlist));
	  copyfld->fieldname=copyfieldptr->fieldname;
	  copyfld->fielddesc=copyfieldptr->fielddesc;
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
	    if ((searchptr->fieldname==copyfieldptr->fieldname)&&(searchptr->fielddesc==copyfieldptr->fielddesc))
	      break;
	    searchptr=searchptr->nextfld;
	  }
	  if(searchptr==NULL) {
	    struct regfieldlist *copyfld=(struct regfieldlist *) calloc(1, sizeof(struct regfieldlist));
	    copyfld->fieldname=copyfieldptr->fieldname;
	    copyfld->fielddesc=copyfieldptr->fielddesc;
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
	      if (indexptr==allregexpr) {
		/* Adjacent nodes...*/
		tmp=indexptr->nextlist->nextlist;
		free(indexptr->nextlist);
		free(allregexpr);
		allregexpr=tmp;
		indexptr=NULL;
		break;
	      } else {
		/* Free the merged expr list*/
		tmp=allregexpr->nextlist;
		free(allregexpr);
		allregexpr=tmp;

		tmp=indexptr->nextlist;
		indexptr->nextlist=tmp->nextlist;
		free(tmp);
		indexptr=NULL;
		break;
	      }
	    }
	    indexptr=indexptr->nextlist;
	  }
	  if (indexptr!=NULL&&indexptr->nextlist==NULL) {
	      struct listofregexprlist *tmp=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
	      tmp->expr=mergeregexprlist(allregexpr->expr,allregexpr->expr);
	      if (tmp->expr==NULL) {
		printf("ERRORXXXX\n");
	      }
	      tmp->nextlist=newlistptr;
	      newlistptr=tmp;
	      tmp=allregexpr->nextlist;
	      free(allregexpr);
	      allregexpr=tmp;
	  }
	}
	copy->subtree=newlistptr;
      }

    } else {
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
    freeeffectregexpr(el->dst);
    free(el);
    el=eltmp;
  }
}

void freeeffectregexpr(struct effectregexpr *ere) {
  if (ere!=NULL) {
    struct regexprlist *tofree=ere->expr;
    freeregexprlist(tofree);
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
      free(rfl);
      rfl=tmpfldptr;
    }
    while(lrel!=NULL) {
      struct listofregexprlist *lrelnew=lrel->nextlist;
      freeregexprlist(lrel->expr);
      free(lrel);
      lrel=lrelnew;
    }
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
  if ((rel1->classname==rel2->classname)&&
      (rel1->fielddesc==rel2->fielddesc)&&
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
      if ((rel==NULL)||((rel->classname!=path->fieldname->classname)||(rel->fielddesc!=path->fieldname->fielddesc))) {
	struct regexprlist *ere=(struct regexprlist *) calloc(1, sizeof(struct regexprlist));
	struct regfieldlist *rfl=(struct regfieldlist *) calloc(1, sizeof(struct regfieldlist));
	ere->multiplicity=0;
	ere->classname=path->fieldname->classname;
	ere->fielddesc=path->fieldname->fielddesc;
	ere->fields=rfl;
	ere->nextreg=rel;
	rel=ere;
	rfl->fieldname=path->fieldname;
	rfl->fielddesc=path->fieldname->fielddesc;
	/* Now check for loopies */
	for(i=2;i<=MAXREPSEQ;i++) {
	  int countmodulo=index % (i);
	  int ringindex=i-2;
	  if (index>=i) {
	    /* Ring full...do comparison and increment counter*/
	    struct regexprlist *oldere=ringbuffers[ringindex][countmodulo];
	    if ((oldere!=NULL)&&compareregexprlist(oldere, ere)) {
	      if ((++loopcount[ringindex])==i) {
		/*We've got a loop...*/
		int j;
		if((lastloopsize+lastloopindex)==index) {
		  /* We've already got a loop node!!!*/
		  int j;
		  struct regexprlist *list1=ere;
		  struct regexprlist *loopnode=NULL;
		  struct listofregexprlist *lorel=NULL;
		  for(j=1;j<i;j++)
		    list1=list1->nextreg;
		  loopnode=list1->nextreg;
		  rel=loopnode;

		  list1->nextreg=NULL;
		  lorel=loopnode->subtree;
		  
		  while(lorel!=NULL) {
		    struct regexprlist *mergedlist=mergeregexprlist(lorel->expr,ere);
		    if (mergedlist!=NULL) {
		      /*Merging with another item*/
		      freeregexprlist(lorel->expr);
		      freeregexprlist(ere);
		      lorel->expr=mergedlist;

		      ere=mergedlist;
		      /*Fill ring buffer with merged list*/
		      
		      for(j=i;j>0;j--) {
			ringbuffers[ringindex][(j+index)%i]=mergedlist;
			mergedlist=mergedlist->nextreg;
		      }	      		     
		      break;
		    }
		    lorel=lorel->nextlist;
		  }
		  if (lorel==NULL) {
		    /*Can't merge it...*/
		    struct listofregexprlist * loel1=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
		    loel1->expr=ere;
		    loel1->nextlist=loopnode->subtree;
		    loopnode->subtree=loel1;
		  }
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
		    loel1->nextlist=loel2;
		    relloop->subtree=loel1;
		  } else {
		    struct listofregexprlist * loel=(struct listofregexprlist *) calloc(1, sizeof(struct listofregexprlist));
		    loel->expr=mergedlist;
		    relloop->subtree=loel;
		    freeregexprlist(list1);
		    freeregexprlist(list2);
		    ere=mergedlist;
		    /*Fill ring buffer with merged list*/
		    list2=ere;

		    for(j=i;j>0;j--) {
		      ringbuffers[ringindex][(j+index)%i]=list2;
		      list2=list2->nextreg;
		    }
		  }

		  relloop->multiplicity=1;

		  relloop->classname=ere->classname;
		  relloop->fielddesc=ringbuffers[ringindex][(countmodulo+1)%i]->fielddesc;

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
	  if (rflptr->fieldname== path->fieldname)
	    break;
	  rflptr=rflptr->nextfld;
	}
	if (rflptr==NULL) {
	  struct regfieldlist *newfield=(struct regfieldlist *)calloc(1, sizeof(struct regfieldlist));
	  newfield->fieldname=path->fieldname;
	  newfield->fielddesc=path->fieldname->fielddesc;
	  newfield->nextfld=rel->fields;
	  rel->fields=newfield;
	}
      }
    } else if(path->paramnum==-1&&path->fieldname==NULL) {
      struct effectregexpr * ere=(struct effectregexpr *)calloc(1, sizeof(struct effectregexpr));
      ere->flag=1;
      ere->paramnum=-1;
      if(rel!=NULL)
	printf("ERROR:  New object part of path\n");
      return ere; /* object created after procedure call...not in original heap*/
    } else if(path->paramnum==-2) {
      struct effectregexpr *src=(struct effectregexpr *) calloc(1, sizeof(struct effectregexpr));
      src->paramnum=-1;
      src->flag=2;
      src->expr=rel;
      return src;
    } else {
      /* At top level...*/
      struct effectregexpr *src=(struct effectregexpr *) calloc(1, sizeof(struct effectregexpr));
      src->paramnum=path->paramnum;
      src->globalname=path->fieldname;
      src->expr=rel;
      return src;
    }
    uid=path->prev_obj;
  }
}

void printeffectlist(struct heap_state *heap,struct effectlist *el) {
  while (el!=NULL) {
    if (el->src!=NULL) {
      printeffectregexpr(heap,el->src);
      if(el->fieldname!=NULL)
	fprintf(heap->methodfile,".%s=", el->fieldname->fieldname);
      else
	fprintf(heap->methodfile,".[]=");
    } else if(el->fieldname==NULL)
      fprintf(heap->methodfile,"Read: ");
    else
      fprintf(heap->methodfile,"%s=",el->fieldname->fieldname);
    if (el->dst!=NULL)
      printeffectregexpr(heap, el->dst);
    else fprintf(heap->methodfile,"NULL");
    fprintf(heap->methodfile,"\n");
    el=el->next;
  }
}

void printeffectregexpr(struct heap_state *heap,struct effectregexpr *ere) {
  if (ere->paramnum==-1&&ere->flag==0)
    fprintf(heap->methodfile,"[%s.%s]", ere->globalname->classname->classname, ere->globalname->fieldname);
  else if (ere->flag==1)
    fprintf(heap->methodfile,"NEW");
  else if (ere->flag==2)
    fprintf(heap->methodfile,"NATIVEREACH");
  else
    fprintf(heap->methodfile,"[Param %d]",ere->paramnum);
  if (ere->expr!=NULL)
    fprintf(heap->methodfile,".");
  printregexprlist(heap,ere->expr);
}

void printregexprlist(struct heap_state *heap, struct regexprlist *rel) {
  while(rel!=NULL) {
    if (rel->fields!=NULL) {
      struct regfieldlist * flptr=rel->fields;
      if (flptr->nextfld!=NULL)
	fprintf(heap->methodfile,"(");
      while(flptr!=NULL) {
	fprintf(heap->methodfile,"[%s.%s]",flptr->fieldname->classname->classname,flptr->fieldname->fieldname);
	if (flptr->nextfld!=NULL)
	  fprintf(heap->methodfile,"|");
	flptr=flptr->nextfld;
      }
      if (rel->fields->nextfld!=NULL)
	fprintf(heap->methodfile,")");
      if (rel->multiplicity==1)
	fprintf(heap->methodfile,"*");
    } else if (rel->subtree!=NULL) {
      struct listofregexprlist * flptr=rel->subtree;
      if (flptr->nextlist!=NULL||flptr->expr->nextreg!=NULL)
	fprintf(heap->methodfile,"(");
      while(flptr!=NULL) {
	if (flptr->expr==NULL)
	  fprintf(heap->methodfile,"XXXXX");
	printregexprlist(heap,flptr->expr);
	if (flptr->nextlist!=NULL)
	  fprintf(heap->methodfile,"|");
	flptr=flptr->nextlist;
      }
      if (rel->subtree->nextlist!=NULL||rel->subtree->expr->nextreg!=NULL)
	fprintf(heap->methodfile,")");
      if (rel->multiplicity==1)
	fprintf(heap->methodfile,"*");
    } else fprintf(heap->methodfile,"ERROR\n");
    rel=rel->nextreg;
    if (rel!=NULL)
      fprintf(heap->methodfile,".");
  }
}
