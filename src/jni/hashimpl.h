/* BORROWED FROM src/mzf/hashimpl.h; BUGS SHOULD BE FIXED IN BOTH PLACES */
/* in this version, gc of hashtable is disabled; inflation done manually */
#ifndef TABLE_SIZE
#define TABLE_SIZE 10240709 /* a prime */
#endif
#ifndef HASH
#define HASH(key,obj) ((((int)key)+((int)obj))%TABLE_SIZE)
#endif
#ifndef HASH2
#define HASH2(key,obj) (TABLE_SIZE-2 -((((int)key)+((int)obj))%(TABLE_SIZE-2)))
#endif

/* #define MAKE_POINTER_VERSION to make a version that doesn't prevent
 * objects or values from being garbage collected (i'm assuming keys
 * are statically allocated).  Only non-null values are considered
 * legit (i.e. 'default_value' should always be NULL). */

struct TABLE_ELEMENT {
  void *key, *obj;
  TYPE value;
} TABLE[TABLE_SIZE]; /* should be initialized to all zeroes */
/* tombstone has obj==null, but key non-null. */
/* (when MAKE_POINTER_VERSION, tombstone has value==null) */

#define hideobj(x) ((void *) HIDE_POINTER(obj))

#ifdef MAKE_POINTER_VERSION
# define TOMB value
#else
# define TOMB obj
#endif

/* double hashing */

static TYPE GET(void *key, void *obj, TYPE default_value) {
  struct TABLE_ELEMENT *t;
  int hash = HASH(key, obj), ohash=hash;
  int u = HASH2(key, obj);
  obj = hideobj(obj);
  for (t=&TABLE[hash]; t->key!=NULL; t=&TABLE[hash]) {
    if (t->key==key && t->obj==obj) return t->value;
    hash = (hash+u) % TABLE_SIZE;
    assert(hash!=ohash); /* table should not fill up */
  }
  return default_value;
}
static void REMOVE(void *key, void *obj) {
  struct TABLE_ELEMENT *t;
  int hash = HASH(key, obj), ohash=hash;
  int u = HASH2(key, obj);
  obj = hideobj(obj);
  for (t=&TABLE[hash]; t->key!=NULL; t=&TABLE[hash]) {
    if (t->key==key && t->obj==obj) { /* found it */
      t->TOMB = NULL; /* leave a tombstone */
#if 0
      GC_unregister_disappearing_link(&(t->TOMB));
#endif
      return;
    }
    hash = (hash+u) % TABLE_SIZE;
    assert(hash!=ohash); /* table should not fill up */
  }
  /* not found */
  return;
}
static void SET(void *key, void *obj, TYPE newval, TYPE default_value) {
  struct TABLE_ELEMENT *t;
  int hash, ohash, u;
#ifdef MAKE_POINTER_VERSION
  assert(default_value==NULL); /* doesn't work otherwise */
#endif
  if (newval==default_value) {
    REMOVE(key, obj);
    return;
  }
  hash = ohash = HASH(key, obj);
  u = HASH2(key, obj);
  for (t=&TABLE[hash]; t->TOMB!=NULL; t=&TABLE[hash]) {
    if (t->key==key && t->obj==hideobj(obj)) {
      /* found old value */
      t->value = newval;
      return;
    }
    /* continue probing */
    hash = (hash+u) % TABLE_SIZE;
    assert(hash!=ohash); /* table should not fill up */
  }
  /* found either an empty spot or a tombstone */
  t->key = key; t->obj = hideobj(obj); t->value = newval;
#if 0
  GC_general_register_disappearing_link(&(t->TOMB), obj);
#endif
  return;
}

#undef hideobj
#undef TOMB
