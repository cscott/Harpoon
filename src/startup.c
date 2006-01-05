#include <signal.h> /* for signal(), to ignore SIGPIPE */
#include <string.h> /* for strcmp, strchr */
#include <jni.h>
#include <jni-private.h>
#include "java.lang/thread.h"
#include "java.lang/properties.h" /*to define property_list, extra_properties*/
#include "flexthread.h"
#ifdef WITH_EXEC_ENV_ARGS
# include <getopt.h> /* for getopt */
#endif
#include <assert.h>
#ifdef WITH_PRECISE_GC
# include "jni-gc.h"
# ifdef WITH_GENERATIONAL_GC
#  include "gc/omit_gc_timer.h"
# endif
#endif
#ifdef WITH_GC_STATS
#include "realtime/GCstats.h"
#endif
#ifdef WITH_REALTIME_JAVA
#include "realtime/RTJmalloc.h"
#endif
#ifdef WITH_REALTIME_THREADS_TIMESYS
#include "realtime/timesys.h"
#endif

#define CHECK_EXCEPTIONS(env) \
if ((*env)->ExceptionOccurred(env)){ (*env)->ExceptionDescribe(env); exit(1); }


static int max_heap_size=0;  /* max heap size (in Mbytes) */
struct property_list *extra_properties = NULL;

#ifdef WITH_EXEC_ENV_ARGS
void process_command_line_options(int *pargc, char ***pargv) {  
  /* Note that getopt accepts '--' to indicate "no more options" */
  enum option_codes { MAX_HEAP_SIZE_OPTION=256, PROPERTY_OPTION };
  static struct option long_options[] = {
    {"Xmx", 1, NULL, MAX_HEAP_SIZE_OPTION},
    {"D", 1, NULL, PROPERTY_OPTION}, /* -D property definitions */
    // (must initialize 'Properties' & intern strings first)
    {NULL, 0, NULL, 0}
  };

  /* prevent unwanted error messages */
  int old_opterr = opterr;
  opterr = 0; 

  while(1) {
    int longopt = 0;
    int option = getopt_long_only(*pargc, *pargv, ":", long_options, &longopt);
    // if no more options or unrecognized options, get out of the loop
    if(option == -1) break;
    if(option == '?') {
	/* last seen token was NOT one of our options */
	optind--;
	break;
    }
    switch(option) {
    case MAX_HEAP_SIZE_OPTION:
      sscanf(optarg, "%d", &max_heap_size);
      fprintf(stderr, "MAX HEAP SIZE = %dMbytes\n", max_heap_size);
      break;
    case PROPERTY_OPTION: {
      struct property_list *np;
      char *equals = strchr(optarg, '=');
      if (equals==NULL) {
	fprintf(stderr, "Missing value for property definition %s\n", optarg);
	break;
      }
      *equals='\0';
      np = malloc(sizeof(*np));
      np->key = strdup(optarg);
      np->value = strdup(equals+1);
      np->next = extra_properties;
      extra_properties=np;
      break;
    }
    case ':':
      fprintf(stderr, "Missing argument for option %s\n",
	      long_options[longopt].name);
      break;
    }
  }  
  opterr = old_opterr; /* re-enable error messages */

  /* eliminate the options that we've parsed from argv
     the rest of the program behaves as if they never existed */
  if(optind > 1) {
      /* optind is the index in argv of the first argument that is NOT an
	 option (if no option is present, optind = 1 because argv[0] is
	 the name of the executable, not an argument). we update argc and
	 argv such that the rest of the program doesn't see the already
	 processed options */
      *pargc -= (optind-1);
      (*pargv)[optind-1] = (*pargv)[0];
      *pargv += (optind-1);
  }
}
#endif

char *name_of_binary;

