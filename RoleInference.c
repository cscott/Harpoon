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

static int programfd;
#define bufsize 1000
//#define DEBUG
long long pointerage=0;


int main(int argc, char **argv) {
  getfile();
  initloopstructures();
  doanalysis(argc, argv);
  return 0;
}

void getfile() {
  programfd=open("roleinfer.mem",O_RDONLY);
  if (programfd<0)
    perror("roleinfer.mem open failure\n");
}

void parseoptions(int argc, char **argv, struct heap_state *heap) {
  int param;
  int exitstate=0;
  for(param=1;param<argc;param++) {
    if(argv[param][0]=='-')
      switch(argv[param][1]) {
      case 'h':
	printf("-h help\n");
	printf("-c output containers\n");
	printf("-u use containers\n");
	printf("-n no effects\n");
	printf("-r no rolechange regular expressions calculated\n");
	exitstate=1;
	break;
      case 'r':
	heap->options|=OPTION_NORCEXPR;
	break;
      case 'n':
	heap->options|=OPTION_NOEFFECTS;
	break;
      case 'c':
	heap->options|=OPTION_FCONTAINERS;
	opencontainerfile(heap);
	break;
      case 'u':
	heap->options|=OPTION_UCONTAINERS;
	openreadcontainer(heap);
	break;
      default:
	exitstate=1;
	printf("Option %s not implemented.\n", argv[param]);
      } else {
	exitstate=1;
    	printf("Option %s not implemented.\n", argv[param]);
      }
  }
  if (exitstate)
    exit(0);
}

