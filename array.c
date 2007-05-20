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

#ifdef RANDOM
#define SHALLOW
#endif
#ifdef COPYALL
#define BASE
#endif

#define NUM_ELEM 10
#define REPETITIONS 10000

typedef int32_t field_t;

#ifdef SHALLOW
struct aarray_cache {
    unsigned length;
    volatile field_t elem[0];
};
struct aarray {
    enum { DIFF, ROOT } type;
    union {
	struct {
	    struct aarray_cache *cache;
	} root;
	struct {
	    struct aarray *rest;
	    unsigned index;
	    field_t value;
	} diff;
    } u;
};
static GC_descr aarray_desc;
static inline struct aarray *make_array(unsigned len) {
    struct aarray_cache *cache;
    struct aarray *a;
    // first make a cache
    cache = GC_MALLOC_ATOMIC(sizeof(struct aarray_cache)+len*sizeof(field_t));
    cache->length = len;
    // now make a root node.
    struct aarray *obj = GC_CALLOC_EXPLICITLY_TYPED(1, sizeof(struct aarray), aarray_desc);
    obj->type = ROOT;
    obj->u.root.cache = cache;
    return obj;
}
static void reroot(struct aarray *obj) {
    struct aarray *rest;
    struct aarray_cache *cache;
    unsigned index;
    field_t newv, oldv;
    static int first = 1;
    assert(obj->type != ROOT);
    // first, ensure that 'rest' is a ROOT node.
    rest = obj->u.diff.rest;
    if (rest->type != ROOT) reroot(rest);
    assert(rest->type == ROOT);
    // now let's read all the information we'll need.
    cache = rest->u.root.cache;
    index = obj->u.diff.index;
    newv = obj->u.diff.value;
    oldv = cache->elem[index];
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
	rest->type = DIFF;
	rest->u.diff.index = index;
	rest->u.diff.value = oldv;
	rest->u.diff.rest = obj;
    }
    // rewrite obj node to be a root.
    memset(obj, 0, sizeof(*obj)); // be nice to the gc.
    obj->type = ROOT;
    obj->u.root.cache = cache;
    // done!
}
    
static inline field_t read(struct aarray *obj, unsigned index) {
    if (obj->type == DIFF) reroot(obj);
    return obj->u.root.cache->elem[index];
}
static inline struct aarray *write(struct aarray *obj, unsigned index, field_t value) {
    struct aarray *obj2 = GC_MALLOC_EXPLICITLY_TYPED(sizeof(struct aarray), aarray_desc);
    obj2->type = DIFF;
    obj2->u.diff.index = index;
    obj2->u.diff.value = value;
    obj2->u.diff.rest = obj;
    return obj2;
}

#else /* BASE */

struct aarray {
    void *claz;
    unsigned length;
    volatile field_t elem[0];
};
static inline struct aarray *make_array(unsigned len) {
    struct aarray *obj = GC_MALLOC_ATOMIC(sizeof(struct aarray)+len*sizeof(field_t));
    obj->claz = NULL;
    obj->length = len;
    return obj;
}
static inline field_t read(struct aarray *obj, unsigned index) {
    return obj->elem[index];
}
static inline struct aarray *write(struct aarray *obj, unsigned index, field_t value) {
#ifdef COPYALL
    struct aarray *obj2 = make_array(obj->length);
    memcpy((void*)obj2->elem,(void*)obj->elem, sizeof(field_t)*obj->length);
    obj = obj2;
#endif
    obj->elem[index] = value;
    return obj;
}
#endif

void do_bench(struct aarray *obj, unsigned len) __attribute__((noinline));
void do_bench(struct aarray *obj, unsigned len) {
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
    unsigned len = argc>1 ? atoi(argv[1]) : NUM_ELEM;
#ifdef SHALLOW
    GC_word aarray_bitmap[GC_BITMAP_SIZE(struct aarray)] = { 0 };
    GC_set_bit(aarray_bitmap, GC_WORD_OFFSET(struct aarray, u.root.cache));
    aarray_desc= GC_make_descriptor(aarray_bitmap, GC_WORD_LEN(struct aarray));
#endif
    do_bench(make_array(len), len);
    return 0;
}
