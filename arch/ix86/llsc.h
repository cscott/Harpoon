/* Macros to access the load linked and store conditional instructions
 * on this architecture.
 *  C. Scott Ananian <cananian@alumni.princeton.edu>
 */
#ifndef INCLUDED_LLSC_H
#define INCLUDED_LLSC_H

#warning x86 does not have load linked/store conditional instructions
#warning generated code will NOT be thread safe.

static inline int32_t load_linked(int32_t *ptr) {
  return *ptr;
}
static inline int store_conditional(int32_t *ptr, int32_t val) {
  *ptr = val;
  return 1;
}
static inline int64_t load_linked_double(int64_t *ptr) {
  return *ptr;
}
static inline int store_conditional_double(int64_t *ptr, int64_t val) {
  *ptr = val;
  return 1;
}
static inline void sync(void) {
  /* do nothing */
}

#define LL(x) load_linked(x)
#define LL_D(x) load_linked_double(x)
#define SC(x,y) store_conditional(x,y)
#define SC_D(x,y) store_conditional_double(x,y)

#endif /* INCLUDED_LLSC_H */
