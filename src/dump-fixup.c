/* print various tables. */
#include <jni.h>
#include <jni-private.h>
#include <stdio.h>

extern JNIEnv *FNI_JNIEnv; /* temporary hack. */

int main(int argc, char *argv[]) {
  struct _fixup_info *ptr;
  int i;
  printf("javamain: %s\n", FNI_javamain);

  /*have to reference something in FNI so that the linker doesn't discard it*/
  FNI_JNIEnv = FNI_ThreadInit();

  printf("FIXUP START: %p\tEND: %p\n", fixup_start, fixup_end);
  for (i=0, ptr=fixup_start; ptr < fixup_end; i++, ptr++) {
    printf("RETADDR: %08p  HANDLER: %08p",
	   ptr->return_address, ptr->handler_target);
    if ((i%2)==0) printf(", \t"); else printf("\n");
  }
  printf("\n");
}
