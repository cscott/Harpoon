#include "config.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#include "../../Contrib/gc/gc_typed.h"
#include "../../Contrib/gc/gcconfig.h"
#endif
#include <jni.h>
#include "jni-private.h"

#ifdef WITH_SEMI_PRECISE_GC

/* Only set GC_EVERY_TIME for debugging, since this is VERY slow. */
#define GC_EVERY_TIME            0
#define WORDSZ          CPP_WORDSZ
#define WORDSZ_IN_BYTES  (WORDSZ/8)

/* Use create_GC_bitmap to make bitmaps for arrays of objects. */
GC_bitmap create_GC_bitmap(size_t size_in_bytes, struct claz* c)
{
  GC_bitmap result; int i;
  int num_bits = size_in_bytes/WORDSZ_IN_BYTES; /* number of bits needed */
  int num_words = (num_bits+WORDSZ-1)/WORDSZ;   /* number of words needed */
  result = (GC_bitmap)malloc(num_words*sizeof(GC_word));
  *(result) = ~07; /* clear bits 0, 1 and 2 */
  for(i = 1; i < num_words; i++)
    *(result+i) = ~0;
  return result;
}

void free_GC_bitmap(GC_bitmap bitmap) { free(bitmap); }

void *SP_malloc (size_t size_in_bytes, struct FNI_classinfo *ci)
{
#ifdef BDW_CONSERVATIVE_GC
  /* Explicitly trigger a full, world-stop collection. */
  if (GC_EVERY_TIME) GC_gcollect();
  {
    GC_descr descr;
    if (ci->claz->size <= WORDSZ*WORDSZ_IN_BYTES) { /* compact */
#ifdef DEBUG
      if (ci->claz->component_claz == NULL) { /* non-array */
	if (size_in_bytes != ci->claz->size)
	  printf("WARNING: Size of object (%d) doesn't match"
		 " allocation size (%d).\n", ci->claz->size, size_in_bytes);
      }
#endif
      /* if the descriptor is not an integral number of words, the 
	 "leftover" cannot be a pointer, which is why we can 
	 simply truncate the bitmap when creating the descriptor */
      descr = GC_make_descriptor
	(&(ci->claz->gc_info.bitmap), size_in_bytes/WORDSZ_IN_BYTES);
    } else {
#ifdef DEBUG
      if (ci->claz->component_claz == NULL) { /* non-array */
	if (size_in_bytes != ci->claz->size)
	  printf("WARNING: Size of object (%d) doesn't match"
		 " allocation size (%d).\n", ci->claz->size, size_in_bytes);
      } else {
	printf("WARNING: Can't happen in SP_malloc.\n");
      }
#endif
      /* if the descriptor is not an integral number of words, the 
	 "leftover" cannot be a pointer, which is why we can 
	 simply truncate the bitmap when creating the descriptor */
      descr = GC_make_descriptor
	(ci->claz->gc_info.ptr, size_in_bytes/WORDSZ_IN_BYTES);
    }
    return GC_malloc_explicitly_typed(size_in_bytes, descr);
  }
#else
  return malloc(size_in_bytes);
#endif
}

/* SP_malloc_array should be used for arrays of objects
   if you want the more efficient implementation that does
   not use BDW's GC_malloc_explicitly_typed */
void *SP_malloc_array(size_t size_in_bytes, struct FNI_classinfo *ci)
{
#ifdef BDW_CONSERVATIVE_GC
  return GC_malloc(size_in_bytes);
#else
  return malloc(size_in_bytes);
#endif
}

/* XXX NOT CURRENTLY USED XXX
   SP_malloc_precise_array should be used for arrays of objects
   if you want to use BDW's GC_malloc_explicity_typed. */
void *SP_malloc_precise_array (size_t size_in_bytes, struct FNI_classinfo *ci) 
{
#ifdef BDW_CONSERVATIVE_GC
#ifdef DEBUG
  if (ci->claz->component_claz == NULL) { /* make sure this is an array */
    printf("WARNING: SP_malloc_array should only be used on arrays.\n");
    return GC_malloc(size_in_bytes);
  }
#endif
  {
    int containsPtrs = ci->claz->gc_info.bitmap;
#ifdef DEBUG
    if (!containsPtrs) { /* array of primitives--use SP_malloc */
      printf("WARNING: SP_malloc_array should not be used"
	     " when array components are primitives.\n");
      return SP_malloc(size_in_bytes, ci);
    }
#endif 
    { /* all clear, proceed */
      GC_bitmap bitmap; GC_descr descr;
#ifdef DEBUG
      /* an array of objects must be an integral number of words */
      if (size_in_bytes%WORDSZ_IN_BYTES != 0)
	printf("WARNING: Can't happen in SP_malloc_array.\n");
#endif
      bitmap = create_GC_bitmap(size_in_bytes, ci->claz);
      descr = GC_make_descriptor(bitmap, size_in_bytes/WORDSZ_IN_BYTES);
      free_GC_bitmap(bitmap);
      return GC_malloc_explicitly_typed(size_in_bytes, descr); 
    }
  }
#else
  return malloc(size_in_bytes);
#endif
}

/* For objects and arrays that do not contain interior pointers */
void *SP_malloc_atomic (size_t size_in_bytes, struct FNI_classinfo *ci) 
{
#ifdef BDW_CONSERVATIVE_GC
  return GC_malloc_atomic(size_in_bytes);
#else
  return malloc(size_in_bytes);
#endif
}
#endif /* ifdef WITH_SEMI_PRECISE_GC */



