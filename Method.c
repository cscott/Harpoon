#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include "Method.h"
#include "Role.h"
#include "Effects.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

#define KARENHACK /* Gross hack for Karen's free stuff...*/

void exitmethod(struct heap_state *heap, struct hashtable *ht, long long uid) {
  //Lets show the roles!!!!
  int i=0;
  struct genhashtable * dommap;
  struct rolereturnstate *rrs=(struct rolereturnstate *)calloc(1, sizeof(struct rolereturnstate));
#ifdef DEBUG
  printf("Returning Context for method %s.%s%s:\n", heap->methodlist->methodname->classname->classname, heap->methodlist->methodname->methodname, heap->methodlist->methodname->signature);
#endif

  doincrementalreachability(heap,ht,1);
  updateroleeffects(heap); /*Merge in this invocation's effects*/
  dommap=builddominatormappings(heap,1);

  if (heap->methodlist->numobjectargs!=0)
    rrs->paramroles=(char **)calloc(heap->methodlist->numobjectargs, sizeof(char *));
  for(;i<heap->methodlist->numobjectargs;i++) {
    if (heap->methodlist->params[i]!=NULL) {
      rrs->paramroles[i]=findrolestring(heap, dommap, heap->methodlist->params[i],1);
    }
  }
  if (uid!=-1) {
    rrs->returnrole=findrolestring(heap, dommap, gettable(ht,uid),1);
  }
  addrolereturn(heap->methodlist->rm,rrs);
  /* Finished building return instantiation of method*/

  /* Assign roles to all objects that might have changed*/
  while(!setisempty(heap->changedset)) {
    struct heap_object *ho=removeobject(heap->changedset,NULL);
    free(findrolestring(heap, dommap, ho,1));
  }
  genfreekeyhashtable(dommap);
  /* Merge in any rolechanges we've observed*/
  mergerolechanges(heap);
}

void entermethod(struct heap_state * heap, struct hashtable * ht) {
  //Lets show the roles!!!!

  /* Create maps so we can calculate roles*/
  int i=0;
  struct genhashtable * dommap;

  /* Create role instantiated method structure*/
  struct rolemethod * rolem=(struct rolemethod *) calloc(1, sizeof(struct rolemethod));
  doincrementalreachability(heap,ht,0);
  dommap=builddominatormappings(heap,0);
  rolem->methodname=heap->methodlist->methodname;
  if (heap->methodlist->numobjectargs)
    rolem->paramroles=(char **)calloc(heap->methodlist->numobjectargs, sizeof(char *));
  rolem->numobjectargs=heap->methodlist->numobjectargs;
  rolem->isStatic=heap->methodlist->isStatic;
  
#ifdef DEBUG
  printf("Calling Context for method %s.%s%s:\n", heap->methodlist->methodname->classname->classname, heap->methodlist->methodname->methodname, heap->methodlist->methodname->signature);
#endif
  for(;i<heap->methodlist->numobjectargs;i++) {
    if (heap->methodlist->params[i]!=NULL) {
      rolem->paramroles[i]=findrolestring(heap, dommap, heap->methodlist->params[i],0);
    }
  }
  methodassignhashcode(rolem);
  rolem=methodaddtable(heap,rolem);
  if (rolem->rolechanges==NULL)
    rolem->rolechanges=genallocatehashtable((int (*)(void *)) &rcshashcode, (int (*)(void *,void *)) &equivalentrcs);


  heap->methodlist->rm=rolem;
  /* Finished with role instantiated method.*/

  /* Assign roles to all objects that might have changed*/
  while(!setisempty(heap->changedset)) {
    struct heap_object *ho=removeobject(heap->changedset,NULL);
    free(findrolestring(heap, dommap, ho,0));
  }

  /* increment call counter */

  rolem->numberofcalls++;

  genfreekeyhashtable(dommap);
  freemethodlist(heap);
} 



int methodhashcode(struct rolemethod * method) {
  return method->hashcode;
}

