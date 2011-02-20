#include "POSIXSignalHandler.h"
#include <signal.h>

JNIEXPORT void JNICALL Java_javax_realtime_POSIXSignalHandler_setSignals
(JNIEnv *env, jobject obj) {
  jclass POSIXClass = (*env)->FindClass(env, "POSIXSignalHandler");
  jfieldID SIG_ID;
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGABRT","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGABRT);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGALRM","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGALRM);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGBUS","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGBUS);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGCHLD","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGCHLD);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGCLD","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGCLD);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGCONT","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGCONT);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGFPE","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGFPE);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGHUP","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGHUP);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGILL","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGILL);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGINT","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGINT);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGIO","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGIO);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGIOT","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGIOT);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGKILL","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGKILL);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGPIPE","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGPIPE);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGPOLL","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGPOLL);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGPROF","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGPROF);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGPWR","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGPWR);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGQUIT","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGQUIT);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGSEGV","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGSEGV);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGSTOP","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGSTOP);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGSYS","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGSYS);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGTERM","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGTERM);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGTRAP","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGTRAP);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGTSTP","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGTSTP);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGTTIN","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGTTIN);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGTTOU","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGTTOU);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGURG","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGURG);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGUSR1","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGUSR1);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGUSR2","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGUSR2);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGVTALRM","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGVTALRM);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGWINCH","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGWINCH);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGXCPU","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGXCPU);
  SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGXFSZ","I");
  (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGXFSZ);

  // This signals are not defined on my machine (RedHat Linux 7.3)
  // If they are defined on yours (in /usr/include/asm/signal.h),
  // uncomment these lines
  /*   SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGCANCEL","I"); */
  /*   (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGCANCEL); */
  /*   SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGEMT","I"); */
  /*   (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGEMT); */
  /*   SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGFREEZE","I"); */
  /*   (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGFREEZE); */
  /*   SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGLOST","I"); */
  /*   (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGLOST); */
  /*   SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGLWP","I"); */
  /*   (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGLWP); */
  /*   SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGTHAW","I"); */
  /*   (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGTHAW); */
  /*   SIG_ID = (*env)->GetStaticFieldID(env, POSIXClass, "SIGWAITING","I"); */
  /*   (*env)->SetStaticIntField(env, POSIXClass, SIG_ID, (int)SIGWAITING); */
}
