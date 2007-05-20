#include <stdint.h>
static inline uint32_t rdtsc() {
  uint32_t lo, hi;
 /* We cannot use "=A", since this would use %rax on x86_64 */
  asm volatile ("rdtsc" : "=a" (lo), "=d" (hi));
  return lo;
}
