#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include "Role.h"
#include "Fields.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

#define DUPTHRESHOLD 2
#define ARRDUPTHRESHOLD 2

static long int rolenumber=1;
struct heap_state *hshash;


void printrole(struct heap_state *heap,struct role *r, char * rolename) {
  struct rolereferencelist *dominators=r->dominatingroots;
  fprintf(heap->rolefile,"Role %s {\n",rolename);
  fprintf(heap->rolefile," Class: %s\n", r->class->classname);
  fprintf(heap->rolefile," Dominated by:\n");
  while(dominators!=NULL) {
    if (dominators->methodname!=NULL) {
      fprintf(heap->rolefile,"  By local variable: (%s) %s in %s.%s%s:%d\n",dominators->lvname, dominators->sourcename, dominators->methodname->classname->classname, dominators->methodname->methodname, dominators->methodname->signature,dominators->linenumber);
    } else {
      fprintf(heap->rolefile,"  By global variable: %s.%s\n",dominators->globalname->classname->classname, dominators->globalname->fieldname);
    }
    dominators=dominators->next;
  }

  fprintf(heap->rolefile," Pointed to by:\n");
  {
    struct rolefieldlist *fl=r->pointedtofl;
    struct rolearraylist *al=r->pointedtoal;
    while(fl!=NULL) {
      if (fl->duplicates<DUPTHRESHOLD)
	fprintf(heap->rolefile,"  Field %s from class %s %d times.\n",fl->field->fieldname, fl->field->classname->classname, fl->duplicates+1);
      else 
	fprintf(heap->rolefile,"  Field %s from class %s multiple times.\n",fl->field->fieldname, fl->field->classname->classname);
      fl=fl->next;
    }
    while(al!=NULL) {
      if (al->duplicates<DUPTHRESHOLD)
	fprintf(heap->rolefile,"  Array of type class %s %d times.\n", al->class->classname, al->duplicates+1);
      else
	fprintf(heap->rolefile,"  Array of type class %s multiple times.\n", al->class->classname);
      al=al->next;
    }
  }

  fprintf(heap->rolefile," Has the following identity relations:\n");
  {
    struct identity_relation *ir=r->identities;
    print_identities(heap,ir);
  }


  fprintf(heap->rolefile," Non-null fields:\n");
  {
    struct rolefieldlist *fl=r->nonnullfields;
    while(fl!=NULL) {
      if (fl->role==0)
	fprintf(heap->rolefile,"  Field %s is non-null.\n",fl->field->fieldname);
      else
	fprintf(heap->rolefile,"  Field %s points to role R%d\n",fl->field->fieldname,fl->role);
      fl=fl->next;
    }
  }

  fprintf(heap->rolefile," Non-null elements:\n");
  {
    struct rolearraylist *al=r->nonnullarrays;
    while(al!=NULL) {
      if (al->role==0) {
	if (al->duplicates<ARRDUPTHRESHOLD)
	  fprintf(heap->rolefile,"  Element %s is non-null with multiplicity %d.\n",al->class->classname, al->duplicates);
	else
	  fprintf(heap->rolefile,"  Element %s is non-null at least %d times.\n",al->class->classname, al->duplicates);
      } else {
	if (al->duplicates<ARRDUPTHRESHOLD)
	  fprintf(heap->rolefile,"  Element %s points to role R%d with multiplicity %d.\n",al->class->classname,al->role,al->duplicates);
	else
	  fprintf(heap->rolefile,"  Element %s points to role R%d at least %d times.\n",al->class->classname,al->role,al->duplicates);
      }
      al=al->next;
    }
  }

  fprintf(heap->rolefile," Methods invoked on:\n");
  {
    if (r->methodscalled!=NULL) {
      int i=0;
      for(i=0;i<hshash->statechangesize;i++) {
	if(r->methodscalled[i]) {
	  char *methodname=(char *)gettable(hshash->statechangereversetable, i);
	  fprintf(heap->rolefile,"   %s\n",methodname);
	}
      }
    }
  }
  fprintf(heap->rolefile,"}\n\n");
}


