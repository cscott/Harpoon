#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include "Role.h"
/*#include <dmalloc.h>*/

static long int rolenumber=0;

void printrole(struct role *r, char * rolename) {
  struct rolereferencelist *dominators=r->dominatingroots;
  printf("Role %s {\n",rolename);
  printf(" Class: %s\n", r->class);
  printf(" Dominated by:\n");
  while(dominators!=NULL) {
    if (dominators->methodname!=NULL) {
      printf("  By local variable: (%s) %s in %s.%s%s:%d\n",dominators->lvname, dominators->sourcename, dominators->classname, dominators->methodname, dominators->signature,dominators->linenumber);
    } else {
      printf("  By global variable: %s.%s\n",dominators->classname, dominators->globalname);
    }
    dominators=dominators->next;
  }

  printf(" Pointed to by:\n");
  {
    struct rolefieldlist *fl=r->pointedtofl;
    struct rolearraylist *al=r->pointedtoal;
    while(fl!=NULL) {
      printf("  Field %s from class %s.\n",fl->field, fl->class);
      fl=fl->next;
    }
    while(al!=NULL) {
      printf("  Array of type class %s.\n", al->class);
      al=al->next;
    }
  }

  printf(" Has the following identity relations:\n");
  {
    struct identity_relation *ir=r->identities;
    print_identities(ir);
  }


  printf(" Non-null fields:\n");
  {
    struct rolefieldlist *fl=r->nonnullfields;
    while(fl!=NULL) {
      printf("  Field %s is non-null.\n",fl->field);
      fl=fl->next;
    }
  }
  printf("}\n\n");
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

int equivalentroles(struct role *role1, struct role *role2) {
  if (role1->hashcode!=role2->hashcode)
    return 0;
  if (strcmp(role1->class,role2->class)!=0)
    return 0;

  {
    struct rolereferencelist * dr1=role1->dominatingroots;
    struct rolereferencelist * dr2=role2->dominatingroots;

    while(dr1!=NULL) {
      if (dr2==NULL)
	return 0;
      if (!equivalentstrings(dr1->classname, dr2->classname))
	return 0;
      if (!equivalentstrings(dr1->globalname, dr2->globalname))
	return 0;
      if (!equivalentstrings(dr1->methodname, dr2->methodname))
	return 0;
      if (!equivalentstrings(dr1->signature, dr2->signature))
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
      if (!equivalentstrings(rfl1->class, rfl2->class))
	return 0;
      if (!equivalentstrings(rfl1->field, rfl2->field))
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
      if (!equivalentstrings(ral1->class, ral2->class))
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
      if (!equivalentstrings(ri1->fieldname1,ri2->fieldname1))
	return 0;
      if (!equivalentstrings(ri1->fieldname2,ri2->fieldname2))
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
      if (!equivalentstrings(rfl1->class, rfl2->class))
	return 0;
      if (!equivalentstrings(rfl1->field, rfl2->field))
	return 0;
      rfl1=rfl1->next;
      rfl2=rfl2->next;
    }
    if (rfl2!=NULL)
      return 0;
  }

  return 1; /*Matched*/
}

void assignhashcode(struct role * role) {
  int hashcode=hashstring(role->class);

  struct rolereferencelist * dr=role->dominatingroots;
  struct rolefieldlist * rfl=role->pointedtofl;
  struct rolearraylist * ral=role->pointedtoal;
  struct identity_relation *ri=role->identities;
  struct rolefieldlist * rfl2=role->nonnullfields;

  while(dr!=NULL) {
    hashcode^=hashstring(dr->classname);
    hashcode^=hashstring(dr->globalname);
    hashcode^=hashstring(dr->methodname);
    hashcode^=hashstring(dr->signature);
    hashcode^=hashstring(dr->lvname);
    hashcode^=hashstring(dr->sourcename);
    dr=dr->next;
  }

  while(rfl!=NULL) {
    hashcode^=hashstring(rfl->class);
    hashcode^=hashstring(rfl->field);
    rfl=rfl->next;
  }

  while(rfl2!=NULL) {
    hashcode^=hashstring(rfl2->class);
    hashcode^=hashstring(rfl2->field);
    rfl2=rfl2->next;
  }

  while(ral!=NULL) {
    hashcode^=hashstring(ral->class);
    ral=ral->next;
  }
  
  while(ri!=NULL) {
    hashcode^=hashstring(ri->fieldname1);
    hashcode^=hashstring(ri->fieldname2);
    ri=ri->next;
  }
  role->hashcode=hashcode;
}

int rolehashcode(struct role * r) {
  return r->hashcode;
}

int hashstring(char *strptr) {
  int hashcode=0;
  int bitstoshift=0;
  if(strptr==NULL)
    return 0;
  while(*strptr!=0) {
    hashcode^=((*strptr)<<bitstoshift);
    bitstoshift+=8;
    if (bitstoshift==32)
      bitstoshift=0;
    strptr++;
  }
  return hashcode;
}

void freerole(struct role * role) {
  struct rolereferencelist * dr=role->dominatingroots;
  struct rolefieldlist * rfl=role->pointedtofl;
  struct rolearraylist * ral=role->pointedtoal;
  struct identity_relation *ri=role->identities;
  struct rolefieldlist * rfl2=role->nonnullfields;
  free(role->class);
  free(role);

  while(dr!=NULL) {
    struct rolereferencelist *tmp=dr->next;
    free(dr->classname);
    free(dr->globalname);
    free(dr->methodname);
    free(dr->signature);
    free(dr->lvname);
    free(dr->sourcename);
    free(dr);
    dr=tmp;
  }

  while(rfl!=NULL) {
    struct rolefieldlist *tmp=rfl->next;
    free(rfl->class);
    free(rfl->field);
    free(rfl);
    rfl=tmp;
  }

  while(rfl2!=NULL) {
    struct rolefieldlist *tmp=rfl2->next;
    free(rfl2->class);
    free(rfl2->field);
    free(rfl2);
    rfl2=tmp;
  }

  while(ral!=NULL) {
    struct rolearraylist *tmp=ral->next;
    free(ral->class);
    free(ral);
    ral=tmp;
  }
  
  free_identities(ri);

}



char * findrolestring(struct heap_state * heap, struct genhashtable * dommapping,struct heap_object *ho) {
  struct role * r=calculaterole(dommapping, ho);
  if (gencontains(heap->roletable,r)) {
    /* Already seen role */
    char *str=copystr((char *)gengettable(heap->roletable,r));
    freerole(r);
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
    genputtable(heap->roletable, r, copystr(&buf[index]));

    return copystr(&buf[index]);
  }
}

struct role * calculaterole(struct genhashtable * dommapping,struct heap_object *ho) {
  struct role * objrole=(struct role *)calloc(1, sizeof(struct role));
  struct referencelist *dominators=calculatedominators(dommapping, ho);

  objrole->class=copystr(ho->class);
  

  while(dominators!=NULL) {
    struct referencelist *tmp=dominators->next;
    struct rolereferencelist *domroots=(struct rolereferencelist *) calloc(1,sizeof(struct rolereferencelist));
    
    if (dominators->lv!=NULL) {
      domroots->classname=copystr(dominators->lv->m->classname);
      domroots->methodname=copystr(dominators->lv->m->methodname);
      domroots->signature=copystr(dominators->lv->m->signature);
      domroots->lvname=copystr(dominators->lv->name);
      domroots->sourcename=copystr(dominators->lv->sourcename);
      domroots->linenumber=dominators->lv->linenumber;
    } else {
      domroots->classname=copystr(dominators->gl->classname);
      domroots->globalname=copystr(dominators->gl->fieldname);
    }
    free(dominators);
    dominators=tmp;
    insertdomroot(objrole, domroots);
  }

  {
    struct fieldlist *fl=ho->reversefield;
    struct arraylist *al=ho->reversearray;
    while(fl!=NULL) {
      struct rolefieldlist *rfl=(struct rolefieldlist *) calloc(1,sizeof(struct rolefieldlist));
      rfl->class=copystr(fl->src->class);
      rfl->field=copystr(fl->fieldname);
      insertrfl(objrole,rfl);
      fl=fl->dstnext;
    }
    while(al!=NULL) {
      struct rolearraylist *ral=(struct rolearraylist *) calloc(1,sizeof(struct rolearraylist));
      ral->class=copystr(al->src->class);
      insertral(objrole,ral);
      al=al->dstnext;
    }
  }

  {
    struct identity_relation *ir=find_identities(ho);
    objrole->identities=ir;
    sortidentities(objrole);
  }

  {
    struct fieldlist *fl=ho->fl;
    while(fl!=NULL) {
      struct rolefieldlist *rfl=(struct rolefieldlist *) calloc(1,sizeof(struct rolefieldlist));
      rfl->class=copystr(fl->src->class);
      rfl->field=copystr(fl->fieldname);
      insertnonfl(objrole,rfl);
      fl=fl->next;
    }
  }
  assignhashcode(objrole);
  return objrole;
}

int comparedomroots(struct rolereferencelist *r1, struct rolereferencelist *r2) {
  int t;
  if ((t=strcmp(r1->classname,r2->classname))!=0)
    return t;
  else if (r1->globalname!=NULL) {
    if (r2->globalname!=NULL) {
      return strcmp(r1->classname, r2->classname);
    } else return -1;
  } else if (r2->globalname!=NULL) return 1;
  else if ((t=strcmp(r1->methodname,r2->methodname))!=0)
    return t;
  else if ((t=strcmp(r1->signature,r2->signature))!=0)
    return t;
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
      }
      irptr->next=tmpptr->next;
      tmpptr->next=irptr;
      irptr=irnxt;
    }
  }
}

