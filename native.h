/** Data structures for native java compilation. */

/** Field info. */
union field {
  int i; // also used for char, boolean, etc.
  float f;
  // long and double are composed of two fields (what about 64-bit machines?)
};
/** Object information. */
struct oobj {
  union field field[n];
  int nfields;
  struct claz *claz; // pointer actually points here.
  int hashcode; // initialized on allocation.
  int nrefs;
  struct oobj *objectref[n];
}
struct objarray {
  int length;
  int nfields = 1;
  struct claz *claz;
  int hashcode;
  int nrefs = LENGTH;
  struct oobj *element[n];
}
struct primitivearray {
  union field element[n];
  int length;
  int nfields = 1+LENGTH;
  struct claz *claz;
  int hashcode;
  int nrefs = 0;
}

struct claz {
  method_t interface[n]; // many
  struct clazinfo *clazinfo; // pointer actually points to middle.
  struct interfz *(interfaces[NUM_INTER+1]); //ptr to null-term lst of interfaces.
  struct claz display[MAX_DEPTH];
  method_t method[n]; // many
}
struct interfz {
  char *name;
  // also tables for reflection.
}
struct clazinfo {
  char *name;
  // also tables for reflection.
}
typedef union field (*method_t)();
