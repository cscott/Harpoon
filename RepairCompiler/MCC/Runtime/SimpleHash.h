#ifndef SIMPLEHASH_H
#define SIMPLEHASH_H

#ifndef bool
#define bool int
#endif

#ifndef true
#define true 1
#endif

#ifndef false
#define false 0
#endif


#include <stdarg.h>
#include <stdlib.h>

/* LinkedHashNode *****************************************************/

struct LinkedHashNode * allocateLinkedHashNode(int key, int data, struct LinkedHashNode *next);
struct LinkedHashNode * noargallocateLinkedHashNode();

struct LinkedHashNode {
    struct LinkedHashNode *next;
    struct LinkedHashNode *lnext,*lprev;
    int data;
    int key;
};

/* SimpleList *********************************************************/

struct SimpleList * allocateSimpleList();
void SimpleListadd(struct SimpleList *, int data);
int SimpleListcontains(struct SimpleList *,int data);
void SimpleListreset(struct SimpleList *);
int SimpleListhasMoreElements(struct SimpleList *);
int SimpleListnextElement(struct SimpleList *);

struct SimpleList {
    struct LinkedHashNode head;
    struct LinkedHashNode *ptr;
};


/* WorkList *********************************************************/
#define WLISTSIZE 4*100

struct WorkList * allocateWorkList();
void freeWorkList(struct WorkList *);
void WorkListreset(struct WorkList *);
void WorkListadd(struct WorkList *,int id, int type, int lvalue, int rvalue);
int WorkListhasMoreElements(struct WorkList *);
int WorkListgetid(struct WorkList *);
int WorkListgettype(struct WorkList *);
int WorkListgetlvalue(struct WorkList *);
int WorkListgetrvalue(struct WorkList *);
void WorkListpop(struct WorkList *);


struct WorkList {
  struct ListNode *head;
  struct ListNode *tail;
  int headoffset;
  int tailoffset;
};

struct ListNode {
  int data[WLISTSIZE];
  struct ListNode *next;
};

/* SimpleHash *********************************************************/

struct SimpleHash * noargallocateSimpleHash();
struct SimpleHash * allocateSimpleHash(int size);
void SimpleHashaddChild(struct SimpleHash *thisvar, struct SimpleHash * child);
void freeSimpleHash(struct SimpleHash *);


int SimpleHashadd(struct SimpleHash *, int key, int data);
int SimpleHashremove(struct SimpleHash *,int key, int data);
bool SimpleHashcontainskey(struct SimpleHash *,int key);
bool SimpleHashcontainskeydata(struct SimpleHash *,int key, int data);
int SimpleHashget(struct SimpleHash *,int key, int* data);
int SimpleHashcountdata(struct SimpleHash *,int data);
void SimpleHashaddParent(struct SimpleHash *,struct SimpleHash* parent);
int SimpleHashfirstkey(struct SimpleHash *);
struct SimpleIterator* SimpleHashcreateiterator(struct SimpleHash *, struct SimpleHash *);
void SimpleHashiterator(struct SimpleHash *, struct SimpleIterator * it);
int SimpleHashcount(struct SimpleHash *, int key);
void SimpleHashaddAll(struct SimpleHash *, struct SimpleHash * set);
struct SimpleHash * SimpleHashimageSet(struct SimpleHash *, int key);

struct SimpleHash {
    int numelements;
    int size;
    struct SimpleNode **bucket;
    struct ArraySimple *listhead;
    struct ArraySimple *listtail;
    int numparents;
    int numchildren;
    struct SimpleHash* parents[10];
    struct SimpleHash* children[10];
    int tailindex;
};

inline int count(struct SimpleHash * thisvar) {
    return thisvar->numelements;
}


/* SimpleHashExcepion  *************************************************/


/* SimpleIterator *****************************************************/
#define ARRAYSIZE 100

struct SimpleNode {
  struct SimpleNode *next;
  int data;
  int key;
  int inuse;
};

struct ArraySimple {
  struct SimpleNode nodes[ARRAYSIZE];
  struct ArraySimple * nextarray;
};


struct SimpleIterator {
  struct ArraySimple *cur, *tail;
  int index,tailindex;
};

inline struct SimpleIterator * noargallocateSimpleIterator() {
    return (struct SimpleIterator*)malloc(sizeof(struct SimpleIterator));
}

inline struct SimpleIterator * allocateSimpleIterator(struct ArraySimple *start, struct ArraySimple *tl, int tlindex) {
    struct SimpleIterator *thisvar=(struct SimpleIterator*)malloc(sizeof(struct SimpleIterator));
    thisvar->cur = start;
    thisvar->index=0;
    thisvar->tailindex=tlindex;
    thisvar->tail=tl;
    return thisvar;
}

inline int hasNext(struct SimpleIterator *thisvar) {
    if (thisvar->cur==thisvar->tail &&
	thisvar->index==thisvar->tailindex)
        return 0;
    while((thisvar->index==ARRAYSIZE)||!thisvar->cur->nodes[thisvar->index].inuse) {
        if (thisvar->index==ARRAYSIZE) {
            thisvar->index=0;
            thisvar->cur=thisvar->cur->nextarray;
        } else
            thisvar->index++;
    }
    if (thisvar->cur->nodes[thisvar->index].inuse)
        return 1;
    else
        return 0;
}

inline int next(struct SimpleIterator *thisvar) {
    return thisvar->cur->nodes[thisvar->index++].data;
}

inline int key(struct SimpleIterator *thisvar) {
    return thisvar->cur->nodes[thisvar->index].key;
}


struct RepairHashNode * allocateRepairHashNode(int setrelation, int rule, int lvalue, int rvalue, int data, int data2,int ismodify);

struct RepairHashNode {
    struct RepairHashNode *next;
    struct RepairHashNode *lnext;
    int data;
    int data2;
    int setrelation;
    int lvalue;
    int rvalue;
    int rule;
    int ismodify;
};

struct RepairHash * noargallocateRepairHash();
struct RepairHash * allocateRepairHash(int size);
void freeRepairHash(struct RepairHash *);
int addset(struct RepairHash *, int setv, int rule, int value, int data);
int addrelation(struct RepairHash *, int relation, int rule, int lvalue, int rvalue, int data);
int addrelation2(struct RepairHash *, int relation, int rule, int lvalue, int rvalue, int data, int data2);
bool containsset(struct RepairHash *, int setv, int rule, int value);
bool containsrelation(struct RepairHash *, int relation, int rule, int lvalue, int rvalue);
int getset(struct RepairHash *, int setv, int rule, int value);
int getrelation(struct RepairHash *, int relation, int rule, int lvalue, int rvalue);
int getrelation2(struct RepairHash *, int relation, int rule, int lvalue, int rvalue);
int ismodify(struct RepairHash *, int relation, int rule, int lvalue, int rvalue);

struct RepairHash {
    int numelements;
    int size;
    struct RepairHashNode **bucket;
    struct RepairHashNode *nodelist;

};

#endif
