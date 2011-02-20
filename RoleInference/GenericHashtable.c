#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <values.h>
#include "GenericHashtable.h"
#include "RoleInference.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

int genputtable(struct genhashtable *ht, void * key, void * object) {
  int bin=genhashfunction(ht,key);
  struct genpointerlist * newptrlist=(struct genpointerlist *) calloc(1,sizeof(struct genpointerlist));
  newptrlist->src=key;
  newptrlist->object=object;
  newptrlist->next=ht->bins[bin];
  ht->bins[bin]=newptrlist;
  ht->counter++;
  if(ht->counter>ht->currentsize&&ht->currentsize!=MAXINT) {
    /* Expand hashtable */
    long newcurrentsize=(ht->currentsize<(MAXINT/2))?ht->currentsize*2:MAXINT;
    long oldcurrentsize=ht->currentsize;
    struct genpointerlist **newbins=(struct genpointerlist **)calloc(newcurrentsize, sizeof(struct genpointerlist *));
    struct genpointerlist **oldbins=ht->bins;
    long i;
    ht->currentsize=newcurrentsize;
    for(i=0;i<oldcurrentsize;i++) {
      struct genpointerlist * tmpptr=oldbins[i];
      while(tmpptr!=NULL) {
        int hashcode=genhashfunction(ht, tmpptr->src);
        struct genpointerlist *nextptr=tmpptr->next;
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

void * gengettable(struct genhashtable *ht, void * key) {
  struct genpointerlist * ptr=ht->bins[genhashfunction(ht,key)];
  while(ptr!=NULL) {
    if (((ht->comp_function==NULL)&&(ptr->src==key))||((ht->comp_function!=NULL)&&(*ht->comp_function)(ptr->src,key)))
      return ptr->object;
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %p\n",key);
  return NULL;
}

int gencontains(struct genhashtable *ht, void * key) {
  struct genpointerlist * ptr=ht->bins[genhashfunction(ht,key)];
  while(ptr!=NULL) {
    if (((ht->comp_function==NULL)&&(ptr->src==key))||((ht->comp_function!=NULL)&&(*ht->comp_function)(ptr->src,key)))
      return 1;
    ptr=ptr->next;
  }
  return 0;
}


void genfreekey(struct genhashtable *ht, void * key) {
  struct genpointerlist * ptr=ht->bins[genhashfunction(ht,key)];

  if (((ht->comp_function==NULL)&&(ptr->src==key))||((ht->comp_function!=NULL)&&(*ht->comp_function)(ptr->src,key))) {
    ht->bins[genhashfunction(ht,key)]=ptr->next;
    free(ptr);
    ht->counter--;
    return;
  }
  while(ptr->next!=NULL) {
    if (((ht->comp_function==NULL)&&(ptr->next->src==key))||((ht->comp_function!=NULL)&&(*ht->comp_function)(ptr->next->src,key))) {
      struct genpointerlist *tmpptr=ptr->next;
      ptr->next=tmpptr->next;
      free(tmpptr);
      ht->counter--;
      return;
    }
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %p\n",key);
}

int genhashfunction(struct genhashtable *ht, void * key) {
  if (ht->hash_function==NULL)
    return ((long int)key) % ht->currentsize;
  else
    return ((*ht->hash_function)(key)) % ht->currentsize;
}

struct genhashtable * genallocatehashtable(int (*hash_function)(void *),int (*comp_function)(void *, void *)) {
  struct genhashtable *ght=(struct genhashtable *) calloc(1,sizeof(struct genhashtable));
  struct genpointerlist **gpl=(struct genpointerlist **) calloc(geninitialnumbins, sizeof(struct genpointerlist *));
  ght->hash_function=hash_function;
  ght->comp_function=comp_function;
  ght->currentsize=geninitialnumbins;
  ght->bins=gpl;
  return ght;
}

void genfreehashtable(struct genhashtable * ht) {
  int i;
  for (i=0;i<ht->currentsize;i++) {
    if (ht->bins[i]!=NULL) {
      struct genpointerlist *genptr=ht->bins[i];
      while(genptr!=NULL) {
	struct genpointerlist *tmpptr=genptr->next;
	free(genptr);
	genptr=tmpptr;
      }
    }
  }
  free(ht->bins);
  free(ht);
}

void genfreekeyhashtable(struct genhashtable * ht) {
  int i;
  for (i=0;i<ht->currentsize;i++) {
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
  free(ht->bins);
  free(ht);
}

struct geniterator * gengetiterator(struct genhashtable *ht) {
  struct geniterator *gi=(struct geniterator *) calloc(1,sizeof(struct geniterator));
  gi->ptr=ht->bins[0];
  gi->binnumber=1;
  gi->ht=ht;
  return gi;
}

void * gennext(struct geniterator *it) {
  int i=it->binnumber;
  if (it->ptr!=NULL) {
    struct genpointerlist * tmp=it->ptr;
    it->ptr=tmp->next;
    return tmp->src;
  }
  for(;i<it->ht->currentsize;i++) {
    if (it->ht->bins[i]!=NULL) {
      it->ptr=it->ht->bins[i]->next;
      it->binnumber=i+1;
      return it->ht->bins[i]->src;
    }
  }
  return NULL;
}

void genfreeiterator(struct geniterator *it) {
  free(it);
}
