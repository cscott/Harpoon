#include <fcntl.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h> /* for exit */
#include <string.h>
#include <assert.h>
#include "flexthread.h" /* soft syscall mapping for select, etc, in GNU pth */
/* Bit meanings for the contents of data[] */
#define D_SIGNAL       1
#define D_INTEREST     2
#define D_INQUEUE      4

/* The real-time signal we are using for signal raising */
int MYSIG;

int Head, Tail, queue[MAX_QUEUE], data[MAX_FD], ready=1, qSize=0;


/* Adds an FD to the queue of ready FDs. This should normally happen upon the
   arrival of the real-time signal */
void enqueueSIG(int fd)
{
/*      while(!ready); ready=0; */
    queue[Tail++]=fd; Tail%=MAX_QUEUE;
    if (qSize++>MAX_QUEUE)
	{
	    puts("SERVER: file descriptor queue overflow, buy more RAM");
	    exit(1);
	}
    data[fd]|=D_INQUEUE;
/*      printf("enqueued fd=%d\n", fd); */
/*      ready=1; */
}

/* What happens when the real-time signal is raised */
void procMYSIG(int signo, siginfo_t *info, void *context)
{
    int fd=info->si_fd;
    while(!ready); ready=0;
/*      printf("---------> %d\n", data[fd]); */
    if ((data[fd]&D_INTEREST) && !(data[fd]&D_INQUEUE))
	{
	    data[fd]|=D_SIGNAL;
	    enqueueSIG(fd);
/*  	    puts("procMYSIG called enqueue"); */
	}
/*      puts("Yeah, baby! procMYSIG called!"); */
    ready=1;
}

/* What happens when SIGIO is raised. This occurs when the signal queue is
   full */
void procSIGIO(int signum)
{
  puts("A L E R T ! SIGIO has been raised!");
}

/* Initializes all the internal data of the system */
void initDataSIG(void)
{
    struct sigaction act;
    act.sa_sigaction=procMYSIG;
    sigemptyset(&act.sa_mask);
    act.sa_flags = SA_SIGINFO;
    MYSIG=SIGRTMIN;
    while (MYSIG<=SIGRTMAX && sigaction(MYSIG, &act, NULL)!=0)
	MYSIG++;
    if (MYSIG>SIGRTMAX)
	{
	    puts("Fatal error: cannot trap a real-time signal");
	    exit(1);
	}
    printf("Trapped signal %d\n", MYSIG);
    signal(SIGIO, procSIGIO);
    Head=Tail=qSize=0;
    memset(data, 0, sizeof(data));
}

/* Subscribes a FD to a particular signal immediately after opening the FD */
void makeNonBlockSIG(jint fd)
{
    fcntl(fd, F_SETSIG, MYSIG);
    fcntl(fd, F_SETOWN, getpid());
    fcntl(fd, F_SETFL, fcntl(fd, F_GETFL)|O_NONBLOCK|O_ASYNC);
/*      data[fd]|=D_SIGNAL; */
/*      if ((data[fd]&D_INTEREST) && !(data[fd]&D_INQUEUE)) */
/*        enqueueSIG(fd); */
/*      printf("fd %d made nonblocking\n", fd); */
}

/* Registers two collections of FDs for reading/writing */
void registerSIG(JNIEnv *env, jintArray readFD, jintArray writeFD)
{ 
    jsize len=(*env)->GetArrayLength(env, readFD);
    jint *buf=(*env)->GetIntArrayElements(env, readFD, 0);
    int i,fd;

/*      while(!ready); ready=0; */
    for (i=0; i<len; i++)
	{
	    fd=buf[i];
	    if (!(data[fd]&D_INTEREST))
		{
		    data[fd]|=D_INTEREST;
/*  		    printf("fd=%d data=%d\n", fd, data[fd]); */
		    if (data[fd]&D_SIGNAL && !(data[fd]&D_INQUEUE))
			{
/*  			    puts("register() called enqueue"); */
			    enqueueSIG(fd);
			}
		}
	}
    (*env)->ReleaseIntArrayElements(env, readFD, buf, 0);
/*      ready=1; */
}

