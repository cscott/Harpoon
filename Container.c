#include "Container.h"
#include <stdio.h>
#include <string.h>
#include <stdlib.h>

void recordcontainer(struct heap_state *hs, struct heap_object *ho) {
  fprintf(hs->container,"%lld\n",ho->uid);
}

void readpolicyfile(struct heap_state *heap, char *filename) {
  FILE *policyfile=fopen(filename,"r");
  /* Policies for class:
     Never
     Always
     Only if one reference ever
     Only if one reference at a time */
  /* first read in default policy */
  char buffer[100];
  if (fscanf(policyfile,"default: %s",buffer)>0) {
    if (strcmp(buffer,"onceever")==0)
      ;
    else if (strcmp(buffer,"oneatatime")==0)
      heap->options|=OPTION_DEFAULTONEATTIME;
    else printf("ERROR: default policy not specified correctly!\n");
  } else {
    printf("ERROR: default policy not specified correctly.\n");
  }
  heap->policytable=genallocatehashtable(NULL,NULL);
  while(1) {
    char class[400];
    char policy[50];
    int fs=fscanf(policyfile, "%s %s",class, policy);
    struct classname *cn=NULL;
    struct policyobject *po=NULL;
    if (fs==EOF)
      break;
    if (fs==0) {
      printf("ERROR in policyfile!\n");
      break;
    }
    cn=getclass(heap->namer, class);
    po=(struct policyobject *)calloc(1,sizeof(struct policyobject));
    if (strcmp(policy,"never")==0) {
      po->policy=0;
    } else if (strcmp(policy,"onceever")==0) {
      po->policy=1;
    } else if (strcmp(policy,"oneatatime")==0) {
      po->policy=2;
    } else if (strcmp(policy,"always")==0) {
      po->policy=3;
    } else printf("Error in policy for class %s.\n",class);
    genputtable(heap->policytable, cn, po);
  }
  
  fclose(policyfile);
}

void opencontainerfile(struct heap_state *hs) {
  hs->container=fopen("container","w");
}

void closecontainerfile(struct heap_state *hs) {
  fclose(hs->container);
  hs->container=NULL;
}

void examineheap(struct heap_state *heap, struct hashtable *ht) {
  struct iterator *it=getiterator(ht);
  while(1) {
    long long uid=next(it);
    struct heap_object *ho;
    if (uid==0)
      break;
    ho=(struct heap_object *) gettable(ht, uid);
    {
    int policy=1;
    if(gencontains(heap->policytable, ho->class)) {
	struct policyobject *po=(struct policyobject *) gengettable(heap->policytable, ho->class);
	policy=po->policy;
      } else if (heap->options&OPTION_DEFAULTONEATTIME)
        policy=2;
      switch(policy) {
      case 0:
	break;
      case 1:
      case 2:
	if ((ho->reachable&FIRSTREF)&&
	    !(ho->reachable&NOTCONTAINER))
	  recordcontainer(heap,ho);
	break;
      case 3:
	recordcontainer(heap,ho);
	break;
      }
    }
  }
}

void openreadcontainer(struct heap_state *heap) {
  FILE *container=fopen("container","r");
  long long uid;
  heap->containedobjects=allocatehashtable();
  while(fscanf(container, "%lld\n", &uid)>0) {
    puttable(heap->containedobjects, uid, NULL);
  }
  fclose(container);
}

