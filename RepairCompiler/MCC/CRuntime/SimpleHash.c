#include "SimpleHash.h"
#include <stdio.h>

/* LINKED HASH NODE ****************************************************/

struct LinkedHashNode * allocateLinkedHashNode(int key, int data, struct LinkedHashNode *next) {
    struct LinkedHashNode *thisvar=(struct LinkedHashNode *)malloc(sizeof(struct LinkedHashNode));
    thisvar->key = key;
    thisvar->data = data;
    thisvar->next = next;
    thisvar->lnext=0;
    thisvar->lprev=0;
    return thisvar;
}

struct LinkedHashNode * noargallocateLinkedHashNode() {
    struct LinkedHashNode *thisvar=(struct LinkedHashNode *)malloc(sizeof(struct LinkedHashNode));
    thisvar->key = -1;
    thisvar->data = -1;
    thisvar->next = 0;
    thisvar->lnext=0;
    thisvar->lprev=0;
    return thisvar;
}

/* SIMPLE LIST ****************************************************/

struct SimpleList * allocateSimpleList() {
    struct SimpleList *thisvar=(struct SimpleList *)malloc(sizeof(struct SimpleList));
    thisvar->ptr = 0;
    thisvar->head.next = 0;
    return thisvar;
}

void SimpleListreset(struct SimpleList *thisvar) {
    thisvar->ptr = &thisvar->head;
}

int SimpleListhasMoreElements(struct SimpleList *thisvar) {
    return (thisvar->ptr->next != 0);
}

int SimpleListnextElement(struct SimpleList *thisvar) {
  thisvar->ptr = thisvar->ptr->next;
  return thisvar->ptr->data;
}

void SimpleListadd(struct SimpleList *thisvar,int data) {
    struct LinkedHashNode *cur = &thisvar->head;
    while (cur->next) {
        cur = cur->next;
        if (cur->data == data) {
            return; /* no duplicates */
        }
    }
    cur->next = allocateLinkedHashNode(0, data, 0);
    return;
}

int SimpleListcontains(struct SimpleList *thisvar,int data) {
    struct LinkedHashNode *cur = &thisvar->head;
    while (cur->next) {
        cur = cur->next;
        if (cur->data == data) {
            return 1; /* found! */
        }
    }
    return 0;
}

/* WORK LIST ****************************************************/

struct WorkList * allocateWorkList() {
    struct WorkList *thisvar=(struct WorkList *)malloc(sizeof(struct WorkList));
    thisvar->head=(struct ListNode *) malloc(sizeof(struct ListNode));
    thisvar->tail=thisvar->head;
    thisvar->head->next=0;
    thisvar->headoffset=0;
    thisvar->tailoffset=0;
    return thisvar;
}

void WorkListreset(struct WorkList *thisvar) {
    thisvar->head=thisvar->tail;
    thisvar->headoffset=0;
    thisvar->tailoffset=0;
}

int WorkListhasMoreElements(struct WorkList *thisvar) {
  return ((thisvar->head!=thisvar->tail)||(thisvar->headoffset!=thisvar->tailoffset));
}

int WorkListgetid(struct WorkList *thisvar) {
  return thisvar->tail->data[thisvar->tailoffset];
}

int WorkListgettype(struct WorkList *thisvar) {
  return thisvar->tail->data[thisvar->tailoffset+1];
}

int WorkListgetlvalue(struct WorkList *thisvar) {
  return thisvar->tail->data[thisvar->tailoffset+2];
}

int WorkListgetrvalue(struct WorkList *thisvar) {
  return thisvar->tail->data[thisvar->tailoffset+3];
}

void freeWorkList(struct WorkList *thisvar) {
  struct ListNode *ptr=thisvar->tail;
  while(ptr) {
    struct ListNode *oldptr=ptr;
    ptr=ptr->next;
    free(oldptr);
  }
  free(thisvar);
}

void WorkListpop(struct WorkList *thisvar) {
  int newoffset=thisvar->tailoffset+4;
  struct ListNode *ptr=thisvar->tail;
  if (newoffset>=WLISTSIZE) {
    if (newoffset!=WLISTSIZE||thisvar->head!=thisvar->tail) {
      struct ListNode *oldptr=ptr;
      newoffset-=WLISTSIZE;
      ptr=ptr->next;
      free(oldptr);
    }
  }
  thisvar->tail=ptr;
  thisvar->tailoffset=newoffset;
}

