/* print various tables. */
#include "constants.h"

int main(int argc, char *argv[]) {
  void **ptr; int i;
  printf("javamain: %p\n", javamain);

  printf("FIXUP START: %p\tEND: %p\n", &fixup_start, &fixup_end);
  for (i=0, ptr=&fixup_start; ptr < &fixup_end; i++) {
    void *label = *ptr++;
    void *target= *ptr++;
    printf("LABEL: %p\tTARGET: %p", label, target);
    if ((i%2)==0) printf(", \t"); else printf("\n");
  }
  printf("\n");

  printf("STATIC INITIALIZERS START: %p\n", &static_inits);
  for (i=0, ptr=&static_inits; *ptr != 0; ptr++, i++) {
    printf("%p", *ptr);
    if ((i%2)==0) printf(", "); else printf("\n");
  }
  printf("\n");
}
