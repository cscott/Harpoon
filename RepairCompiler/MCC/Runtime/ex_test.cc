#include "ex_aux.h"
#include "memory.h"
struct Node {
     int data;
     struct Node *next;
     struct Node *prev;
};

int main(int argc, char **argv) {
  initializemmap();
  struct Node * head = 0;
  (struct Node *) malloc(sizeof (struct Node));
  head->prev=0;
  head->next=0;
  for(int i=0;i<300;i++) {
    struct Node * tmp =(struct Node *) malloc(sizeof (struct Node));
    tmp->next=head;
    head->prev=tmp;
    head=tmp;
    }
  
  for(int j=0;j<600;j++) {
#include "ex.cc"
  }
 
}
