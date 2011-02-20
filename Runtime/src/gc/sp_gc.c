#include <assert.h>
#include "config.h"
#include "gc.h"
#include "gc_typed.h"
#include <jni.h>
#include "jni-private.h"
#include "jni-gc.h"
#include "precise_gc.h"

#ifndef BDW_CONSERVATIVE_GC
# error BDW GC required by semi-precise allocation strategy
#endif


/* requires: that the object being allocated is not an array */
void *SP_malloc (size_t size_in_bytes, struct FNI_classinfo *ci)
{
  GC_descr descr;
  size_t size_in_words;

  // check that the size being requested is consistent with the
  // classinfo provided, and that the object is not an array
  assert(ci->claz->size == size_in_bytes && ci->claz->component_claz == NULL);

  size_in_words = (size_in_bytes + SIZEOF_VOID_P - 1)/SIZEOF_VOID_P;

  if (size_in_words > BITS_IN_GC_BITMAP)
    {
      // auxiliary bitmap is used
      descr = GC_make_descriptor(ci->claz->gc_info.ptr, size_in_words);
    }
  else
    {
      // in-line bitmap is used
      descr = GC_make_descriptor(&(ci->claz->gc_info.bitmap), size_in_words);
    }

  return GC_malloc_explicitly_typed(size_in_bytes, descr);
}


/* requires: that the object being allocated is an array containing pointers */
void *SP_malloc_array(size_t size_in_bytes, struct FNI_classinfo *ci)
{
  return GC_malloc(size_in_bytes);
}


/* requires: that the object being allocated not contain pointers */
void *SP_malloc_atomic (size_t size_in_bytes, struct FNI_classinfo *ci) 
{
  return GC_malloc_atomic(size_in_bytes);
}



