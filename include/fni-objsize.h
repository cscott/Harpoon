/* this routine gets the size of an object, including array types.
 * for efficiency, it uses a slight hack: the size of objects are
 * guaranteed to be at least sizeof(struct oobj) (which is the header
 * size) so we can distinguish primitive array types from object
 * array types by looking at claz->component_claz->size. */

#ifndef INCLUDED_FNI_OBJSIZE
#define INCLUDED_FNI_OBJSIZE


static inline jsize FNI_ObjectSize(struct oobj *o) {
  struct claz *claz = FNI_CLAZ((struct oobj*) PTRMASK(o));
  struct claz *cclaz = claz->component_claz;
  /* handle non-array first */
  if (cclaz==NULL) return claz->size;
  /* now array type: determine if primitive array or not */
  /* (primitives have null in the first slot of the display
   *  but so do interfaces.  weed them out using the interface list.) */
  /* SEE Java_lang_Class_isPrimitive() */
  if (cclaz->display[0]==NULL && *(cclaz->interfaces)==NULL) {
    /* primitive array type */
    return claz->size + (cclaz->size * ((struct aarray *)o)->length);
  } else { 
    /* object or interface array type */
    return claz->size + (sizeof(ptroff_t) * ((struct aarray *)o)->length);
  }
}

#endif /* INCLUDED_FNI_OBJSIZE */