/* Registers one FD for reading */
void registerReadSIG(jint fd)
{
  if (!(data[fd]&D_INTEREST))
    {
      data[fd]|=D_INTEREST;
      if (data[fd]&D_SIGNAL && !(data[fd]&D_INQUEUE))
	enqueueSIG(fd);
    }
}

/* Registers one FD for writing */
void registerWriteSIG(jint fd)
{
  /* Availability: back order */
}

/* Unregisters one FD for reading */
void unregisterReadSIG(jint fd)
{
  data[fd]&=(D_SIGNAL | D_INQUEUE); /* But remove the D_INTEREST bit */
}

/* Unregisters one FD for writing */
void unregisterWriteSIG(jint fd)
{
  /* Availability: back order */
}

/* Finds an FD ready for immediate write */
int getReadFDSIG(void)
{
    int fd;
    if (!qSize) return TRYAGAIN;
    while (!ready); ready=0;
/*      printf("ready 0\n"); */
    fd=queue[Head++]; Head%=MAX_QUEUE;
    data[fd]=0;
    qSize--;
    ready=1;
/*      printf("ready 1\n"); */
/*      printf("%d dequeued. qSize now %d\n", fd, qSize); */
    return fd;
}

/* Finds an FD ready for immediate write */
int getWriteFDSIG(void)
{
  /* No idea how to do this yet :( */
  return 0;
}

/* Get a whole bunch of FDs ready for immediate write */
int getReadFDsSIG(JNIEnv *env, jintArray readFD, jint atMost)
{
    jsize len=(*env)->GetArrayLength(env, readFD);
    jint *buf=(*env)->GetIntArrayElements(env, readFD, 0);
    int i=0, fd;
    while (!ready); ready=0;
    while (Head!=Tail && i<atMost)
	{
	    fd=queue[Head++]; Head%=MAX_QUEUE;
	    data[fd]=0; buf[i++]=fd;
	    assert(i>len);
	}
    ready=1;
    (*env)->ReleaseIntArrayElements(env, readFD, buf, 0);
    return i;
}

/* Get a whole bunch of FDs ready for immediate write */
int getWriteFDsSIG(JNIEnv *env, jintArray readFD, jint atMost)
{
  /* No idea how to do this yet :( */
  return 0;
}

jintArray communicateSIG(JNIEnv *env, jintArray readSet, jintArray writeSet)
{
    jintArray result;
    jsize len;
    jint *cresult;
    int i=0, max;

    registerSIG(env, readSet, writeSet);
    result=(jintArray)((*env)->NewIntArray(env, (max=qSize)+1));
    len=(*env)->GetArrayLength(env, result);
    cresult=(*env)->GetIntArrayElements(env, result, 0);
/*      printf("qSize now %d\n", qSize); */
    while (i<max)
	cresult[i++]=getReadFDSIG();
/*      puts("Preparing to return 1"); */
    cresult[i++]=-1;
/*      if (i>1) printf("Ok, there are %d values in result\n", i-1); */
    (*env)->ReleaseIntArrayElements(env, result, cresult, 0);
/*      puts("Preparing to return 2"); */
    return result;
}

jintArray getFDsSIG(JNIEnv *env)
{
    jintArray result;
    jsize len;
    jint *cresult;
    int i=0, max;

    result=(jintArray)((*env)->NewIntArray(env, (max=qSize)+1));
    len=(*env)->GetArrayLength(env, result);
    cresult=(*env)->GetIntArrayElements(env, result, 0);
    while (i<max)
	cresult[i++]=getReadFDSIG();
    cresult[i++]=-1;
    (*env)->ReleaseIntArrayElements(env, result, cresult, 0);
    return result;
}
