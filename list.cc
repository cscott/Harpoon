#include "list.h"
//#include "dmalloc.h"

List::List() {
  array=new void*[INITIAL_LIST_SIZE];
  length=0;
  arraysize=INITIAL_LIST_SIZE;
}

List::~List() {
  delete[](array);
}

void List::addobject(void *object) {
  if ((length+1)>arraysize) {
    void **oldarray=array;
    int oldarraysize=arraysize;
    arraysize*=2;
    array=new void*[arraysize];
    for(int i=0;i<length;i++)
      array[i]=oldarray[i];
    delete[](oldarray);
  }
  array[length++]=object;
}

void List::toArray(void **writearray) {
  for(int i=0;i<length;i++) {
    writearray[i]=array[i];
  }
}

unsigned int List::size() {
  return length;
}
