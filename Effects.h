#ifndef EFFECTS_H
#define EFFECTS_H

#include "RoleInference.h"

void addpath(struct heap_state *hs, long long obj, struct fieldname * field, struct fielddesc * fielddesc, long long dstobj);
void addarraypath(struct heap_state *hs, struct hashtable * ht, long long obj, long long dstobj);

/* Path data structure*/
/* Cases:(nonmention=NULL)*/
/* Global */
/* prev_obj=-1, paramnum=-1, fieldname=global*/

/* New object */
/* paramnum=-1, prev_obj=-1*/

/* Normal */
/* fielddesc=fielddesc, fieldname=fieldname, paramnum=-1, prev_obj=object*/

/* Param*/
/* paramnum=paramnum, prev_obj=-1 */

/* Array */
/* fielddesc=NULL, field=(class, []), paramnum=-1, prev_obj=array object*/

/* Out of hat */
/* prev_obj=-1, paramnum =-2*/
struct path {
  int paramnum;
  long long prev_obj;
  struct fieldname * fieldname;
  struct fielddesc * fielddesc;
};

struct epointerlist {
  void * object;
  struct epointerlist * next;
};

/* Effect data structure */
/* src.fieldname=dst */
/* or if fieldname==NULL then src[]=dst */
/* or if src==NULL then read(dst) */
struct effectlist {
  struct effectregexpr *src;
  struct fieldname *fieldname;
  struct effectregexpr *dst;
  struct effectlist * next;
};

struct effectregexpr {
  char flag; /* 0=Normal, 1=NEW, 2=Native reached object*/
  int paramnum; /*parameter number if !=-1...in which case flag=0*/
  struct fieldname * globalname;/* global name...if paramnum=-1, flag=0*/
  struct regexprlist *expr;
};

struct regexprlist {
  char multiplicity; /* 0=1, 1=many */
  struct classname * classname; /* Either the fields or the subtree of regexpr's must */
  struct fielddesc *fielddesc; /* be defined in classname and end in a pointer of type fielddesc*/
  struct regfieldlist * fields;
  struct listofregexprlist * subtree;
  struct regexprlist * nextreg;
};

struct listofregexprlist {
  struct regexprlist *expr;
  struct listofregexprlist * nextlist;
};

struct regfieldlist {
  struct fielddesc * fielddesc;
  struct fieldname * fieldname;
  struct regfieldlist * nextfld;
};

struct effectregexpr * buildregexpr(struct hashtable *pathtable, long long uid);
struct effectlist * mergeeffectlist(struct effectlist * el1, struct effectlist *el2);
void freeeffectlist(struct effectlist *el);
void freeeffectregexpr(struct effectregexpr *ere);
void freeeffects(struct path *);
void initializepaths(struct heap_state *hs);
void addeffect(struct heap_state *heap, long long suid, struct fieldname * fieldname, long long duid);
void initloopstructures();
void freeregexprlist(struct regexprlist *tofree);
struct effectregexpr * mergeeffectregexpr(struct effectregexpr * ere1, struct effectregexpr * ere2);
struct regexprlist * mergeregexprlist(struct regexprlist * rel1, struct regexprlist *rel2);
void printregexprlist(struct regexprlist *rel);
void printeffectregexpr(struct effectregexpr *ere);
void printeffectlist(struct effectlist *el);
void addnewobjpath(struct heap_state *hs, long long obj);
struct effectlist * mergemultipleeffectlist(struct effectlist *el1, struct effectlist *el2);
void updateroleeffects(struct heap_state *heap);
#endif
