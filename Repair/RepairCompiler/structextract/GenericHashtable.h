// implements a generic hash table

#ifndef GENHASHTABLE
#define GENHASHTABLE
#define geninitialnumbins 10
#define bool int

struct genhashtable {
  unsigned int (*hash_function)(void *);
  int (*comp_function)(void *,void *);
  struct genpointerlist ** bins;
  long counter;
  int currentsize;
  struct genpointerlist *list;
  struct genpointerlist *last;
};


struct genpointerlist {
  void * src;
  void * object;
  struct genpointerlist * next;

  struct genpointerlist * inext;
  struct genpointerlist * iprev;
};


struct geniterator {
  struct genpointerlist * ptr;
  bool finished;
};

struct genhashtable * genallocatehashtable(unsigned int (*hash_function)(void *),int (*comp_function)(void *,void *));
void genfreehashtable(struct genhashtable * ht);

void * getnext(struct genhashtable *,void *);
int genputtable(struct genhashtable *, void *, void *);
void * gengettable(struct genhashtable *, void *);
int gencontains(struct genhashtable *, void *);
unsigned int genhashfunction(struct genhashtable *,void *);

int hashsize(struct genhashtable * ht);
void genfreekey(struct genhashtable *ht, void *);
struct geniterator * gengetiterator(struct genhashtable *ht);
void * gennext(struct geniterator *it);
void genfreeiterator(struct geniterator *it);
#endif