void doanalysis(int argc, char **argv) {
  struct heap_state heap;
  struct hashtable * ht=allocatehashtable();
  int currentparam=0;
  heap.options=0;

  parseoptions(argc, argv,&heap);

  heap.K=createobjectpair();
  heap.N=createobjectset();
  

  heap.gl=NULL;
  heap.methodlist=0;
  heap.newreferences=NULL;
  heap.freelist=NULL;
  heap.freemethodlist=NULL;
  heap.namer=allocatenamer();
  heap.roletable=genallocatehashtable((int (*)(void *)) &rolehashcode, (int (*)(void *,void *)) &equivalentroles);
  heap.reverseroletable=genallocatehashtable((int (*)(void *)) &hashstring, (int (*)(void *,void *)) &equivalentstrings);
  heap.methodtable=genallocatehashtable((int (*)(void *)) &methodhashcode, (int (*)(void *,void *)) &comparerolemethods);
  heap.currentmethodcount=0;
  heap.atomicmethodtable=genallocatehashtable((int (*)(void *)) &hashstring, (int (*)(void *,void *)) &equivalentstrings);
  heap.statechangemethodtable=genallocatehashtable((int (*)(void *)) &hashstring, (int (*)(void *,void *)) &equivalentstrings);
  heap.statechangereversetable=allocatehashtable();
  heap.changedset=createobjectset();

  heap.statechangesize=0;
  setheapstate(&heap);
  
  loadatomics(&heap);
  loadstatechange(&heap);
  while(1) {
    char *line=getline();

#ifdef DEBUG
    printf("------------------------------------------------------\n");
#endif

    if (line==0) {
      break;
    }
#ifdef DEBUG
    printf("[%s]\n",line);
#endif
    switch(line[0]) {
    case 'C': {
      /* Special action for copying array*/
      long long srcuid, dstuid;
      int srcpos, dstpos,length;
      struct arraylist *tmp=NULL, *al;
      struct heap_object *ho, *dsto;
      struct arraylist *tmp2=NULL;
      sscanf(line,"CA: %lld %d %lld %d %d", &srcuid, &srcpos, &dstuid, &dstpos, &length);
      ho=(struct heap_object *)gettable(ht, srcuid);
      dsto=(struct heap_object *)gettable(ht, dstuid);
      al=ho->al;
      /* Build copy of source region*/
      while(al!=NULL) {
	if((al->index>=srcpos)&&(al->index<(srcpos+length))) {
	  struct arraylist *tmpal=(struct arraylist *)calloc(1,sizeof(struct arraylist));
	  tmpal->index=al->index;
	  tmpal->object=al->object;
	  tmpal->next=tmp;
	  tmp=tmpal;
	}
	al=al->next;
      }

      al=dsto->al;

      /* Kill dead edges on dst region*/
      while(al!=NULL) {
	if((al->index>=dstpos)&&(al->index<(dstpos+length))) {
	  struct arraylist *tmpal=(struct arraylist *)calloc(1,sizeof(struct arraylist));
	  tmpal->index=al->index;
	  tmpal->object=al->object;
	  tmpal->next=tmp2;
	  tmp2=tmpal;
	}
	al=al->next;
      }

      while(tmp2!=NULL) {
	struct arraylist *tmpal=tmp2->next;
	doarrayassignment(&heap, dsto, tmp2->index, NULL);
	free(tmp2);
	tmp2=tmpal;
      }

      /* Now do writes*/
      while(tmp!=NULL) {
	struct arraylist *tmpal=tmp->next;
	doarrayassignment(&heap, dsto, tmp->index-srcpos+dstpos, tmp->object);
#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  addarraypath(&heap, ht, srcuid ,tmp->object->uid);
	  addeffect(&heap, dstuid, NULL, tmp->object->uid);
	}
#endif
	free(tmp);
	tmp=tmpal;
      }
    }
    break;
    case 'O': {
      /* Clone object*/
      long long origuid, cloneuid;
      struct fieldlist *fl;
      struct arraylist *al;
      struct heap_object *ho, *clone;
      sscanf(line, "ON: %lld %lld", &origuid, &cloneuid);
      ho=(struct heap_object *)gettable(ht, origuid);
      clone=(struct heap_object *)gettable(ht, cloneuid);
      fl=ho->fl;
      al=ho->al;
      /* Copy fields and arrays*/
      while(fl!=NULL) {
	dofieldassignment(&heap, clone, fl->fieldname, fl->object);
#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  addeffect(&heap, origuid, fl->fieldname, cloneuid);	
	}
#endif
	fl=fl->next;
      }
      while(al!=NULL) {
	doarrayassignment(&heap, clone, al->index, al->object);
#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  addeffect(&heap, origuid, NULL, cloneuid);	
	}
#endif
	al=al->next;
      }
    }
    break;
    case 'N':
      {
	/* Natively created object...may not have pointer to it*/
	struct heap_object *ho=(struct heap_object *) calloc(1, sizeof(struct heap_object));

	char buf[1000];
	sscanf(line,"NI: %s %lld",buf, &ho->uid);
	ho->class=getclass(heap.namer,buf);
	ho->reachable=2;
	puttable(ht, ho->uid, ho);
	addobject(heap.changedset, ho);
#ifdef EFFECTS
      if (!(heap.options&OPTION_NOEFFECTS)) {
	addnewobjpath(&heap, ho->uid);
      }
#endif
      }
      break;
    case 'U':
      {
	/* New object*/
	struct heap_object *ho=(struct heap_object *) calloc(1, sizeof(struct heap_object));
	char buf[1000];
	sscanf(line,"UI: %s %lld",buf, &ho->uid);
	ho->class=getclass(heap.namer,buf);
	puttable(ht, ho->uid, ho);
	addobject(heap.changedset, ho);
#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS))
	  addnewobjpath(&heap, ho->uid);
#endif
      }
      break;
    case 'K':
      /* Kill Local*/
      {
	char buf[1000];
	sscanf(line,"KL: %s",buf);
	/* removelvlist add's to K set */
	removelvlist(&heap, buf, heap.methodlist );
      }
      break; 
      
    case 'L':
      /* Do Load */
      {
	struct localvars * lv=(struct localvars *) calloc(1, sizeof(struct localvars));
	long long uid, objuid;
	char fieldname[600], classname[600], fielddesc[600];


	sscanf(line,"LF: %s %ld %s %lld %s %s %s %lld",lv->name,&lv->linenumber, lv->sourcename, &objuid, classname, fieldname, fielddesc, &uid);
	lv->lvnumber=lvnumber(lv->name);
	lv->age=pointerage++;
	lv->m=heap.methodlist;
	
	if (uid!=-1) {
	  lv->object=gettable(ht, uid);
	  doaddlocal(&heap, lv);
	  doaddfield(&heap,lv->object);
	  addobject(heap.changedset, lv->object);
	}

#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  if ((uid!=-1)&&(objuid!=-1)) {
	    addpath(&heap, objuid, getfield(heap.namer,classname, fieldname,fielddesc), uid);
	  }
	}
#endif
	
	/* addtolvlist add's to K set */
	addtolvlist(&heap, lv, heap.methodlist);
      }
      break;

    case 'G':
      /* Do Array Load */
      {
	struct localvars * lv=(struct localvars *) calloc(1, sizeof(struct localvars));
	long long uid, objuid;

	sscanf(line,"GA: %s %ld %s %lld %lld",lv->name,&lv->linenumber, lv->sourcename, &objuid, &uid);
	lv->lvnumber=lvnumber(lv->name);
	lv->age=pointerage++;
	lv->m=heap.methodlist;
	
	if (uid!=-1) {
	  lv->object=gettable(ht, uid);
	  doaddlocal(&heap, lv);
	  doaddfield(&heap,lv->object);
	  addobject(heap.changedset, lv->object);
	}

#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  if ((uid!=-1)&&(objuid!=-1)) {
	    addarraypath(&heap, ht, objuid, uid);
	  }
	}
