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
    struct readerList * volatile readerList;
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

#ifdef WCHECKUNOPT
#define WCHECK
#define UNOPT
#endif

#ifdef RCHECK
field_t unusualRead(struct oobj *obj, int idx) { assert(0); }
/* simple check-before-read/write */
static inline field_t read(struct oobj *obj, int idx) {
    field_t val = obj->field[idx];
    if (__builtin_expect(val==FLAG, 0)) return unusualRead(obj,idx);
    else return val;
}
#else /* base case */
static inline field_t read(struct oobj *obj, int idx) {
    field_t val = obj->field[idx];
    return val;
}
#endif

#ifdef WCHECK
void unusualWrite(struct oobj *obj, int idx, field_t val) { assert(0); }
static inline field_t write(struct oobj *obj, int idx, field_t val) {
    if (__builtin_expect(val==FLAG, 0))
	unusualWrite(obj,idx,val); // not quite right
    else {
#if defined(_ARCH_PPC) && !defined(UNOPT)
	struct readerList *rl;
	__asm__ ("0:\n\
                  lwarx %[tmp],0,%[rlt]\n\
                  cmpwi %[tmp],0\n\
                  beq+ 1f\n\
                  b . # xxx: branch to handler\n\
1:                stwcx. %[val],0,%[fld]\n\
                  bne- 0b\n\
2:\n" :
		 [tmp] "=&r"(rl), "=m" (obj->field[idx]) :
		 [fld] "r"(&(obj->field[idx])), [val] "r"(val),
		 [rlt] "r"(&(obj->readerList)), "m" (obj->readerList) :
		 "cr0");
#else /* unoptimized version */
	do { // XXX: this loop can be tighter in assembly
	    if (__builtin_expect(NULL != LL(&(obj->readerList)), 0)) {
		unusualWrite(obj,idx,val);
		break;
	    }
	} while (__builtin_expect(SC(&(obj->field[idx]), val)==0, 0));
#endif
    }
}
#else /* base case */
static inline field_t write(struct oobj *obj, int idx, field_t val) {
    obj->field[idx] = val;
}
#endif

void do_bench(struct oobj *obj, struct transid *tid) __attribute__((noinline));
void do_bench(struct oobj *obj, struct transid *tid) {
#if defined(RWCHECKOPT)
	struct readerList *rl;
	int idx = 0, i=REPETITIONS;
	field_t v1, v2, v3;
	__asm__ ("b 0f\n\
                  .balign 32 # align to 32-byte cache-line boundary\n\
                  nop\n\
                  nop\n\
                  nop\n\
                  nop\n\
                  nop\n\
                  nop\n\
0:                lwarx %[rl],0,%[rlp] # 3{e} (must be oldest to begin)\n\
                  lwz %[v1], 0(%[fld]) # 3:1\n\
1:                ori %[v3], %[v1], 3 # 1\n\
                  addi %[v2], %[v1], 1 # 1\n\
                  cmpwi 1, %[rl],0 # 1\n\
                  cmpwi 2, %[v3], 0xFFFFCACA # 1\n\
                  bne- 1, 0b # xxx: should do copyback\n\
                  beq- 2, 0b # xxx: should do copyback or transactional write\n\
                  stwcx. %[v2],0,%[fld] # 3:1{s} no forwarding from stwcx\n\
                  lwarx %[rl],0,%[rlp]\n\
                  lwz %[v1], 0(%[fld])\n\
                  bne- 0, 1b\n\
                  bdnz 1b\n" :
		 [rl] "=&r"(rl), "=m" (obj->field[idx]), "+c" (i),
		 [v1] "=&r" (v1), [v2] "=&r" (v2), [v3] "=&r" (v3) :
		 [fld] "r"(&(obj->field[idx])),
		 [rlp] "r"(&(obj->readerList)),
		 "m" (obj->readerList), "m" (obj->field[idx]) :
		 "cr0", "cr1", "cr2");
#else
    int i;
    for (i=0; i<REPETITIONS; i++) {
	// xxx optionally create new transaction here.
	field_t v = read(obj, 0);
	v++;
	write(obj, 0, v);
    }
#endif
    assert(read(obj,0)==REPETITIONS);
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
