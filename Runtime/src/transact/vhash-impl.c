/** implementation of version cache; a simple hashtable.
 *  linear-probing.  no remove method (hence no tombstones).
 *  #define the valuetype
 */

#include "transact/preproc.h" /* Defines 'T()' macro. */
// could define DECL to be "extern inline"
#define DECL

#define NOT_HERE_VALUE (-1)

#if defined(IN_HEADER)
// rough struction description
struct T(version_hashtable) {
  unsigned int num_entries;
  unsigned int num_keys;
  jint offsets[0]; // actually NUM_ENTRIES entries.
  VALUETYPE values[0]; // don't trust where C says this is.
};

// prototypes
extern unsigned T(version_hash_sizeof)(unsigned entries);
extern void T(version_hash_init)
     (struct T(version_hashtable) *table, unsigned entries);
extern VALUETYPE T(version_hash_read)
     (struct T(version_hashtable) *table, unsigned offset);
extern int T(version_hash_write_alloc)
     (struct T(version_hashtable) *table, unsigned offset);
extern VALUETYPE * T(version_hash_write_loc)
     (struct T(version_hashtable) *table, unsigned offset);
extern void T(version_hash_write)
     (struct T(version_hashtable) *table, unsigned offset, VALUETYPE value);
extern int T(version_hash_copy)
     (struct T(version_hashtable) *dest, struct T(version_hashtable) *src);

extern VALUETYPE T(version_hash_read_Int_to)
     (struct version_hashtable_Int *table, unsigned offset);
extern void T(version_hash_write_Int_from)
     (struct version_hashtable_Int *table, unsigned offset, VALUETYPE value);
#endif /* IN_HEADER */


#if DO_HASH && !defined(IN_HEADER)
DECL unsigned T(version_hash_sizeof)(unsigned entries) {
  return sizeof(struct T(version_hashtable)) +
    entries*(sizeof(jint)+sizeof(VALUETYPE));
}
DECL void T(version_hash_init)(struct T(version_hashtable) *table,
			       unsigned entries) {
  table->num_entries = entries;
  table->num_keys = 0;
  memset(table->offsets, NOT_HERE_VALUE, sizeof(jint)*entries);
  memset(table->offsets+entries, TRANS_FLAG_Byte, sizeof(VALUETYPE)*entries);
}

// note: we don't allow offset==-1
DECL VALUETYPE T(version_hash_read)
     (struct T(version_hashtable) *table, unsigned offset) {
  unsigned int num_entries =  table->num_entries;
  unsigned int key_mask = num_entries - 1;//since num_entries is a power of two
  jint *offset_base =  table->offsets;
  VALUETYPE *value_base = (VALUETYPE*)(offset_base+num_entries);
  unsigned int hash_index = offset & key_mask;
  
  while(1) {
    jint hash_offset = offset_base[hash_index];
    if (hash_offset==offset) return value_base[hash_index];
    // if entry is not present, then it really *was* return FLAG
    if (hash_offset==NOT_HERE_VALUE) return T(TRANS_FLAG);
    // linear probing
    hash_index = (hash_index+1) & key_mask;
  }
}
/* allocate an entry in the hash table for the given offset.
 * returns 0 if the hash table needs to be enlarged. */
DECL int T(version_hash_write_alloc)
     (struct T(version_hashtable) *table, unsigned offset) {
  unsigned int num_entries =  table->num_entries;
  unsigned int key_mask = num_entries - 1;//since num_entries is a power of two
  jint *offset_base =  table->offsets;
  VALUETYPE *value_base = (VALUETYPE*)(offset_base+num_entries);
  unsigned int hash_index = offset & key_mask;
  int i=0;
  do {
    jint hash_offset = offset_base[hash_index];
    if (hash_offset == offset)// entry already exists
      return 1; // success.
    else if (hash_offset == NOT_HERE_VALUE) { // need to alloc entry here.
      // XXX this assertion is not thread-safe; remove.
      assert(value_base[hash_index]==T(TRANS_FLAG)); // init'd at alloc time.
      if (NOT_HERE_VALUE == LL(&offset_base[hash_index]) &&
	  SC(&offset_base[hash_index], offset))
	return 1; // successfully allocated this entry for 'offset'
    } else {
      // linear probing
      hash_index = (hash_index+1) & key_mask;
      i++;
    }
  } while (i<num_entries/2);
  // okay, need to resize the table to squeeze this one in.
  return 0;
}

