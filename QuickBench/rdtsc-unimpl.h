#include <stdint.h>
static inline uint32_t rdtsc() {
    static uint32_t acc = 0;
    return acc++;
}
