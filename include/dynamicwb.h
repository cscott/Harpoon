#ifndef INCLUDED_DYNAMIC_WB_H
#define INCLUDED_DYNAMIC_WB_H

/* ----- macros and definitions for dynamic write barriers ----- */

#ifdef WITH_DYNAMIC_WB
#define HASHCODE_MASK(x) ((x) & ~2)
#define INFLATED_MASK(x) ((struct inflated_oobj *) (((ptroff_t) (x)) & ~2))
#define DYNAMIC_WB_ON(x) ((x)->hashunion.hashcode & 2)
#define DYNAMIC_WB_CLEAR(x) \
            ((x)->hashunion.hashcode = HASHCODE_MASK((x)->hashunion.hashcode)) 
#else
#define HASHCODE_MASK(x) (x)
#define INFLATED_MASK(x) (x)
#endif

#endif /* INCLUDED_DYNAMIC_WB_H */
