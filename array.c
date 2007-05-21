#include <assert.h>
#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <gc/gc_typed.h>
#ifdef _ARCH_PPC
# include "llsc-ppc32.h"
# include "rdtsc-ppc32.h"
#else
# include "llsc-unimpl.h"
# ifdef i386
#  include "rdtsc-x86.h"
# else
#  include "rdtsc-unimpl.h"
# endif
#endif

#ifdef LOCKFREE
# define RANDOM
# define VOLATILE volatile
#else
# define VOLATILE
#endif
#ifdef RANDOM
# define SHALLOW
#endif
#ifdef COPYALL
# define BASE
#endif

#define NUM_ELEM 100
#define REPETITIONS 100000

typedef int32_t index_t;
typedef int32_t field_t;

#ifdef SHALLOW
struct aarray_cache {
    index_t length;
    VOLATILE field_t elem[0];
};
struct aarray {
    void * VOLATILE cache_or_rest;
    index_t index;
    field_t value;
};
#define IS_DIFF(ptr) (((int)(ptr))&1)
#define DIFF_PTR(ptr) ((struct aarray*)(((void*)(ptr))-1))
#define CACHE_PTR(ptr) ((struct aarray_cache*)(ptr))

static GC_descr aarray_desc;
static inline struct aarray *make_array(index_t len) {
    struct aarray_cache *cache;
    struct aarray *a;
    // first make a cache
    cache = GC_MALLOC_ATOMIC(sizeof(struct aarray_cache)+len*sizeof(field_t));
    cache->length = len;
    // now make a root node.
    struct aarray *obj = GC_CALLOC_EXPLICITLY_TYPED(1, sizeof(struct aarray), aarray_desc);
    obj->cache_or_rest = cache;
    assert(!IS_DIFF(obj->cache_or_rest));
    return obj;
}
static inline field_t read(struct aarray *obj, index_t index);

#ifdef LOCKFREE
// note that contents of "array w/ pointer p" are constant, even though
// rotations/cache mutations are happening.  This protects us from ABA
// problems, since value of cache_or_rest is sufficient to identify contents.
static void reroot(struct aarray *obj) {
    static int first = 1;
    struct aarray *rest, *nroot;
    struct aarray_cache *cache;
    index_t index;
    field_t newv, oldv;
    void *p1, *p2;

    p1 = obj->cache_or_rest;
    if (!IS_DIFF(p1)) return; // race; this is already re-rooted
    rest = DIFF_PTR(p1);
    index = obj->index; // this index should always be a valid index
    newv = obj->value;  // but newv could be out of date.
    oldv = read(rest, index); // safe; rotates rest as a side-effect.

    p2 = rest->cache_or_rest;
    if (IS_DIFF(p2)) return; // race; try again.
    cache = CACHE_PTR(p2);

    // maybe clone the cache here.
    if (
#ifdef RANDOM
	(rdtsc() % cache->length) == 0 ||
#endif
	first) {
	first = 0; //workaround for conservative collection: top-level obj held
	// clone the cache
	int size = sizeof(struct aarray_cache)+cache->length*sizeof(field_t);
	struct aarray_cache *ncache = GC_MALLOC_ATOMIC(size);
	memcpy(ncache, cache, size);
	ncache->elem[index] = newv;

	// relink to point to new cache.
	if (LL(&(obj->cache_or_rest)) == p1 &&
	    rest->cache_or_rest == p2) // aba?
	    SC_PTR(&(obj->cache_or_rest), ncache); // try it
	// maybe we succeeded, maybe we didn't
	return;
    }

    // make new root.
    nroot = GC_CALLOC_EXPLICITLY_TYPED(1, sizeof(struct aarray), aarray_desc);
    nroot->cache_or_rest = cache;
    // race w/ another rotation here, which will overwrite our index/value
    if (!(LL(&(rest->cache_or_rest)) == p2 &&
	  SC(&(rest->index), index)))
	return; // race; bail.
    if (!(LL(&(rest->cache_or_rest)) == p2 &&
	  SC(&(rest->value), oldv)))
	return; // race; bail.
    if (!(LL(&(rest->cache_or_rest)) == p2 && // LL protects index/value, too
	  rest->index == index && rest->value == oldv &&
	  SC_PTR(&(rest->cache_or_rest), (((void*)nroot)+1))))
	return; // race; bail.
    // point us at the new root.  But note that rest may not point at
    // nroot any more, due to races.
    if (!(LL(&(obj->cache_or_rest)) == p1 &&
	  SC_PTR(&(obj->cache_or_rest), (((void*)nroot)+1))))
	return; // race; bail.
    // and point old root at us.  here we'll see if rest doesn't point at nroot
    if (!(LL(&(rest->cache_or_rest)) == (((void*)nroot)+1) &&
	  SC_PTR(&(rest->cache_or_rest), (((void*)obj)+1))))
	return; // race; bail.

    // okay, here's the dangerous part: mutate the cache.
    // only a single escaped pointer to the cache at any point
    // only the owner of the root pointing to the cache can mutate it.
    if (!(LL(&(nroot->cache_or_rest)) == cache && SC(&(cache->elem[index]), newv)))
	return; // race; bail.

    // okay, now if we're still unmodified, relink to point directly at cache.
    // some people might still have live ptr to nroot?  shouldn't matter.
    if (!(LL(&(obj->cache_or_rest)) == (((void*)nroot)+1) && SC_PTR(&(obj->cache_or_rest), cache)))
	return;
    // done!
}