struct rolemethod * methodaddtable(struct heap_state * heap , struct rolemethod *method) {
  if (gencontains(heap->methodtable, method)) {
    struct rolemethod * retval=gengettable(heap->methodtable, method);
    methodfree(method);
    return retval;
  } else {
    genputtable(heap->methodtable, method,method);
    return method;
  }
}

void methodfree(struct rolemethod *method) {
  struct rolereturnstate * rrs=method->returnstates;
  int i;
  for(i=0;i<method->numobjectargs;i++) {
    free(method->paramroles[i]);
  }
  free(method->paramroles);
  while(rrs!=NULL) {
    struct rolereturnstate * tmp=rrs->next;
    freerolereturnstate(method->numobjectargs, rrs);
    rrs=tmp;
  }
  free(method);
}

void freerolereturnstate(int numobjectargs, struct rolereturnstate * rrs) {
  int i;
  for(i=0;i<numobjectargs;i++) {
    free(rrs->paramroles[i]);
  }
  free(rrs->paramroles);
  free(rrs->returnrole);
  free(rrs);
}

int equivalentrolereturnstate(int numobjectargs, struct rolereturnstate *rrs1, struct rolereturnstate *rrs2) {
  int i;
  if (!equivalentstrings(rrs1->returnrole, rrs2->returnrole))
    return 0;
  for(i=0;i<numobjectargs;i++)
    if (!equivalentstrings(rrs1->paramroles[i], rrs2->paramroles[i]))
      return 0;
  return 1;
}

void addrolereturn(struct rolemethod * method, struct rolereturnstate *rrs) {
  struct rolereturnstate *rptr=method->returnstates;
  while(rptr!=NULL) {
    if(equivalentrolereturnstate(method->numobjectargs, rptr, rrs)) {
      freerolereturnstate(method->numobjectargs,rrs);
      return;
    }
    rptr=rptr->next;
  }

  rrs->next=method->returnstates;
  method->returnstates=rrs;
}

void methodassignhashcode(struct rolemethod * method) {
  int i;
  int hashcode=hashptr(method->methodname);
  for(i=0;i<method->numobjectargs;i++)
    hashcode^=hashstring(method->paramroles[i]);
  method->hashcode=hashcode;
}

int comparerolemethods(struct rolemethod * m1, struct rolemethod *m2) {
  int i;

  if (m1->hashcode!=m2->hashcode)
    return 0;
  if (m1->methodname!=m2->methodname)
    return 0;
  if (m1->numobjectargs!=m2->numobjectargs) {
    printf("ERROR:  numobjectargs mimatch\n");
    return 0;
  }
  if (m1->isStatic!=m2->isStatic) {
    printf("ERROR:  isStatic mimatch\n");
    return 0;
  }
  for(i=0;i<m1->numobjectargs;i++)
    if (!equivalentstrings(m1->paramroles[i],m2->paramroles[i]))
      return 0;

  return 1;
}

void printrolemethod(struct rolemethod *method) {
  int i;
  struct rolereturnstate *rrs=method->returnstates;
  printf("Method {\n");
  printf(" %s.%s%s\n",method->methodname->classname->classname,method->methodname->methodname,method->methodname->signature);
  printf("  isStatic=%d\n  ",method->isStatic);
  for(i=0;i<method->numobjectargs;i++)
    printf("%s ",method->paramroles[i]);
  if (method->numobjectargs!=0)
    printf("\n");
  printf(" Effects:\n");
  printeffectlist(method->effects);
  while(rrs!=NULL) {
    printf("\n  Return Context:\n");
    if (method->numobjectargs!=0)
      printf("   ");
    for(i=0;i<method->numobjectargs;i++)
      printf("%s ",rrs->paramroles[i]);
    if (method->numobjectargs!=0)
      printf("\n");
   printf("   Return value=%s\n",rrs->returnrole);
    rrs=rrs->next;
  }
  printrolechanges(method);
  printf("}\n\n");
}

