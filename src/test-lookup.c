#include "constants.h"
#include "asm/stack.h"

/* value to be thrown is in r0, so be careful with that.
 * return address is in [fp, #-4] */

/*
asm("1:	.word _fixup_start\n"
    "	.word _fixup_end-8\n"
    "_lookup:\n"
    "   ldr r1, [fp, #-4] @ key (== return address of caller)\n"
    "	ldr r2, 1b    @ left\n"
    "	ldr r3, 1b+4  @ right\n"
    "	mov r6, #0 @ a zero constant's useful.\n"
    "2:	cmp r2, r3    @ while (r >= l) ...\n"
    "   add r4, r2, r3 @ x = (l + r) / 2\n"
    "   mov r4, r4, lsr #4\n"
    "   movhi pc, #0  @  [die horrible death if no match found]\n"
    "   ldr r5, [r6, r4, lsl #3] @ load x->label\n"
    "	mov r4, r4, lsl #3\n"
    "	cmp r1, r5   @ compare key with x->label\n"
    "   subcc r3, r4, #8 @ if (key < x->label) r = x-1;\n"
    "   addhi r2, r4, #8 @ else l = x+1\n"
    "	bne 2b @ loop if not found.\n"
    "	ldr r4, [r4, #4]\n"
    "	str r4, [fp, #-4]\n"
    "	mov pc, lr\n");
    */

typedef struct { void * label; void * target; } pair_t;
void * binsearch(void *retaddr) {
  pair_t *l=(pair_t*)&fixup_start, *r=((pair_t*)&fixup_end)-1;
  while (r >= l) {
    pair_t *x = l + ((r-l)/2); /* (l+r)/2 */
    if (retaddr <  x->label) r = x-1; else l = x+1;
    if (retaddr == x->label) return x->target;
  }
  return 0;
}

int main(int argc, char *argv[]) {
  void *p, **ptr; int i;

  for (i=0, ptr=&fixup_start; ptr < &fixup_end; i++) {
    void *label = *ptr++;
    void *target= *ptr++;
    printf("LABEL: %p\tTARGET: %p", label, target);
    if ((i%2)==0) printf(", \t"); else printf("\n");
  }
  printf("\n");

  printf("Find which address? ");
  scanf("%x", &p);
  printf("LOOKING FOR: %p\n", p);
  printf("FOUND(c): %p\n", binsearch(p));
  {
    void *pc = get_retaddr();
    set_retaddr(p);
    lookup();
    printf("FOUND(asm): %p\n", get_retaddr());
    set_retaddr(pc);
  }
}

const int end_of_main = 0;
