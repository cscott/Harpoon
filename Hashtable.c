#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include "Hashtable.h"
/*#include <dmalloc.h>*/

int puttable(struct hashtable *ht, long long key, void * object) {
  int bin=hashfunction(key);
  struct pointerlist * newptrlist=(struct pointerlist *) calloc(1,sizeof(struct pointerlist));
  newptrlist->uid=key;
  newptrlist->object=object;
  newptrlist->next=ht->bins[bin];
  ht->bins[bin]=newptrlist;
  return 1;
}

void * gettable(struct hashtable *ht, long long key) {
  struct pointerlist * ptr=ht->bins[hashfunction(key)];
  while(ptr!=NULL) {
    if (ptr->uid==key)
      return ptr->object;
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %lld\n",key);
  return NULL;
}

int contains(struct hashtable *ht, long long key) {
  struct pointerlist * ptr=ht->bins[hashfunction(key)];
  while(ptr!=NULL) {
    if (ptr->uid==key)
      return 1;
    ptr=ptr->next;
  }
  return 0;
}

void freekey(struct hashtable *ht, long long key) {
  struct pointerlist * ptr=ht->bins[hashfunction(key)];

  if (ptr->uid==key) {
    ht->bins[hashfunction(key)]=ptr->next;
    free(ptr);
    return;
  }
  while(ptr->next!=NULL) {
    if (ptr->next->uid==key) {
      struct pointerlist *tmpptr=ptr->next;
      ptr->next=tmpptr->next;
      free(tmpptr);
      return;
    }
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %lld\n",key);
}

int hashfunction(long long key) {
  return key % numbins;
}

struct hashtable * allocatehashtable() {
  return (struct hashtable *) calloc(1,sizeof(struct hashtable));
}

void freehashtable(struct hashtable * ht) {
  int i;
  for (i=0;i<numbins;i++) {
    if (ht->bins[i]!=NULL) {
      struct pointerlist *genptr=ht->bins[i];
      while(genptr!=NULL) {
	struct pointerlist *tmpptr=genptr->next;
	free(genptr);
	genptr=tmpptr;
      }
    }
  }
  free(ht);
}

void freedatahashtable(struct hashtable * ht, void (*freefunction)(void *)) {
  int i;
  for (i=0;i<numbins;i++) {
    if (ht->bins[i]!=NULL) {
      struct pointerlist *genptr=ht->bins[i];
      while(genptr!=NULL) {
	struct pointerlist *tmpptr=genptr->next;
	freefunction(genptr->object);
	free(genptr);
	genptr=tmpptr;
      }
    }
  }
  free(ht);
}
