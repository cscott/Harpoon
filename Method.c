#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include "Method.h"
#include "Role.h"
#include "Effects.h"
/*#include <dmalloc.h>*/

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
  free(method->classname);
  free(method->methodname);
  free(method->signature);
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
  free(rrs->returnrole);
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
  int hashcode=hashstring(method->classname);
  hashcode^=hashstring(method->methodname);
  hashcode^=hashstring(method->signature);
  for(i=0;i<method->numobjectargs;i++)
    hashcode^=hashstring(method->paramroles[i]);
  method->hashcode=hashcode;
}

int comparerolemethods(struct rolemethod * m1, struct rolemethod *m2) {
  int i;

  if (m1->hashcode!=m2->hashcode)
    return 0;
  if (!equivalentstrings(m1->classname, m2->classname))
    return 0;
  if (!equivalentstrings(m1->methodname, m2->methodname))
    return 0;
  if (!equivalentstrings(m1->signature, m2->signature))
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
  printf(" %s.%s%s\n",method->classname,method->methodname,method->signature);
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
  printf("}\n\n");
}