#endif
	
	/* addtolvlist add's to K set */
	addtolvlist(&heap, lv, heap.methodlist);
      }
      break;


    case 'M':
      /* Mark Local*/
      {
	struct localvars * lv=(struct localvars *) calloc(1, sizeof(struct localvars));
	long long uid;

	sscanf(line,"ML: %s %ld %s %lld",lv->name,&lv->linenumber, lv->sourcename, &uid);
	lv->lvnumber=lvnumber(lv->name);
	lv->age=pointerage++;
	lv->m=heap.methodlist;
	
	if (uid!=-1) {
	  lv->object=gettable(ht, uid);
	  doaddlocal(&heap, lv);
	  doaddfield(&heap,lv->object);
	  addobject(heap.changedset, lv->object);
	}
	/* addtolvlist add's to K set */
	addtolvlist(&heap, lv, heap.methodlist);
	


	if (currentparam<heap.methodlist->numobjectargs) {
	  if (uid!=-1) {
	    heap.methodlist->params[currentparam]=lv->object;
#ifdef EFFECTS
	    if (!(heap.options&OPTION_NOEFFECTS)) {
	      if (!contains(heap.methodlist->pathtable, uid)) {
		struct path * pth=(struct path *) calloc(1, sizeof(struct path));
		pth->paramnum=currentparam;
		pth->prev_obj=-1;
		puttable(heap.methodlist->pathtable, uid ,pth);
	      }
	    }
#endif
	  }
	  currentparam++;
	  if(!atomic(&heap)&&
	     currentparam==heap.methodlist->numobjectargs) {
	    //Lets show the roles!!!!
	    entermethod(&heap,ht);
	  } 
	  if(currentparam==heap.methodlist->numobjectargs) {
	    int i=0;
	    for(;i<heap.methodlist->numobjectargs;i++) {
	      char buf[600];
	      sprintf(buf,"%s.%s%s %d", heap.methodlist->methodname->classname->classname,
		      heap.methodlist->methodname->methodname, heap.methodlist->methodname->signature,
		      convertnumberingobjects(heap.methodlist->methodname->signature, heap.methodlist->isStatic,i));
	      if (gencontains(heap.statechangemethodtable, buf)) {
		/* Got string....*/
		struct statechangeinfo *sci=(struct statechangeinfo *) gengettable(heap.statechangemethodtable, buf);
		int id=sci->id;
		struct heap_object *ho=heap.methodlist->params[i];
		if (ho->methodscalled==NULL)
		  ho->methodscalled=(int *)calloc(heap.statechangesize ,sizeof(int));
		ho->methodscalled[id]=1; /*Flip flag*/
	      }
	    }
	  }
	}
      }
      break;
    case 'I':
      /* Enter Method*/
      {
	struct method* newmethod=(struct method *) calloc(1,sizeof(struct method));
	char classname[600], methodname[600],signature[600];
	sscanf(line,"IM: %s %s %s %hd", classname, methodname, signature, &newmethod->isStatic);
	newmethod->methodname=getmethod(heap.namer, classname, methodname, signature);
	calculatenumobjects(newmethod);
	newmethod->caller=heap.methodlist;
	heap.methodlist=newmethod;
	atomiceval(&heap);
#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  initializepaths(&heap);
	}
#endif
	heap.methodlist->rolechangetable=genallocatehashtable((int (*)(void *)) &rchashcode, (int (*)(void *,void *)) &equivalentrc);
	currentparam=0;
      }
      if (!atomic(&heap)&&
	  currentparam==heap.methodlist->numobjectargs) {
	entermethod(&heap, ht);
      }
#ifdef DEBUG
      showmethodstack(&heap);
#endif
      break;
    case 'R':
      /* Return from method */
      {
	long long uid;
	struct method* ptr=heap.methodlist;
	sscanf(line,"RM: %lld",&uid);
	atomiceval(&heap);
	if (!atomic(&heap)) {
	  doreturnmethodinference(&heap, uid, ht);
	}
	heap.methodlist=ptr->caller;
	freemethod(&heap, ptr);
	currentparam=10000;
	/* Don't want to mess up callers parameter count.  They've all been processed by now.  No method could possibly have 10,000 parameters, so we're using this as a flag value.*/
      }
#ifdef DEBUG
      showmethodstack(&heap);
