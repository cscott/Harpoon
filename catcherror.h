#ifndef CATCHERROR_H
#define CATCHERROR_H

#include "stack.h"
#include <signal.h>
#include <setjmp.h>
#include <stdio.h>
#include <stdlib.h>

void handler(int signal);
void installhandlers();
extern struct StackElement * stackptr;

#define STARTREPAIR(repair, label) { \
  jmp_buf save_buf;     \
  if (setjmp(save_buf)) { \
    repair \
    goto label$error; \
  } \
  pushstack(&stackptr, &save_buf); \
}

#define ENDREPAIR(label) popstack(&stackptr); \
  goto label$skip;\
label$error: \
label$skip:


#endif
