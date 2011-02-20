/** Data structures for native java compilation. */

/** Field info. */
union field {
  struct oobj *objectref;
  int i; // also used for char, boolean, etc.
  float f;
  // long and double are composed of two fields (what about 64-bit machines?)
};
/** Object information. */
struct oobj {
  int hashcode; // initialized on allocation, if neccessary.
  struct claz *claz; // pointer actually points here.
  struct oobj *next; // embedded linked list. (null if not on any list)
  union field objectdata[0];    // field info goes here:
  // for an array, first word of objectdata is always length.
}

struct claz {
  method_t interface[x]; // many
  struct clazinfo *clazinfo; // pointer actually points to middle.
  gc_t gc_func; // how to garbage collect this function.
  struct interfz *(interfaces[NUM_INTER+1]);//ptr to null-term lst of interfaces
  struct claz display[MAX_DEPTH];
  method_t method[NUM_METHODS]; // many
}
struct interfz {
  char *name;
  // also tables for reflection.
}
struct clazinfo {
  char *name;
  // also tables for reflection.
}
typedef union field (*method_t)(); // (how to return long/double values?)
typedef void *(*gc_t)();
//
static struct oobj *to_be_finalized; // list of objects that need finalization
static struct oobj *all_objects[2]; // one for from space, one for to space
// objects that don't define a finalizer don't need to be added to all_objects

/** Garbage collectors for weak references add themselves to this list, which
 * we traverse *last*.  Broken hearts in the fields of weak references then
 * correspond to live objects; uncopied objects are not live except through
 * the weak reference, and should be deleted. */
static struct oobj *weak_references;

/* objects that have fields whose type can be determined exactly at
 * compile time can hard-code the garbage-collection routine to avoid
 * the double-indirection. */