#endif
      break;
    case 'F':
      /* Field Assignment */
      {
	long long suid;
	long long duid;
	struct heap_object * src=NULL;
	struct heap_object * dst=NULL;
	char classname[1000];
	char fieldname[1000];
	char descname[1000];
	sscanf(line,"FA: %lld %s %s %s %lld", &suid, classname, fieldname, descname, &duid);
	if (suid!=-1)
	  src=gettable(ht,suid);
	if (duid!=-1) {
	  dst=gettable(ht,duid);
#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  checkpath(&heap, duid);
	}
#endif
	}
	if (src!=NULL) {
	  dofieldassignment(&heap, src, getfield(heap.namer, classname, fieldname, descname), dst);
#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  addeffect(&heap, suid, getfield(heap.namer, classname, fieldname, descname), duid);
	}
#endif
	} else {
	  doglobalassignment(&heap,getfield(heap.namer,classname,fieldname, descname),dst);
#ifdef EFFECTS
	  if (!(heap.options&OPTION_NOEFFECTS)) {
	    addeffect(&heap, -1, getfield(heap.namer, classname, fieldname, descname),duid);
	  }
#endif
	}
      }
      break;
    case 'A':
      /* Array Assignment */
      {
      	long long suid;
	long long duid;
	struct heap_object * src=NULL;
	struct heap_object * dst=NULL;
	long index;
	sscanf(line,"AA: %lld %ld %lld", &suid, &index, &duid);
	src=gettable(ht,suid);
	if (duid!=-1)
	  dst=gettable(ht,duid);
	doarrayassignment(&heap,src,index,dst);
#ifdef EFFECTS
	if (!(heap.options&OPTION_NOEFFECTS)) {
	  fflush(NULL);
	  addeffect(&heap, suid, NULL, duid);	
	}
#endif
      }
      break;
    }
    free(line);
  }

  {
    struct geniterator *it=gengetiterator(heap.methodtable);
    struct genhashtable * dotmethodtable=genallocatehashtable(NULL, NULL);

    while(1) {
      struct rolemethod *method=(struct rolemethod *) gennext(it);
      if (method==NULL)
	break;
      printrolemethod(method);
      dotrolemethod(dotmethodtable, heap.reverseroletable, method);
    }
    genfreeiterator(it);
    it=gengetiterator(heap.roletable);
    while(1) {
      struct role *role=(struct role *) gennext(it);
      char *rolename;
      if (role==NULL)
	break;
      rolename=gengettable(heap.roletable, role);
      printrole(role, rolename);
    }
    genfreeiterator(it);
    it=gengetiterator(dotmethodtable);
    while(1) {
      struct classname *class=(struct classname *)gennext(it);
      struct dotclass *dotclass=NULL;
      if (class==NULL)
	break;
      dotclass=(struct dotclass *) gengettable(dotmethodtable, class);
      printdot(class, dotclass);
    }
    genfreeiterator(it);
  }

  /* Clean up heap state related stuff*/
  if (heap.options&OPTION_FCONTAINERS) {
    examineheap(&heap, ht);
    closecontainerfile(&heap);
  } 
}



void doreturnmethodinference(struct heap_state *heap, long long uid, struct hashtable *ht) {
  struct localvars * lvptr, * lvptrn;
  struct method *m=heap->methodlist;
  lvptr=m->lv;
  m->lv=NULL;
  while(lvptr!=NULL) {
    lvptrn=lvptr->next;
    dodellvfield(heap, lvptr, lvptr->object);
    freelv(heap,lvptr);
    lvptr=lvptrn;
  }


  /* Keep alive the return value */
  if (uid!=-1) {
    struct localvars * lv=(struct localvars *) calloc(1, sizeof(struct localvars));
    lv->lvnumber=-1;
    strcpy(lv->name,"$$return");
    lv->linenumber=0;
    strcpy(lv->sourcename,"$$NA");
    lv->age=pointerage+1;/*Lose to everything*/
    lv->m=heap->methodlist;
    lv->object=gettable(ht,uid);
    doaddlocal(heap, lv);
    doaddfield(heap,lv->object);
    addtolvlist(heap, lv, heap->methodlist);
  }

  exitmethod(heap,ht,uid);
}

