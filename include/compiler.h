/* Stripped down version of some useful definitions from
 * linux/include/linux/compiler.h
 * This copy is from linux-2.5.74; feel free to update it
 * from a more recent version of linux if things seem to
 * change.
 *
 * License is GPL, like the rest of the linux kernel.
 */
#ifndef INCLUDED_COMPILER_H // different from the name in linux/compiler.h, in
#define INCLUDED_COMPILER_H // case our versions diverge.

/* When we say 'inline', we mean it! */
#if (__GNUC__ > 3) || (__GNUC__ == 3 && __GNUC_MINOR__ >= 1)
#define inline		__inline__ __attribute__((always_inline))
#define __inline__	__inline__ __attribute__((always_inline))
#define __inline	__inline__ __attribute__((always_inline))
#endif

/* Somewhere in the middle of the GCC 2.96 development cycle, we implemented
   a mechanism by which the user can annotate likely branch directions and
   expect the blocks to be reordered appropriately.  Define __builtin_expect
   to nothing for earlier compilers.  */

#if __GNUC__ == 2 && __GNUC_MINOR__ < 96
#define __builtin_expect(x, expected_value) (x)
#endif

#define likely(x)	__builtin_expect((x),1)
#define unlikely(x)	__builtin_expect((x),0)

/*
 * Allow us to avoid 'defined but not used' warnings on functions and data,
 * as well as force them to be emitted to the assembly file.
 *
 * As of gcc 3.3, static functions that are not marked with attribute((used))
 * may be elided from the assembly file.  As of gcc 3.3, static data not so
 * marked will not be elided, but this may change in a future gcc version.
 *
 * In prior versions of gcc, such functions and data would be emitted, but
 * would be warned about except with attribute((unused)).
 */
#if __GNUC__ == 3 && __GNUC_MINOR__ >= 3 || __GNUC__ > 3
#define __attribute_used__	__attribute__((__used__))
#else
#define __attribute_used__	__attribute__((__unused__))
#endif

#endif /* INCLUDED_COMPILER_H */
