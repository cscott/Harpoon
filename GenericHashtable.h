#ifndef GENHASHTABLE
#define GENHASHTABLE
#define gennumbins 10000

struct genhashtable {
  void * bins[gennumbins];
};


struct genpointerlist {
  void * src;
  void * object;
  struct genpointerlist * next;
};


int genputtable(struct genhashtable *, void *, void *);
void * gengettable(struct genhashtable *, void *);
int genhashfunction(void *);
struct genhashtable * genallocatehashtable();
void genfreehashtable(struct genhashtable * ht);
void genfreekeyhashtable(struct genhashtable * ht);
void genfreekey(struct genhashtable *ht, void *);
#endif
