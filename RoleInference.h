#ifndef ROLE_INFER
#define ROLE_INFER

#include "ObjectSet.h"
#include "ObjectPair.h"
#include "Hashtable.h"
#include "GenericHashtable.h"
#include "Names.h"

#define EFFECTS

struct heap_object {
  struct classname *class; /*class of object*/
  long long uid; /* unique id of object */
  struct fieldlist *fl; /*list of objects field*/
  struct arraylist *al; /*list of objects array elements*/
  struct referencelist *rl;/*reachable from these roots*/

  struct fieldlist *reversefield;/* Objects pointing at us*/
  struct arraylist *reversearray;/* arrays pointing at us*/

  struct rolechange *rc; /* track role changes outside of methods*/
  int * methodscalled; /* array of methods called on heap object*/


  short reachable;/* low order bit=reachable*/
  /* next bit=root*/
};

#define stringsize 1000
#define compilernamesize 100
#define sourcenamesize 200


struct method {
  struct methodname * methodname; /* method name structure*/
  struct method *caller; /* caller of this method*/
  struct localvars *lv; /* list of this methods local variables */
  struct heap_object ** params; /* List of parameters called with */
  struct rolemethod * rm; /* Appropriate role instantiated method for this method*/
  int numobjectargs; /* Number of object type arguments*/
  short isStatic; /* Is method static */
#ifdef EFFECTS
  struct hashtable * pathtable; /* table of paths for effect use*/
  struct effectlist *effects; /* list of effects for methods */
#endif
};

/* Data structure to keep track of references to kill in kill stage of
   incremental reachability analysis*/
struct killtuplelist {
  struct heap_object * ho;
  struct referencelist * rl;
  struct killtuplelist * next;
  short reachable; /* Native reachable bits */
};

struct referencelist {
  struct globallist *gl;
  struct localvars *lv;
  struct referencelist *next;
};

struct globallist {
  struct fieldname * fieldname;
  struct heap_object *object;
  struct globallist * next;
  long long age;
  short invalid;
};

struct fieldlist {
  struct fieldname * fieldname;
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
  short invalid;/* if 1, then the reference isn't valid anymore...it has been killed*/
};

struct heap_state {
  /* Pointer to top of method stack */
  struct namer * namer;
  struct method *methodlist;
  struct globallist *gl;
  struct referencelist *newreferences;

  struct method *freemethodlist;
  struct referencelist *freelist;
  struct objectpair * K;
  struct objectset * N;

  struct genhashtable *roletable;
  struct genhashtable *reverseroletable;
  struct genhashtable *methodtable;

  long long currentmethodcount;
  struct hashtable *dynamiccallchain;
  struct genhashtable *rolechangetable;
  struct genhashtable *atomicmethodtable;
  
  struct genhashtable *statechangemethodtable;
  struct hashtable *statechangereversetable;
  int statechangesize;
};

struct identity_relation {
  struct fieldname * fieldname1;
  struct fieldname * fieldname2;
  struct identity_relation * next;
};

struct dynamiccallmethod {
  struct methodname * methodname;
  struct methodname * methodnameto;

  char status; /* 0=entry, 1=exit */
};

struct statechangeinfo {
  int id;
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
void dofieldassignment(struct heap_state *hs, struct heap_object * src, struct fieldname * field, struct heap_object * dst);
void doarrayassignment(struct heap_state *hs, struct heap_object * src, int lindex, struct heap_object *dst);
void doglobalassignment(struct heap_state *hs, struct fieldname * field, struct heap_object * dst);
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
void recordentry(struct heap_state *heap, struct methodname *methodname);
void recordexit(struct heap_state *heap);
int atomic(struct heap_state *heap);
void loadatomics(struct heap_state *heap);
int atomicmethod(struct heap_state *hs, struct method *m);
void atomiceval(struct heap_state *heap);
void loadstatechange(struct heap_state *heap);
int convertnumberingobjects(char *sig, int isStatic, int orignumber);
void dccfree(struct dynamiccallmethod *dcm);
#endif
