#include <assert.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#ifdef _ARCH_PPC
# include "llsc-ppc32.h"
#else
# include "llsc-unimpl.h"
#endif

#define NUM_FIELDS 5
#define REPETITIONS 1000000000
#define FLAG 0xFFFFCACA

typedef int32_t field_t;

struct oobj {
    struct version *version;
    struct readerList *readerList;
    volatile field_t field[NUM_FIELDS];
};
struct version {
    struct transid *transid;
    struct version *next;
    field_t field[NUM_FIELDS];
};
struct readerList {
    struct transid *transid;
    struct readerList *next;
};
struct transid {
    enum { WAITING, COMMITTED, ABORTED } status;
};
#ifdef RWCHECK
#define RCHECK
#define WCHECK
#endif

#ifdef RCHECK
field_t readT(struct transid *tid, struct oobj *obj, int idx) { assert(0); }
/* simple check-before-read/write */
static inline field_t read(struct transid *tid, struct oobj *obj, int idx) {
    field_t val = obj->field[idx];
    if (__builtin_expect(val==FLAG, 0)) return readT(tid,obj,idx);
    else return val;
}
#else /* base case */
static inline field_t read(struct transid *tid, struct oobj *obj, int idx) {
    field_t val = obj->field[idx];
    return val;
}
#endif

#ifdef WCHECK
void writeT(struct transid *tid, struct oobj *obj, int idx, field_t val) { assert(0); }
static inline field_t write(struct transid *tid, struct oobj *obj, int idx, field_t val) {
    if (__builtin_expect(val==FLAG, 0) ||
	__builtin_expect(NULL != LL(&(obj->readerList)), 0))
      writeT(tid,obj,idx,val);
    else if (__builtin_expect(SC(&(obj->field[idx]), val)==0, 0))
      /* XXX: SC failure is reasonably common.  Recode this inner loop 
       * (not including val check: just the LL, compare, SC, and loop)
       * in assembly. */
      printf("%d\n", (int)val);
}
#else /* base case */
static inline field_t write(struct transid *tid, struct oobj *obj, int idx, field_t val) {
    obj->field[idx] = val;
}
#endif

void do_bench(struct oobj *obj, struct transid *tid) __attribute__((noinline));
void do_bench(struct oobj *obj, struct transid *tid) {
    int i;
    for (i=0; i<REPETITIONS; i++) {
	// xxx optionally create new transaction here.
	field_t v = read(tid, obj, 0);
	v++;
	write(tid, obj, 0, v);
    }
    assert(read(tid,obj,0)==REPETITIONS);
}
/* make these variables extern, or else the compiler will toss updates to them
 * because they do not escape. */
int main(int argc, char **argv) {
struct transid m_tid = { .status = WAITING };
struct oobj m_obj = { .version = NULL, .readerList = NULL,
		      .field = { [0 ... (NUM_FIELDS-1)] = 0 } };

    // xxx: get time info
    do_bench(&m_obj, &m_tid);
    // xxx: print timing
    return 0;
}
