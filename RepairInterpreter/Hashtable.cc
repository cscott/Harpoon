#include <stdlib.h>
#include "Hashtable.h"
#include "element.h"

Hashtable::Hashtable() {
  forward=genallocatehashtable((unsigned int (*)(void *)) & hashelement,(int (*)(void *,void *)) & elementequals);
  parent=NULL;
}

void Hashtable::setparent(Hashtable *parent) {
  this->parent=parent;
}

Hashtable::Hashtable(unsigned int (*hash_function)(void *),int (*comp_function)(void *, void *)) {
  forward=genallocatehashtable(hash_function,comp_function);
  parent=NULL;
}

void Hashtable::put(void *key, void*object) {
  if (contains(key))
    remove(key);
  genputtable(forward,key,object);
}

void Hashtable::remove(void *key) {
  genfreekey(forward,key);
}

void* Hashtable::get(void *key) {
  if (!gencontains(forward,key)) {
    if (parent!=NULL)
      return parent->get(key);
    else
      return NULL;
  } else
    return gengettable(forward,key);
}

bool Hashtable::contains(void *key) {
  if (!gencontains(forward,key)) {
    if (parent==NULL)
      return false;
    else
      return parent->contains(key);
  }  else
  return true;
}

Hashtable::~Hashtable() {
  genfreehashtable(forward);
}
