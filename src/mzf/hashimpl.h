#ifndef TABLE_SIZE
#define TABLE_SIZE 102407
#endif
#ifndef HASH
#define HASH(key,obj) ((((int)key)+((int)obj))%TABLE_SIZE)
#endif

struct TABLE_ELEMENT {
  void *key, *obj;
  TYPE value;
} TABLE[TABLE_SIZE]; /* should be initialized to all zeroes */
/* tombstone has obj==null, but key non-null. */

/* double hashing */

static TYPE GET(void *key, void *obj, TYPE default_value) {
  struct TABLE_ELEMENT *t;
  int hash = HASH(key, obj);
  for (t=&TABLE[hash]; t->key!=NULL; t=&TABLE[hash]) {
    if (t->key==key && t->obj==obj) return t->value;
    hash = (hash*hash) % TABLE_SIZE;
  }
  return default_value;
}
static void REMOVE(void *key, void *obj) {
  struct TABLE_ELEMENT *t;
  int hash = HASH(key, obj);
  for (t=&TABLE[hash]; t->key!=NULL; t=&TABLE[hash]) {
    if (t->key==key && t->obj==obj) { /* found it */
      t->obj = NULL; /* leave a tombstone */
      GC_unregister_disappearing_link(&(t->obj));
      return;
    }
    hash = (hash*hash) % TABLE_SIZE;
  }
  /* not found */
  return;
}
static void SET(void *key, void *obj, TYPE newval, TYPE default_value) {
  struct TABLE_ELEMENT *t;
  int hash;
  if (newval==default_value) {
    REMOVE(key, obj);
    return;
  }
  hash = HASH(key, obj);
  for (t=&TABLE[hash]; t->obj!=NULL; t=&TABLE[hash]) {
    hash = (hash*hash) % TABLE_SIZE;
  }
  /* found either an empty spot or a tombstone */
  t->key = key; t->obj = obj; t->value = newval;
  GC_general_register_disappearing_link(&(t->obj), obj);
  return;
}
