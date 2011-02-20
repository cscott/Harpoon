#include "../user/threads.h"
#include <stdlib.h>

struct native_io_struct {
  int blockingFD;
  char read;
  pthread_cond_t waitCond;
  pthread_mutex_t waitMutex;
  struct native_io_struct* next;
};

struct native_io_struct* fds;

void handle_io(int signal);