void setheapstate(struct heap_state *hs) {
  hshash=hs;
}

int rchashcode(struct rolechange *rc) {
  int hashcode=hashstring(rc->origrole);
  hashcode^=hashstring(rc->newrole);
  hashcode^=((int)rc->uid);
  return hashcode;
}

int equivalentrc(struct rolechange *rc1, struct rolechange *rc2) {
  if ((rc1->uid==rc2->uid) &&
      equivalentstrings(rc1->origrole, rc2->origrole) &&
      equivalentstrings(rc1->newrole, rc2->newrole))
    return 1;
  else
    return 0;
}

void printrolechange(struct heap_state * hs, struct rolechange *rc) {
  printf("Role Change: %s -> %s\n",rc->origrole, rc->newrole);
}

int equivalentstrings(char *str1, char *str2) {
  if ((str1!=NULL)&&(str2!=NULL)) {
    if (strcmp(str1,str2)!=0)
      return 0;
    else
      return 1;
  } else if ((str1==NULL)&&(str2==NULL))
    return 1;
  else return 0;
}

void rolechange(struct heap_state *hs, struct genhashtable * dommapping, struct heap_object *ho, char *newrole, int enterexit) {
  struct rolechange *rc=ho->rc;
  if (rc==NULL) {
    if (enterexit<2) {
      rc=(struct rolechange *) calloc(1,sizeof(struct rolechange));
      rc->origrole=copystr(newrole);
      ho->rc=rc;
    }
    return;
  }
  if(equivalentstrings(rc->origrole, newrole)) {
    if (enterexit>=2) {
      free(ho->rc->origrole);
      free(ho->rc);
    }
    return;
  }
  /* Have Real Role Change...*/

  rc->newrole=copystr(newrole);
  rc->uid=ho->uid;
  if (enterexit&1) {
    /* Exit case*/
    if (!gencontains(hs->methodlist->rolechangetable,rc))
      genputtable(hs->methodlist->rolechangetable,rc,NULL);
    else {
      free(rc->newrole);
      free(rc->origrole);
      free(rc);
    }
  } else {
    /* Enter case*/
    if (!gencontains(hs->methodlist->caller->rolechangetable,rc))
      genputtable(hs->methodlist->caller->rolechangetable,rc,NULL);
    else {
      free(rc->newrole);
      free(rc->origrole);
      free(rc);
    }
  }
  /* Build new rolechange object*/
  if (enterexit<2) {
    rc=(struct rolechange *) calloc(1,sizeof(struct rolechange));
    rc->origrole=copystr(newrole);
    ho->rc=rc;
  }

  {
    /* Need to backwards propagation of rolechange */
    struct fieldlist * rfl=ho->reversefield;
    while(rfl!=NULL) {
      if (rfl->propagaterole) {
	/* Need to propagate role change upwards*/
	if (dommapping!=NULL)
	  findrolestring(hs, dommapping, rfl->object ,enterexit);
	/* Gross hack, but works...*/
	/* If dommapping==NULL, then we are garbage and hence anything that points to us is also*/
	/* The garbage collector will catch it then*/
      }
      rfl=rfl->dstnext;
    }
  }
  return;
}

