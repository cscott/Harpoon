/* These are the data structures associated with object layout.
 * They've been moved from jni-private.h for the use of dynsync.h
 * when included from precisec.h. */

#ifndef INCLUDED_FNI_OBJLAYOUT
#define INCLUDED_FNI_OBJLAYOUT

/* --------------------------------------------------------------*/
/* the oobj structure tells you what's inside the object layout. */
struct oobj {
#ifdef WITH_CLAZ_SHRINK
  /* claz shrink replaces the pointer w/ a table index. */
  /* (claz_index should be aligned w/ start of oobj, but bitfields
   *  don't necessarily provide this guarantee) */
  unsigned int claz_index:(WITH_CLAZ_SHRINK*8);
# if WITH_CLAZ_SHRINK < SIZEOF_VOID_P
  int _padding_:((SIZEOF_VOID_P-WITH_CLAZ_SHRINK)*8);
# endif
#else
  struct claz *claz;
#endif
#ifndef WITH_HASHLOCK_SHRINK
  /* if low bit is one, then this is a fair-dinkum hashcode. else, it's a
   * pointer to a struct inflated_oobj. this pointer needs to be freed
   * when the object is garbage collected, which is done w/ a finalizer. */
  union { ptroff_t hashcode; struct inflated_oobj *inflated; } hashunion;
#endif
  /* fields below this point */
  char field_start[0];
};
/** use this version of the oobj structure if you're looking at an array. */
struct aarray {
  struct oobj obj;
  char _padding_[OBJECT_PADDING]; /* by default, OBJECT_PADDING is zero */
  jsize length; /* first field in an array is the length */
#if SIZEOF_VOID_P == 8
  jint _padding_64; /* word-align element start on 64-bit platforms. */
#endif
  char element_start[0]; /* place holder for start of elements */
};
/* --------------------------------------------------------------*/

#endif /* INCLUDED_FNI_OBJLAYOUT */
