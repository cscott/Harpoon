#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include "GenericHashtable.h"
//#include <dmalloc.h>

int genputtable(struct genhashtable *ht, void * key, void * object) {
  int bin=genhashfunction(key);
  struct genpointerlist * newptrlist=(struct genpointerlist *) calloc(1,sizeof(struct genpointerlist));
  newptrlist->src=key;
  newptrlist->object=object;
  newptrlist->next=ht->bins[bin];
  ht->bins[bin]=newptrlist;
  return 1;
}

void * gengettable(struct genhashtable *ht, void * key) {
  struct genpointerlist * ptr=ht->bins[genhashfunction(key)];
  while(ptr!=NULL) {
    if (ptr->src==key)
      return ptr->object;
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %p\n",key);
  return NULL;
}

void genfreekey(struct genhashtable *ht, void * key) {
  struct genpointerlist * ptr=ht->bins[genhashfunction(key)];

  if (ptr->src==key) {
    ht->bins[genhashfunction(key)]=ptr->next;
    free(ptr);
    return;
  }
  while(ptr->next!=NULL) {
    if (ptr->next->src==key) {
      struct genpointerlist *tmpptr=ptr->next;
      ptr->next=tmpptr->next;
      free(tmpptr);
      return;
    }
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %p\n",key);
}

int genhashfunction(void * key) {
  return ((long int)key) % gennumbins;
}

struct genhashtable * genallocatehashtable() {
  return (struct genhashtable *) calloc(1,sizeof(struct genhashtable));
}

void genfreehashtable(struct genhashtable * ht) {
  int i;
  for (i=0;i<gennumbins;i++) {
    if (ht->bins[i]!=NULL) {
      struct genpointerlist *genptr=ht->bins[i];
      while(genptr!=NULL) {
	struct genpointerlist *tmpptr=genptr->next;
	free(genptr);
	genptr=tmpptr;
      }
    }
  }
  free(ht);
}

void genfreekeyhashtable(struct genhashtable * ht) {
  int i;
  for (i=0;i<gennumbins;i++) {
    if (ht->bins[i]!=NULL) {
      struct genpointerlist *genptr=ht->bins[i];
      while(genptr!=NULL) {
	struct genpointerlist *tmpptr=genptr->next;
	free(genptr->object);
	free(genptr);
	genptr=tmpptr;
      }
    }
  }
  free(ht);
}