void printrolechanges(struct rolemethod *rm) {
  struct genhashtable *rolechanges=rm->rolechanges;
  struct geniterator *it=gengetiterator(rolechanges);
  while(1) {
    struct rolechangesum *rcs=(struct rolechangesum *)gennext(it);
    struct rolechangeheader *rch;
    struct rolechangepath *rcp;
    if (rcs==NULL) break;
    printf("%s -> %s ",rcs->origrole, rcs->newrole);
    rch=(struct rolechangeheader *)gengettable(rolechanges, rcs);
    rcp=rch->rcp;
    while(rcp!=NULL) {
      printf(" Path: ");
      if (rcp->exact==rm->numberofcalls)
	printf("Exact ");
      if (rcp->inner==2)
	printf("Inner ");
      else if(rcp->inner==1)
	printf("Maybe Inner ");

      printeffectregexpr(rcp->expr);
      printf("\n");
      rcp=rcp->next;
    }
  }
  genfreeiterator(it);
}

void mergerolechanges(struct heap_state *heap) {
  struct geniterator *it=gengetiterator(heap->methodlist->rolechangetable);
  while(1) {
    struct rolechange *rc=(struct rolechange *)gennext(it);
    struct method *method=heap->methodlist;
    int inner=2;
    if (rc==NULL)
      break;

    while(method!=NULL) {
      struct rolemethod *rm=method->rm;
      struct genhashtable * rolechanges=rm->rolechanges;
      struct rolechangesum *rcs=(struct rolechangesum *)calloc(1,sizeof(struct rolechangesum));
      struct rolechangeheader *rch;
      struct effectregexpr *ere=buildregexpr(method->pathtable, rc->uid);
      struct rolechangepath *rcp;

      rcs->origrole=copystr(rc->origrole);
      rcs->newrole=copystr(rc->newrole);
      if (!gencontains(rolechanges, rcs)) {
	rch=(struct rolechangeheader *)calloc(1,sizeof(struct rolechangeheader));
	genputtable(rolechanges,rcs,rch);
      } else {
	rch=(struct rolechangeheader *) gengettable(rolechanges,rcs);
	free(rcs->origrole);
	free(rcs->newrole);
	free(rcs);
      }
      /* rch points to appropriate rolechangeheader */
      /* ere points to our regular expression */
      rcp=rch->rcp;

      while(rcp!=NULL) {
	struct effectregexpr *ere2=rcp->expr;
	struct effectregexpr *erem=mergeeffectregexpr(ere,ere2);
	if (erem!=NULL) {
	  rcp->expr=erem;
	  if ((rcp->inner||inner)&&((inner==0)||(rcp->inner==0))) {
	    rcp->inner=1;
	  }
	  /*Update count */
	  if ((rcp->exact+1)==rm->numberofcalls)
	    rcp->exact=rm->numberofcalls;
	  if (rcp->exact<rm->numberofcalls)
	    rcp->exact=0;
	  freeeffectregexpr(ere2);
	  freeeffectregexpr(ere);
	  break;
	}
	rcp=rcp->next;
      }
      if(rcp==NULL) {
	/* Couldn't merge in */
	struct rolechangepath *rcp2=(struct rolechangepath *)calloc(1, sizeof(struct rolechangepath));
	rcp2->expr=ere;
	if(rm->numberofcalls==1)
	  rcp2->exact=1;
	rcp2->next=rch->rcp;
	rcp2->inner=inner;
	rch->rcp=rcp2;
      }
      method=method->caller;
      inner=0;
    }
    free(rc->origrole);
    free(rc->newrole);
    free(rc);
  }
  genfreeiterator(it);
  genfreehashtable(heap->methodlist->rolechangetable);
  heap->methodlist->rolechangetable=NULL;
}

int rcshashcode(struct rolechangesum *rcs) {
  int hashcode=hashstring(rcs->newrole);
#ifndef KARENHACK
  hashcode^=hashstring(rcs->origrole);
#endif
  return hashcode;
}

int equivalentrcs(struct rolechangesum *rcs1, struct rolechangesum *rcs2) {
  if (
#ifndef KARENHACK
equivalentstrings(rcs1->origrole, rcs2->origrole) &&
#endif
      equivalentstrings(rcs1->newrole, rcs2->newrole))
    return 1;
  else
    return 0;
}
