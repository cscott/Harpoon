#define _GNU_SOURCE 1
#include <jni.h>
#include "NativeIO.h"
#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include "flexthread.h" /* soft syscall mapping for select, etc, in GNU pth */

#define ERROR         -2
#define TRYAGAIN         -3
#define BUFFERFULL       -4
#define OP_READ           1
#define OP_WRITE          2
#define OP_READWRITE      3
/* Timeout for select operations, in microseconds */
#define TIMEOUT          10
#define MAX_FD        20000
#define MAX_QUEUE     20000
/* Scheduler models */
#define MOD_SELECT        0
#define MOD_SIGNAL        1

#include "SelectImp.c"
#include "SignalImp.c"

int schedulerModel=0;


static jfieldID SI_fdObjID = 0; /* The field ID of SocketImpl.fd */
static jfieldID SI_addrID  = 0; /* The field ID of SocketImpl.address */
static jfieldID SI_portID  = 0; /* The field ID of SocketImpl.port */
static jfieldID SI_localportID = 0; /* The field ID of SocketImpl.localport */
static jfieldID IA_addrID  = 0; /* The field ID of InetAddress.address */
static jfieldID IA_familyID= 0; /* The field ID of InetAddress.family */
static jclass IOExcCls  = 0; /* The java/io/IOException class object */
static int inited = 0; /* whether the above variables have been initialized */

