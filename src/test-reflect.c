#include "constants.h"
#include "asm/stack.h"

/*
typedef struct { void * namestr; void * classobj; } pair_t;
void * binsearch(void * namestr) {
  pair_t *l=(pair_t*)&fixup_start, *r=((pair_t*)&fixup_end)-1;
  while (r >= l) {
    pair_t *x = l + ((r-l)/2); // (l+r)/2
    if (retaddr <  x->label) r = x-1; else l = x+1;
    if (retaddr == x->label) return x->target;
  }
  return 0;
}
*/

int main(int argc, char *argv[]) {
  void **ptr; int i;

  for (i=0, ptr=&name2class_start; ptr < &name2class_end; i++) {
    char *namestr = *ptr++;
    void *classobj= *ptr++;
    //printf("NAMESTR: %p  CLASSOBJ: %p", namestr, classobj);
    //if ((i%2)==0) printf(", \t"); else printf("\n");
    printf("%s\n", namestr);
  }
}
