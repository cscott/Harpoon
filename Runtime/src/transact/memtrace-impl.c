/** Memory tracing routines. */

// VALUETYPE and VALUENAME must be defined unless NO_VALUETYPE is.

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */
// could define DECL to be "extern inline"
#define DECL

////////////////////////////////////////////////////////////////////////
//                         prototypes
#if defined(IN_MEMTRACE_HEADER)
#if defined(NO_VALUETYPE)
struct oobj;
void EXACT_XACTION_BEGIN(void);
void EXACT_XACTION_END(void);
#else /* !NO_VALUETYPE */
void TA(EXACT_traceRead)(struct oobj *obj, int offset, int istran);
void TA(EXACT_traceWrite)(struct oobj *obj, int offset, int istran);
#endif /* !NO_VALUETYPE */
#endif /* IN_MEMTRACE_HEADER */
//                         end prototypes
////////////////////////////////////////////////////////////////////////

#if !defined(IN_MEMTRACE_HEADER)
#if defined(NO_VALUETYPE)

static FILE *trace=NULL;

static void trace_close() {
  assert(trace!=NULL);
  fflush(trace);
  pclose(trace);
}
static void trace_init() {
  char *trace_name, *bzip_cmd = "gzip > ";
  assert(trace==NULL);

  trace_name = getenv("MEMTRACE_FILE");
  if (trace_name==NULL) trace_name="memtrace.gz";
  {
    char buf[strlen(bzip_cmd)+strlen(trace_name)+1];
    strcpy(buf, bzip_cmd);
    strcat(buf, trace_name);

    trace = popen(buf, "w");
  }
  atexit(trace_close);
}

#ifdef COUNT_OPS
  static int tx = 0; /* counts the number of transactional ops */
#endif

void EXACT_XACTION_BEGIN(void) {
#ifdef COUNT_OPS
  tx = 0;
#else
  if (trace==NULL) trace_init();
  fprintf(trace, "x\n");
#endif
}
void EXACT_XACTION_END(void) {
  if (trace==NULL) trace_init();
#ifdef COUNT_OPS
  fprintf(trace, "%d\n", tx);
#else
  fprintf(trace, "X\n");
#endif
}
#else /* !NO_VALUETYPE */
void TA(EXACT_traceRead)(struct oobj *obj, int offset, int istran) {
  VALUETYPE *ptr = (VALUETYPE *)(FIELDBASE(obj) + offset);
#ifdef COUNT_OPS
  if (istran) tx++;
#else
  if (trace==NULL) trace_init();
  fprintf(trace, "%c %p %d %d\n", istran ? 'r' : 'R', ptr, (int)sizeof(*ptr),
	  (int)FNI_ObjectSize(obj));
#endif
}
void TA(EXACT_traceWrite)(struct oobj *obj, int offset, int istran) {
  VALUETYPE *ptr = (VALUETYPE *)(FIELDBASE(obj) + offset);
#ifdef COUNT_OPS
  if (istran) tx++;
#else
  if (trace==NULL) trace_init();
  fprintf(trace, "%c %p %d %d\n", istran ? 'w' : 'W', ptr, (int)sizeof(*ptr),
	  (int)FNI_ObjectSize(obj));
#endif
}
#endif /* !NO_VALUETYPE */
#endif /* !IN_MEMTRACE_HEADER */

/* clean up after ourselves */
#include "transact/preproc.h"
#undef DECL