static int initializePSI(JNIEnv *env) {
    jclass PSICls, IACls;

    FLEX_MUTEX_LOCK(&init_mutex);
    // other thread may win race to lock and init before we do.
    if (inited) goto done;

    PSICls  = (*env)->FindClass(env, "java/net/PlainSocketImpl");
    if ((*env)->ExceptionOccurred(env)) goto done;
    SI_fdObjID = (*env)->GetFieldID(env, PSICls,
				    "fd","Ljava/io/FileDescriptor;");
    if ((*env)->ExceptionOccurred(env)) goto done;
    SI_addrID  = (*env)->GetFieldID(env, PSICls,
				    "address", "Ljava/net/InetAddress;");
    if ((*env)->ExceptionOccurred(env)) goto done;
    SI_portID  = (*env)->GetFieldID(env, PSICls, "port", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    SI_localportID  = (*env)->GetFieldID(env, PSICls, "localport", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IACls   = (*env)->FindClass(env, "java/net/InetAddress");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IA_addrID  = (*env)->GetFieldID(env, IACls, "address", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IA_familyID= (*env)->GetFieldID(env, IACls, "family", "I");
    if ((*env)->ExceptionOccurred(env)) goto done;
    IOExcCls = (*env)->FindClass(env, "java/io/IOException");
    if ((*env)->ExceptionOccurred(env)) goto done;
    /* make IOExcCls into a global reference for future use */
    IOExcCls = (*env)->NewGlobalRef(env, IOExcCls);

    /* done. */
    inited = 1;
 done:
    FLEX_MUTEX_UNLOCK(&init_mutex);
    return inited;
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_openJNI
  (JNIEnv *env, jobject obj, jstring path, jint oflag)
{ 
  char *str=(char*)((*env)->GetStringUTFChars(env, path, 0));
  int fd=open(str, oflag|O_NONBLOCK);  /* Make sure it's asynchronous! */
  (*env)->ReleaseStringUTFChars(env, path, str);
  if (fd!=-1) return fd;
  return (errno==EAGAIN)?TRYAGAIN:ERROR;
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_openRightsJNI
  (JNIEnv *env, jobject obj, jstring path, jint oflag, jint mode)
{ 
  char *str=(char*)((*env)->GetStringUTFChars(env, path, 0));
  int fd=open(str, oflag|O_NONBLOCK, mode);
  (*env)->ReleaseStringUTFChars(env, path, str);
  if (fd!=-1) return fd;
  return (errno==EAGAIN)?TRYAGAIN:ERROR;
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_closeJNI
  (JNIEnv *env, jobject obj, jint handle)
{ 
  if (!close(handle)) return 0;
  return (errno==EAGAIN)?TRYAGAIN:ERROR;
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_readJNI
  (JNIEnv *env, jobject obj, jint handle, jbyteArray buf, jint ofs, jint size)
{
    jint x;
  jsize len=(*env)->GetArrayLength(env, buf);
  jbyte *_cbuf=(*env)->GetByteArrayElements(env, buf, 0);
  int tmp_errno;
/*      puts("read start..."); */
  do {
  x=read(handle, _cbuf + ofs, size);
  tmp_errno= errno;
  } while (x<0 && tmp_errno==EINTR);
/*    printf("readJNI read %d bytes\n", (int)x);  */
  (*env)->ReleaseByteArrayElements(env, buf, _cbuf, 0);
  if (x>0) return x;                                 /* Normal return */
  if (!x) return EOF;                                /* EOF reached */
  if (tmp_errno==EAGAIN) return TRYAGAIN;
  printf(" -> Failed read: returned: %s\n -> x: %d, errno: %d,  EINTR: %d\n",strerror(errno),x,errno,EINTR);
  return ERROR;
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_writeJNI
  (JNIEnv *env, jobject obj, jint handle, jbyteArray buf, jint ofs, jint size)
{ 
  jsize len=(*env)->GetArrayLength(env, buf);
  jbyte *_cbuf=(*env)->GetByteArrayElements(env, buf, 0);
  jint x=write(handle, _cbuf + ofs, size);
  // printf("writeJNI wrote %d bytes\n", (int)x);
  (*env)->ReleaseByteArrayElements(env, buf, _cbuf, 0);
  if (x>=0) return x;
  return (errno==EAGAIN)?TRYAGAIN:ERROR;
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_getCharJNI
  (JNIEnv *env, jobject obj, jint handle)
{ 
  char c;
  printf("getCharJNI called\n");
  switch (read(handle, &c, 1)) {
    case 1:  return c;      /* Normal return */
    case 0:  return EOF;
    default: return (errno==EAGAIN)?TRYAGAIN:ERROR;
  }
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_putCharJNI
  (JNIEnv *env, jobject obj, jint handle, jint c)
{ 
  char cc=c;
  printf("putCharJNI called\n");
  switch (write(handle, &cc, 1)) {
    case 1:  return c;            /* Normal return */
    case 0:  return TRYAGAIN;
    default: return (errno==EAGAIN)?TRYAGAIN:EOF;
  }
}

JNIEXPORT jboolean JNICALL Java_java_io_NativeIO_canReadJNI
  (JNIEnv *env, jobject obj, jint fd)
{
  fd_set fdset;
  struct timeval timeout;
  timeout.tv_sec=0;
  timeout.tv_usec=TIMEOUT;
  FD_ZERO(&fdset);
  FD_SET(fd, &fdset);
  return (select(fd+1, &fdset, NULL, NULL, &timeout)==1)? 1:0;
}

JNIEXPORT jboolean JNICALL Java_java_io_NativeIO_canWriteJNI
  (JNIEnv *env, jobject obj, jint fd)
{
  fd_set fdset;
  struct timeval timeout;
  timeout.tv_sec=0;
  timeout.tv_usec=TIMEOUT;
  FD_ZERO(&fdset);
  FD_SET(fd, &fdset);
  return (select(fd+1, NULL, &fdset, NULL, &timeout)==1)? 1:0;
}

/* Subscribes a FD to a particular signal immediately after opening the FD.
   In the select-based implementation, it simply makes the fd async */
JNIEXPORT void JNICALL Java_java_io_NativeIO_makeNonBlockJNI
    (JNIEnv *env, jobject obj, jint fd)
{
/*      puts("The NativeIOImp makeNonBlock"); */
    (schedulerModel==MOD_SELECT) ?
	makeNonBlockSEL(fd):
	makeNonBlockSIG(fd);
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_socketJNI
  (JNIEnv *env, jobject obj, jint domain, jint type, jint protocol)
{ 
  int s=socket(domain, type, protocol);
  if (s<0) return ERROR;
  Java_java_io_NativeIO_makeNonBlockJNI(env, obj, s);
  return s;
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_startListenerJNI
  (JNIEnv *env, jobject obj, jint port)
{
  int s, n;
  struct sockaddr_in sin;

  bzero (&sin, sizeof (sin));
  sin.sin_family = AF_INET;
  sin.sin_port = htons (port);
  sin.sin_addr.s_addr = htonl (INADDR_ANY);

  s = socket (AF_INET, SOCK_STREAM, 0);
  if (s < 0)
    { fputs("startListener: Could not create socket", stderr);
      return ERROR;
    }

  n = 1;
  if (setsockopt (s, SOL_SOCKET, SO_REUSEADDR, (char *)&n, sizeof (n)) < 0)
    { fputs("startListener: Could not reuse addresses", stderr);
      close(s);
      return ERROR;
    }
 
  fcntl (s, F_SETFD, 1);
  Java_java_io_NativeIO_makeNonBlockJNI(env, obj, s);

  if (bind(s, (struct sockaddr *) &sin, sizeof(sin))<0)
    { fputs("startListener: TCP port error", stderr);
      close (s);
      return ERROR;
    }
  if (listen(s, 5)<0)
    { fputs("startListener: Damned server wouldn't wanna listen", stderr);
      close (s);
      return ERROR;
    }
  return s;
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_socketAccept
  (JNIEnv *env, jobject obj, jint fd, jobject/*SocketImpl*/ s) {
    struct sockaddr_in sa;
    jobject fdObj, address;
    int rc, sa_size = sizeof(sa);
    if (!inited && !initializePSI(env)) return;
    do {
      rc = accept(fd, (struct sockaddr *) &sa, &sa_size);
    } while (rc<0 && errno==EINTR); /* repeat if interrupted */

    /* Check for error condition */
    if (rc<0) {
      if (errno==EAGAIN)
	return TRYAGAIN;
      (*env)->ThrowNew(env, IOExcCls, strerror(errno));
      return 0;
    }

    /* fill in SocketImpl */
    fdObj = (*env)->GetObjectField(env, s, SI_fdObjID);
    Java_java_io_FileDescriptor_setfd(env, fdObj, rc);
    address = (*env)->GetObjectField(env, s, SI_addrID);
    (*env)->SetIntField(env, address, IA_familyID, sa.sin_family);
    (*env)->SetIntField(env, address, IA_addrID, ntohl(sa.sin_addr.s_addr));
    (*env)->SetIntField(env, s, SI_portID, ntohs(sa.sin_port));
    return 0;
    /* done */
}


JNIEXPORT jint JNICALL Java_java_io_NativeIO_acceptJNI
  (JNIEnv *env, jobject obj, jint handle, jbyteArray IP)
{
  struct sockaddr_in sin;
  int sinlen=sizeof(sin);
  int fd=accept(handle, (struct sockaddr *)&sin, &sinlen);
  char *guest;
  jsize len=(*env)->GetArrayLength(env, IP);
  jbyte *cIP=(*env)->GetByteArrayElements(env, IP, 0);

  if (fd<0 && errno==EWOULDBLOCK) return TRYAGAIN;
  if (fd<0)
    { fputs("Accept: Error", stderr);
      return ERROR;
    }
/*    printf("Connection from %s!\n", inet_ntoa(sin.sin_addr)); */
  fcntl(fd, F_SETFL, fcntl(fd, F_GETFL)|O_NONBLOCK);
  cIP[0]=15; cIP[1]=20; cIP[2]=25; cIP[3]=30;
  (*env)->ReleaseByteArrayElements(env, IP, cIP, 0);
  return fd;
}

JNIEXPORT jboolean JNICALL Java_java_io_NativeIO_canAcceptJNI
  (JNIEnv *env, jobject obj, jint handle)
{
  fd_set fdset;
  struct timeval timeout;
  int res;
  // puts("Entered canAcceptJNI");
  timeout.tv_sec=0;
  timeout.tv_usec=TIMEOUT;
  FD_ZERO(&fdset);
  FD_SET(handle, &fdset);
  res = select(handle+1, &fdset, NULL, NULL, &timeout) >0;
  //printf("canAccept is about to return %d\n", res);
  return res;
}

/************************* SELECT STUFF ***********************************/

JNIEXPORT jintArray JNICALL Java_java_io_NativeIO_selectJNI
  (JNIEnv *env, jobject obj, jintArray readFD, jintArray writeFD)
{
  struct timeval timeout;
  jsize readNo=(*env)->GetArrayLength(env, readFD),
        writeNo=(*env)->GetArrayLength(env, writeFD);
  jintArray result=(jintArray)((*env)->NewIntArray(env,
						   readNo+writeNo+2));
  jsize len3=(*env)->GetArrayLength(env, result);
  
  jint *creadFD=(*env)->GetIntArrayElements(env, readFD, 0),
       *cwriteFD=(*env)->GetIntArrayElements(env, writeFD, 0);
  jint *cresult=(*env)->GetIntArrayElements(env, result, 0);
  fd_set ReadFDSet, WriteFDSet;
  int i, k, maxFD=0;;

  puts("selectJNI called");

  timeout.tv_sec=0;
  timeout.tv_usec=TIMEOUT;

  FD_ZERO(&ReadFDSet); FD_ZERO(&WriteFDSet);
  for (i=0; i<readNo; i++)
    {
      FD_SET(creadFD[i], &ReadFDSet);
      if (creadFD[i]>maxFD) maxFD=creadFD[i];
    }
  for (i=0; i<writeNo; i++)
    {
      FD_SET(cwriteFD[i], &WriteFDSet);
      if (cwriteFD[i]>maxFD) maxFD=cwriteFD[i];
    }
  select(maxFD+1, &ReadFDSet, &WriteFDSet, NULL, &timeout);
  k=0;
  for (i=0; i<readNo; i++)
    if (FD_ISSET(creadFD[i], &ReadFDSet))
      cresult[k++]=i;
  cresult[k++]=-1;
  for (i=0; i<writeNo; i++)
    if (FD_ISSET(cwriteFD[i], &WriteFDSet))
      cresult[k++]=i;
  cresult[k++]=-1;
  (*env)->ReleaseIntArrayElements(env, readFD, creadFD, 0);
  (*env)->ReleaseIntArrayElements(env, writeFD, cwriteFD, 0);
  (*env)->ReleaseIntArrayElements(env, result, cresult, 0);
  return result;
}

/*********************** SCHEDULER STUFF ***********************/

/* Initializes all the internal data of the system */
JNIEXPORT void JNICALL Java_java_io_NativeIO_initScheduler
  (JNIEnv *env, jclass obj, jint model)
{
  schedulerModel=model;
  (model==MOD_SELECT) ? initDataSEL() : initDataSIG();
}

JNIEXPORT void JNICALL Java_java_io_NativeIO_initScheduler_00024_00024initcheck
  (JNIEnv *env, jclass obj, jint model)
{
  schedulerModel=model;
  (model==MOD_SELECT) ? initDataSEL() : initDataSIG();
}

/* Registers two collections of FDs for reading/writing */
/*  JNIEXPORT void JNICALL Java_java_io_NativeIO_register */
/*      (JNIEnv *env, jobject obj, jintArray readFD, jintArray writeFD) */
/*  {  */
/*    (schedulerModel==MOD_SELECT) ? */
/*      registerSEL(env, readFD, writeFD): */
/*      registerSIG(env, readFD, writeFD); */
/*  } */

/* Registers one FD for reading */
JNIEXPORT void JNICALL Java_java_io_NativeIO_registerRead
    (JNIEnv *env, jobject obj, jint fd)
{ 
  (schedulerModel==MOD_SELECT) ?
    registerReadSEL(fd):
    registerReadSIG(fd);
}

/* Registers one FD for writing */
JNIEXPORT void JNICALL Java_java_io_NativeIO_registerWrite
    (JNIEnv *env, jobject obj, jint fd)
{ 
  (schedulerModel==MOD_SELECT) ?
    registerWriteSEL(fd):
    registerWriteSIG(fd);
}

/* Unregisters one FD for reading */
JNIEXPORT void JNICALL Java_java_io_NativeIO_unregisterRead
    (JNIEnv *env, jobject obj, jint fd)
{ 
  (schedulerModel==MOD_SELECT) ?
    unregisterReadSEL(fd):
    unregisterReadSIG(fd);
}

/* Unregisters one FD for writing */
JNIEXPORT void JNICALL Java_java_io_NativeIO_unregisterWrite
    (JNIEnv *env, jobject obj, jint fd)
{ 
  (schedulerModel==MOD_SELECT) ?
    unregisterWriteSEL(fd):
    unregisterWriteSIG(fd);
}

/* Finds an FD ready for immediate write */
/*  JNIEXPORT jint JNICALL Java_java_io_NativeIO_getReadFD */
/*      (JNIEnv *env, jobject obj) */
/*  { */
/*    return (schedulerModel==MOD_SELECT) ? */
/*      getReadFDSEL(): */
/*      getReadFDSIG(); */
/*  } */

/* Finds an FD ready for immediate write */
/*  JNIEXPORT jint JNICALL Java_java_io_NativeIO_getWriteFD */
/*      (JNIEnv *env, jobject obj) */
/*  { */
/*    return (schedulerModel==MOD_SELECT) ? */
/*      getWriteFDSEL(): */
/*      getWriteFDSIG(); */
/*  } */

/* Get a whole bunch of FDs ready for immediate write */
/*  JNIEXPORT jint JNICALL Java_java_io_NativeIO_getReadFDs */
/*      (JNIEnv *env, jobject obj, jintArray readFD, jint atMost) */
/*  { */
/*    return (schedulerModel==MOD_SELECT) ? */
/*      getReadFDsSEL(env, readFD, atMost): */
/*      getReadFDsSIG(env, readFD, atMost); */
/*  } */

/* Get a whole bunch of FDs ready for immediate write */
/*  JNIEXPORT jint JNICALL Java_java_io_NativeIO_getWriteFDs */
/*      (JNIEnv *env, jobject obj, jintArray writeFD, jint atMost) */
/*  { */
/*    return (schedulerModel==MOD_SELECT) ? */
/*      getWriteFDsSEL(env, writeFD, atMost): */
/*      getWriteFDsSIG(env, writeFD, atMost); */
/*  } */

/*  JNIEXPORT jintArray JNICALL Java_java_io_NativeIO_communicate */
/*    (JNIEnv *env, jobject obj, jintArray readSet, jintArray writeSet) */
/*  { */
/*    return (schedulerModel==MOD_SELECT) ? */
/*      communicateSEL(env, readSet, writeSet): */
/*      communicateSIG(env, readSet, writeSet);     */
/*  } */

JNIEXPORT jint JNICALL Java_java_io_NativeIO_getFDs
  (JNIEnv *env, jobject obj, jintArray array)
{
  //  return (schedulerModel==MOD_SELECT) ?
    return getFDsSEL(env,1, array);
      // getFDsSIG(env);    
}

JNIEXPORT jint JNICALL Java_java_io_NativeIO_getFDsSmart
  (JNIEnv *env, jobject obj, jboolean blockMode, jintArray array)
{
  //  return (schedulerModel==MOD_SELECT) ?
    return getFDsSEL(env,blockMode,array);
    // getFDsSIG(env);    
}
