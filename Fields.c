#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include "Fields.h"
#include "Names.h"
#include "GenericHashtable.h"

#ifdef MDEBUG
#include <dmalloc.h>
#endif

void loadfields(struct heap_state *heap) {
  FILE *file=fopen("fields","r");
  char classname[600],fieldname[600], fielddesc[600];
  
  heap->includedfieldtable=genallocatehashtable((int (*)(void *)) &hashfield, (int (*)(void *,void *)) &comparefield);

  if (file==NULL)
    printf("ERROR:  fields file does not exist\n");

  while(1) {
    int flag=fscanf(file, "%s %s %s\n", classname, fieldname, fielddesc);
    struct fieldname *field;
    if (flag<=0)
      break;
    field=getfield(heap->namer,classname, fieldname, fielddesc);
    genputtable(heap->includedfieldtable, field, NULL);
  }
}

int fieldcontained(struct heap_state *heap, struct fieldname *field) {
  return gencontains(heap->includedfieldtable, field);
}
