#ifndef GENHASHTABLE
#define GENHASHTABLE
#define gennumbins 10000

struct genhashtable {
  int (*hash_function)(void *);
  int (*comp_function)(void *,void *);
  struct genpointerlist * bins[gennumbins];
};


struct genpointerlist {
  void * src;
  void * object;
  struct genpointerlist * next;
};


struct geniterator {
  int binnumber;
  struct genhashtable *ht;
  struct genpointerlist * ptr;
};

int genputtable(struct genhashtable *, void *, void *);
void * gengettable(struct genhashtable *, void *);
int genhashfunction(struct genhashtable *,void *);
struct genhashtable * genallocatehashtable(int (*hash_function)(void *),int (*comp_function)(void *,void *));
void genfreehashtable(struct genhashtable * ht);
void genfreekeyhashtable(struct genhashtable * ht);
void genfreekey(struct genhashtable *ht, void *);
struct geniterator * gengetiterator(struct genhashtable *ht);
void * gennext(struct geniterator *it);
void genfreeiterator(struct geniterator *it);
#endif



