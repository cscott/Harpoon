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
#ifdef MDEBUG
#include <dmalloc.h>
#endif

static int programfd;
#define bufsize 1000
#define DEBUG
long long pointerage=0;


int main() {
  getfile();
  initloopstructures();
  doanalysis();
  return 0;
}

void getfile() {
  programfd=open("roleinfer.mem",O_RDONLY);
  if (programfd<0)
    perror("roleinfer.mem open failure\n");
}

void doanalysis() {
  struct heap_state heap;
  struct hashtable * ht=allocatehashtable();
  int currentparam=0;
  
  heap.K=createobjectpair();
  heap.N=createobjectset();
  heap.gl=NULL;
  heap.methodlist=0;
  heap.newreferences=NULL;
  heap.freelist=NULL;
  heap.freemethodlist=NULL;
  heap.roletable=genallocatehashtable((int (*)(void *)) &rolehashcode, (int (*)(void *,void *)) &equivalentroles);
  heap.methodtable=genallocatehashtable((int (*)(void *)) &methodhashcode, (int (*)(void *,void *)) &comparerolemethods);


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
      long long srcuid, dstuid;
      int srcpos, dstpos,length;
      struct arraylist *tmp=NULL, *al;
      struct heap_object *ho, *dsto;
      sscanf(line,"CA: %lld %d %lld %d %d", &srcuid, &srcpos, &dstuid, &dstpos, &length);
      ho=(struct heap_object *)gettable(ht, srcuid);
      dsto=(struct heap_object *)gettable(ht, dstuid);
      al=ho->al;
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
      while(tmp!=NULL) {
	struct arraylist *tmpal=tmp->next;
	doarrayassignment(&heap, dsto, tmp->index-srcpos+dstpos, tmp->object);
#ifdef EFFECTS
	addeffect(&heap, dstuid, "[]", tmp->object->uid);
#endif
	free(tmp);
	tmp=tmpal;
      }
    }
    break;
    case 'O': {
      long long origuid, cloneuid;
      struct fieldlist *fl;
      struct arraylist *al;
      struct heap_object *ho, *clone;
      sscanf(line, "ON: %lld %lld", &origuid, &cloneuid);
      ho=(struct heap_object *)gettable(ht, origuid);
      clone=(struct heap_object *)gettable(ht, cloneuid);
      fl=ho->fl;
      al=ho->al;
      while(fl!=NULL) {
	dofieldassignment(&heap, clone, fl->fieldname, fl->object);
#ifdef EFFECTS
	addeffect(&heap, origuid, fl->fieldname, cloneuid);	
#endif
	fl=fl->next;
      }
      while(al!=NULL) {
	doarrayassignment(&heap, clone, al->index, al->object);
#ifdef EFFECTS
	addeffect(&heap, origuid, "[]", cloneuid);	
#endif
	al=al->next;
      }
    }
    break;
    case 'N':
      {
	struct heap_object *ho=(struct heap_object *) calloc(1, sizeof(struct heap_object));

	char buf[1000];
	sscanf(line,"NI: %s %lld",buf, &ho->uid);
	ho->class=copystr(buf);
	ho->reachable=2;
	puttable(ht, ho->uid, ho);
	addnewobjpath(&heap, ho->uid);
      }
      break;
    case 'U':
      {
	struct heap_object *ho=(struct heap_object *) calloc(1, sizeof(struct heap_object));
	char buf[1000];
	sscanf(line,"UI: %s %lld",buf, &ho->uid);
	ho->class=copystr(buf);
	puttable(ht, ho->uid, ho);
	addnewobjpath(&heap, ho->uid);
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
	char fieldname[100], classname[100], fielddesc[100];


	sscanf(line,"LF: %s %ld %s %lld %s %s %s %lld",lv->name,&lv->linenumber, lv->sourcename, &objuid, classname, fieldname, fielddesc, &uid);
	lv->lvnumber=lvnumber(lv->name);
	lv->age=pointerage++;
	lv->m=heap.methodlist;
	
	if (uid!=-1) {
	  lv->object=gettable(ht, uid);
	  doaddlocal(&heap, lv);
	  doaddfield(&heap,lv->object);
	}

#ifdef EFFECTS
	if ((uid!=-1)&&(objuid!=-1)) {
	  addpath(&heap, objuid, classname, fieldname, fielddesc, uid);
	}
#endif
	
	/* addtolvlist add's to K set */
	addtolvlist(&heap, lv, heap.methodlist);
      }
      break;

    case 'G':
      /* Do Load */
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
	}

