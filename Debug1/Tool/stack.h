#ifndef STACK_H
#define STACK_H


struct StackElement {
  struct StackElement * next;
  void * contents;
};
void pushstack(struct StackElement **septr,void * obj);
void * popstack(struct StackElement **septr);
#endif