#else

static void reroot(struct aarray *obj) {
    static int first = 1;
    struct aarray *rest;
    struct aarray_cache *cache;
    index_t index;
    field_t newv, oldv;
    // first, ensure that 'rest' is a ROOT node.
    index = obj->index;
    newv = obj->value;
    assert(IS_DIFF(obj->cache_or_rest));
    rest = DIFF_PTR(obj->cache_or_rest);
    oldv = read(rest, index); // rotates rest as a side-effect.
    assert(!IS_DIFF(rest->cache_or_rest));
    cache = CACHE_PTR(rest->cache_or_rest);
    // maybe clone the cache here.
    if (
#ifdef RANDOM
	(rdtsc() % cache->length) == 0 ||
#endif
	first) {
	// clone the cache
	int size = sizeof(struct aarray_cache)+cache->length*sizeof(field_t);
	struct aarray_cache *ncache = GC_MALLOC_ATOMIC(size);
	memcpy(ncache, cache, size);
	ncache->elem[index] = newv;
	cache = ncache;
	first = 0; //workaround for conservative collection: top-level obj held
    } else {
	// rewrite cache
	cache->elem[index] = newv;
	// rewrite old 'rest' node to be a diff
	rest->index = index;
	rest->value = oldv;
	rest->cache_or_rest = ((void*)obj)+1; // indicate it's a DIFF
    }
    // rewrite obj node to be a root.
    obj->cache_or_rest = cache; // 0 lsb == this is a root node.
    // done!
}
#endif
    
static inline field_t read(struct aarray *obj, index_t index) {
    void *p; field_t val;
 retry:
    p = obj->cache_or_rest;
    if (IS_DIFF(p)) { reroot(obj); goto retry; }
    val = CACHE_PTR(p)->elem[index];
#ifdef LOCKFREE
    if (obj->cache_or_rest != p) goto retry; // consistency check
#endif
    return val;
}
static inline struct aarray *write(struct aarray *obj, index_t index, field_t value) {
    struct aarray *obj2 = GC_MALLOC_EXPLICITLY_TYPED(sizeof(struct aarray), aarray_desc);
    obj2->index = index;
    obj2->value = value;
    obj2->cache_or_rest = ((void*)obj)+1; // indicate that this is a DIFF
    return obj2;
}

#else /* BASE */

struct aarray {
    void *claz;
    index_t length;
    volatile field_t elem[0];
};
static inline struct aarray *make_array(index_t len) {
    struct aarray *obj = GC_MALLOC_ATOMIC(sizeof(struct aarray)+len*sizeof(field_t));
    obj->claz = NULL;
    obj->length = len;
    return obj;
}
static inline field_t read(struct aarray *obj, index_t index) {
    return obj->elem[index];
}
static inline struct aarray *write(struct aarray *obj, index_t index, field_t value) {
#ifdef COPYALL
    struct aarray *obj2 = make_array(obj->length);
    memcpy((void*)obj2->elem,(void*)obj->elem, sizeof(field_t)*obj->length);
    obj = obj2;
#endif
    obj->elem[index] = value;
    return obj;
}
#endif

void do_bench(struct aarray *obj, index_t len) __attribute__((noinline));
void do_bench(struct aarray *obj, index_t len) {
    int i, j;

    /** Initialize the array */
    for (i=0; i<len; i++)
	obj = write(obj, i, i);
    /** Now reverse the array a number of times. */
    for (j=0; j<(REPETITIONS*2); j++) {
	for (i=0; i<len/2; i++) {
	    field_t v1 = read(obj, i);
	    field_t v2 = read(obj, len-i-1);
	    obj = write(obj, i, v2);
	    obj = write(obj, len-i-1, v1);
	}
    }
    /** Check the array has the expected values */
    for (i=0; i<len; i++)
	assert(read(obj, i)==i);
}


int main(int argc, char **argv) {
/* make these variables extern, or else the compiler will toss updates to them
 * because they do not escape. */
    index_t len = argc>1 ? atoi(argv[1]) : NUM_ELEM;
#ifdef SHALLOW
    GC_word aarray_bitmap[GC_BITMAP_SIZE(struct aarray)] = { 0 };
    GC_set_bit(aarray_bitmap, GC_WORD_OFFSET(struct aarray, cache_or_rest));
    aarray_desc= GC_make_descriptor(aarray_bitmap, GC_WORD_LEN(struct aarray));
    GC_REGISTER_DISPLACEMENT(1);
#endif
    do_bench(make_array(len), len);
    return 0;
}
