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
  head->next=0;
  headoffset=0;
  tailoffset=0;
}

void WorkList::reset() {
  head=tail;
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
    if (head->next==0) {
      head->next=(struct ListNode *)malloc(sizeof(struct ListNode));
      head->next=0;
    }
    headoffset=0;
    head=head->next;
  }
  head->data[headoffset++]=id;
  head->data[headoffset++]=type;
  head->data[headoffset++]=lvalue;
  head->data[headoffset++]=rvalue;
}

/* SIMPLE HASH ********************************************************/
SimpleIterator* SimpleHash::iterator() {
  return new SimpleIterator(listhead,listtail,tailindex/*,this*/);
}

void SimpleHash::iterator(SimpleIterator & it) {
  //  it.table=this;
  it.cur=listhead;
  it.index=0;
  it.tailindex=tailindex;
  it.tail=listtail;
}

SimpleHash::SimpleHash(int size) {
    if (size <= 0) {
        throw SimpleHashException();
    }
    this->size = size;
    this->bucket = (struct SimpleNode **) calloc(sizeof(struct SimpleNode *)*size,1);
    /* Set allocation blocks*/
    this->listhead=(struct ArraySimple *) calloc(sizeof(struct ArraySimple),1);
    this->listtail=this->listhead;
    this->tailindex=0;
    /*Set data counts*/
    this->numparents = 0;
    this->numchildren = 0;
    this->numelements = 0;
}

SimpleHash::~SimpleHash() {
  free(bucket);
  struct ArraySimple *ptr=listhead;
  while(ptr) {
      struct ArraySimple *next=ptr->nextarray;
      free(ptr);
      ptr=next;
  }
}

int SimpleHash::firstkey() {
  struct ArraySimple *ptr=listhead;
  int index=0;
  while((index==ARRAYSIZE)||!ptr->nodes[index].inuse) {
    if (index==ARRAYSIZE) {
      index=0;
      ptr=ptr->nextarray;
    } else
      index++;
  }
  return ptr->nodes[index].key;
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
    
    struct SimpleNode **ptr = &bucket[hashkey];

    for (int i = 0; i < numchildren; i++) {
      children[i]->remove(key, data);
    }

    while (*ptr) {
        if ((*ptr)->key == key && (*ptr)->data == data) {
	  struct SimpleNode *toremove=*ptr;
	  *ptr=(*ptr)->next;

	  toremove->inuse=0; /* Marked as unused */

	  numelements--;
	  return 1;
        }
        ptr = &((*ptr)->next);
    }

    return 0;
}



int SimpleHash::add(int key, int data) {
    unsigned int hashkey = (unsigned int)key % size;
    
    struct SimpleNode **ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (*ptr) {
        if ((*ptr)->key == key && (*ptr)->data == data) {
            return 0;
        }
        ptr = &((*ptr)->next);
    }
    if (tailindex==ARRAYSIZE) {
      listtail->nextarray=(struct ArraySimple *) calloc(sizeof(struct ArraySimple),1);
      tailindex=0;
      listtail=listtail->nextarray;
    }
    
    *ptr = &listtail->nodes[tailindex++];
    (*ptr)->key=key;
    (*ptr)->data=data;
    (*ptr)->inuse=1;

    numelements++;
    
    for (int i = 0; i < numparents; i++) {
        parents[i]->add(key, data);
    }

    return 1;
}

bool SimpleHash::contains(int key) {
    unsigned int hashkey = (unsigned int)key % size;
    
    struct SimpleNode *ptr = bucket[hashkey];
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
    
    struct SimpleNode *ptr = bucket[hashkey];
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

    struct SimpleNode *ptr = bucket[hashkey];
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
    
    struct SimpleNode *ptr = bucket[hashkey];
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
    struct ArraySimple *ptr = listhead;
    while(ptr) {
      if (ptr->nextarray) {
	for(int i=0;i<ARRAYSIZE;i++)
	  if (ptr->nodes[i].data == data
	      &&ptr->nodes[i].inuse) {
	    count++;
	  }
      } else {
	for(int i=0;i<tailindex;i++)
	  if (ptr->nodes[i].data == data
	      &&ptr->nodes[i].inuse) {
	    count++;
	  }
      }
      ptr = ptr->nextarray;
    }
    return count;
}

SimpleHashException::SimpleHashException() {}
// ************************************************************


RepairHashNode::RepairHashNode(int setrelation, int rule, int lvalue, int rvalue, int data, int data2, int ismodify){
    this->setrelation = setrelation;
    this->lvalue=lvalue;
    this->rvalue=rvalue;
    this->data = data;
    this->data2 = data2;
    this->next = 0;
    this->lnext=0;
    this->rule=rule;
    this->ismodify=ismodify;
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

#define REPAIRSIZE 100
RepairHash::RepairHash() {
    this->size = REPAIRSIZE;
    this->bucket = new RepairHashNode* [REPAIRSIZE];
    for (int i=0;i<REPAIRSIZE;i++)
      bucket[i]=0;
    this->nodelist=0;
    this->numelements = 0;
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
	    (*ptr)->data == data&&
	    (*ptr)->data2 == 0) {
            return 0;
        }
        ptr = &((*ptr)->next);
    }
    
    *ptr = new RepairHashNode(relation,rule,lvalue,rvalue, data,0,0);
    (*ptr)->lnext=nodelist;
    nodelist=*ptr;
    numelements++;
    return 1;
}

int RepairHash::addrelation(int relation, int rule, int lvalue, int rvalue, int data, int data2) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % size;
    
    RepairHashNode **ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (*ptr) {
        if ((*ptr)->setrelation == relation && 
	    (*ptr)->rule==rule &&
	    (*ptr)->lvalue==lvalue &&
	    (*ptr)->rvalue==rvalue &&
	    (*ptr)->data == data &&
	    (*ptr)->data2 == data2) {
            return 0;
        }
        ptr = &((*ptr)->next);
    }
    
    *ptr = new RepairHashNode(relation,rule,lvalue,rvalue, data,data2,1);
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

int RepairHash::ismodify(int relation, int rule, int lvalue,int rvalue) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % size;
    
    RepairHashNode **ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (*ptr) {
        if ((*ptr)->setrelation == relation && 
	    (*ptr)->rule==rule &&
	    (*ptr)->lvalue==lvalue &&
	    (*ptr)->rvalue==rvalue) {
	  return (*ptr)->ismodify;
        }
        ptr = &((*ptr)->next);
    }
    return 0;
}

int RepairHash::getrelation2(int relation, int rule, int lvalue,int rvalue) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % size;
    
    RepairHashNode **ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (*ptr) {
        if ((*ptr)->setrelation == relation && 
	    (*ptr)->rule==rule &&
	    (*ptr)->lvalue==lvalue &&
	    (*ptr)->rvalue==rvalue) {
	  return (*ptr)->data2;
        }
        ptr = &((*ptr)->next);
    }
    return 0;
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
