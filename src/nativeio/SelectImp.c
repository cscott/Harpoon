
#include <errno.h> /* for EINTR */
#include <stdio.h>
#include <string.h>
#include <sys/time.h>
#include <sys/types.h>
#include <unistd.h>
#include "flexthread.h" /* soft syscall mapping for select, etc, in GNU pth */

typedef struct {
    int maxFD;             // The highest FD in the fd_set
    fd_set Interest;       // The FD's that we're waiting for
} TData;

TData readStruct, writeStruct;
struct timeval timeout;

/* Initializes all the internal data of the system */
void initDataSEL(void)
{
  readStruct.maxFD=0; FD_ZERO(&readStruct.Interest);
  writeStruct.maxFD=0; FD_ZERO(&writeStruct.Interest);
  timeout.tv_sec=0; timeout.tv_usec=TIMEOUT;
}

void makeNonBlockSEL(int fd)
{
  fcntl(fd, F_SETFL, fcntl(fd, F_GETFL)|O_NONBLOCK);
}

/* Adds a collection of FDs to the set of interesting FDs */
/*  void newInterest(TData *D, jint *buf, jsize len) */
/*  { */
/*      int i,fd; */
/*      for (i=0; i<len; i++) */
/*  	{ */
/*  	    fd=buf[i]; */
/*  	    FD_SET(fd, &(D->Interest)); */
/*  	    if (fd>D->maxFD) D->maxFD=fd; */
/*  	} */
/*  } */

/* Registers two collections of FDs for reading/writing */
/*  void registerSEL(JNIEnv *env, jintArray readFD, jintArray writeFD) */
/*  { */
/*      jsize len; */
/*      jint *buf; */
/*      len=(*env)->GetArrayLength(env, readFD); */
/*      buf=(*env)->GetIntArrayElements(env, readFD, 0); */
/*      newInterest(&readStruct, buf, len); */
/*      (*env)->ReleaseIntArrayElements(env, readFD, buf, 0); */
/*      len=(*env)->GetArrayLength(env, writeFD); */
/*      buf=(*env)->GetIntArrayElements(env, writeFD, 0); */
/*      newInterest(&writeStruct, buf, len); */
/*      (*env)->ReleaseIntArrayElements(env, writeFD, buf, 0); */
/*  } */

/* Registers one FD for reading */
void registerReadSEL(jint fd)
{
  FD_SET(fd, &(readStruct.Interest));
  if (fd>readStruct.maxFD) readStruct.maxFD=fd;
}

/* Registers one FD for reading */
void registerWriteSEL(jint fd)
{
  FD_SET(fd, &(writeStruct.Interest));
  if (fd>writeStruct.maxFD) writeStruct.maxFD=fd;
}

/* Unregisters one FD for reading */
void unregisterReadSEL(jint fd)
{
    FD_CLR(fd, &(readStruct.Interest));
    while (!FD_ISSET(readStruct.maxFD, &readStruct.Interest) 
	   && readStruct.maxFD>0)
	readStruct.maxFD--;
}

/* Unregisters one FD for writing */
void unregisterWriteSEL(jint fd)
{
    FD_CLR(fd, &(writeStruct.Interest));
    while (!FD_ISSET(writeStruct.maxFD, &writeStruct.Interest) 
	   && writeStruct.maxFD>0)
	writeStruct.maxFD--;
}

/*  jintArray communicateSEL(JNIEnv *env, jintArray readSet, jintArray writeSet) */
/*  { */
  /* OBSOLETE */
/*      jintArray result; */
/*      jsize len; */
/*      jint *cresult; */
/*      int i=0, fd; */

/*      registerSEL(env, readSet, writeSet); */
/*      runSelect(); */
/*      result=(*env)->NewIntArray(env, readStruct.qSize+writeStruct.qSize+1); */
/*      len=(*env)->GetArrayLength(env, result); */
/*      cresult=(*env)->GetIntArrayElements(env, result, 0); */
/*      while (readStruct.qSize) */
/*  	cresult[i++]=dequeue(&readStruct); */
/*      cresult[i++]=-1; */
/*      while (writeStruct.qSize) */
/*  	cresult[i++]=dequeue(&writeStruct); */
/*      (*env)->ReleaseIntArrayElements(env, result, cresult, 0); */
/*      return result; */
/*  } */

