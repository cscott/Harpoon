/** Memory tracing routines. */

// VALUETYPE and VALUENAME must be defined unless NO_VALUETYPE is.

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */
// could define DECL to be "extern inline"
#define DECL

////////////////////////////////////////////////////////////////////////
//                         prototypes
#if defined(IN_VERSIONS_HEADER)
#if defined(NO_VALUETYPE)
void EXACT_XACTION_BEGIN(void);
void EXACT_XACTION_END(void);
#else /* !NO_VALUETYPE */
void TA(EXACT_traceRead)(struct oobj *obj, int offset, int istran);
void TA(EXACT_traceWrite)(struct oobj *obj, int offset, int istran);
#endif /* !NO_VALUETYPE */
#endif /* IN_VERSIONS_HEADER */
//                         end prototypes
////////////////////////////////////////////////////////////////////////

#if !defined(IN_VERSIONS_HEADER)
#if defined(NO_VALUETYPE)

static FILE *trace=NULL;

static void trace_close() {
  assert(trace!=NULL);
  fflush(trace);
  pclose(trace);
}
static void trace_init() {
  char *trace_name, *bzip_cmd = "bzip2 > ", *cmd;
  assert(trace==NULL);

  trace_name = getenv("MEMTRACE_FILE");
  if (trace_name==NULL) trace_name="memtrace.out";
  {
    char buf[strlen(bzip_cmd)+strlen(trace_name)+1];
    strcpy(buf, bzip_cmd);
    strcat(buf, trace_name);

    trace = popen(buf, "w");
  }
  atexit(trace_close);
}

void EXACT_XACTION_BEGIN(void) {
  if (trace==NULL) trace_init();
  fprintf(trace, "x\n");
}
void EXACT_XACTION_END(void) {
  if (trace==NULL) trace_init();
  fprintf(trace, "X\n");
}
#else /* !NO_VALUETYPE */
void TA(EXACT_traceRead)(struct oobj *obj, int offset, int istran) {
  VALUETYPE *ptr = FIELDBASE(obj) + offset;
  if (trace==NULL) trace_init();
  fprintf(trace, "%c %p %d\n", istran ? 'r' : 'R', ptr, sizeof(*ptr));
}
void TA(EXACT_traceWrite)(struct oobj *obj, int offset, int istran) {
  VALUETYPE *ptr = FIELDBASE(obj) + offset;
  if (trace==NULL) trace_init();
  fprintf(trace, "%c %p %d\n", istran ? 'w' : 'W', ptr, sizeof(*ptr));
}
#endif /* !NO_VALUETYPE */
#endif /* !IN_VERSIONS_HEADER */

/* clean up after ourselves */
#include "transact/preproc.h"
#undef DECL
