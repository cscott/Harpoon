#include "set.h"
#include "GenericHashtable.h"
#include "element.h"
#include <stdio.h>


// class WorkSet

WorkSet::WorkSet() {
  ght=genallocatehashtable((unsigned int (*)(void *)) & hashelement,(int (*)(void *,void *)) & elementequals);
}

WorkSet::WorkSet(unsigned int (*hashf)(void *),int (*equals)(void *,void *)) {
  ght=genallocatehashtable(hashf,equals);
}

WorkSet::WorkSet(bool) {
  ght=genallocatehashtable(NULL,NULL);
}

void WorkSet::addobject(void *obj) {
  if (!contains(obj))
    {
      fflush(NULL);
      genputtable(ght,obj,obj);
    }
}

void WorkSet::removeobject(void *obj) {
  genfreekey(ght,obj);
}

bool WorkSet::contains(void *obj) {
  return (gencontains(ght,obj)==1);
}

void * WorkSet::firstelement() {
  if (ght->list==NULL)
    return NULL;
  else
    return ght->list->src;
}

Iterator * WorkSet::getiterator() {
  return new Iterator(ght);
}

void * WorkSet::getnextelement(void *src) {
  return getnext(ght,src);
}

void * WorkSet::getelement(int i) {
  void *v=firstelement();
  while(i>0) {
    i--;
    v=getnext(ght,v);
  }
  return v;
}

int WorkSet::size() {
  return hashsize(ght);
}

bool WorkSet::isEmpty() {
  return (size()==0);
}

WorkSet::~WorkSet() {
  genfreehashtable(ght);
}




// class Iterator

Iterator::Iterator(struct genhashtable *ght) {
  gi=gengetiterator(ght);
}
  
Iterator::~Iterator() {
  genfreeiterator(gi);
}
  
void * Iterator::next() {
  return gennext(gi);
}
