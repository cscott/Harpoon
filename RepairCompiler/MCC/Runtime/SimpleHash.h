#ifndef SIMPLEHASH_H
#define SIMPLEHASH_H

/* LinkedHashNode *****************************************************/

class LinkedHashNode {
    
public:
    LinkedHashNode *next;
    LinkedHashNode *lnext,*lprev;
    int data;
    int key;  


    LinkedHashNode(int key, int data, LinkedHashNode *next);
    LinkedHashNode();

};

/* SimpleList *********************************************************/

class SimpleList {
private:
    LinkedHashNode head;
    LinkedHashNode *ptr;
public:
    SimpleList();
    void add(int data);
    int contains(int data);
    void reset();
    int hasMoreElements();
    int nextElement();
};


/* WorkList *********************************************************/
#define WLISTSIZE 4*100

class WorkList {
private:
  struct ListNode *head;
  struct ListNode *tail;
  int headoffset;
  int tailoffset;

public:
    WorkList();
    ~WorkList();
    void add(int id, int type, int lvalue, int rvalue);
    int hasMoreElements();
    int getid();
    int gettype();
    int getlvalue();
    int getrvalue();
    void pop();
};

struct ListNode {
  int data[WLISTSIZE];
  ListNode *next;
};
/* SimpleHash *********************************************************/
class SimpleIterator;

class SimpleHash {
private:
    int numelements;
    int size;
    struct SimpleNode **bucket;
    struct ArraySimple *listhead;
    struct ArraySimple *listtail;


    int numparents;
    int numchildren;
    SimpleHash* parents[10];
    SimpleHash* children[10];
    void addChild(SimpleHash* child);
public:
    int tailindex;
    SimpleHash(int size=100);
    ~SimpleHash();
    int add(int key, int data);
    int remove(int key, int data);
    bool contains(int key);
    bool contains(int key, int data);
    int get(int key, int& data);
    int countdata(int data);
    void addParent(SimpleHash* parent);
    int firstkey();
    SimpleIterator* iterator();
    void iterator(SimpleIterator & it);
    inline int count() {
        return numelements;
    }
    int count(int key);

};

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


class SimpleIterator {
 public:

  struct ArraySimple *cur, *tail;
  int index,tailindex;
  //  SimpleHash * table;
  inline SimpleIterator() {}

  inline SimpleIterator(struct ArraySimple *start, struct ArraySimple *tl, int tlindex/*, SimpleHash *t*/) {
    cur = start;
    //    table=t;
    index=0;
    tailindex=tlindex;
    tail=tl;
  }

  inline int hasNext() {
    if (cur==tail &&
	index==tailindex)
      return 0;
    while((index==ARRAYSIZE)||!cur->nodes[index].inuse) {
      if (index==ARRAYSIZE) {
	index=0;
	cur=cur->nextarray;
      } else
	index++;
    }
    if (cur->nodes[index].inuse)
      return 1;
    else
      return 0;
  }

  inline int next() {
    return cur->nodes[index++].data;
  }
  
  inline int key() {
    return cur->nodes[index].key;
  }
};

/* SimpleHashExcepion  *************************************************/

class SimpleHashException {
public:
    SimpleHashException();
};

class RepairHashNode {
 public:
    RepairHashNode *next;
    RepairHashNode *lnext;
    int data;
    int data2;
    int setrelation;  
    int lvalue;  
    int rvalue;  
    int rule;
    int ismodify;
    RepairHashNode(int setrelation, int rule, int lvalue, int rvalue, int data, int data2,int ismodify);
};

class RepairHash {

private:
    int numelements;
    int size;
    RepairHashNode **bucket;
    RepairHashNode *nodelist;

public:
    RepairHash();
    RepairHash(int size);
    ~RepairHash();
    int addset(int setv, int rule, int value, int data);
    int addrelation(int relation, int rule, int lvalue, int rvalue, int data);
    int addrelation(int relation, int rule, int lvalue, int rvalue, int data, int data2);
    bool containsset(int setv, int rule, int value);
    bool containsrelation(int relation, int rule, int lvalue, int rvalue);
    int getset(int setv, int rule, int value);
    int getrelation(int relation, int rule, int lvalue, int rvalue);
    int getrelation2(int relation, int rule, int lvalue, int rvalue);
    int ismodify(int relation, int rule, int lvalue, int rvalue);
};

#endif

