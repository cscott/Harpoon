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


/* SimpleIterator *****************************************************/

class SimpleIterator {

private:

    LinkedHashNode *cur;

public:

    inline SimpleIterator(LinkedHashNode *start) {
        cur = start;
    }

    inline int hasNext() {
        return (int)cur->next == 0 ? 0 : 1;
    }

    inline int next() {
        cur = cur->next;
        return cur->data;
    }

    inline int key() {
        return cur->key;
    }

    inline int size() {
        int temp = 0;
        while (cur->next) {
            temp++;
            cur = cur->next;
        }
        return temp;
    }

};

/* SimpleHash *********************************************************/

class SimpleHash {
private:
    int numelements;
    int size;
    LinkedHashNode **bucket;
    LinkedHashNode *nodelist;
    int numparents;
    int numchildren;
    SimpleHash* parents[10];
    SimpleHash* children[10];
    void addChild(SimpleHash* child);
public:
    SimpleHash();
    SimpleHash(int size);
    ~SimpleHash();
    int add(int key, int data);
    int remove(int key, int data);
    bool contains(int key);
    bool contains(int key, int data);
    int get(int key, int& data);
    int countdata(int data);
    void addParent(SimpleHash* parent);
    inline int firstkey() {
	return nodelist->key;
    }
    inline SimpleIterator* iterator() {
        return new SimpleIterator(nodelist);
    }
    inline int count() {
        return numelements;
    }
    int count(int key);

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
    int setrelation;  
    int lvalue;  
    int rvalue;  
    int rule;
    RepairHashNode(int setrelation, int rule, int lvalue, int rvalue, int data);
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
    bool containsset(int setv, int rule, int value);
    bool containsrelation(int relation, int rule, int lvalue, int rvalue);
    int getset(int setv, int rule, int value);
    int getrelation(int relation, int rule, int lvalue, int rvalue);
};

#endif

