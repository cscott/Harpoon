#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "RoleInference.h"
#include "CalculateDominators.h"
#include "Role.h"
#include "Method.h"
#include "Effects.h"
#include "dot.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

void dccfree(struct dynamiccallmethod *dcm) {
  free(dcm);
}

void recordentry(struct heap_state *heap, struct methodname *methodname) {
  struct dynamiccallmethod * m=(struct dynamiccallmethod *) calloc(1, sizeof(struct dynamiccallmethod));
  m->methodname=methodname;
  m->status=0;
  puttable(heap->dynamiccallchain, heap->currentmethodcount++, m);
}

void recordexit(struct heap_state *heap) {
  struct dynamiccallmethod * m=(struct dynamiccallmethod *) calloc(1, sizeof(struct dynamiccallmethod));
  m->methodname=heap->methodlist->methodname;
  if (heap->methodlist->caller!=NULL) {
    m->methodnameto=heap->methodlist->caller->methodname;
  }
  m->status=1;
  puttable(heap->dynamiccallchain, heap->currentmethodcount++, m);
}
