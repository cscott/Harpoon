#ifndef LIST_H
#define LIST_H

class List {
 public:
  List();
  ~List();
  void addobject(void *);
  void toArray(void **);
  unsigned int size();
	      
  
 private:
  void **array;
  unsigned int arraysize;
  unsigned int length;
};

#define INITIAL_LIST_SIZE 10
#endif
