#include "config.h"

#ifdef WITH_REALTIME_THREADS

#define USER_THREADS_COMPATIBILITY 1
#include <jni.h>
#include "threads.h"
#include "nativeio.h"
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/select.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/time.h>
#include "../user/threads.h"

struct native_io_struct* fds = NULL;

struct native_io_struct* addFD(int blockingFD, char read) {
  struct native_io_struct* newFD = fds;
#ifdef RTJ_DEBUG_THREADS
  printf("\naddFD(%d, %d)", blockingFD, (int)read);
#endif
  while (newFD!=NULL) {
    if ((newFD->blockingFD==blockingFD)&&(newFD->read==read)) {
      return newFD;
    }
    newFD = newFD->next;
  }
  newFD = malloc(sizeof(struct native_io_struct));
  newFD->blockingFD = blockingFD;
  newFD->read = read;
  pthread_cond_init(&(newFD->waitCond), NULL);
  pthread_mutex_init(&(newFD->waitMutex), NULL);
  newFD->next = fds;
  return fds = newFD;
}

void removeFD(int blockingFD, char read) {
  struct native_io_struct* newFD = fds;
  struct native_io_struct* oldFD = fds;
#ifdef RTJ_DEBUG_THREADS
  printf("\nremoveFD(%d, %d)", blockingFD, (int)read);
#endif
  while (newFD!=NULL) {
    if ((newFD->blockingFD==blockingFD)&&(newFD->read==read)) {
      if (oldFD==newFD) {
	fds = fds->next;
	free(newFD);
	oldFD=newFD=fds;
      } else {
	oldFD->next=newFD->next;
	free(newFD);
	newFD=oldFD->next;
      }
    } else {
      newFD = (oldFD=newFD)->next;
    }
  }
}

void stop_io() {
  sigset_t sigmask;
#ifdef RTJ_DEBUG_THREADS
  printf("\nstop_io()");
#endif
  sigemptyset(&sigmask);
  sigaddset(&sigmask, SIGIO);
  sigprocmask(SIG_BLOCK, &sigmask, NULL);
}

void start_io() {
  sigset_t sigmask;
#ifdef RTJ_DEBUG_THREADS
  printf("\nstart_io()");
#endif
  sigemptyset(&sigmask);
  sigaddset(&sigmask, SIGIO);
  sigprocmask(SIG_UNBLOCK, &sigmask, NULL);
}

void register_io_handler() {
  struct sigaction io;
#ifdef RTJ_DEBUG_THREADS
  printf("\nregister_handler()");
#endif
  io.sa_handler=&handle_io;
  sigemptyset(&io.sa_mask);
  io.sa_flags=SA_ONESHOT;
  sigaction(SIGIO, &io, NULL);
  siginterrupt(SIGIO, 0);
}

#define IO_NOT_READY 0
#define IO_READY 1
#define IO_EXCEPTION 2

char is_ready(int fd, char read) {
  fd_set readfds, writefds, exceptfds;
  struct timeval timeout;
#ifdef RTJ_DEBUG_THREADS
  printf("\nis_ready(%d, %d)", fd, (int)read);
#endif
  if (fd == -1) return IO_NOT_READY;
  timeout.tv_sec=0;
  timeout.tv_usec=0;
  FD_ZERO(&readfds);
  FD_ZERO(&writefds);
  FD_ZERO(&exceptfds);
  FD_SET(fd, read?(&readfds):(&writefds));
  FD_SET(fd, &exceptfds);
  select(2, &readfds, &writefds, &exceptfds, &timeout);
  return FD_ISSET(fd, read?(&readfds):(&writefds))?IO_READY:
    (FD_ISSET(fd, &exceptfds)?IO_EXCEPTION:IO_NOT_READY);
}

void handle_io(int signal) {
  struct native_io_struct* lookFDs = fds;
#ifdef RTJ_DEBUG_THREADS
  printf("\nhandle_io(%d)", signal);
#endif
  register_io_handler();

  while(lookFDs != NULL) {
/*     if (is_ready(lookFDs->blockingFD, lookFDs->read)) { */
#ifdef RTJ_DEBUG_THREADS    
      printf("waking up #%d\n", lookFDs->blockingFD);
#endif
      pthread_mutex_lock(&(lookFDs->waitMutex));
      pthread_cond_signal(&(lookFDs->waitCond));
      pthread_mutex_unlock(&(lookFDs->waitMutex));
/*     } */
    lookFDs = lookFDs->next;
  }
}

void blocking_io(int fd, char read) {
  /* Check to see if it's immediately available */
  struct native_io_struct* newFD = addFD(fd, read);
#ifdef RTJ_DEBUG_THREADS
  printf("do_blocking_io(%d, %d)\n", fd, read);
#endif
/*   while (!is_ready(fd, read)) { */
#ifdef RTJ_DEBUG_THREADS
    printf("...not ready...\n");
#endif
    pthread_mutex_lock(&(newFD->waitMutex));
    register_io_handler();
    fcntl(fd, F_SETOWN, getpid());
    fcntl(fd, F_SETFL, fcntl(fd, F_GETFL)|O_ASYNC|O_NONBLOCK);
    pthread_cond_wait(&(newFD->waitCond), &(newFD->waitMutex));
    pthread_mutex_unlock(&(newFD->waitMutex));
#ifdef RTJ_DEBUG_THREADS
    printf("trying again!\n");
#endif
/*   } */
#ifdef RTJ_DEBUG_THREADS
  printf("done!\n");
#endif
  removeFD(fd, read);
}

extern void (*do_blocking_io)(int fd, char read);

void nativeIO_init() {
  do_blocking_io = &blocking_io;
}

#endif
