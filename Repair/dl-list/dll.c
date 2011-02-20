#include <stdlib.h>

struct Node {
     int data;
     struct Node *next;
     struct Node *prev;
};
struct Node* head = NULL;

void add(int v) {
  struct Node *newnode = (struct Node*) malloc(sizeof(struct Node));
  newnode->data = v;
  newnode->next = head;
  newnode->prev = NULL;
  if (head != NULL) {
    head->prev = newnode;
  }
  head = newnode;
}

struct Node* find(int v) {
  struct Node* p = head;
  while ((p != NULL) && (p->data != v))
    p = p->next;

  return p;
}

void remove(int v) {
  struct Node* p = find(v);
  if (p == NULL)  /* value not found */
    return; 

  if (p->prev == NULL) { /* p is the head */
    head = head->next;
    head->prev = NULL;
  }
  else 
    if (p->next == NULL) /* p is the tail */
      p->prev->next = NULL;
    else {
      p->prev->next = p->next;
      p->next->prev = p->prev;
    }
  free(p);
}

void printlist() {
  struct Node* p = head;
  while (p != NULL) {
    printf("%d\n", p->data);
    p = p->next;
  }
}

int main(int argc, char **argv) {  
  add(1);
  add(3);
  add(4); 
  add(5);
  add(2);

  remove(10);
  remove(4);
  remove(12);
  remove(2);
  remove(1);

  printlist();
}