void doarrayassignment(struct heap_state *heap, struct heap_object * src, int index, struct heap_object *dst) {
  struct arraylist *arrptr=src->al;
  struct arraylist *al=(struct arraylist *)calloc(1, sizeof(struct arraylist));
  al->index=index;
  al->object=dst;
  al->src=src;
  addobject(heap->changedset, src);

  if (dst!=NULL) {
    /* is dst contained object ?*/
    if (heap->options&OPTION_FCONTAINERS) {
      if (dst->reachable&FIRSTREF)
	dst->reachable|=NOTCONTAINER;
      dst->reachable|=FIRSTREF;
    } else if (heap->options&OPTION_UCONTAINERS) {
      if (contains(heap->containedobjects, dst->uid))
	al->propagaterole=1;
    }
    addobject(heap->changedset, dst);
    doaddfield(heap, src);
    al->dstnext=dst->reversearray;
    dst->reversearray=al;
  }

  /* Handle empty list case */
  if (arrptr==NULL) {
    if(dst!=NULL)
      src->al=al;
    else
      free(al);
    return;
  }
  /* Handle match on first element */
  if (arrptr->index==al->index) {
    struct arraylist * arrtmp=arrptr->next;
    if(dst!=NULL) {
      al->next=arrtmp;
      src->al=al;
    } else {
      src->al=arrtmp;
      free(al);
    }

    removereversearrayreference(arrptr);
    dodelfield(heap, src, arrptr->object);
    free(arrptr);
    return;
  }
  
  /* Handle match on nth element */
  while (arrptr->next!=NULL) {
    if (arrptr->next->index==al->index) {
      struct arraylist * arrtmp=arrptr->next;
      if (dst!=NULL) {
	al->next=arrptr->next->next;
	arrptr->next=al;
      } else {
	arrptr->next=arrptr->next->next;
	free(al);
      }
      removereversearrayreference(arrtmp);
      dodelfield(heap, src, arrtmp->object);
      free(arrtmp);
      return;
    }
    arrptr=arrptr->next;
  }
  /*No match, just add*/
  if (dst!=NULL)
    arrptr->next=al;
  else
    free(al);
}

void removeforwardarrayreference(struct arraylist * al) {
  struct heap_object * dst=al->src;
  struct arraylist * dal=dst->al;

  if(dal==al) {
    dst->al=dal->next;
    return;
  }
  
  while(dal->next!=al)
    dal=dal->next;

  dal->next=al->next;
}

void removeforwardfieldreference(struct fieldlist * al) {
  struct heap_object * dst=al->src;
  struct fieldlist * dal=dst->fl;

  if(dal==al) {
    dst->fl=dal->next;
    return;
  }
  
  while(dal->next!=al)
    dal=dal->next;

  dal->next=al->next;
}

void removereversearrayreference(struct arraylist * al) {
  struct heap_object * dst=al->object;
  struct arraylist * dal=dst->reversearray;

  if(dal==al) {
    dst->reversearray=dal->dstnext;
    return;
  }
  
  while(dal->dstnext!=al)
    dal=dal->dstnext;

  dal->dstnext=al->dstnext;
}

void removereversefieldreference(struct fieldlist * al) {
  struct heap_object * dst=al->object;
  struct fieldlist * dal=dst->reversefield;

  if(dal==al) {
    dst->reversefield=dal->dstnext;
    return;
  }
  
  while(dal->dstnext!=al)
    dal=dal->dstnext;

  dal->dstnext=al->dstnext;
}


void freemethodlist(struct heap_state *hs) {
  while(hs->freemethodlist!=NULL) {
    struct method *tmp=hs->freemethodlist->caller;
#ifdef EFFECTS
    if (!(hs->options&OPTION_NOEFFECTS)) {
      freedatahashtable(hs->freemethodlist->pathtable,(void (*) (void*)) &freeeffects);
      freeeffectlist(hs->freemethodlist->effects);
    }
#endif
    free(hs->freemethodlist->params);
    free(hs->freemethodlist);
    hs->freemethodlist=tmp;
  }
}


void freekilltuplelist(struct killtuplelist * tl) {
  struct referencelist *ptr=tl->rl;
  while(ptr!=NULL) {
    struct referencelist *tmp=ptr->next;
    free(ptr);
    ptr=tmp;
  }
  free(tl);
}

int matchlist(struct referencelist *list1, struct referencelist *list2) {
  struct referencelist *list2it=list2;
  while(list1!=NULL) {
    while(list2it!=NULL) {
      if((list1->gl==list2it->gl)&&(list1->lv==list2it->lv))
	return 1;
      list2it=list2it->next;
    }
    list1=list1->next;
    list2it=list2;
  }
  return 0;
}

int matchrl(struct referencelist *key, struct referencelist *list) {
  while(list!=NULL) {
    if((key->gl==list->gl)&&(key->lv==list->lv))
      return 1;
    list=list->next;
  }
  return 0;
}

void doaddglobal(struct heap_state *hs, struct globallist *gl) {
  struct referencelist *nr=(struct referencelist *)calloc(1, sizeof(struct referencelist));
  nr->gl=gl;
  nr->next=hs->newreferences;
  hs->newreferences=nr;
}

void doaddlocal(struct heap_state *hs, struct localvars *lv) {
  struct referencelist *nr=(struct referencelist *)calloc(1, sizeof(struct referencelist));
  nr->lv=lv;
  nr->next=hs->newreferences;
  hs->newreferences=nr;
}

void doaddfield(struct heap_state *hs, struct heap_object *ho) {
  addobject(hs->N, ho);
  return;
}

