#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
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
    
    

    method=method->caller;
  }
}

struct effectregexpr * buildregexpr(struct hashtable *pathtable, long long uid) {
  struct regexprlist *rel=NULL;
  struct path * prev=NULL;
  while(uid!=-1) {
    prev=gettable(pathtable, uid);
    if (path->prev_obj!=-1) {
      if (rel->
      struct regexprlist *ere=(struct regexprlist *) calloc(1, sizeof(struct regexprlist));
      ere->
    }
    uid=path->prev_obj;
  }

}
