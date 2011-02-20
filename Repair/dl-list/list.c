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

void RepOk() {

  if ((head->prev == NULL) && (head->next == NULL))
    return; // list has a single valid element

  if (head->prev != NULL) {
    printf("head->prev should be NULL\n");
    _exit(0);
  }

  // checking for cycles
  struct Node* head1 = head->next;
  struct Node* head2 = head->next->next;

  while ( (head1 != NULL) && (head2 != NULL) && (head1 != head2) ) {
    head1 = head1->next;
    
    if (head2->next != NULL)
      head2 = head2->next->next;
    else head2 = NULL;
  }

  if (head1 == head2) {
    printf("List contains cycles!\n");
    _exit(0);
  }

  head1 = head;
  while (head1->next != NULL) {
    if (head1->next->prev != head1) {
      printf("e->next->prev=e violated!\n");
      _exit(0);
    }
    
    head1 = head1->next;
  }
  
}

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

  head->next = head; // Error insertion

  // RepOk call
  RepOk();  

  /* tool call */
  addmapping(hstring, head, "Node");
  doanalysisfordebugging("Invocation");
  /* --------- */
}
