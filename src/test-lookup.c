#include <jni.h>
#include <jni-private.h>
#include <stdio.h>
#include "asm/stack.h"

/* value to be thrown is in r0, so be careful with that.
 * return address is in [fp, #-4] */

typedef struct _fixup_info  pair_t;
void * binsearch(void *retaddr) {
  pair_t *l=fixup_start, *r=fixup_end-1;
  while (r >= l) {
    pair_t *x = l + ((r-l)/2); /* (l+r)/2 */
    if (retaddr <  x->return_address) r = x-1; else l = x+1;
    if (retaddr == x->return_address) return x->handler_target;
  }
  return 0;
}

int main(int argc, char *argv[]) {
  void *p; pair_t *ptr; int i;

  /*have to reference something in FNI so that the linker doesn't discard it*/
  FNI_InitJNIEnv();

  printf("FIXUP START: %p\tEND: %p\n", fixup_start, fixup_end);
  for (i=0, ptr=fixup_start; ptr < fixup_end; i++, ptr++) {
    printf("RETADDR: %08p  HANDLER: %08p",
	   ptr->return_address, ptr->handler_target);
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
