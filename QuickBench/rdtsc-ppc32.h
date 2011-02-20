#include <stdint.h>
static inline uint32_t rdtsc() {
    uint32_t result;
    asm volatile ("mftb %[result]" : [result] "=r" (result));
    return result;
}
