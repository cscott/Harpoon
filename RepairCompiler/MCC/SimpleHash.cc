#include "SimpleHash.h"
#include <stdarg.h>
#include <stdlib.h>

/* LINKED HASH NODE ****************************************************/

LinkedHashNode::LinkedHashNode(int key, int data, LinkedHashNode *next) {
    this->key = key;
    this->data = data;
    this->next = next;
    this->lnext=0;
    this->lprev=0;
}

LinkedHashNode::LinkedHashNode() {
    this->key = -1;
    this->data = -1;
    this->next = 0;
    this->lnext=0;
    this->lprev=0;
}

/* SIMPLE LIST ****************************************************/

SimpleList::SimpleList() {
    ptr = 0;
    head.next = 0;
}

void SimpleList::reset() {
  // ptr = head.next;
  ptr = &head;
}

int SimpleList::hasMoreElements() {
  //  return (ptr != 0);
  return (ptr->next != 0);
}

int SimpleList::nextElement() {
  ptr = ptr->next;
  return ptr->data;

  //int data = ptr->data;
  //ptr = ptr->next;
  //return data;
}

void SimpleList::add(int data) {
    LinkedHashNode *cur = &head;
    while (cur->next) {
        cur = cur->next;
        if (cur->data == data) {
            return; // no duplicates
        }
    }
    cur->next = new LinkedHashNode(0, data, 0);
    return;
}

int SimpleList::contains(int data) {
    LinkedHashNode *cur = &head;
    while (cur->next) {
        cur = cur->next;
        if (cur->data == data) {
            return 1; // found!
        }
    }
    return 0;    
}

/* WORK LIST ****************************************************/

WorkList::WorkList() {
  head=(struct ListNode *) malloc(sizeof(struct ListNode));
  tail=head;
  headoffset=0;
  tailoffset=0;
}

int WorkList::hasMoreElements() {
  //  return (ptr != 0);
  return ((head!=tail)||(headoffset!=tailoffset));
}

int WorkList::getid() {
  return tail->data[tailoffset];
}

int WorkList::gettype() {
  return tail->data[tailoffset+1];
}

int WorkList::getlvalue() {
  return tail->data[tailoffset+2];
}

int WorkList::getrvalue() {
  return tail->data[tailoffset+3];
}

WorkList::~WorkList() {
  struct ListNode *ptr=tail;
  while(ptr) {
    struct ListNode *oldptr=ptr;
    ptr=ptr->next;
    free(oldptr);
  }
}

void WorkList::pop() {
  int newoffset=tailoffset+4;
  struct ListNode *ptr=tail;
  if (newoffset>=WLISTSIZE) {
    newoffset-=WLISTSIZE;
    struct ListNode *oldptr=ptr;
    ptr=ptr->next;
    free(oldptr);
  }
  tail=ptr;
  tailoffset=newoffset;
}

void WorkList::add(int id,int type, int lvalue, int rvalue) {
  if (headoffset==WLISTSIZE) {
    head->next=(struct ListNode *)malloc(sizeof(struct ListNode));
    headoffset=0;
    head=head->next;
  }
  head->data[headoffset++]=id;
  head->data[headoffset++]=type;
  head->data[headoffset++]=lvalue;
  head->data[headoffset++]=rvalue;
}

/* SIMPLE HASH ********************************************************/

SimpleHash::SimpleHash(int size) {
    if (size <= 0) {
        throw SimpleHashException();
    }
    this->size = size;
    this->bucket = new LinkedHashNode* [size];
    for (int i=0;i<size;i++)
      bucket[i]=0;
    this->nodelist=0;
    this->numparents = 0;
    this->numchildren = 0;
    this->numelements = 0;
}

SimpleHash::SimpleHash() {
    this->size = 100;
    this->bucket = new LinkedHashNode* [size];
    for (int i=0;i<size;i++)
      bucket[i]=0;
    this->nodelist = 0;
    this->numparents = 0;
    this->numelements = 0;
    this->numchildren = 0;
}


SimpleHash::~SimpleHash() {
  delete [] this->bucket;
  LinkedHashNode *ptr=nodelist;
  while(ptr) {
      LinkedHashNode *next=ptr->lnext;
      delete ptr;
      ptr=next;
  }
}

void SimpleHash::addParent(SimpleHash* parent) {
    parents[numparents++] = parent;
    parent->addChild(this);
}

void SimpleHash::addChild(SimpleHash *child) {
  children[numchildren++]=child;
}

int SimpleHash::remove(int key, int data) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode **ptr = &bucket[hashkey];

    for (int i = 0; i < numchildren; i++) {
      children[i]->remove(key, data);
    }

    while (*ptr) {
        if ((*ptr)->key == key && (*ptr)->data == data) {
	  LinkedHashNode *toremove=*ptr;
	  *ptr=(*ptr)->next;
	  if (toremove->lprev)
	    toremove->lprev->lnext=toremove->lnext;
	  if (toremove->lnext)
	    toremove->lnext->lprev=toremove->lprev;
	  delete toremove;
	  numelements--;
	  return 1;
        }
        ptr = &((*ptr)->next);
    }

    return 0;
}



