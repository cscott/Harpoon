#ifndef INCLUDED_SYSTEM_PAGE_SIZE_H
#define INCLUDED_SYSTEM_PAGE_SIZE_H

/* the system page size is determined and set during GC initialization */
extern size_t SYSTEM_PAGE_SIZE;
extern size_t PAGE_MASK;

#define ROUND_TO_NEXT_PAGE(x) (((x) + PAGE_MASK) & (~PAGE_MASK))

#endif
