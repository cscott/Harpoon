#include <stdlib.h>
#include "catcherror.h"

struct StackElement * stackptr=NULL;

void handler(int signal) {
  jmp_buf *jb=(jmp_buf *)popstack(&stackptr);
  if (jb==NULL) {
    printf("Signal %d\n",signal);
    exit(-1);
  }
  longjmp(*jb,1);
}

void installhandlers() {
  signal(SIGSEGV,&handler);
  signal(SIGFPE,&handler);
}