int equivalentroles(struct role *role1, struct role *role2) {
  if (role1->hashcode!=role2->hashcode)
    return 0;
  if (role1->class!=role2->class)
    return 0;

  if(role1->methodscalled!=NULL&&role2->methodscalled!=NULL) {
    int i;
    for(i=0;i<hshash->statechangesize;i++)
      if (role1->methodscalled[i]!=role2->methodscalled[i])
	return 0;
  } else if (role1->methodscalled!=NULL||role2->methodscalled!=NULL)
    return 0;

  {
    struct rolereferencelist * dr1=role1->dominatingroots;
    struct rolereferencelist * dr2=role2->dominatingroots;

    while(dr1!=NULL) {
      if (dr2==NULL)
	return 0;
      if (dr1->globalname!=dr2->globalname)
	return 0;
      if (dr1->methodname!=dr2->methodname)
	return 0;
      if (!equivalentstrings(dr1->lvname, dr2->lvname))
	return 0;
      if (!equivalentstrings(dr1->sourcename, dr2->sourcename))
	return 0;
      dr1=dr1->next;
      dr2=dr2->next;
    }
    if (dr2!=NULL)
      return 0;
  }

  {
    struct rolefieldlist * rfl1=role1->pointedtofl;
    struct rolefieldlist * rfl2=role2->pointedtofl;
    while(rfl1!=NULL) {
      if (rfl2==NULL)
	return 0;
      if (rfl1->field!=rfl2->field)
	return 0;
      if (rfl1->duplicates!=rfl2->duplicates)
	return 0;
      rfl1=rfl1->next;
      rfl2=rfl2->next;
    }
    if (rfl2!=NULL)
      return 0;
  }

  {
    struct rolearraylist * ral1=role1->pointedtoal;
    struct rolearraylist * ral2=role2->pointedtoal;

    while(ral1!=NULL) {
      if (ral2==NULL)
	return 0;
      if (ral1->class!=ral2->class)
	return 0;
      if (ral1->duplicates!=ral2->duplicates)
	return 0;
      ral1=ral1->next;
      ral2=ral2->next;
    }
    if (ral2!=NULL)
      return 0;
  }


  {
    struct identity_relation *ri1=role1->identities;
    struct identity_relation *ri2=role2->identities;
    while(ri1!=NULL) {
      if (ri2==NULL)
	return 0;
      if (ri1->fieldname1!=ri2->fieldname1)
	return 0;
      if (ri1->fieldname2!=ri2->fieldname2)
	return 0;
      ri1=ri1->next;
      ri2=ri2->next;
    }
    if (ri2!=NULL)
      return 0;
  }


  {
    struct rolefieldlist * rfl1=role1->nonnullfields;
    struct rolefieldlist * rfl2=role2->nonnullfields;
    while(rfl1!=NULL) {
      if (rfl2==NULL)
	return 0;
      if (rfl1->field!=rfl2->field)
	return 0;
      if (rfl1->role!=rfl2->role)
	return 0;

      rfl1=rfl1->next;
      rfl2=rfl2->next;
    }
    if (rfl2!=NULL)
      return 0;
  }

  {
    struct rolearraylist * ral1=role1->nonnullarrays;
    struct rolearraylist * ral2=role2->nonnullarrays;
    while(ral1!=NULL) {
      if (ral2==NULL)
	return 0;
      if (ral1->class!=ral2->class)
	return 0;
      if (ral1->role!=ral2->role)
	return 0;
      if (ral1->duplicates!=ral2->duplicates)
	return 0;

      ral1=ral1->next;
      ral2=ral2->next;
    }
    if (ral2!=NULL)
      return 0;
  }

  return 1; /*Matched*/
}

void assignhashcode(struct role * role) {
  int hashcode=hashptr(role->class);

  struct rolereferencelist * dr=role->dominatingroots;
  struct rolefieldlist * rfl=role->pointedtofl;
  struct rolearraylist * ral=role->pointedtoal;
  struct identity_relation *ri=role->identities;
  struct rolefieldlist * rfl2=role->nonnullfields;
  struct rolearraylist * ral2=role->nonnullarrays;
  
  
  if(role->methodscalled!=NULL) {
    int i;
    for(i=0;i<hshash->statechangesize;i++)
      hashcode^=role->methodscalled[i];
  }

  while(dr!=NULL) {
    hashcode^=hashptr(dr->globalname);
    hashcode^=hashptr(dr->methodname);

    hashcode^=hashstring(dr->lvname);
    hashcode^=hashstring(dr->sourcename);
    dr=dr->next;
  }

  while(rfl!=NULL) {
    hashcode^=hashptr(rfl->field);
    rfl=rfl->next;
  }

  while(rfl2!=NULL) {
    hashcode^=hashptr(rfl2->field);
    hashcode^=rfl2->role;
    rfl2=rfl2->next;
  }

  while(ral2!=NULL) {
    hashcode^=hashptr(ral2->class);
    hashcode^=ral2->role;
    hashcode^=ral2->duplicates;
    ral2=ral2->next;
  }

  while(ral!=NULL) {
    hashcode^=hashptr(ral->class);
    ral=ral->next;
  }
  
  while(ri!=NULL) {
    hashcode^=hashptr(ri->fieldname1);
    hashcode^=hashptr(ri->fieldname2);
    ri=ri->next;
  }

  role->hashcode=hashcode;
}