int main(int argc, char *argv[]) {
  int top_of_stack; /* special variable holding the top-of-stack position */
  char *firstclasses[] = {
#ifdef CLASSPATH_VERSION
    /* These are pre-initializations for String.intern().  They may well
     * have to change if you try to compile CLASSPATH w/o WITH_INIT_CHECK.
     * I wouldn't actually recommend doing that. */
    "java/lang/String", "java/lang/ref/Reference", "java/util/WeakHashMap",
# if 0 /* (CLASSPATH_VERSION >= 0.06) */
    /* this #if test is not yet functional; when it is made so, work-around
     * code below here can/ought be removed. */
    "java/lang/VMString",
# endif
#else
    "java/util/Properties", "java/io/FileDescriptor", "java/lang/System", 
    "java/io/BufferedWriter",
#endif
    NULL
  };
  JNIEnv *env;
  jclass cls;
  jmethodID mid;
  jobject args;
  jclass thrCls;
  jobject mainthread;
  jthrowable threadexc;
  char **namep;
  int st=0;
  int i;

#ifdef WITH_PREALLOC_OPT
  jclass    preallocMemCls;
  jmethodID preallocMemInitFields;
#endif
  
#ifdef WITH_REALTIME_THREADS
  jmethodID getCurrentThreadMethod;
#endif

#ifdef WITH_EXEC_ENV_ARGS
  process_command_line_options(&argc, &argv);
#endif

#ifdef WITH_REALTIME_THREADS
  StopSwitching(); //turn off thread switching to start
#endif
#ifdef WITH_GENERATIONAL_GC
  init_timer();
#endif

  /* set up for bfd */
  name_of_binary = argv[0];

  /* random package initialization */
#ifdef WITH_PTH_THREADS
  pth_init();
#endif
#ifdef WITH_USER_THREADS
  inituser(&top_of_stack);
#endif
  /* ignore SIGPIPE; we look at errno to handle this condition */
  signal(SIGPIPE, SIG_IGN);

  /* set up JNIEnv structures. */
  FNI_InitJNIEnv();
  env = FNI_CreateJNIEnv();
  ((struct FNI_Thread_State *)(env))->stack_top = &top_of_stack;
  ((struct FNI_Thread_State *)(env))->is_alive = JNI_TRUE;
  /* setup GC */
#ifdef WITH_PRECISE_GC
  precise_gc_init();
#endif


  if(max_heap_size != 0) {
#ifdef BDW_CONSERVATIVE_GC
    GC_set_max_heap_size(max_heap_size*1024*1024);
#else
    fprintf(stderr, "max heap size can currently be set only for BDW");
    exit(1);
#endif
  }

  /* initialize Realtime Java extensions */
  /* setup main thread info. */
#ifdef WITH_GC_STATS
  setup_GC_stats();
#endif

#ifdef WITH_PREALLOC_OPT
  preallocMemCls  = 
      (*env)->FindClass(env, "java/lang/Object");
  CHECK_EXCEPTIONS(env);
  preallocMemInitFields = 
      (*env)->GetStaticMethodID(env, preallocMemCls, "initFields", "()V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod
      (env, preallocMemCls, preallocMemInitFields);
  CHECK_EXCEPTIONS(env);
#endif

#if defined(WITH_REALTIME_JAVA) || defined(WITH_FAKE_SCOPES) 
  RTJ_preinit();
#endif
  FNI_java_lang_Thread_setupMain(env);
#if defined(WITH_REALTIME_JAVA) || defined(WITH_FAKE_SCOPES) 
  RTJ_init();
#endif
#ifdef WITH_REALTIME_THREADS
  thrCls  = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
  CHECK_EXCEPTIONS(env);
  //need to get the current RealtimeThread for Realtime Java
  getCurrentThreadMethod = (*env)->GetStaticMethodID
    (env, thrCls, "currentRealtimeThread","()Ljavax/realtime/RealtimeThread;");
  CHECK_EXCEPTIONS(env);
  mainthread = (*env)->CallStaticObjectMethod
    (env, thrCls, getCurrentThreadMethod);
  CHECK_EXCEPTIONS(env);
#else
  thrCls  = (*env)->FindClass(env, "java/lang/Thread");
  CHECK_EXCEPTIONS(env);

  mainthread = fni_thread_currentThread(env);
  CHECK_EXCEPTIONS(env);
#endif

  /* initialize pre-System.initializeSystemClass()/String.intern()
     initializers. */
  for (i=0; firstclasses[i]!=NULL; i++) {
    cls = (*env)->FindClass(env, firstclasses[i]);
#ifdef CLASSPATH_VERSION /* really, WITH_MINILIB */
    if ((*env)->ExceptionOccurred(env)) {
      /* minilib doesn't have all of these; just ignore the missing ones. */
      (*env)->ExceptionClear(env);
      continue;
    }
#else
    CHECK_EXCEPTIONS(env);
#endif /* WITH_MINILIB */
    mid = (*env)->GetStaticMethodID(env, cls, "<clinit>","()V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallStaticVoidMethod(env, cls, mid);
    CHECK_EXCEPTIONS(env);
    (*env)->DeleteLocalRef(env, cls);
  }

#ifdef CLASSPATH_VERSION
  /* classpath need only initialize the String.intern() table. */
  /* --- this code can be deleted when CLASSPATH_VERSION is reliable --- */
  cls = (*env)->FindClass(env, "java/lang/VMString");
  if ((*env)->ExceptionOccurred(env) != NULL) {
    /* VMString only exists on classpath >= 0.06 */
    (*env)->ExceptionClear(env);
  } else {
    /* If VMString exists, initialize it. */
    mid = (*env)->GetStaticMethodID(env, cls, "<clinit>","()V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallStaticVoidMethod(env, cls, mid);
    CHECK_EXCEPTIONS(env);
    (*env)->DeleteLocalRef(env, cls);
  }
  /* --- end CLASSPATH_VERSION brokenness work-around --- */
  cls = (*env)->FindClass(env, "java/lang/String");
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetMethodID(env, cls, "intern","()Ljava/lang/String;");
  CHECK_EXCEPTIONS(env);
  {
    char *p;
    int strsize = FNI_GetClassInfo(cls)->claz->size;
    (*env)->DeleteLocalRef(env, cls);
    for (p = (char*) &string_constants_start; 
	 p < (char*) &string_constants_end;
	 p += strsize) {
      jobject s1, s2;
      s1 = FNI_WRAP((struct oobj *)p);
      CHECK_EXCEPTIONS(env);
      s2 = (*env)->CallObjectMethod(env, s1, mid);
      CHECK_EXCEPTIONS(env);
      assert((*env)->IsSameObject(env, s1, s2));
      (*env)->DeleteLocalRef(env, s2);
      (*env)->DeleteLocalRef(env, s1);
    }
  }
#else
  /* Execute java.lang.System.initializeSystemClass */
  cls = (*env)->FindClass(env, "java/lang/System");
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "initializeSystemClass","()V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid);
  CHECK_EXCEPTIONS(env);
  (*env)->DeleteLocalRef(env, cls);
#endif

  /* Execute rest of static initializers, in proper order. */
  for (namep=FNI_static_inits; *namep!=NULL; namep++) {
    for (i=0; firstclasses[i]!=NULL; i++)
      if (strcmp(*namep, firstclasses[i])==0)
	goto skip;
    cls = (*env)->FindClass(env, *namep);
    CHECK_EXCEPTIONS(env);

    //printf("Getting READY FOR %s\n", FNI_GetClassInfo(cls)->name);

    mid = (*env)->GetStaticMethodID(env, cls, "<clinit>","()V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallStaticVoidMethod(env, cls, mid);
    CHECK_EXCEPTIONS(env);
    (*env)->DeleteLocalRef(env, cls);
  skip:;
  }

#if defined(WITH_INIT_CHECK) && defined(WITH_THREADS)
  initDone = 1;
  //printf("initialization completed\n"); fflush(NULL);

  /* start up any threads that were deferred during static initialization */
  fni_thread_startDeferredThreads(env);
#endif /* WITH_INIT_CHECK && WITH_THREADS */

  /* Wrap argv strings */
  cls = (*env)->FindClass(env, "java/lang/String");
  CHECK_EXCEPTIONS(env);
  args = (*env)->NewObjectArray(env, argc-1, cls, NULL);
  CHECK_EXCEPTIONS(env);
  for (i=1; i<argc; i++) {
    jstring str = (*env)->NewStringUTF(env, argv[i]);
    CHECK_EXCEPTIONS(env);
    (*env)->SetObjectArrayElement(env, args, i-1, str);
    CHECK_EXCEPTIONS(env);
    (*env)->DeleteLocalRef(env, str);
  }
  (*env)->DeleteLocalRef(env, cls);

#ifdef WITH_STATISTICS
  /* print out collected statistics */
  { void print_statistics(void); print_statistics(); }
#endif

#ifdef WITH_REALTIME_THREADS
  start_realtime_threads(env, mainthread, args, thrCls);
#else /* !WITH_REALTIME_THREADS */
  /* Execute main() method. */
  cls = (*env)->FindClass(env, FNI_javamain);
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid, args);
#endif /* !WITH_REALTIME_THREADS */

#ifdef WITH_GC_STATS
  print_GC_stats();
#endif

  // handle uncaught exception in main thread. (see also thread_startup)
  if ( (threadexc = (*env)->ExceptionOccurred(env)) != NULL) {
    // call thread.getThreadGroup().uncaughtException(thread, exception)
#ifndef WITH_REALTIME_JAVA /* ThreadGroups are explicitly disallowed in RTJ */
    jclass thrGrpCls;
    jobject threadgroup;
    jmethodID gettgID, uncaughtID;
#else
    (*env)->ExceptionDescribe(env);
#endif
    (*env)->ExceptionClear(env); /* clear the thread's exception */
    st=1; /* main() exit status will be non-zero. */
#ifndef WITH_REALTIME_JAVA
    // Thread.currentThread().getThreadGroup()...
    gettgID = (*env)->GetMethodID(env, thrCls, "getThreadGroup",
				  "()Ljava/lang/ThreadGroup;");
    CHECK_EXCEPTIONS(env);
    threadgroup = (*env)->CallObjectMethod(env, mainthread, gettgID);
    CHECK_EXCEPTIONS(env);
    // ...uncaughtException(Thread.currentThread(), exception)
    thrGrpCls  = (*env)->FindClass(env, "java/lang/ThreadGroup");
    CHECK_EXCEPTIONS(env);
    uncaughtID = (*env)->GetMethodID(env, thrGrpCls, "uncaughtException",
				 "(Ljava/lang/Thread;Ljava/lang/Throwable;)V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallVoidMethod(env, threadgroup, uncaughtID, mainthread, threadexc);
    CHECK_EXCEPTIONS(env); // catch exception thrown by uncaughtException() ?
    (*env)->DeleteLocalRef(env, threadgroup);
    (*env)->DeleteLocalRef(env, threadexc);
#endif
  }
  (*env)->DeleteLocalRef(env, args);
  (*env)->DeleteLocalRef(env, cls);
  // call Thread.currentThread().exit() at this point.
#ifdef WITH_REALTIME_THREADS
  setupEnv(env); // make sure main still has access to env for exit
#endif
#ifdef CLASSPATH_VERSION
  {/* remove main thread from thread group using ThreadGroup.removeThread() */
#ifndef WITH_REALTIME_JAVA /* ThreadGroups are explicitly disallowed in RTJ */
    // call thread.getThreadGroup().removeThread(thread)
    jclass thrGrpCls;
    jobject threadgroup;
    jmethodID gettgID, removeThreadID;
    // Thread.currentThread().getThreadGroup()...
    gettgID = (*env)->GetMethodID(env, thrCls, "getThreadGroup",
				  "()Ljava/lang/ThreadGroup;");
    CHECK_EXCEPTIONS(env);
    threadgroup = (*env)->CallObjectMethod(env, mainthread, gettgID);
    CHECK_EXCEPTIONS(env);
    // ...removeThread(Thread.currentThread())
    thrGrpCls  = (*env)->FindClass(env, "java/lang/ThreadGroup");
    CHECK_EXCEPTIONS(env);
    removeThreadID = (*env)->GetMethodID(env, thrGrpCls, "removeThread",
					 "(Ljava/lang/Thread;)V");
    CHECK_EXCEPTIONS(env);
    if (threadgroup) { /* threadgroup can be NULL when we disable them */
      (*env)->CallVoidMethod(env, threadgroup, removeThreadID, mainthread);
      CHECK_EXCEPTIONS(env); // catch exception thrown?
      (*env)->DeleteLocalRef(env, threadgroup);
    }
#endif
  }
#else /* in the sunjdk, the Thread.exit() method does this for us. */
 {
  jmethodID exitID = (*env)->GetMethodID(env, thrCls, "exit", "()V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallNonvirtualVoidMethod(env, mainthread, thrCls, exitID);
  CHECK_EXCEPTIONS(env);
 }
#endif /* !CLASSPATH_VERSION */
  (*env)->DeleteLocalRef(env, thrCls);

  /* main thread is dead now. */
  ((struct FNI_Thread_State *)(env))->is_alive = JNI_FALSE;
  FNI_SetJNIData(env, mainthread, NULL, NULL); /* clear the env from the obj */
  /* Notify others that it's dead (before we deallocate the thread object!). */
  FNI_MonitorEnter(env, mainthread);
  FNI_MonitorNotify(env, mainthread, JNI_TRUE);
  FNI_MonitorExit(env, mainthread);
  // wait for all threads to finish up.
  FNI_java_lang_Thread_finishMain(env);
#ifdef WITH_REALTIME_THREADS
  destroyEnv();
#endif

#ifdef WITH_REALTIME_THREADS_TIMESYS
  destroyRES();
#endif

#ifdef WITH_STATISTICS
  /* print out collected statistics */
  { void print_statistics(void); print_statistics(); }
#endif

  return st;
}

