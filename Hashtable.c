#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <values.h>
#include "Hashtable.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

int puttable(struct hashtable *ht, long long key, void * object) {
  int bin=hashfunction(ht,key);
  struct pointerlist * newptrlist=(struct pointerlist *) calloc(1,sizeof(struct pointerlist));
  newptrlist->uid=key;
  newptrlist->object=object;
  newptrlist->next=ht->bins[bin];
  ht->bins[bin]=newptrlist;
  ht->counter++;
  if (ht->counter>ht->currentsize&&ht->currentsize!=MAXINT) {
    /* Expand hashtable */
    long newcurrentsize=(ht->currentsize<(MAXINT/2))?ht->currentsize*2:MAXINT;
    long oldcurrentsize=ht->currentsize;
    struct pointerlist **newbins=(struct pointerlist **)calloc(newcurrentsize, sizeof(struct pointerlist *));
    struct pointerlist **oldbins=ht->bins;
    long i;
    ht->currentsize=newcurrentsize;
    for(i=0;i<oldcurrentsize;i++) {
      struct pointerlist * tmpptr=oldbins[i];
      while(tmpptr!=NULL) {
	int hashcode=hashfunction(ht, tmpptr->uid);
	struct pointerlist *nextptr=tmpptr->next;
	tmpptr->next=newbins[hashcode];
	newbins[hashcode]=tmpptr;
	tmpptr=nextptr;
      }
    }
    ht->bins=newbins;
    free(oldbins);
  }
  return 1;
}

void * gettable(struct hashtable *ht, long long key) {
  struct pointerlist * ptr=ht->bins[hashfunction(ht,key)];
  while(ptr!=NULL) {
    if (ptr->uid==key)
      return ptr->object;
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %lld\n",key);
  return NULL;
}

int contains(struct hashtable *ht, long long key) {
  struct pointerlist * ptr=ht->bins[hashfunction(ht,key)];
  while(ptr!=NULL) {
    if (ptr->uid==key)
      return 1;
    ptr=ptr->next;
  }
  return 0;
}

void freekey(struct hashtable *ht, long long key) {
  struct pointerlist * ptr=ht->bins[hashfunction(ht,key)];

  if (ptr->uid==key) {
    ht->bins[hashfunction(ht,key)]=ptr->next;
    free(ptr);
    ht->counter--;
    return;
  }
  while(ptr->next!=NULL) {
    if (ptr->next->uid==key) {
      struct pointerlist *tmpptr=ptr->next;
      ptr->next=tmpptr->next;
      free(tmpptr);
      ht->counter--;
      return;
    }
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %lld\n",key);
}

int hashfunction(struct hashtable *ht, long long key) {
  return ((int)key) % ht->currentsize;
}

struct hashtable * allocatehashtable() {
  struct hashtable * ht=(struct hashtable *) calloc(1,sizeof(struct hashtable));
  ht->bins=(struct pointerlist **)calloc(initialnumbins, sizeof(struct pointerlist *));
  ht->currentsize=initialnumbins;
  return ht;
}

void freehashtable(struct hashtable * ht) {
  int i;
  for (i=0;i<ht->currentsize;i++) {
    if (ht->bins[i]!=NULL) {
      struct pointerlist *genptr=ht->bins[i];
      while(genptr!=NULL) {
	struct pointerlist *tmpptr=genptr->next;
	free(genptr);
	genptr=tmpptr;
      }
    }
  }
  free(ht->bins);
  free(ht);
}

void freedatahashtable(struct hashtable * ht, void (*freefunction)(void *)) {
  int i;
  for (i=0;i<ht->currentsize;i++) {
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
  free(ht->bins);
  free(ht);
}

struct iterator * getiterator(struct hashtable *ht) {
  struct iterator *gi=(struct iterator *) calloc(1,sizeof(struct iterator));
  gi->ptr=ht->bins[0];
  gi->binnumber=1;
  gi->ht=ht;
  return gi;
}

long long next(struct iterator *it) {
  int i=it->binnumber;
  if (it->ptr!=NULL) {
    struct pointerlist * tmp=it->ptr;
    it->ptr=tmp->next;
    return tmp->uid;
  }
  for(;i<it->ht->currentsize;i++) {
    if (it->ht->bins[i]!=NULL) {
      it->ptr=it->ht->bins[i]->next;
      it->binnumber=i+1;
      return it->ht->bins[i]->uid;
    }
  }
  return 0;
}

void freeiterator(struct iterator *it) {
  free(it);
}



