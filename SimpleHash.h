#ifndef SIMPLEHASH_H
#define SIMPLEHASH_H

/* LinkedHashNode *****************************************************/

class LinkedHashNode {
    
public:
    LinkedHashNode *next;
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
    LinkedHashNode *bucket;
    LinkedHashNode all;    
    LinkedHashNode *last;

    int numparents;
    SimpleHash* parents[10];

public:

    SimpleHash();

    SimpleHash(int size);

    ~SimpleHash();

    void add(int key, int data);

    bool contains(int key);

    bool contains(int key, int data);

    int get(int key, int& data);

    int countdata(int data);

    void addParent(SimpleHash* parent);

    inline SimpleIterator* iterator() {
        return new SimpleIterator(&all);
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

#endif

