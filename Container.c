#include "Container.h"
#include <stdio.h>

void recordcontainer(struct heap_state *hs, struct heap_object *ho) {
  fprintf(hs->container,"%lld\n",ho->uid);
}

void opencontainerfile(struct heap_state *hs) {
  hs->container=fopen("container","w");
}

void closecontainerfile(struct heap_state *hs) {
  fclose(hs->container);
  hs->container=NULL;
}

void examineheap(struct heap_state *hs, struct hashtable *ht) {
  struct iterator *it=getiterator(ht);
  while(1) {
    long long uid=next(it);
    struct heap_object *ho;
    if (uid==0)
      break;
    ho=(struct heap_object *) gettable(ht, uid);
    if ((ho->reachable&FIRSTREF)&&
	!(ho->reachable&NOTCONTAINER))
      recordcontainer(hs,ho);
  }
}

void openreadcontainer(struct heap_state *hs) {
  FILE *container=fopen("container","r");
  long long uid;
  hs->containedobjects=allocatehashtable();
  while(fscanf(container, "%lld\n", &uid)>0) {
    puttable(hs->containedobjects, uid, NULL);
  }
  fclose(container);
}