int SimpleHash::add(int key, int data) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode **ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (*ptr) {
        if ((*ptr)->key == key && (*ptr)->data == data) {
            return 0;
        }
        ptr = &((*ptr)->next);
    }
    
    *ptr = new LinkedHashNode(key, data, 0);
    (*ptr)->lnext=nodelist;
    if (nodelist!=0)
      nodelist->lprev=*ptr;
    nodelist=*ptr;
    numelements++;
    
    for (int i = 0; i < numparents; i++) {
        parents[i]->add(key, data);
    }

    return 1;
}

bool SimpleHash::contains(int key) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode *ptr = bucket[hashkey];
    while (ptr) {
        if (ptr->key == key) {
            // we already have this object 
            // stored in the hash so just return
            return true;
        }
        ptr = ptr->next;
    }
    return false;
}

bool SimpleHash::contains(int key, int data) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode *ptr = bucket[hashkey];
    while (ptr) {
        if (ptr->key == key && ptr->data == data) {
            // we already have this object 
            // stored in the hash so just return
            return true;
        }
        ptr = ptr->next;
    }
    return false;
}

int SimpleHash::count(int key) {
    unsigned int hashkey = (unsigned int)key % size;
    int count = 0;

    LinkedHashNode *ptr = bucket[hashkey];
    while (ptr) {
        if (ptr->key == key) {
            count++;
        }
        ptr = ptr->next;
    }
    return count;
}

int SimpleHash::get(int key, int&data) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode *ptr = bucket[hashkey];
    while (ptr) {
        if (ptr->key == key) {
            data = ptr->data;
            return 1; // success
        }
        ptr = ptr->next;
    }
        
    return 0; // failure
}

int SimpleHash::countdata(int data) {
    int count = 0;
    LinkedHashNode *ptr = nodelist;
    while(ptr) {
      if (ptr->data == data) {
	count++;
      }
      ptr = ptr->next;
    }
    return count;
}

SimpleHashException::SimpleHashException() {}
// ************************************************************


RepairHashNode::RepairHashNode(int setrelation, int rule, int lvalue, int rvalue, int data){
    this->setrelation = setrelation;
    this->lvalue=lvalue;
    this->rvalue=rvalue;
    this->data = data;
    this->next = 0;
    this->lnext=0;
    this->rule=rule;
}

// ************************************************************

RepairHash::RepairHash(int size) {
    if (size <= 0) {
        throw SimpleHashException();
    }
    this->size = size;
    this->bucket = new RepairHashNode* [size];
    for (int i=0;i<size;i++)
      bucket[i]=0;
    this->nodelist=0;
    this->numelements = 0;
}

RepairHash::RepairHash() {
  RepairHash(100);
}

RepairHash::~RepairHash() {
  delete [] this->bucket;
  RepairHashNode *ptr=nodelist;
  while(ptr) {
      RepairHashNode *next=ptr->lnext;
      delete ptr;
      ptr=next;
  }
}

#define SETFLAG (1<<31)

int RepairHash::addset(int setv, int rule, int value, int data) {
  return addrelation(setv||SETFLAG, rule, value,0,data);
}

int RepairHash::addrelation(int relation, int rule, int lvalue, int rvalue, int data) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % size;
    
    RepairHashNode **ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (*ptr) {
        if ((*ptr)->setrelation == relation && 
	    (*ptr)->rule==rule &&
	    (*ptr)->lvalue==lvalue &&
	    (*ptr)->rvalue==rvalue &&
	    (*ptr)->data == data) {
            return 0;
        }
        ptr = &((*ptr)->next);
    }
    
    *ptr = new RepairHashNode(relation,rule,lvalue,rvalue, data);
    (*ptr)->lnext=nodelist;
    nodelist=*ptr;
    numelements++;
    return 1;
}

bool RepairHash::containsset(int setv, int rule, int value) {
  return containsrelation(setv||SETFLAG, rule, value,0);
}

bool RepairHash::containsrelation(int relation, int rule, int lvalue, int rvalue) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % size;
    
    RepairHashNode **ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (*ptr) {
        if ((*ptr)->setrelation == relation && 
	    (*ptr)->rule==rule &&
	    (*ptr)->lvalue==lvalue &&
	    (*ptr)->rvalue==rvalue) {
            return true;
        }
        ptr = &((*ptr)->next);
    }
    return false;
}

int RepairHash::getset(int setv, int rule, int value) {
  return getrelation(setv||SETFLAG, rule, value,0);
}

int RepairHash::getrelation(int relation, int rule, int lvalue,int rvalue) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % size;
    
    RepairHashNode **ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (*ptr) {
        if ((*ptr)->setrelation == relation && 
	    (*ptr)->rule==rule &&
	    (*ptr)->lvalue==lvalue &&
	    (*ptr)->rvalue==rvalue) {
	  return (*ptr)->data;
        }
        ptr = &((*ptr)->next);
    }
    return 0;
}
