#include <stdio.h>
#include <jni.h>

#define FLAG_VALUE (0xCACACACACACACACALL)

int main(int argc, char **argv) {
  union {
    jboolean z;
    jbyte b;
    jchar c;
    jshort s;
    jint i;
    jlong l;
    jfloat f;
    jdouble d;
    void *o;
  } u;

  u.l = FLAG_VALUE;

  printf("#ifndef INCLUDED_TRANSACT_FLAGS_H\n"
	 "#define INCLUDED_TRANSACT_FLAGS_H\n"
	 "\n");
  printf("#define TRANS_FLAG_Boolean ((jboolean)%d)\n", u.z);
  printf("#define TRANS_FLAG_Byte    ((jbyte)%d)\n", u.b);
  printf("#define TRANS_FLAG_Char    ((jchar)%u)\n", u.c);
  printf("#define TRANS_FLAG_Short   ((jshort)%d)\n", u.s);
  printf("#define TRANS_FLAG_Int     ((jint)%d)\n", u.i);
  printf("#define TRANS_FLAG_Long    ((jlong)%LdLL)\n", u.l);
  printf("#define TRANS_FLAG_Float   ((jfloat)%LA)\n", (long double)u.f);
  printf("#define TRANS_FLAG_Double  ((jdouble)%LA)\n", (long double)u.d);
  printf("#define TRANS_FLAG_Object  ((void*)%p)\n", u.o);

  printf("\n"
	 "#endif /* INCLUDED_TRANSACT_FLAGS_H */\n");
  return 0; // success!
}