void dodelfield(struct heap_state *hs, struct heap_object *src, struct heap_object *dst) {
  addobject(hs->changedset, dst);
  addobjectpair(hs->K, src, NULL,NULL,dst);
}

void dodellvfield(struct heap_state *hs, struct localvars *src, struct heap_object *dst) {
  addobject(hs->changedset, dst);
  addobjectpair(hs->K, NULL, src,NULL,dst);
}

void dodelglbfield(struct heap_state *hs, struct globallist *src, struct heap_object *dst) {
  addobject(hs->changedset, dst);
  addobjectpair(hs->K, NULL, NULL,src,dst);
}

void dofieldassignment(struct heap_state *hs, struct heap_object * src, struct fieldname * field, struct heap_object * dst) {
  struct fieldlist *fldptr=src->fl;
  struct fieldlist *newfld=(struct fieldlist *)calloc(1,sizeof(struct fieldlist));



  newfld->fieldname=field;
  newfld->object=dst;
  newfld->src=src;
  addobject(hs->changedset, src);

  if (dst!=NULL) {
    /* is dst contained object ?*/
    if (hs->options&OPTION_FCONTAINERS) {
      if (dst->reachable&FIRSTREF)
	dst->reachable|=NOTCONTAINER;
      dst->reachable|=FIRSTREF;
    } else if (hs->options&OPTION_UCONTAINERS) {
      if (contains(hs->containedobjects, dst->uid))
	newfld->propagaterole=1;
    }

    addobject(hs->changedset, dst);
    doaddfield(hs, src);
    newfld->dstnext=dst->reversefield;
    dst->reversefield=newfld;
  }

  /* Handle empty field list */
  if (fldptr==NULL) {
    if (dst!=NULL)
      src->fl=newfld;
    else
      free(newfld);
    return;
  }

  /*Handle match on first field */
  if (fldptr->fieldname==newfld->fieldname) {
    struct fieldlist * fldtmp=fldptr->next;
    if (dst!=NULL) {
      newfld->next=fldtmp;
      src->fl=newfld;
    } else {
      src->fl=fldtmp;
      free(newfld);
    }
    removereversefieldreference(fldptr);
    dodelfield(hs, src, fldptr->object);
    free(fldptr);
    return;
  }

  /*Handle match on nth field*/
  while(fldptr->next!=NULL) {
    if(fldptr->next->fieldname==newfld->fieldname) {
      struct fieldlist * tmpptr=fldptr->next;
      if (dst!=NULL) {
	newfld->next=fldptr->next->next;
	fldptr->next=newfld;
      } else {
	fldptr->next=fldptr->next->next;
	free(newfld);
      }
      removereversefieldreference(tmpptr);
      dodelfield(hs, src, tmpptr->object);
      free(tmpptr);
      return;
    }
    fldptr=fldptr->next;
  }
  
  /*No match*/
  if (dst!=NULL) {
    fldptr->next=newfld;
  }
  else
    free(newfld);
}

void doglobalassignment(struct heap_state *hs, struct fieldname *field, struct heap_object * dst) {
  struct globallist *fldptr=hs->gl;
  struct globallist *newfld=(struct globallist *)calloc(1,sizeof(struct globallist));
  newfld->age=pointerage++;
  newfld->fieldname=field;
  newfld->object=dst;


  if (dst!=NULL) {
    doaddglobal(hs, newfld);
    doaddfield(hs, dst);
    addobject(hs->changedset, dst);
  }

  /* Handle empty field list */
  if (fldptr==NULL) {
    if (dst!=NULL)
      hs->gl=newfld;
    else 
      free(newfld);
    return;
  }

  /*Handle match on first field */
  if (fldptr->fieldname==newfld->fieldname) {
    if (dst!=NULL) {
      newfld->next=fldptr->next;
      hs->gl=newfld;
    } else {
      hs->gl=hs->gl->next;
      free(newfld);
    }
    dodelglbfield(hs, fldptr, fldptr->object);
    freeglb(hs,fldptr);
  }
  /*Handle match on nth field*/
  while(fldptr->next!=NULL) {
    if (fldptr->next->fieldname==newfld->fieldname) {
      struct globallist * tmpptr=fldptr->next;
      if (dst!=NULL) {
	newfld->next=fldptr->next->next;
	fldptr->next=newfld;
      } else {
	fldptr->next=fldptr->next->next;
	free(newfld);
      }
      dodelglbfield(hs, tmpptr, tmpptr->object);
      freeglb(hs,tmpptr);
      return;
    }
    fldptr=fldptr->next;
  }
  
  /*No match*/
  if (dst!=NULL)
    fldptr->next=newfld;
  else
    free(newfld);
}