int rolehashcode(struct role * r) {
  return r->hashcode;
}

int hashstring(char *strptr) {
  int hashcode=0;
  int *intptr=(int *) strptr;
  if(intptr==NULL)
    return 0;
  while(1) {
    int copy1=*intptr;
    if((copy1&0xFF000000)&&
       (copy1&0xFF0000)&&
       (copy1&0xFF00)&&
       (copy1&0xFF)) {
      hashcode^=*intptr;
      intptr++;
    } else {
      if (!copy1&0xFF000000)
	hashcode^=copy1&0xFF000000;
      else if (!copy1&0xFF0000)
	hashcode^=copy1&0xFF0000;
      else if (!copy1&0xFF00)
	hashcode^=copy1&0xFF00;
      else if (!copy1&0xFF)
	hashcode^=copy1&0xFF;
      return hashcode;
    }
  }
}

void freerole(struct role * role) {
  struct rolereferencelist * dr=role->dominatingroots;
  struct rolefieldlist * rfl=role->pointedtofl;
  struct rolearraylist * ral=role->pointedtoal;
  struct identity_relation *ri=role->identities;
  struct rolefieldlist * rfl2=role->nonnullfields;
  struct rolearraylist * ral2=role->nonnullarrays;
  free(role->methodscalled);
  free(role);

  while(dr!=NULL) {
    struct rolereferencelist *tmp=dr->next;
    free(dr->lvname);
    free(dr->sourcename);
    free(dr);
    dr=tmp;
  }

  while(rfl!=NULL) {
    struct rolefieldlist *tmp=rfl->next;
    free(rfl);
    rfl=tmp;
  }

  while(rfl2!=NULL) {
    struct rolefieldlist *tmp=rfl2->next;
    free(rfl2);
    rfl2=tmp;
  }

  while(ral!=NULL) {
    struct rolearraylist *tmp=ral->next;
    free(ral);
    ral=tmp;
  }

  while(ral2!=NULL) {
    struct rolearraylist *tmp=ral2->next;
    free(ral2);
    ral2=tmp;
  }
  free_identities(ri);
}

int currentrolenumber() {
  return rolenumber;
}

int parserolestring(char * input) {
  int value=0;
  sscanf(input,"R%d",&value);
  return value;
}

char * findrolestring(struct heap_state * heap, struct genhashtable * dommapping,struct heap_object *ho, int enterexit) {
  struct role * r=calculaterole(heap,dommapping, ho, enterexit);
  if (gencontains(heap->roletable,r)) {
    /* Already seen role */
    char *str=copystr((char *)gengettable(heap->roletable,r));
    freerole(r);
    rolechange(heap,dommapping,ho, str,enterexit);
    return str;
  } else {
    /* Synthesize string */
    char buf[30];
    int rn=rolenumber;
    int index=28;
    rolenumber++;
    buf[29]=0;
    if(rn==0) {
      buf[index--]='0';
    } else while(rn!=0) {
      buf[index--]='0'+rn%10;
      rn=rn/10;
    }

    buf[index]='R';
    {
      char *rolename=copystr(&buf[index]);
      genputtable(heap->roletable, r, rolename);
      genputtable(heap->reverseroletable, rolename, r);
    }
    rolechange(heap,dommapping,ho, &buf[index],enterexit);
    return copystr(&buf[index]);
  }
}

struct role * calculaterole(struct heap_state *heap, struct genhashtable * dommapping,struct heap_object *ho, int enterexit) {
  struct role * objrole=(struct role *)calloc(1, sizeof(struct role));
  struct referencelist *dominators=calculatedominators(dommapping, ho);

