#ifndef INCLUDED_HWTRANS_H
#define INCLUDED_HWTRANS_H

#ifdef DONT_REALLY_DO_HWTRANS
# define XBEGIN()	0
# define XEND()		0
# define NXBEGIN()	0
# define NXEND()	0
# define NOP()		0
#else /* !DONT_REALLY_DO_HWTRANS */

# ifdef _MIPS_ARCH_MIPS4
/*   asm(string : output : input : clobbered hard registers) */
/* xbegin is assumed to take a register snapshot. */
#  define XBEGIN()	asm volatile (".word 0x0404FFFF # xbegin" ::: "memory")
#  define XEND()  	asm volatile (".word 0x00000029 # xend" ::: "memory")
#  define NXBEGIN()	asm volatile (".word 0x00000028 # nxbegin" :::"memory")
#  define NXEND() 	asm volatile (".word 0x00000035 # nxend" ::: "memory")
#  define NOP()		asm ("nop")
# endif /* _MIPS_ARCH_MIPS4 */

#endif /* !DONT_REALLY_DO_HWTRANS */

/* gcc3.3 manual takes about using __attribute__((always_inline)) for the
 * next two functions, but it doesn't seem to actually be supported in the
 * gcc 3.3.1 we're using. */

extern inline void EXACT_XACTION_BEGIN(void) {
  int delay = 1, i;
  XBEGIN();
  for (i=1; i<delay; i++)
    NOP();
  NXBEGIN();
  delay *= 2;
  NXEND();
}
extern inline void EXACT_XACTION_END(void) {
  XEND();
}

extern inline void *NXALLOC(int size) {
  extern void *malloc(int);
  void *result;
  NXBEGIN();
  result = malloc(size);
  NXEND();
  return result;
}

#endif /* INCLUDED_HWTRANS_H */
