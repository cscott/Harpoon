#include "stack.h"
#include <stdio.h>
#include <stdlib.h>


void pushstack(struct StackElement **septr,void * obj) {
  struct StackElement * nse=(struct StackElement *)malloc(sizeof(struct StackElement));
  nse->contents=obj;
  nse->next=*septr;
  *septr=nse;
}

void * popstack(struct StackElement **septr) {
  if(*septr==NULL) {
    printf("Empty Stack\n");
    return NULL;/* Empty stack */
  }
  {
    void *obj=(*septr)->contents;
    struct StackElement *ose=*septr;
    (*septr)=ose->next;
    free(ose);
    return obj;
  }
}