void addtolvlist(struct heap_state * heap, struct localvars * lv, struct method * method) {
  int lvnum=lv->lvnumber;
  struct localvars * lvlist=method->lv;

  /* Empty list case */
  if (method->lv==NULL) {
    if (lv->object!=NULL)
      method->lv=lv;
    else
      free(lv);
    return;
  }
  
  /* First element case */
  if (((lvnum==-1)&&strcmp(lvlist->name, lv->name)==0)||
      ((lvnum!=-1)&&(lvnum==lvlist->lvnumber))) {
    struct localvars * lvnext=lvlist->next;
    dodellvfield(heap, lvlist, lvlist->object);
    freelv(heap,lvlist);
    if (lv->object!=NULL) {
      lv->next=lvnext;
      method->lv=lv;
    } else {
      method->lv=lvnext;
      free(lv);
    }
    return;
  }

  /* Nth element case */
    
  while (lvlist->next!=NULL) {
    if (((lvnum==-1)&&strcmp(lvlist->next->name, lv->name)==0)||
	((lvnum!=-1)&&(lvnum==lvlist->next->lvnumber))) {
      struct localvars * lvnext=lvlist->next->next;
      dodellvfield(heap, lvlist->next, lvlist->next->object);
      freelv(heap,lvlist->next);
      if (lv->object!=NULL) {
	lv->next=lvnext;
	lvlist->next=lv;
      } else {
	lvlist->next=lvnext;
	free(lv);
      }
      return;
    }
    lvlist=lvlist->next;
  }
  /* No match case...lvlist->next==null*/
  if (lv->object!=NULL)
    lvlist->next=lv;
  else 
    free(lv);
}

void freelv(struct heap_state* heap, struct localvars * lv) {
  struct referencelist *rl=(struct referencelist *) calloc(1, sizeof(struct referencelist));
  rl->lv=lv;
  rl->next=heap->freelist;
  heap->freelist=rl;
  lv->invalid=1;
}

void freeglb(struct heap_state *heap, struct globallist * glb) {
  struct referencelist *rl=(struct referencelist *) calloc(1, sizeof(struct referencelist));
  rl->gl=glb;
  rl->next=heap->freelist;
  heap->freelist=rl;
  glb->invalid=1;
}

void removelvlist(struct heap_state * heap, char * lvname, struct method * method) {
  struct localvars * lvlist=method->lv;

  /* Empty list case */
  if (method->lv==NULL) {
    return;
  }
  
  /* First element case */
  if (strcmp(lvlist->name, lvname)==0) { 
    method->lv=lvlist->next;
    dodellvfield(heap, lvlist, lvlist->object);
    freelv(heap,lvlist);
    return;
  }

  /* Nth element case */
  while (lvlist->next!=NULL) {
    if (strcmp(lvlist->next->name, lvname)==0) {
      struct localvars * lvtmp=lvlist->next;
      lvlist->next=lvtmp->next;
      dodellvfield(heap, lvtmp, lvtmp->object);
      freelv(heap,lvtmp);
      return;
    }
    lvlist=lvlist->next;
  }

  /* No match case...Do nothing*/
}

int lvnumber(char *lv) {
  if (strncmp("lv",lv,2)==0) {
    int lvnumber;
    sscanf(lv,"lv%d_",&lvnumber);
    return lvnumber;
  } else
    return -1;
}

void freemethod(struct heap_state *heap, struct method * m) {
  struct localvars * lvptr, * lvptrn;
  if (m!=NULL) {
    lvptr=m->lv;
    while(lvptr!=NULL) {
      lvptrn=lvptr->next;

      dodellvfield(heap, lvptr, lvptr->object);
      freelv(heap,lvptr);

      lvptr=lvptrn;
    }
    m->caller=heap->freemethodlist;
    heap->freemethodlist=m;
  }
}

void showmethodstack(struct heap_state *heap) {
  struct method * mptr=heap->methodlist;
  while(mptr) {
    printmethod(*mptr);
    mptr=mptr->caller;
  }
}

void printmethod(struct method m) {
  printf("%s.%s %s %hd\n",m.methodname->classname->classname, m.methodname->methodname,m.methodname->signature, m.isStatic);
  
}

char bufdata[bufsize];
unsigned long buflength=0;
unsigned long bufstart=0;

char * copystr(const char *buf) {
  int i;
  if (buf==NULL)
    return NULL;
  for(i=0;;i++)
    if (buf[i]==0) {
      char *ptr=(char *)malloc(i+1);
      memcpy(ptr,buf,i+1);
      return ptr;
    }
}