int compareidentity(struct identity_relation *ir1, struct identity_relation *ir2) {
  int tmp;
  if ((tmp=strcmp(ir1->fieldname1, ir2->fieldname1))!=0)
    return tmp;
  else
    printf("ERROR: MATCHING FIRST FIELD for compareidentity\n");
  return strcmp(ir1->fieldname2, ir2->fieldname2);
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
    if (fieldcompare(rfl,tmpptr->next)>=0)
      break;
    tmpptr=tmpptr->next;
  }
  rfl->next=tmpptr->next;
  tmpptr->next=rfl;
  return;
}

void insertrfl(struct role * role, struct rolefieldlist * rfl) {
  struct rolefieldlist *tmpptr=role->pointedtofl;

  if (role->pointedtofl==NULL) {
    role->pointedtofl=rfl;
    return;
  }
  if (fieldcompare(rfl, role->pointedtofl)<0) {
    rfl->next=role->pointedtofl;
    role->pointedtofl=rfl;
    return;
  }
  while(tmpptr->next!=NULL) {
    if (fieldcompare(rfl,tmpptr->next)>=0)
      break;
    tmpptr=tmpptr->next;
  }
  rfl->next=tmpptr->next;
  tmpptr->next=rfl;
  return;
}

int fieldcompare(struct rolefieldlist *field1, struct rolefieldlist *field2) {
  int tmp;
  if ((tmp=strcmp(field1->class,field2->class))!=0)
    return tmp;
  else
    return strcmp(field1->field, field2->field);
}