void WorkListadd(struct WorkList *thisvar, int id,int type, int lvalue, int rvalue) {
  if (thisvar->headoffset==WLISTSIZE) {
    if (thisvar->head->next==0) {
      thisvar->head->next=(struct ListNode *)malloc(sizeof(struct ListNode));
      thisvar->head->next->next=0;
    }
    thisvar->headoffset=0;
    thisvar->head=thisvar->head->next;
    if (thisvar->tailoffset==WLISTSIZE) { /* roll the tail over also */
        thisvar->tailoffset=0;
        thisvar->tail=thisvar->tail->next;
    }
  }
  thisvar->head->data[thisvar->headoffset++]=id;
  thisvar->head->data[thisvar->headoffset++]=type;
  thisvar->head->data[thisvar->headoffset++]=lvalue;
  thisvar->head->data[thisvar->headoffset++]=rvalue;
}

/* SIMPLE HASH ********************************************************/
struct SimpleIterator* createiterator(struct SimpleHash * thisvar) {
    return allocateSimpleIterator(thisvar->listhead,thisvar->listtail,thisvar->tailindex/*,thisvar*/);
}

void iterator(struct SimpleHash *thisvar, struct SimpleIterator * it) {
  it->cur=thisvar->listhead;
  it->index=0;
  it->tailindex=thisvar->tailindex;
  it->tail=thisvar->listtail;
}

struct SimpleHash * noargallocateSimpleHash() {
    return allocateSimpleHash(100);
}

struct SimpleHash * allocateSimpleHash(int size) {
    struct SimpleHash *thisvar=(struct SimpleHash *)malloc(sizeof(struct SimpleHash));
    if (size <= 0) {
        printf("Negative Hashtable size Exception\n");
        exit(-1);
    }
    thisvar->size = size;
    thisvar->bucket = (struct SimpleNode **) calloc(sizeof(struct SimpleNode *)*size,1);
    /* Set allocation blocks*/
    thisvar->listhead=(struct ArraySimple *) calloc(sizeof(struct ArraySimple),1);
    thisvar->listtail=thisvar->listhead;
    thisvar->tailindex=0;
    /*Set data counts*/
    thisvar->numparents = 0;
    thisvar->numchildren = 0;
    thisvar->numelements = 0;
    return thisvar;
}

void freeSimpleHash(struct SimpleHash *thisvar) {
    struct ArraySimple *ptr=thisvar->listhead;
    free(thisvar->bucket);
    while(ptr) {
        struct ArraySimple *next=ptr->nextarray;
        free(ptr);
        ptr=next;
    }
    free(thisvar);
}

