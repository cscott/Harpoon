#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <stdlib.h>
#include <string.h>
#include "Classes.h"
#include "Names.h"
#include "GenericHashtable.h"

#ifdef MDEBUG
#include <dmalloc.h>
#endif

void loadclass(struct heap_state *heap) {
  FILE *file=fopen("classes","r");
  char classname[600];
  
  heap->excludedclasstable=genallocatehashtable((int (*)(void *)) &hashclass, (int (*)(void *,void *)) &compareclass);

  if (file==NULL)
    printf("ERROR:  classes file does not exist\n");

  while(1) {
    int flag=fscanf(file, "%s\n", classname);
    struct classname *class;
    if (flag<=0)
      break;
    class=getclass(heap->namer,classname);
    genputtable(heap->excludedclasstable, class, NULL);
  }
}

int classcontained(struct heap_state *heap, struct classname *class) {
  return gencontains(heap->excludedclasstable, class);
}
