#include <stdlib.h>
#include "test.h"
#include "cmemory.h"

void addint(int);

char *hstring="headobject\0";

struct Node {
     int data;
     struct Node *next;
     struct Node *prev;
};
static struct Node* head;


int main(int argc, char **argv) {  
  initializeanalysis();

  head=NULL;
  addint(1);
  addint(3);
  addint(4); 

  //head->next->next->prev=head;  // Error insertion

  addint(5);
  addint(2);
}

void addint(int v) {
  struct Node *newnode = (struct Node*) malloc(sizeof(struct Node));
  newnode->next=head;
  newnode->prev=NULL;
  if (head!=NULL) {
    head->prev=newnode;
  }
  head=newnode;

  //head->next = head; // Error insertion

  addmapping(hstring, head, "Node");
  doanalysisfordebugging("Invokation");
}
