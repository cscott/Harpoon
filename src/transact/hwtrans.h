#ifndef INCLUDED_HWTRANS_H
#define INCLUDED_HWTRANS_H

/*   asm(string : output : input : clobbered hard registers) */
/* xbegin is assumed to take a register snapshot. */
#ifdef _MIPS_ARCH_MIPS4
#define XBEGIN()	asm volatile (".word 0x0404FFFF # xbegin" :: "memory")
#define XEND()  	asm volatile (".word 0x00000029 # xend" :: "memory")
#define NXBEGIN()	asm volatile (".word 0x00000028 # nxbegin" :: "memory")
#define NXEND() 	asm volatile (".word 0x00000035 # nxend" :: "memory")
#define NOP()		asm ("nop")
#endif

inline void EXACT_XBEGIN(void) __attribute__((always_inline)) {
  int delay = 1, i;
  XBEGIN();
  for (i=1; i<delay; i++)
    NOP;
  NXBEGIN();
  delay *= 2;
  NXEND();
}
inline void EXACT_XEND(void) __attribute__((always_inline)) {
  XEND();
}

#endif /* INCLUDED_HWTRANS_H */