// knows when to block

jint getFDsSEL(JNIEnv *env, jint blockMode, jintArray result)
{
  //    jintArray result;
    jsize len;
    jint *cresult, *buf;
    int size=0, j;
    fd_set newReadSet=readStruct.Interest, newWriteSet=writeStruct.Interest;
    int nFD=(readStruct.maxFD>writeStruct.maxFD)?
      readStruct.maxFD:writeStruct.maxFD;
    int rc;

    /** buffer has jints inside it; safe to use malloc instead of GC_malloc */
    buf=(jint*)malloc(sizeof(jint)*(3+readStruct.maxFD+writeStruct.maxFD));
    do {  /* repeat if interruptted */
      rc = select(nFD+1, &newReadSet, &newWriteSet, NULL,
		  blockMode? NULL: &timeout);
    } while (blockMode && rc < 0 && errno == EINTR);
    /* XXX should really throw exception on rc<0 here.
     * all descriptor sets are undefined in this case and should not be
     * tested. */
    for (j=0; j<=readStruct.maxFD; j++)
      if (FD_ISSET(j, &newReadSet))
	{
	  FD_CLR(j, &readStruct.Interest);
	  buf[size++]=j;
	}
    buf[size++]=-1;
    for (j=0; j<=writeStruct.maxFD; j++)
      if (FD_ISSET(j, &newWriteSet))
	{
	  FD_CLR(j, &writeStruct.Interest);
	  buf[size++]=j;
	}
    while (!FD_ISSET(readStruct.maxFD, &readStruct.Interest) 
	   && readStruct.maxFD>0)
      readStruct.maxFD--;
    while (!FD_ISSET(writeStruct.maxFD, &writeStruct.Interest) 
	   && writeStruct.maxFD>0)
      writeStruct.maxFD--;
    //    result=(*env)->NewIntArray(env, size);
    //   len=(*env)->GetArrayLength(env, result);
    cresult=(*env)->GetIntArrayElements(env, result, 0);
    memcpy(cresult, buf, sizeof(jint)*size);
    (*env)->ReleaseIntArrayElements(env, result, cresult, 0);
    free(buf);
    return (jint)size;
}

int * getFDsintSEL(int blockMode) {
    int *cresult, *buf;
    int size=0, j;
    fd_set newReadSet=readStruct.Interest, newWriteSet=writeStruct.Interest;
    int nFD=(readStruct.maxFD>writeStruct.maxFD)?
      readStruct.maxFD:writeStruct.maxFD;
    int rc;
    size=nFD+2;

    /** buffer has ints inside it; safe to use malloc instead of GC_malloc */
    buf=(int*)malloc(sizeof(int)*size);
    do {  /* repeat if interruptted */
      rc = select(nFD+1, &newReadSet, &newWriteSet, NULL,
                  blockMode? NULL: &timeout);
    } while (blockMode && rc < 0 && errno == EINTR);
    /* XXX should really throw exception on rc<0 here.
     * all descriptor sets are undefined in this case and should not be
     * tested. */
    for (j=0; j<=readStruct.maxFD; j++)
      if (FD_ISSET(j, &newReadSet))
        {
          FD_CLR(j, &readStruct.Interest);
          buf[j]=1;
        } else
	  buf[j]=0;

    for(;j<=writeStruct.maxFD;j++)
      buf[j]=0; //Zero out remaining entries
    buf[size-1]=-1; //Put end flag up

    for (j=0; j<=writeStruct.maxFD; j++)
      if (FD_ISSET(j, &newWriteSet))
        {
          FD_CLR(j, &writeStruct.Interest);
          buf[j]=buf[j]+2;
        }

    while (!FD_ISSET(readStruct.maxFD, &readStruct.Interest) 
           && readStruct.maxFD>0)
      readStruct.maxFD--;
    while (!FD_ISSET(writeStruct.maxFD, &writeStruct.Interest) 
           && writeStruct.maxFD>0)
      writeStruct.maxFD--;
    
    return buf;
}