/* write to the hash table.  an entry must have already been allocated. */
// note: we don't allow offset==0
DECL VALUETYPE * T(version_hash_write_loc)
     (struct T(version_hashtable) *table, unsigned offset) {
  unsigned int num_entries =  table->num_entries;
  unsigned int key_mask = num_entries - 1;//since num_entries is a power of two
  jint *offset_base =  table->offsets;
  VALUETYPE *value_base = (VALUETYPE*)(offset_base+num_entries);
  unsigned int hash_index = offset & key_mask;
  do {
    jint hash_offset = offset_base[hash_index];
    if (likely(hash_offset == offset)) { // found the entry
      // don't actually do the write, just return its location.
      return &value_base[hash_index]; // don't actually do the write
    }
    assert(hash_offset!=NOT_HERE_VALUE); // must have been allocated!
    // linear probing
    hash_index = (hash_index+1) & key_mask;
  } while (1);
}

DECL void T(version_hash_write)
     (struct T(version_hashtable) *table, unsigned offset, VALUETYPE value) {
  VALUETYPE *loc = T(version_hash_write_loc)(table, offset);
  *loc = value;
  return;
}

DECL int T(version_hash_copy)
     (struct T(version_hashtable) *dest, struct T(version_hashtable) *src) {
  // copy all keys/values from src to dest.  num_entries field
  // of src must have been set up; everything else ought to be zero.
  unsigned int num_entries =  src->num_entries;
  jint *offset_base =  src->offsets;
  VALUETYPE *value_base = (VALUETYPE*)(offset_base+num_entries);
  unsigned int hash_index;
  for (hash_index=0; hash_index < num_entries; hash_index++) {
    jint hash_offset = offset_base[hash_index];
    if (hash_offset!=NOT_HERE_VALUE) {
      if (unlikely(!T(version_hash_write_alloc)(dest, hash_offset)))
	return 0; // table not large enough!?!
      T(version_hash_write)(dest, hash_offset, value_base[hash_index]);
    }					  
  }
  return 1; // done
}

//----------------------------------------------------------------
// conversion methods:
//    to store a variety of types in an 'Int' hash table.
DECL VALUETYPE T(version_hash_read_Int_to)
     (struct version_hashtable_Int *table, unsigned offset) {
  return
    __builtin_choose_expr
    (sizeof(VALUETYPE) > sizeof(jint),
     ({
       union {
	 jint i[2]; VALUETYPE v;
       } u = {
	 .i = { version_hash_read_Int(table, offset),
		version_hash_read_Int(table, offset+4) }
       };
       u.v;
     }), __builtin_choose_expr
     (sizeof(VALUETYPE) == sizeof(jint) &&
      !__builtin_types_compatible_p(VALUETYPE,jint),
      ({
	union {
	  jint i; VALUETYPE v;
	} u = {
	  .i = version_hash_read_Int(table, offset)
	};
	u.v; // hopefully gcc will optimize this intelligently
      }),
      // for smaller (integer) types, a simple cast will do.
      (VALUETYPE) version_hash_read_Int(table, offset)
      ));
}

DECL void T(version_hash_write_Int_from)
     (struct version_hashtable_Int *table, unsigned offset, VALUETYPE value) {
  __builtin_choose_expr
    (sizeof(VALUETYPE) > sizeof(jint),
     ({
       union {
	 jint i[2]; VALUETYPE v;
       } u = { .v = value };
       version_hash_write_Int(table, offset, u.i[0]);
       version_hash_write_Int(table, offset+4, u.i[1]);
     }), __builtin_choose_expr
     (sizeof(VALUETYPE) == sizeof(jint) &&
      !__builtin_types_compatible_p(VALUETYPE,jint),
      ({
	union {
	  jint i; VALUETYPE v;
	} u = { .v = value };
	version_hash_write_Int(table, offset, u.i);
      }),
      // simple cast suffices
      version_hash_write_Int(table, offset, (jint) value)
      ));
}
#endif /* !IN_HEADER */

/* clean up after ourselves */
#undef TA
#undef A
#undef T
#undef x1
#undef x2
#undef DECL
