#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <values.h>
#include "GenericHashtable.h"
//#include "dmalloc.h"

int genputtable(struct genhashtable *ht, void * key, void * object) {
  unsigned int bin=genhashfunction(ht,key);
  struct genpointerlist * newptrlist=(struct genpointerlist *) new genpointerlist;
  newptrlist->src=key;
  newptrlist->object=object;
  newptrlist->next=ht->bins[bin];
  newptrlist->inext=NULL;
  /* maintain linked list of ht entries for iteration*/
  if (ht->last==NULL) {
    ht->last=newptrlist;
    ht->list=newptrlist;
    newptrlist->iprev=NULL;
  } else {
    ht->last->inext=newptrlist;
    newptrlist->iprev=ht->last;
    ht->last=newptrlist;
  }
  ht->bins[bin]=newptrlist;
  ht->counter++;
  if(ht->counter>ht->currentsize&&ht->currentsize!=MAXINT) {
    /* Expand hashtable */
    long newcurrentsize=(ht->currentsize<(MAXINT/2))?ht->currentsize*2:MAXINT;
    long oldcurrentsize=ht->currentsize;
    struct genpointerlist **newbins=(struct genpointerlist **) new genpointerlist*[newcurrentsize];
    struct genpointerlist **oldbins=ht->bins;
    for(long j=0;j<newcurrentsize;j++) newbins[j]=NULL;
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
    delete[](oldbins);
  }
  return 1;
}

int hashsize(struct genhashtable *ht) {
  return ht->counter;
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

void * getnext(struct genhashtable *ht, void * key) {
  struct genpointerlist * ptr=ht->bins[genhashfunction(ht,key)];
  while(ptr!=NULL) {
    if (((ht->comp_function==NULL)&&(ptr->src==key))||((ht->comp_function!=NULL)&&(*ht->comp_function)(ptr->src,key)))
      if (ptr->inext!=NULL) {
	return ptr->inext->src;
      } else
	return NULL;
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %p...\n Likely concurrent removal--bad user!!!\n",key);
  return NULL;
}

int gencontains(struct genhashtable *ht, void * key) {
  struct genpointerlist * ptr=ht->bins[genhashfunction(ht,key)];
  //printf("In gencontains2\n");fflush(NULL);
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

    if (ptr==ht->last)
      ht->last=ptr->iprev;

    if (ptr==ht->list)
      ht->list=ptr->inext;

    if (ptr->iprev!=NULL)
      ptr->iprev->inext=ptr->inext;
    if (ptr->inext!=NULL)
      ptr->inext->iprev=ptr->iprev;
    
    delete(ptr);
    ht->counter--;
    return;
  }
  while(ptr->next!=NULL) {
    if (((ht->comp_function==NULL)&&(ptr->next->src==key))||((ht->comp_function!=NULL)&&(*ht->comp_function)(ptr->next->src,key))) {
      struct genpointerlist *tmpptr=ptr->next;
      ptr->next=tmpptr->next;
      if (tmpptr==ht->list)
	ht->list=tmpptr->inext;
      if (tmpptr==ht->last)
	ht->last=tmpptr->iprev;
      if (tmpptr->iprev!=NULL)
	tmpptr->iprev->inext=tmpptr->inext;
      if (tmpptr->inext!=NULL)
	tmpptr->inext->iprev=tmpptr->iprev;
      delete(tmpptr);
      ht->counter--;
      return;
    }
    ptr=ptr->next;
  }
  printf("XXXXXXXXX: COULDN'T FIND ENTRY FOR KEY %p\n",key);
}

unsigned int genhashfunction(struct genhashtable *ht, void * key) {
  if (ht->hash_function==NULL)
    return ((long unsigned int)key) % ht->currentsize;
  else
    return ((*ht->hash_function)(key)) % ht->currentsize;
}

struct genhashtable * genallocatehashtable(unsigned int (*hash_function)(void *),int (*comp_function)(void *, void *)) {
  struct genhashtable *ght=(struct genhashtable *) new genhashtable;
  struct genpointerlist **gpl=(struct genpointerlist **) new genpointerlist *[geninitialnumbins];
  for(int i=0;i<geninitialnumbins;i++)
    gpl[i]=NULL;
  ght->hash_function=hash_function;
  ght->comp_function=comp_function;
  ght->currentsize=geninitialnumbins;
  ght->bins=gpl;
  ght->counter=0;
  ght->list=NULL;
  ght->last=NULL;
  return ght;
}

void genfreehashtable(struct genhashtable * ht) {
  int i;
  for (i=0;i<ht->currentsize;i++) {
    if (ht->bins[i]!=NULL) {
      struct genpointerlist *genptr=ht->bins[i];
      while(genptr!=NULL) {
	struct genpointerlist *tmpptr=genptr->next;
	delete(genptr);
	genptr=tmpptr;
      }
    }
  }
  delete[](ht->bins);
  delete(ht);
}

struct geniterator * gengetiterator(struct genhashtable *ht) {
  struct geniterator *gi=new geniterator();
  gi->ptr=ht->list;
  return gi;
}

void * gennext(struct geniterator *it) {
  struct genpointerlist *curr=it->ptr;
  if (curr==NULL)
    return NULL;
  if (it->finished&&(curr->inext==NULL))
    return NULL;
  if (it->finished) {
    it->ptr=curr->inext;
    return it->ptr->src;
  }
  if(curr->inext!=NULL)
    it->ptr=curr->inext;
  else
    it->finished=true; /* change offsetting scheme */
  return curr->src;
}

void genfreeiterator(struct geniterator *it) {
  delete(it);
}
