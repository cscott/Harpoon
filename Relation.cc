#include "Relation.h"
#include "element.h"
#include "GenericHashtable.h"
#include "set.h"
#include "stdio.h"


// class Tuple

Tuple::Tuple(void *l,void *r) {
  left=l;
  right=r;
}
 
Tuple::Tuple() {
  left=NULL;right=NULL;
}

bool Tuple::isnull() {
  if (left==NULL&&right==NULL)
    return true;
  else
    return false;
}




// class WorkRelation

WorkRelation::WorkRelation() {
  forward=genallocatehashtable((unsigned int (*)(void *)) & hashelement,(int (*)(void *,void *)) & elementequals);
  inverse=genallocatehashtable((unsigned int (*)(void *)) & hashelement,(int (*)(void *,void *)) & elementequals);
  flag=false;
}

WorkRelation::WorkRelation(bool flag) {
  forward=genallocatehashtable(NULL,NULL);
  inverse=genallocatehashtable(NULL,NULL);
  flag=true;
}

Tuple WorkRelation::firstelement() {
  if (forward->list==NULL)
    return Tuple();
  void * forwardfirst=forward->list->src;
  WorkSet *ws=getset(forwardfirst);
  return Tuple(forwardfirst,ws->firstelement());
}
 
Tuple WorkRelation::getnextelement(void *left,void *right) {
  WorkSet *ws=getset(left);
  if (ws->getnextelement(right)!=NULL) {
    return Tuple(left, ws->getnextelement(right));
  }
  void *leftnext=getnext(forward,left);
  if (leftnext!=NULL) {
    return Tuple(leftnext,getset(leftnext)->firstelement());
  } else return Tuple();
}

bool WorkRelation::contains(void *key, void*object) {
  /*Set up forward reference*/
  if(!gencontains(forward,key))
    return false;
  WorkSet *w=(WorkSet *)gengettable(forward,key);
  return w->contains(object);
}

void WorkRelation::put(void *key, void*object) {
  {    /*Set up forward reference*/
    if(!gencontains(forward,key)) {
      WorkSet *w=flag?new WorkSet(true):new WorkSet();
      genputtable(forward,key,w);
    }
    WorkSet *w=(WorkSet *)gengettable(forward,key);
    w->addobject(object);
  }
  {/*Set up backwars reference*/
    if(!gencontains(inverse,object)) {
      WorkSet *w=flag?new WorkSet(true):new WorkSet();
      genputtable(inverse,object,w);
    }
    WorkSet *w=(WorkSet *) gengettable(inverse,object);
    w->addobject(key);
  }
}
  
void WorkRelation::remove(void *key, void *object) {
  { /*Set up forward reference*/
    WorkSet *w=(WorkSet *)gengettable(forward,key);
    w->removeobject(object);
    if (w->isEmpty()) {
      genfreekey(forward,key);
      delete(w);
    }
  }


  { /*Set up backwards reference*/
    WorkSet *w=(WorkSet *)gengettable(inverse,object);
    w->removeobject(key);
    if (w->isEmpty()) {
      genfreekey(inverse,object);
      delete(w);
    }
  }
}

WorkSet* WorkRelation::getset(void *key) {
  if (gencontains(forward,key))
    return (WorkSet *) gengettable(forward,key);
  else return NULL;
}

void* WorkRelation::getobj(void *key) {
  WorkSet *ws=getset(key);
  if (ws==NULL)
    return NULL;
  return ws->firstelement();
}

WorkSet* WorkRelation::invgetset(void *key) {
  if (gencontains(inverse,key))
    return (WorkSet *) gengettable(inverse,key);
  else
    return NULL;
}

void* WorkRelation::invgetobj(void *key) {
  WorkSet *ws=invgetset(key);
  if (ws==NULL)
    return NULL;
  return ws->firstelement();
}

WorkRelation::~WorkRelation() {
  destroyer(forward);
  destroyer(inverse);
}


void WorkRelation::destroyer(struct genhashtable *d) {
  struct geniterator *it=gengetiterator(d);
  while (true) {
    void *key=gennext(it);
    if (key==NULL)
      break;
    WorkSet *ws=(WorkSet *)gengettable(d,key);
    delete(ws);
  }
  genfreeiterator(it);
  genfreehashtable(d);
}