int SimpleHashfirstkey(struct SimpleHash *thisvar) {
  struct ArraySimple *ptr=thisvar->listhead;
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

void SimpleHashaddParent(struct SimpleHash *thisvar,struct SimpleHash* parent) {
    thisvar->parents[thisvar->numparents++] = parent;
    SimpleHashaddChild(parent,thisvar);
}

void SimpleHashaddChild(struct SimpleHash *thisvar, struct SimpleHash *child) {
    thisvar->children[thisvar->numchildren++]=child;
}

int SimpleHashremove(struct SimpleHash *thisvar, int key, int data) {
    unsigned int hashkey = (unsigned int)key % thisvar->size;

    struct SimpleNode **ptr = &thisvar->bucket[hashkey];
    int i;
    for (i = 0; i < thisvar->numchildren; i++) {
        SimpleHashremove(thisvar->children[i], key, data);
    }

    while (*ptr) {
        if ((*ptr)->key == key && (*ptr)->data == data) {
	  struct SimpleNode *toremove=*ptr;
	  *ptr=(*ptr)->next;

	  toremove->inuse=0; /* Marked as unused */

	  thisvar->numelements--;
	  return 1;
        }
        ptr = &((*ptr)->next);
    }

    return 0;
}

void SimpleHashaddAll(struct SimpleHash *thisvar, struct SimpleHash * set) {
    struct SimpleIterator it;
    SimpleHashiterator(set, &it);
    while(hasNext(&it)) {
        int keyv=key(&it);
        int data=next(&it);
        SimpleHashadd(thisvar,keyv,data);
    }
}

int SimpleHashadd(struct SimpleHash * thisvar,int key, int data) {
  /* Rehash code */
  unsigned int hashkey;
  struct SimpleNode **ptr;

  if (thisvar->numelements>=thisvar->size) {
    int newsize=2*thisvar->size+1;
    struct SimpleNode ** newbucket = (struct SimpleNode **) calloc(sizeof(struct SimpleNode *)*newsize,1);
    int i;
    for(i=thisvar->size-1;i>=0;i--) {
        struct SimpleNode *ptr;
        for(ptr=thisvar->bucket[i];ptr!=NULL;) {
            struct SimpleNode * nextptr=ptr->next;
            unsigned int newhashkey=(unsigned int)ptr->key % newsize;
            ptr->next=newbucket[newhashkey];
            newbucket[newhashkey]=ptr;
            ptr=nextptr;
        }
    }
    thisvar->size=newsize;
    free(thisvar->bucket);
    thisvar->bucket=newbucket;
  }

  hashkey = (unsigned int)key % thisvar->size;
  ptr = &thisvar->bucket[hashkey];

  /* check that thisvar key/object pair isn't already here */
  /* TBD can be optimized for set v. relation */

  while (*ptr) {
    if ((*ptr)->key == key && (*ptr)->data == data) {
      return 0;
    }
    ptr = &((*ptr)->next);
  }
  if (thisvar->tailindex==ARRAYSIZE) {
    thisvar->listtail->nextarray=(struct ArraySimple *) calloc(sizeof(struct ArraySimple),1);
    thisvar->tailindex=0;
    thisvar->listtail=thisvar->listtail->nextarray;
  }

  *ptr = &thisvar->listtail->nodes[thisvar->tailindex++];
  (*ptr)->key=key;
  (*ptr)->data=data;
  (*ptr)->inuse=1;

  thisvar->numelements++;
  {
      int i;
      for (i = 0; i < thisvar->numparents; i++) {
          SimpleHashadd(thisvar->parents[i], key, data);
      }
  }
  return 1;
}

bool SimpleHashcontainskey(struct SimpleHash *thisvar,int key) {
    unsigned int hashkey = (unsigned int)key % thisvar->size;

    struct SimpleNode *ptr = thisvar->bucket[hashkey];
    while (ptr) {
        if (ptr->key == key) {
            /* we already have thisvar object
               stored in the hash so just return */
            return true;
        }
        ptr = ptr->next;
    }
    return false;
}

bool SimpleHashcontainskeydata(struct SimpleHash *thisvar, int key, int data) {
    unsigned int hashkey = (unsigned int)key % thisvar->size;

    struct SimpleNode *ptr = thisvar->bucket[hashkey];
    while (ptr) {
        if (ptr->key == key && ptr->data == data) {
            /* we already have thisvar object
               stored in the hash so just return*/
            return true;
        }
        ptr = ptr->next;
    }
    return false;
}

int SimpleHashcount(struct SimpleHash *thisvar,int key) {
    unsigned int hashkey = (unsigned int)key % thisvar->size;
    int count = 0;

    struct SimpleNode *ptr = thisvar->bucket[hashkey];
    while (ptr) {
        if (ptr->key == key) {
            count++;
        }
        ptr = ptr->next;
    }
    return count;
}

struct SimpleHash * SimpleHashimageSet(struct SimpleHash *thisvar, int key) {
  struct SimpleHash * newset=allocateSimpleHash(2*SimpleHashcount(thisvar,key)+4);
  unsigned int hashkey = (unsigned int)key % thisvar->size;

  struct SimpleNode *ptr = thisvar->bucket[hashkey];
  while (ptr) {
    if (ptr->key == key) {
        SimpleHashadd(newset,ptr->data,ptr->data);
    }
    ptr = ptr->next;
  }
  return newset;
}

int SimpleHashget(struct SimpleHash *thisvar, int key, int *data) {
    unsigned int hashkey = (unsigned int)key % thisvar->size;

    struct SimpleNode *ptr = thisvar->bucket[hashkey];
    while (ptr) {
        if (ptr->key == key) {
            *data = ptr->data;
            return 1; /* success */
        }
        ptr = ptr->next;
    }

    return 0; /* failure */
}

int SimpleHashcountdata(struct SimpleHash *thisvar,int data) {
    int count = 0;
    struct ArraySimple *ptr = thisvar->listhead;
    while(ptr) {
      if (ptr->nextarray) {
          int i;
          for(i=0;i<ARRAYSIZE;i++)
              if (ptr->nodes[i].data == data
                  &&ptr->nodes[i].inuse) {
                  count++;
              }
      } else {
          int i;
          for(i=0;i<thisvar->tailindex;i++)
              if (ptr->nodes[i].data == data
                  &&ptr->nodes[i].inuse) {
                  count++;
              }
      }
      ptr = ptr->nextarray;
    }
    return count;
}

struct RepairHashNode * allocateRepairHashNode(int setrelation, int rule, int lvalue, int rvalue, int data, int data2, int ismodify){
    struct RepairHashNode * thisvar=(struct RepairHashNode *)malloc(sizeof(struct RepairHashNode));
    thisvar->setrelation = setrelation;
    thisvar->lvalue=lvalue;
    thisvar->rvalue=rvalue;
    thisvar->data = data;
    thisvar->data2 = data2;
    thisvar->next = 0;
    thisvar->lnext=0;
    thisvar->rule=rule;
    thisvar->ismodify=ismodify;
    return thisvar;
}

struct RepairHash * allocateRepairHash(int size) {
    struct RepairHash *thisvar=(struct RepairHash*)malloc(sizeof(struct RepairHash));
    if (size <= 0) {
        printf("Negative size for RepairHash\n");
        exit(-1);
    }

    thisvar->size = size;
    thisvar->bucket = (struct RepairHashNode **) calloc(1,sizeof(struct RepairHashNode*)*size);
    thisvar->nodelist=0;
    thisvar->numelements = 0;
    return thisvar;
}

#define REPAIRSIZE 100
struct RepairHash * noargallocateRepairHash() {
    return allocateRepairHash(REPAIRSIZE);
}

void freeRepairHash(struct RepairHash *thisvar) {
  struct RepairHashNode *ptr=thisvar->nodelist;
  free(thisvar->bucket);
  while(ptr) {
      struct RepairHashNode *next=ptr->lnext;
      free(ptr);
      ptr=next;
  }
  free(thisvar);
}

#define SETFLAG (1<<31)

int addset(struct RepairHash *thisvar, int setv, int rule, int value, int data) {
  return addrelation(thisvar, setv||SETFLAG, rule, value,0,data);
}

int addrelation(struct RepairHash *thisvar, int relation, int rule, int lvalue, int rvalue, int data) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % thisvar->size;

    struct RepairHashNode **ptr = &thisvar->bucket[hashkey];

    /* check that thisvar key/object pair isn't already here */
    /* TBD can be optimized for set v. relation */
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

    *ptr = allocateRepairHashNode(relation,rule,lvalue,rvalue, data,0,0);
    (*ptr)->lnext=thisvar->nodelist;
    thisvar->nodelist=*ptr;
    thisvar->numelements++;
    return 1;
}

