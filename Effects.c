#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include "Effects.h"

void addpath(struct heap_state *hs, long long obj, char * class, char * field, long long dstobj) {
  struct method *method=hs->methodlist;
  while(method!=NULL) {
    struct hashtable * pathtable=method->pathtable;

    if (!contains(pathtable, dstobj)) {
      struct path * path=(struct path *) calloc(1,sizeof(struct path));
      path->prev_obj=obj;
      path->classname=copystr(class);
      path->fieldname=copystr(field);
      path->paramnum=-1;
      puttable(pathtable, dstobj, path);
    }
    
    method=method->caller;
  }
}

void freeeffects(struct path * pth) {
  free(pth->fieldname);
  free(pth->globalname);
  free(pth->classname);
  free(pth);
}

void initializepaths(struct heap_state *hs) {
  struct globallist *gl=hs->gl;
  hs->methodlist->pathtable=allocatehashtable();
  while(gl!=NULL) {
    long long uid=gl->object->uid;
    if(!contains(hs->methodlist->pathtable,uid)) {
      struct path * initpth=(struct path *)calloc(1, sizeof(struct path));
      initpth->prev_obj=-1;
      initpth->paramnum=-1;
      initpth->classname=copystr(gl->classname);
      initpth->globalname=copystr(gl->fieldname);
      puttable(hs->methodlist->pathtable, uid,initpth);
    }
    gl=gl->next;
  }
}

void addeffect(struct heap_state *heap, long long suid, char * fieldname, long long duid) {
  struct method * method=heap->methodlist;
  while(method!=NULL) {
    struct hashtable *pathtable=method->pathtable;
    struct effectregexpr* srcexpr=buildregexpr(pathtable, suid);
    struct effectregexpr* dstexpr=buildregexpr(pathtable, duid);
    struct effectlist * efflist=(struct effectlist *) calloc(1,sizeof(struct effectlist));
    efflist->fieldname=copystr(fieldname);
    efflist->src=srcexpr;
    efflist->dst=dstexpr;

    method=method->caller;
  }
}

void freeeffectregexpr(struct effectregexpr *ere) {
  struct regexprlist *tofree=ere->expr;
  while(tofree!=NULL) {
    struct regexprlist *tmpptr=tofree->nextreg;
    struct regfieldlist *rfl=tofree->fields;
    while(rfl!=NULL) {
      struct regfieldlist *tmpfldptr=rfl->nextfld;
      free(rfl->fieldname);
      free(rfl);
      rfl=tmpfldptr;
    }
    free(tofree->classname);
    free(tofree);
    tofree=tmpptr;
  }
  free(ere->classname);
  free(ere-globalname);
  free(ere);
}

struct effectregexpr * buildregexpr(struct hashtable *pathtable, long long uid) {
  struct regexprlist *rel=NULL;
  struct path * path=NULL;
  while(1) {
    path=gettable(pathtable, uid);
    if (path->prev_obj!=-1) {
      if ((rel==NULL)||(strcmp(rel->classname, path->classname)!=0)) {
	struct regexprlist *ere=(struct regexprlist *) calloc(1, sizeof(struct regexprlist));
	struct regfieldlist *rfl=(struct regfieldlist *) calloc(1, sizeof(struct regfieldlist));
	ere->multiplicity=0;
	ere->classname=copystr(path->classname);
	ere->fields=rfl;
	ere->nextreg=rel;
	rel=ere;
	rfl->fieldname=copystr(path->fieldname);
      } else {
	/* Class exists, simply add field */
	struct regfieldlist *rflptr=rel->fields;
	rel->multiplicity=1;
	while(rflptr!=NULL)
	  if (strcmp(rflptr->fieldname, path->fieldname)==0)
	    break;
	if (rflptr==NULL) {
	  struct regfieldlist *newfield=(struct regfieldlist *)calloc(1, sizeof(struct regfieldlist));
	  newfield->fieldname=copystr(path->fieldname);
	  newfield->nextfld=rel->fields;
	  rel->fields=newfield;
	}
      }
    } else {
      /* At top level...*/
      struct effectregexpr *src=(struct effectregexpr *) calloc(1, sizeof(struct effectregexpr));
      src->paramnum=path->paramnum;
      src->classname=copystr(path->classname);
      src->globalname=copystr(path->globalname);
      src->expr=rel;
      return src;
    }
    uid=path->prev_obj;
  }
}