char * getline() {
  char *bufreturn=(char *)malloc(bufsize+1);
  int i,offset;
  if (bufstart==buflength) {
    /*Base Case*/
    buflength=read(programfd, bufdata, bufsize);
    bufstart=0;
    if (buflength<0)
      perror("Error reading line\n");
    if (buflength==0)
      return 0;
  }
  offset=0;

  while(1) {
    for(i=bufstart;i<buflength;i++)
      if (bufdata[i]==10) {
	if ((offset+i+1-bufstart)>bufsize)
	  printf("ERROR: Buffer overrun");
	memcpy(bufreturn+offset, bufdata+bufstart, i+1-bufstart);
	bufreturn[offset+i+1-bufstart]=0;
	bufstart=i+1;
	return bufreturn;
      }
    
    if ((offset+buflength-bufstart)>bufsize)
      printf("ERROR: Buffer overrun");

    memcpy(bufreturn+offset, bufdata+bufstart, buflength-bufstart);
    offset+=buflength-bufstart;
    buflength=read(programfd, bufdata, bufsize);
    if (buflength<0)
      perror("Error reading line\n");
    if (buflength==0)
      return 0;
    bufstart=0;
  }
}

void calculatenumobjects(struct method * m) {
  int numobjects=0,i=0;
  char * sig=m->methodname->signature;

  if (m->isStatic==0)
    numobjects++;

  for(i=1;sig[i]!=0;i++) {
    switch(sig[i]) {
    case '[':
      numobjects++;
      i++;
      if (sig[i]=='L') 
	for(;sig[i]!=';';i++);
      break;
    case 'L':
      numobjects++;
      for(;sig[i]!=';';i++);
      break;
    case ')':
      m->numobjectargs=numobjects;
      if (numobjects!=0)
	m->params=(struct heap_object **) calloc(numobjects, sizeof(struct heap_object *));
      return;
    default:
    }
  }
}

int atomicflag=0;

int atomic(struct heap_state *heap) {
  /* Return 1 if atomic method is on methodstack*/
  return atomicflag;
}

void atomiceval(struct heap_state *heap) {
  if(atomicflag==1) {
    /*Check entire stack*/
    struct method *m=heap->methodlist;
    m=m->caller;
    while(m!=NULL) {
      if (atomicmethod(heap,m)) {
	atomicflag=1;
	return;
      }
      m=m->caller;
    }
    atomicflag=0;
    return;
  } else {
    /*Just need to check second to top*/
    if (heap->methodlist->caller!=NULL&&
	atomicmethod(heap, heap->methodlist->caller))
      atomicflag=1;
  }
}

int atomicmethod(struct heap_state *hs, struct method *m) {
  char buf[700];
  sprintf(buf, "%s.%s%s",m->methodname->classname->classname,m->methodname->methodname,m->methodname->signature);
  if (gencontains(hs->atomicmethodtable, buf))
    return 1;
  else
    return 0;
}

void loadatomics(struct heap_state *heap) {
  FILE *file=fopen("atomic","r");
  char buf[200];

  if (file==NULL)
    return;
  while(1) {
    char *ptr;
    int flag=fscanf(file, "%s\n", buf);
    if (flag<=0)
      break;
    ptr=copystr(buf);
    genputtable(heap->atomicmethodtable, ptr, NULL);
  }
}

void loadstatechange(struct heap_state *heap) {
  FILE *file=fopen("statechange","r");
  char buf[600],tmp[600];
  int counter=0;
  if (file==NULL)
    return;
  while(1) {
    char *ptr;
    int position;
    int flag=fscanf(file, "%s %d\n", buf, &position);
    if (flag<=0)
      break;
    {
      struct statechangeinfo *sci=(struct statechangeinfo *) calloc(1, sizeof(struct statechangeinfo));
      sci->id=counter++;
      sprintf(tmp, "%s %d", buf, position);
      ptr=copystr(tmp);
      genputtable(heap->statechangemethodtable, ptr, sci);
      sprintf(tmp, "%s PARAM: %d", buf, position);
      ptr=copystr(tmp);
      puttable(heap->statechangereversetable, sci->id, ptr);
    }
  }
  heap->statechangesize=counter;
}

int convertnumberingobjects(char *sig, int isStatic, int orignumber) {
  int numobjects=0,i=0, c=0;



  if (isStatic==0) {
    numobjects++;
    if (orignumber==0)
      return c;
    c++;
  }

  for(i=1;sig[i]!=0;i++) {
    switch(sig[i]) {
    case '[':
      if (orignumber==numobjects)
	return c;
      numobjects++;
      i++;
      if (sig[i]=='L') 
	for(;sig[i]!=';';i++);
      break;
    case 'L':
      if (orignumber==numobjects)
	return c;
      numobjects++;
      for(;sig[i]!=';';i++);
      break;
    case ')':
      return c;
    default:
    }
    c++;
  }
  printf("ERROR 2 in convertnumberingobjects\n");
  return -1;
}
