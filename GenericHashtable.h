#ifndef GENHASHTABLE
#define GENHASHTABLE
#define gennumbins 10000

struct genhashtable {
  int (*hash_function)(void *);
  int (*comp_function)(void *,void *);
  void * bins[gennumbins];
};


struct genpointerlist {
  void * src;
  void * object;
  struct genpointerlist * next;
};


int genputtable(struct genhashtable *, void *, void *);
void * gengettable(struct genhashtable *, void *);
int genhashfunction(struct genhashtable *,void *);
struct genhashtable * genallocatehashtable(int (*hash_function)(void *),int (*comp_function)(void *,void *));
void genfreehashtable(struct genhashtable * ht);
void genfreekeyhashtable(struct genhashtable * ht);
void genfreekey(struct genhashtable *ht, void *);
#endif