int addrelation2(struct RepairHash *thisvar, int relation, int rule, int lvalue, int rvalue, int data, int data2) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % thisvar->size;

    struct RepairHashNode **ptr = &thisvar->bucket[hashkey];

    /* check that thisvar key/object pair isn't already here */
    /* TBD can be optimized for set v. relation */
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

    *ptr = allocateRepairHashNode(relation,rule,lvalue,rvalue, data,data2,1);
    (*ptr)->lnext=thisvar->nodelist;
    thisvar->nodelist=*ptr;
    thisvar->numelements++;
    return 1;
}

bool containsset(struct RepairHash *thisvar, int setv, int rule, int value) {
  return containsrelation(thisvar, setv||SETFLAG, rule, value,0);
}

bool containsrelation(struct RepairHash *thisvar, int relation, int rule, int lvalue, int rvalue) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % thisvar->size;

    struct RepairHashNode **ptr = &thisvar->bucket[hashkey];

    /* check that thisvar key/object pair isn't already here */
    /*  TBD can be optimized for set v. relation */
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

int getset(struct RepairHash *thisvar, int setv, int rule, int value) {
  return getrelation(thisvar, setv||SETFLAG, rule, value,0);
}

int ismodify(struct RepairHash *thisvar, int relation, int rule, int lvalue,int rvalue) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % thisvar->size;

    struct RepairHashNode **ptr = &thisvar->bucket[hashkey];

    /* check that thisvar key/object pair isn't already here */
    /* TBD can be optimized for set v. relation */
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

int getrelation2(struct RepairHash *thisvar, int relation, int rule, int lvalue,int rvalue) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % thisvar->size;

    struct RepairHashNode **ptr = &thisvar->bucket[hashkey];

    /* check that thisvar key/object pair isn't already here */
    /* TBD can be optimized for set v. relation */
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

int getrelation(struct RepairHash *thisvar, int relation, int rule, int lvalue,int rvalue) {
    unsigned int hashkey = ((unsigned int)(relation ^ rule ^ lvalue ^ rvalue)) % thisvar->size;

    struct RepairHashNode **ptr = &thisvar->bucket[hashkey];

    /* check that this key/object pair isn't already here */
    /* TBD can be optimized for set v. relation */
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
