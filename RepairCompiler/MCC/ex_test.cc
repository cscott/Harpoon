#include "ex_aux.h"

struct Node {
     int data;
     struct Node *next;
     struct Node *prev;
};

int main(int argc, char **argv) {
  struct Node * head =(struct Node *) malloc(sizeof (struct Node));
  for(int i=0;i<300;i++) {
    struct Node * tmp =(struct Node *) malloc(sizeof (struct Node));
    tmp->next=head;
    head->prev=tmp;
    head=tmp;
  }
  
  for(int j=0;j<6000;j++) {
#include "ex.cc"
  }
  
}