  objrole->class=ho->class;

  if(ho->methodscalled!=NULL) {
    int i=0;
    int * methodscalled=(int *)calloc(heap->statechangesize, sizeof(int));
    objrole->methodscalled=methodscalled;
    for(i=0;i<heap->statechangesize;i++) {
      methodscalled[i]=ho->methodscalled[i];
    }
  }
  

  while(dominators!=NULL) {
    struct referencelist *tmp=dominators->next;
    struct rolereferencelist *domroots=(struct rolereferencelist *) calloc(1,sizeof(struct rolereferencelist));
    
    if (dominators->lv!=NULL) {
      domroots->methodname=dominators->lv->m->methodname;
      domroots->lvname=copystr(dominators->lv->name);
      domroots->sourcename=copystr(dominators->lv->sourcename);
      domroots->linenumber=dominators->lv->linenumber;
    } else {
      domroots->globalname=dominators->gl->fieldname;
    }
    free(dominators);
    dominators=tmp;
    insertdomroot(objrole, domroots);
  }

  {
    struct fieldlist *fl=ho->reversefield;
    struct arraylist *al=ho->reversearray;
    while(fl!=NULL) {
      if (!(heap->options&OPTION_LIMITFIELDS)||fieldcontained(heap,fl->fieldname)) {
	struct rolefieldlist *rfl=(struct rolefieldlist *) calloc(1,sizeof(struct rolefieldlist));
	rfl->field=fl->fieldname;
	insertrfl(objrole,rfl);
      }
      fl=fl->dstnext;
    }
    while(al!=NULL) {
      struct rolearraylist *ral=(struct rolearraylist *) calloc(1,sizeof(struct rolearraylist));
      ral->class=al->src->class;
      ral->duplicates=1;
      insertral(objrole,ral);
      al=al->dstnext;
    }
  }

  {
    struct identity_relation *ir=find_identities(heap,ho);
    objrole->identities=ir;
    sortidentities(objrole);
  }

  {
    struct fieldlist *fl=ho->fl;
    struct arraylist *al=ho->al;

    while(fl!=NULL) {
      if (!(heap->options&OPTION_LIMITFIELDS)||fieldcontained(heap,fl->fieldname)) {
	struct rolefieldlist *rfl=(struct rolefieldlist *) calloc(1,sizeof(struct rolefieldlist));
	rfl->field=fl->fieldname;
	if (fl->propagaterole) {
	  char *tmp=findrolestring(heap, dommapping, fl->object, enterexit);
	  rfl->role=parserolestring(tmp);
	  free(tmp);
	}
	insertnonfl(objrole,rfl);
      }
      fl=fl->next;
    }

    while(al!=NULL) {
      if (!(heap->options&OPTION_LIMITARRAYS)) {
	struct rolearraylist *ral=(struct rolearraylist *) calloc(1,sizeof(struct rolearraylist));
	ral->class=al->object->class;
	if (al->propagaterole) {
	  char *tmp=findrolestring(heap, dommapping, al->object, enterexit);
	  ral->role=parserolestring(tmp);
	  free(tmp);
	}
	insertnonal(objrole,ral);
      }
      al=al->next;
    }
  }
  assignhashcode(objrole);
  return objrole;
}

int comparedomroots(struct rolereferencelist *r1, struct rolereferencelist *r2) {
  int t;
  if (r1->globalname!=NULL) {
    if (r2->globalname!=NULL) {
      if (r1->globalname==r2->globalname)
	return 0;
      if (r1->globalname>r2->globalname)
	return 1;
      else
	return -1;
    } else return -1;
  } else if (r2->globalname!=NULL) return 1;
  else if (r1->methodname!=r2->methodname) {
    if (r1->methodname>r2->methodname)
      return 1;
    else
      return -1;
  }
  else if ((t=strcmp(r1->lvname,r2->lvname))!=0)
    return t;
  else if ((t=strcmp(r1->sourcename,r2->sourcename))!=0)
    return t;
  else return 0;
}

