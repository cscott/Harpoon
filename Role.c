#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include "Role.h"

void printrole(struct genhashtable * dommapping,struct heap_object *ho) {
  struct referencelist *dominators=calculatedominators(dommapping, ho);
  printf("Role {\n");
  printf(" Class: %s\n", ho->class);
  printf(" Dominated by:\n");
  while(dominators!=NULL) {
    struct referencelist *tmp=dominators->next;
    if (dominators->lv!=NULL) {
      printf("  By local variable: (%s) %s in %s %s %s %d\n",dominators->lv->name, dominators->lv->sourcename, dominators->lv->m->classname, dominators->lv->m->methodname, dominators->lv->m->signature, dominators->lv->lvnumber);
    } else {
      printf("  By global variable: %s.%s\n",dominators->gl->classname, dominators->gl->fieldname);
    }
    free(dominators);
    dominators=tmp;
  }

  printf(" Pointed to by:\n");
  {
    struct fieldlist *fl=ho->reversefield;
    struct arraylist *al=ho->reversearray;
    while(fl!=NULL) {
      printf("  Field %s from class %s.\n",fl->fieldname, fl->src->class);
      fl=fl->dstnext;
    }
    while(al!=NULL) {
      printf("  Array of type class %s.\n", al->src->class);
      al=al->dstnext;
    }
  }
  printf(" Has the following identity relations:\n");
  {
    struct identity_relation *ir=find_identities(ho);
    print_identities(ir);
    free_identities(ir);
  }


  printf(" Non-null fields:\n");
  {
    struct fieldlist *fl=ho->fl;
    while(fl!=NULL) {
      printf("  Field \"%s\" is non-null\n",fl->fieldname);
      fl=fl->next;
    }
  }
  printf("}\n");
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
