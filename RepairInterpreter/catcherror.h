#ifndef CATCHERROR_H
#define CATCHERROR_H

#include "stack.h"
#include <signal.h>
#include <setjmp.h>
#include <stdio.h>

void handler(int signal);
void installhandlers();
extern struct StackElement * stackptr;

#define STARTREPAIR(repair, label)  \
  jmp_buf label_save_buf;     \
  if (setjmp(label_save_buf)) { \
    repair \
    resetanalysis(); \
    goto label_error; \
  } \
label_error:\
  pushstack(&stackptr, &label_save_buf);




#define ENDREPAIR(label) popstack(&stackptr);



#endif