void insertral(struct role * role, struct rolearraylist * ral) {
  struct rolearraylist *tmpptr=role->pointedtoal;

  if (role->pointedtoal==NULL) {
    role->pointedtoal=ral;
    return;
  }
  if (strcmp(ral->class, role->pointedtoal->class)<0) {
    ral->next=role->pointedtoal;
    role->pointedtoal=ral;
    return;
  }
  while(tmpptr->next!=NULL) {
    if (strcmp(ral->class,tmpptr->next->class)>=0)
      break;
    tmpptr=tmpptr->next;
  }
  ral->next=tmpptr->next;
  tmpptr->next=ral;
  return;
}

struct identity_relation * find_identities(struct heap_object *ho) {
  struct fieldlist *fl1=ho->fl, *fl2=NULL;
  struct identity_relation * irptr=NULL;
  
  while(fl1!=NULL) {
    fl2=fl1->object->fl;
    while(fl2!=NULL) {
      if (fl2->object==ho) {
	struct identity_relation *newidentity=(struct identity_relation *) calloc(1,sizeof(struct identity_relation));
	newidentity->fieldname1=copystr(fl1->fieldname);
	newidentity->fieldname2=copystr(fl2->fieldname);	
	newidentity->next=irptr;
	irptr=newidentity;
      }
      fl2=fl2->next;
    }
    fl1=fl1->next;
  }
  return irptr;
}

void free_identities(struct identity_relation *irptr) {
  while(irptr!=NULL) {
    struct identity_relation * tmpptr=irptr->next;
    free(irptr->fieldname1);
    free(irptr->fieldname2);
    free(irptr);
    irptr=tmpptr;
  }
}

void print_identities(struct identity_relation *irptr) {
  while(irptr!=NULL) {
    printf("  Relation: %s.%s\n",irptr->fieldname1,irptr->fieldname2);
    irptr=irptr->next;
  }
}