void insertdomroot(struct role * role, struct rolereferencelist * domroots) {
  struct rolereferencelist * rrl=role->dominatingroots;
  if (role->dominatingroots==NULL) {
    role->dominatingroots=domroots;
    return;
  }
  if (comparedomroots(domroots,role->dominatingroots)<0) {
    domroots->next=role->dominatingroots;
    role->dominatingroots=domroots;
    return;
  }
  
  while(rrl->next!=NULL) {
    if (comparedomroots(domroots,rrl->next)>=0)
      break;
    rrl=rrl->next;
  }
  domroots->next=rrl->next;
  rrl->next=domroots;
  return;
}

void sortidentities(struct role *role) {
  struct identity_relation *irptr=role->identities;
  role->identities=NULL;
  while(irptr!=NULL) {
    struct identity_relation *irnxt=irptr->next;
    struct identity_relation *tmpptr=role->identities;
    irptr->next=NULL;

    if(role->identities==NULL) {
      role->identities=irptr;
      irptr=irnxt;
    } else if (compareidentity(irptr, role->identities)<0) {
      irptr->next=role->identities;
      role->identities=irptr;
      irptr=irnxt;
    } else {
      while(tmpptr->next!=NULL) {
	if (compareidentity(irptr, tmpptr->next)<0)
	  break;
	tmpptr=tmpptr->next;
      }
      irptr->next=tmpptr->next;
      tmpptr->next=irptr;
      irptr=irnxt;
    }
  }
}

int compareidentity(struct identity_relation *ir1, struct identity_relation *ir2) {
  if (ir1->fieldname1!=ir2->fieldname1) {
    if (ir1->fieldname1<ir2->fieldname1)
      return 1;
    else
      return -1;
  }
  if (ir1->fieldname2==ir2->fieldname2)
    return 0;
  if (ir1->fieldname2<ir2->fieldname2)
    return 1;
  return -1;
}

void insertnonfl(struct role * role, struct rolefieldlist * rfl) {
  struct rolefieldlist *tmpptr=role->nonnullfields;

  if (role->nonnullfields==NULL) {
    role->nonnullfields=rfl;
    return;
  }
  if (fieldcompare(rfl, role->nonnullfields)<0) {
    rfl->next=role->nonnullfields;
    role->nonnullfields=rfl;
    return;
  }
  while(tmpptr->next!=NULL) {
    int fc=fieldcompare(rfl, tmpptr->next);
    if (fc>=0)
      break;
    tmpptr=tmpptr->next;
  }

  rfl->next=tmpptr->next;
  tmpptr->next=rfl;
  return;
}

void insertnonal(struct role * role, struct rolearraylist * ral) {
  struct rolearraylist *tmpptr=role->nonnullarrays;
  int ac;

  if (role->nonnullarrays==NULL) {
    role->nonnullarrays=ral;
    return;
  }
  if ((ac=arraycompare(ral, role->nonnullarrays))<=0) {
    if (ac<0) {
      ral->next=role->nonnullarrays;
      role->nonnullarrays=ral;
      return;
    } else {
      /* increment counter */
      /*ac=0, match!!!*/
      if (role->nonnullarrays->duplicates<ARRDUPTHRESHOLD)
	role->nonnullarrays->duplicates++;
      free(ral);
      return;
    }
  }

  while(tmpptr->next!=NULL) {
    ac=arraycompare(ral, tmpptr->next);
    if (ac>=0)
      break;
    tmpptr=tmpptr->next;
  }
  
  if (tmpptr->next==NULL||ac>0) {
    ral->next=tmpptr->next;
    tmpptr->next=ral;
    return;
  } else {
    /* increment counter */
    if (tmpptr->next->duplicates<ARRDUPTHRESHOLD)
      tmpptr->next->duplicates++;
    free(ral);
    return;
  }
}

