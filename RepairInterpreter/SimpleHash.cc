#include "SimpleHash.h"

/* LINKED HASH NODE ****************************************************/

LinkedHashNode::LinkedHashNode(int key, int data, LinkedHashNode *next) {
    this->key = key;
    this->data = data;
    this->next = next;
}

LinkedHashNode::LinkedHashNode() {
    this->key = -1;
    this->data = -1;
    this->next = 0;
}

/* SIMPLE LIST ****************************************************/

SimpleList::SimpleList() {
    ptr = 0;
    head.next = 0;
}

void SimpleList::reset() {
    ptr = &head;
}

int SimpleList::hasMoreElements() {
    return (ptr->next == 0);
}

int SimpleList::nextElement() {
    ptr = ptr->next;
    return ptr->data;
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
    
/* SIMPLE HASH ********************************************************/

SimpleHash::SimpleHash(int size) {
    if (size <= 0) {
        throw SimpleHashException();
    }
    this->size = size;
    this->bucket = new LinkedHashNode[size];
    this->last = &all;
    all.next = 0;
    this->numparents = 0;
    this->numelements = 0;
}

SimpleHash::SimpleHash() {
    this->size = 100;
    this->bucket = new LinkedHashNode[size];
    this->last = &all;
    all.next = 0;
    this->numparents = 0;
    this->numelements = 0;
}


SimpleHash::~SimpleHash() {
}

void SimpleHash::addParent(SimpleHash* parent) {
    parents[numparents++] = parent;    
}

int SimpleHash::add(int key, int data) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode *ptr = &bucket[hashkey];

    /* check that this key/object pair isn't already here */
    // TBD can be optimized for set v. relation */
    while (ptr->next) {
        ptr = ptr->next;
        if (ptr->key == key && ptr->data == data) {
            return 0;
        }
    }
    
    ptr->next = new LinkedHashNode(key, data, 0);
    last = last->next = new LinkedHashNode(key, data, 0);
    numelements++;
    
    for (int i = 0; i < numparents; i++) {
        parents[i]->add(key, data);
    }

    return key;
}

bool SimpleHash::contains(int key) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode *ptr = &bucket[hashkey];
    while (ptr->next) {
        ptr = ptr->next;
        if (ptr->key == key) {
            // we already have this object 
            // stored in the hash so just return
            return true;
        }
    }
    return false;
}

bool SimpleHash::contains(int key, int data) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode *ptr = &bucket[hashkey];
    while (ptr->next) {
        ptr = ptr->next;
        if (ptr->key == key && ptr->data == data) {
            // we already have this object 
            // stored in the hash so just return
            return true;
        }
    }
    return false;
}

int SimpleHash::count(int key) {
    unsigned int hashkey = (unsigned int)key % size;
    int count = 0;

    LinkedHashNode *ptr = &bucket[hashkey];
    while (ptr->next) {
        ptr = ptr->next;
        if (ptr->key == key) {
            count++;
        }
    }
    return count;
}

int SimpleHash::get(int key, int&data) {
    unsigned int hashkey = (unsigned int)key % size;
    
    LinkedHashNode *ptr = &bucket[hashkey];
    while (ptr->next) {
        ptr = ptr->next;
        if (ptr->key == key) {
            data = ptr->data;
            return 1; // success
        }
    }
        
    return 0; // failure
}

int SimpleHash::countdata(int data) {
    int count = 0;
    for (int i = 0; i < size ; i++) {
        LinkedHashNode *ptr = &bucket[i];
        while(ptr->next) {
            ptr = ptr->next;
            if (ptr->data == data) {
                count++;
            }
        }
    }
    return count;
}

SimpleHashException::SimpleHashException() {}
