#include "ex_aux.h"

struct Node {
     int data;
     struct Node *next;
     struct Node *prev;
};

int main(int argc, char **argv) {
  struct Node * head =0;//(struct Node *) malloc(sizeof (struct Node));
  //  head->next=0;
  //head->prev=0;

#include "ex.cc"
  
  
}
