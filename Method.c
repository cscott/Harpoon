#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include "Method.h"
#include "Role.h"

int methodhashcode(struct rolemethod * method) {
  return method->hashcode;
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

