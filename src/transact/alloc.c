#include <jni.h>
#include <jni-private.h>
#include "config.h"

/** GC_malloc_atomic is never appropriate with transactions, since we've
 *  got two "hidden" pointer fields.  Let's redefine this to Do The Right
 *  Thing.... the appropriate BDW descriptor is initialized in startup.c */

#if BDW_CONSERVATIVE_GC
#include "gc_typed.h"

static GC_descr Transaction_Descriptor;

GC_PTR GC_malloc_atomic_trans(size_t sz) {
    return GC_malloc_explicitly_typed(sz, Transaction_Descriptor);
}
#endif /* BDW_CONSERVATIVE_GC */

void transact_init_alloc() {
#if BDW_CONSERVATIVE_GC
    typedef struct tranobj {
	struct oobj header;
	struct vinfo *versions;
	struct tlist *readers;
    } to;
    GC_word td[GC_BITMAP_SIZE(to)] = {0};
    GC_set_bit(td, GC_WORD_OFFSET(to, versions));
    GC_set_bit(td, GC_WORD_OFFSET(to, readers));
    Transaction_Descriptor = GC_make_descriptor(td, GC_WORD_LEN(to));
#endif /* BDW_CONSERVATIVE_GC */
}