void insertrfl(struct role * role, struct rolefieldlist * rfl) {
  struct rolefieldlist *tmpptr=role->pointedtofl;
  int fc;

  if (role->pointedtofl==NULL) {
    role->pointedtofl=rfl;
    return;
  }
  fc=fieldcompare(rfl, role->pointedtofl);
  if (fc<0) {
    rfl->next=role->pointedtofl;
    role->pointedtofl=rfl;
    return;
  }
  if (fc==0) {
    if (role->pointedtofl->duplicates<DUPTHRESHOLD)
      role->pointedtofl->duplicates++;
    free(rfl);
    return;
  }
  while(tmpptr->next!=NULL) {
    int fc=fieldcompare(rfl,tmpptr->next);
    if (fc>0)
      break;
    if (fc==0) {
      if (tmpptr->next->duplicates<DUPTHRESHOLD)
	tmpptr->next->duplicates++;
      free(rfl);
      return;
    }
    tmpptr=tmpptr->next;
  }
  rfl->next=tmpptr->next;
  tmpptr->next=rfl;
  return;
}

int fieldcompare(struct rolefieldlist *field1, struct rolefieldlist *field2) {
  if (field1->field==field2->field)
    return 0;
  if (field1->field<field2->field)
    return 1;
  return -1;
}

int arraycompare(struct rolearraylist *array1, struct rolearraylist *array2) {
  if (array1->class==array2->class) {
    if (array1->role==array2->role)
      return 0;
    if (array1->role<array2->role)
      return 1;
    return -1;
  }
  if (array1->class<array2->class)
    return 1;
  return -1;
}

void insertral(struct role * role, struct rolearraylist * ral) {
  struct rolearraylist *tmpptr=role->pointedtoal;
  int rcmp;

  if (role->pointedtoal==NULL) {
    role->pointedtoal=ral;
    return;
  }
  rcmp=(ral->class< role->pointedtoal->class);
  if (rcmp<0) {
    ral->next=role->pointedtoal;
    role->pointedtoal=ral;
    return;
  }
  if (rcmp==0) {
    if (role->pointedtoal->duplicates<DUPTHRESHOLD)
      role->pointedtoal->duplicates++;
    free(ral);
    return;
  }
  while(tmpptr->next!=NULL) {
    if (ral->class>tmpptr->next->class)
      break;
    if (ral->class==tmpptr->next->class) {
      if (tmpptr->next->duplicates<DUPTHRESHOLD)
	tmpptr->next->duplicates++;
      free(ral);
      return;
    }
    tmpptr=tmpptr->next;
  }
  ral->next=tmpptr->next;
  tmpptr->next=ral;
  return;
}

struct identity_relation * find_identities(struct heap_state *heap, struct heap_object *ho) {
  struct fieldlist *fl1=ho->fl, *fl2=NULL;
  struct identity_relation * irptr=NULL;
  
  while(fl1!=NULL) {
    if (!(heap->options&OPTION_LIMITFIELDS)||fieldcontained(heap,fl1->fieldname)) {
      fl2=fl1->object->fl;
      while(fl2!=NULL) {
	if ((!(heap->options&OPTION_LIMITFIELDS)||fieldcontained(heap,fl2->fieldname))&&fl2->object==ho) {
	  struct identity_relation *newidentity=(struct identity_relation *) calloc(1,sizeof(struct identity_relation));
	  newidentity->fieldname1=fl1->fieldname;
	  newidentity->fieldname2=fl2->fieldname;	
	  newidentity->next=irptr;
	  irptr=newidentity;
	}
	fl2=fl2->next;
      }
    }
    fl1=fl1->next;
  }
  return irptr;
}

void free_identities(struct identity_relation *irptr) {
  while(irptr!=NULL) {
    struct identity_relation * tmpptr=irptr->next;
    free(irptr);
    irptr=tmpptr;
  }
}

void print_identities(struct heap_state *heap,struct identity_relation *irptr) {
  while(irptr!=NULL) {
    fprintf(heap->rolefile,"  Relation: %s.%s\n",irptr->fieldname1->fieldname,irptr->fieldname2->fieldname);
    irptr=irptr->next;
  }
}
