#ifndef ROLE_INFER
#define ROLE_INFER

#include "ObjectSet.h"
#include "ObjectPair.h"
#include "Hashtable.h"
#include "GenericHashtable.h"


struct heap_object {
  char *class;
  long long uid;
  struct fieldlist *fl;
  struct arraylist *al;
  struct referencelist *rl;/*reachable from these roots*/

  struct fieldlist *reversefield;/* Objects pointing at us*/
  struct arraylist *reversearray;

  short reachable;/* low order bit=reachable*/
  /* next bit=root*/
};

#define stringsize 1000
#define compilernamesize 100
#define sourcenamesize 200


struct method {
  char classname[stringsize];
  char methodname[stringsize];
  char signature[stringsize];
  struct method *caller;
  struct localvars *lv;
  struct heap_object ** params;
  struct rolemethod * rm;
  int numobjectargs;
  short isStatic;
};

struct killtuplelist {
  struct heap_object * ho;
  struct referencelist * rl;
  struct killtuplelist * next;
  short reachable;
};

struct referencelist {
  struct globallist *gl;
  struct localvars *lv;
  struct referencelist *next;
};

struct globallist {
  char *classname;
  char *fieldname;
  struct heap_object *object;
  struct globallist * next;
  long long age;
  short invalid;
};

struct fieldlist {
  char *fieldname;
  struct heap_object *src;
  struct fieldlist * dstnext;
  struct heap_object *object;
  struct fieldlist * next;
};

struct arraylist {
  int index;
  struct heap_object *src;
  struct arraylist *dstnext;
  struct heap_object *object;
  struct arraylist * next;
};

struct localvars {
  struct heap_object *object;
  long int linenumber;
  char name[sourcenamesize];
  char sourcename[compilernamesize];
  struct method *m;
  int lvnumber;
  struct localvars *next;
  long long age;
  short invalid;
};

struct heap_state {
  /* Pointer to top of method stack */
  struct method *methodlist;
  struct globallist *gl;
  struct referencelist *newreferences;

  struct method *freemethodlist;
  struct referencelist *freelist;
  struct objectpair * K;
  struct objectset * N;

  struct genhashtable *roletable;
  struct genhashtable *methodtable;
};

struct identity_relation {
  char * fieldname1;
  char * fieldname2;
  struct identity_relation * next;
};

void doincrementalreachability(struct heap_state *hs, struct hashtable *ht);
struct objectset * dokills(struct heap_state *hs);
void donews(struct heap_state *hs, struct objectset * os);
void removelvlist(struct heap_state *, char * lvname, struct method * method);
void addtolvlist(struct heap_state *,struct localvars *, struct method *);
void freemethod(struct heap_state *heap, struct method * m);
void getfile();
void doanalysis();
char *getline();
char * copystr(const char *);
void showmethodstack(struct heap_state * heap);
void printmethod(struct method m);
void dofieldassignment(struct heap_state *hs, struct heap_object * src, char * field, struct heap_object * dst);
void doarrayassignment(struct heap_state *hs, struct heap_object * src, int lindex, struct heap_object *dst);
void doglobalassignment(struct heap_state *hs, char * class, char * field, struct heap_object * dst);
void doaddfield(struct heap_state *hs, struct heap_object *ho);
void dodelfield(struct heap_state *hs, struct heap_object *src,struct heap_object *dst);
void freelv(struct heap_state *hs,struct localvars * lv);
void freeglb(struct heap_state *hs,struct globallist * glb);
void dodellvfield(struct heap_state *hs, struct localvars *src, struct heap_object *dst);
void dodelglbfield(struct heap_state *hs, struct globallist *src, struct heap_object *dst);
int matchrl(struct referencelist *key, struct referencelist *list);
void freekilltuplelist(struct killtuplelist * tl);
void doaddglobal(struct heap_state *hs, struct globallist *gl);
void doaddlocal(struct heap_state *hs, struct localvars *lv);
void propagaterinfo(struct objectset * set, struct heap_object *src, struct heap_object *dst);
int lvnumber(char *lv);
int matchlist(struct referencelist *list1, struct referencelist *list2);
void removereversearrayreference(struct arraylist * al);
void removereversefieldreference(struct fieldlist * al);
void removeforwardarrayreference(struct arraylist * al);
void removeforwardfieldreference(struct fieldlist * al);
void freemethodlist(struct heap_state *hs);
void calculatenumobjects(struct method * m);
void doreturnmethodinference(struct heap_state *heap, long long uid, struct hashtable *ht);
#endif
