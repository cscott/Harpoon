#ifndef Hashtable_H
#define Hashtable_H

#include "GenericHashtable.h"
#include "classlist.h"

class Hashtable {
 public:
  Hashtable();
  Hashtable(unsigned int (*hash_function)(void *),int (*comp_function)(void *, void *));
  ~Hashtable();
  void put(void *key, void*object);
  void remove(void *key);
  void* get(void *key);
  bool contains(void *key);
  void setparent(Hashtable *parent);
 private:
  Hashtable *parent;
  struct genhashtable *forward;
};

#endif