#ifdef EFFECTS
	if ((uid!=-1)&&(objuid!=-1)) {
	  addarraypath(&heap, ht, objuid, uid);
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
	}
	/* addtolvlist add's to K set */
	addtolvlist(&heap, lv, heap.methodlist);
	


	if (currentparam<heap.methodlist->numobjectargs) {
	  if (uid!=-1) {
	    heap.methodlist->params[currentparam]=lv->object;
#ifdef EFFECTS
	    if (!contains(heap.methodlist->pathtable, uid)) {
	      struct path * pth=(struct path *) calloc(1, sizeof(struct path));
	      pth->paramnum=currentparam;
	      pth->prev_obj=-1;
	      puttable(heap.methodlist->pathtable, uid ,pth);
	    }
#endif
	  }
	  currentparam++;
	  if(currentparam==heap.methodlist->numobjectargs) {
	    //Lets show the roles!!!!
	    doincrementalreachability(&heap,ht);
	    {
	    int i=0;
	    struct genhashtable * dommap=builddominatormappings(&heap,0);
	    struct rolemethod * rolem=(struct rolemethod *) calloc(1, sizeof(struct rolemethod));
	    
	    rolem->classname=copystr(heap.methodlist->classname);
	    rolem->methodname=copystr(heap.methodlist->methodname);
	    rolem->signature=copystr(heap.methodlist->signature);
	    rolem->paramroles=(char **)calloc(heap.methodlist->numobjectargs, sizeof(char *));
	    rolem->numobjectargs=heap.methodlist->numobjectargs;
	    rolem->isStatic=heap.methodlist->isStatic;
	    
#ifdef DEBUG
	    printf("Calling Context for method %s.%s%s:\n", heap.methodlist->classname, heap.methodlist->methodname, heap.methodlist->signature);
#endif
	    for(;i<heap.methodlist->numobjectargs;i++) {
	      if (heap.methodlist->params[i]!=NULL) {
		rolem->paramroles[i]=findrolestring(&heap, dommap, heap.methodlist->params[i]);
	      }
	    }
	    methodassignhashcode(rolem);
	    rolem=methodaddtable(&heap,rolem);
	    heap.methodlist->rm=rolem;
	    genfreekeyhashtable(dommap);
	    }
	  }

	  freemethodlist(&heap);
	}

      }
      break;
    case 'I':
      /* Enter Method*/
      {
	struct method* newmethod=(struct method *) calloc(1,sizeof(struct method));
	sscanf(line,"IM: %s %s %s %hd", newmethod->classname, newmethod->methodname, newmethod->signature, &newmethod->isStatic);
	calculatenumobjects(newmethod);
	newmethod->caller=heap.methodlist;
	heap.methodlist=newmethod;
#ifdef EFFECTS
	initializepaths(&heap);
#endif
	currentparam=0;
      }
      if (currentparam==heap.methodlist->numobjectargs) {
	struct rolemethod * rolem=(struct rolemethod *) calloc(1, sizeof(struct rolemethod));
	
	rolem->classname=copystr(heap.methodlist->classname);
	rolem->methodname=copystr(heap.methodlist->methodname);
	rolem->signature=copystr(heap.methodlist->signature);
	if (heap.methodlist->numobjectargs!=0)
	  rolem->paramroles=(char **)calloc(heap.methodlist->numobjectargs, sizeof(char *));
	rolem->numobjectargs=heap.methodlist->numobjectargs;
	rolem->isStatic=heap.methodlist->isStatic;
	methodassignhashcode(rolem);
	rolem=methodaddtable(&heap,rolem);
	heap.methodlist->rm=rolem;
#ifdef DEBUG
	printf("Calling Context for method %s.%s%s:\n", heap.methodlist->classname, heap.methodlist->methodname, heap.methodlist->signature);
#endif
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
	doreturnmethodinference(&heap, uid, ht);
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
	sscanf(line,"FA: %lld %s %s %lld", &suid, classname, fieldname, &duid);
	if (suid!=-1)
	  src=gettable(ht,suid);
	if (duid!=-1)
	  dst=gettable(ht,duid);
	if (src!=NULL) {
	  dofieldassignment(&heap, src, fieldname, dst);
	  addeffect(&heap, suid, fieldname, duid);
	} else {
	  char buffer[1000];
	  sprintf(buffer,"%s.%s", classname,fieldname);
	  doglobalassignment(&heap,classname,fieldname,dst);
	  addeffect(&heap, -1, buffer,duid);
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
	addeffect(&heap, suid, "[]", duid);	
#endif
      }
      break;
    }
    free(line);
  }

  {
    struct geniterator *it=gengetiterator(heap.methodtable);
    while(1) {
      struct rolemethod *method=(struct rolemethod *) gennext(it);
      if (method==NULL)
	break;
      printrolemethod(method);
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

  doincrementalreachability(heap,ht);
  updateroleeffects(heap); /*Merge in this invocation's effects*/
    //Lets show the roles!!!!
  {
    int i=0;
    struct genhashtable * dommap=builddominatormappings(heap,1);
    struct rolereturnstate *rrs=(struct rolereturnstate *)calloc(1, sizeof(struct rolereturnstate));
    if (heap->methodlist->numobjectargs!=0)
      rrs->paramroles=(char **)calloc(heap->methodlist->numobjectargs, sizeof(char *));
#ifdef DEBUG
    printf("Returning Context for method %s.%s%s:\n", heap->methodlist->classname, heap->methodlist->methodname, heap->methodlist->signature);
#endif
    for(;i<heap->methodlist->numobjectargs;i++) {
      if (heap->methodlist->params[i]!=NULL) {
	rrs->paramroles[i]=findrolestring(heap, dommap, heap->methodlist->params[i]);
      }
    }
    if (uid!=-1) {
      rrs->returnrole=findrolestring(heap, dommap, gettable(ht,uid));
    }
    addrolereturn(heap->methodlist->rm,rrs);

    genfreekeyhashtable(dommap);
  }
}

void doarrayassignment(struct heap_state *heap, struct heap_object * src, int index, struct heap_object *dst) {
  struct arraylist *arrptr=src->al;
  struct arraylist *al=(struct arraylist *)calloc(1, sizeof(struct arraylist));
  al->index=index;
  al->object=dst;
  al->src=src;

  if (dst!=NULL) {
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

void doincrementalreachability(struct heap_state *hs, struct hashtable *ht) {
  struct objectset * changedset;

  changedset=dokills(hs);
  donews(hs, changedset);

  {
    /* Kill dead reference structures*/
    struct referencelist * rl=hs->freelist;
    hs->freelist=NULL;
    while (rl!=NULL) {
      struct referencelist * nxtrl=rl->next;
      if (rl->lv!=NULL)
	free(rl->lv);
      else if(rl->gl!=NULL) {
	free(rl->gl->classname);
	free(rl->gl->fieldname);
	free(rl->gl);
      }
      free(rl);
      rl=nxtrl;
    }
  }

  /* Lets do our GC now...*/
  while(1) {
    struct heap_object * possiblegarbage=removeobject(changedset);
    if (possiblegarbage==NULL)
      break;
    if ((possiblegarbage->rl==NULL)&&(possiblegarbage->reachable==0)) {
      /*We've got real garbage!!!*/
      struct fieldlist *fl;
      struct arraylist *al;
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
      free(possiblegarbage->class);
      /* printf("Freeing Key %lld\n",possiblegarbage->uid);*/
      freekey(ht,possiblegarbage->uid);
      free(possiblegarbage);
    }
  }


  freeobjectset(changedset);
}

void freemethodlist(struct heap_state *hs) {
  while(hs->freemethodlist!=NULL) {
    struct method *tmp=hs->freemethodlist->caller;
#ifdef EFFECTS
    freedatahashtable(hs->freemethodlist->pathtable,(void (*) (void*)) &freeeffects);
    freeeffectlist(hs->freemethodlist->effects);
#endif
    free(hs->freemethodlist->params);
    free(hs->freemethodlist);
    hs->freemethodlist=tmp;
  }
}

struct objectset * dokills(struct heap_state *hs) {
  /* Flush out old reachability information */
  /* Remove K set */
  struct killtuplelist * kptr=NULL;
  struct objectpair * op=hs->K;
  struct objectset *os=(struct objectset *)calloc(1,sizeof(struct objectset));
  
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
	if (srcobj->reachable!=0)
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
    addobject(os, tuple->ho);
    
    /*cycle through the lists*/
    /*adding only if R(t) intersect S!={}*/
    while(fl!=NULL) {
      if (matchlist(fl->object->rl, tuple->rl)||((fl->object->reachable==1)&&(tuple->reachable==1))) {
	struct killtuplelist *ktpl=(struct killtuplelist *) calloc(1,sizeof(struct killtuplelist));
	struct referencelist *rtmp=tuple->rl;
	if (fl->object->reachable==1)
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
      if (matchlist(al->object->rl, tuple->rl)||((al->object->reachable==1)&&(tuple->reachable==1))) {
	struct killtuplelist *ktpl=(struct killtuplelist *) calloc(1,sizeof(struct killtuplelist));
	struct referencelist *rtmp=tuple->rl;
	if (rtmp!=NULL) {
	  ktpl->ho=al->object;
	  ktpl->next=kptr;
	  kptr=ktpl;
	  if (al->object->reachable==1)
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
    
    if ((ho->reachable==1)&&(tuple->reachable==1))
      ho->reachable=0;
    
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

void donews(struct heap_state *hs, struct objectset * os) {
  struct ositerator *it=getIterator(os);
  struct objectset *N=hs->N;
  while(hasNext(it)) {
    struct heap_object* ho=nextobject(it);
    addobject(N, ho);
  }
  freeIterator(it);
  /*N set now has all elements of interest*/

  /* Add root edges in reachability info*/
  while(hs->newreferences!=NULL) {
    struct referencelist *rl=hs->newreferences;
    struct heap_object *ho=NULL;
    hs->newreferences=rl->next;

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
    struct heap_object *object=removeobject(N);

    if (object==NULL)
      break; /* We're done!!!! */
    else {
      struct arraylist *al=object->al;
      struct fieldlist *fl=object->fl;

      /* Cycle through all fields/array indexes*/
      while(al!=NULL) {
	propagaterinfo(N, object, al->object);
	al=al->next;
      }
      while(fl!=NULL) {
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
  if ((src->reachable!=0)&&(dst->reachable==0)) {
    dst->reachable=1;
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
  addobjectpair(hs->K, src, NULL,NULL,dst);
}

void dodellvfield(struct heap_state *hs, struct localvars *src, struct heap_object *dst) {
  addobjectpair(hs->K, NULL, src,NULL,dst);
}

void dodelglbfield(struct heap_state *hs, struct globallist *src, struct heap_object *dst) {
  addobjectpair(hs->K, NULL, NULL,src,dst);
}

void dofieldassignment(struct heap_state *hs, struct heap_object * src, char * field, struct heap_object * dst) {
  struct fieldlist *fldptr=src->fl;
  struct fieldlist *newfld=(struct fieldlist *)calloc(1,sizeof(struct fieldlist));
  newfld->fieldname=copystr(field);
  newfld->object=dst;
  newfld->src=src;

  if (dst!=NULL) {
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
  if (strcmp(fldptr->fieldname,newfld->fieldname)==0) {
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
    if(strcmp(fldptr->next->fieldname,newfld->fieldname)==0) {
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
  if (dst!=NULL)
    fldptr->next=newfld;
  else
    free(newfld);
}

void doglobalassignment(struct heap_state *hs, char * class, char * field, struct heap_object * dst) {
  struct globallist *fldptr=hs->gl;
  struct globallist *newfld=(struct globallist *)calloc(1,sizeof(struct globallist));
  newfld->age=pointerage++;
  newfld->classname=copystr(class);
  newfld->fieldname=copystr(field);
  newfld->object=dst;


  if (dst!=NULL) {
    doaddglobal(hs, newfld);
    doaddfield(hs, dst);
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
  if ((strcmp(fldptr->classname,newfld->classname)==0)&&(strcmp(fldptr->fieldname,newfld->fieldname)==0)) {
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
    if ((strcmp(fldptr->next->classname,newfld->classname)==0)&&(strcmp(fldptr->next->fieldname,newfld->fieldname)==0)) {
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
  printf("%s.%s %s %hd\n",m.classname, m.methodname,m.signature, m.isStatic);
  
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
  char * sig=m->signature;

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
