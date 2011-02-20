#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include "RoleInference.h"
#include "Method.h"
#include "dot.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

void dotrolemethod(struct genhashtable * htable, struct genhashtable *reverseroletable, struct rolemethod *rm) {
  int i;
  struct geniterator * it=gengetiterator(rm->rolechanges);
  while(1) {
    struct rolechangesum * rcs=(struct rolechangesum *) gennext(it);
    struct role* role;
    char buf[600];
    struct rolechangeheader *rch=NULL;
    if (rcs==NULL) break;
    rch=(struct rolechangeheader *)gengettable(rm->rolechanges,rcs);
    if (rch->inner) {
      role=(struct role *)gengettable(reverseroletable, rcs->origrole);
      sprintf(buf,"%s.%s\\n%s", rm->methodname->classname->classname,
	      rm->methodname->methodname, rm->methodname->signature);
      
      addtransition(htable, role->class, rcs->origrole,
		    buf ,rcs->newrole,1);
    }
  }


  for (i=0;i<rm->numobjectargs;i++) {
    struct rolereturnstate *rs=rm->returnstates;
    if (rm->paramroles[i]!=NULL)
      while(rs!=NULL) {
	char buf[600],buf1[100];
	int place=convertnumberingobjects(rm->methodname->signature, rm->isStatic, i);
	int total=convertnumberingobjects(rm->methodname->signature, rm->isStatic, -1);
	int ii=0,j=0;
	struct role* role=(struct role *)gengettable(reverseroletable, rm->paramroles[i]);
	for(ii=0;ii<place;ii++) 
	  buf1[j++]=',';
	buf1[j++]='t';	buf1[j++]='h';	buf1[j++]='i';	buf1[j++]='s';
	for(;ii<(total-1);ii++)
	  buf1[j++]=',';
	buf1[j++]=0;
	sprintf(buf, " %s.%s\\n%s(%s)", rm->methodname->classname->classname, rm->methodname->methodname, rm->methodname->signature,buf1);
	
	addtransition(htable, role->class, rm->paramroles[i],
		      buf ,rs->paramroles[i],0);
	rs=rs->next;
      }
  }
}

void addtransition(struct genhashtable *htable, struct classname *class, char *role1, char * transitionname, char *role2, int type) {
  struct dotclass *dot=NULL;
  struct dottransition *trans=NULL,*oldtrans=NULL;
  if (gencontains(htable, class))
    dot=(struct dotclass *)gengettable(htable, class);
  else {
    dot=(struct dotclass *) calloc(1, sizeof(struct dotclass));
    genputtable(htable, class, dot);
  }
  trans=dot->transitions;
  if(trans==NULL) {
    struct dottransition * newtrans=(struct dottransition *) calloc(1, sizeof(struct dottransition));
    newtrans->role1=copystr(role1);
    newtrans->role2=copystr(role2);
    newtrans->transitionname=copystr(transitionname);
    newtrans->type=type;
    newtrans->next=dot->transitions;
    dot->transitions=newtrans;
    return;
  }
  while(trans!=NULL) {
    if(equivalentstrings(trans->role1,role1)&& 
       equivalentstrings(trans->role2,role2)) {
      /*&& 
	equivalentstrings(trans->transitionname,transitionname)){*/
      if (type==1&&trans->type==1) {
	struct dottransition * newtrans=trans;
	while(newtrans!=NULL) {
	  if (equivalentstrings(newtrans->transitionname, transitionname))
	    return;
	  newtrans=newtrans->same;
	}
	newtrans=(struct dottransition *) calloc(1, sizeof(struct dottransition));
	newtrans->role1=copystr(role1);
	newtrans->role2=copystr(role2);
	newtrans->transitionname=copystr(transitionname);
	newtrans->type=type;
	newtrans->same=trans->same;
	trans->same=newtrans;
	return;	
      }
      if (type==1)
	return;
      if (type==0&&trans->type==0) {
	struct dottransition * newtrans=trans;
	while(newtrans!=NULL) {
	  if (equivalentstrings(newtrans->transitionname, transitionname))
	    return;
	  newtrans=newtrans->same;
	}
	newtrans=(struct dottransition *) calloc(1, sizeof(struct dottransition));
	newtrans->role1=copystr(role1);
	newtrans->role2=copystr(role2);
	newtrans->transitionname=copystr(transitionname);
	newtrans->type=type;
	newtrans->same=trans->same;
	trans->same=newtrans;
	return;
      }

      if (type==0&&trans->type==1) {
	/*	remove old element...*/
	struct dottransition *tmp=NULL;
	if (oldtrans==NULL) {
	  dot->transitions=trans->next;
	} else {
	  oldtrans->next=trans->next;
	}
	tmp=trans->next;
	while(trans!=NULL) {
	  struct dottransition *tmptmp=trans->same;
	  free(trans->role1);
	  free(trans->role2);
	  free(trans->transitionname);
	  free(trans);
	  trans=tmptmp;
	}
	trans=tmp;
	continue;
      }
    }
    oldtrans=trans;
    trans=trans->next;
  }
  {
    struct dottransition * newtrans=(struct dottransition *) calloc(1, sizeof(struct dottransition));
    newtrans->role1=copystr(role1);
    newtrans->role2=copystr(role2);
    newtrans->transitionname=copystr(transitionname);
    newtrans->type=type;
    newtrans->next=dot->transitions;
    dot->transitions=newtrans;
    return;
  }
}

void printdot(struct heap_state *heap,struct classname *class, struct dotclass *dotclass) {
  struct dottransition *dot=dotclass->transitions;
  fprintf(heap->dotfile,"digraph \"%s\" {\n",class->classname);
  fprintf(heap->dotfile,"ratio=auto\n");
  while(dot!=NULL) {
    if (dot->type==0) {
      fprintf(heap->dotfile,"  %s -> %s [fontsize=10,label=\"%s", dot->role1,dot->role2, dot->transitionname);
      {
	struct dottransition *n=dot->same;
	while(n!=NULL) {
	  fprintf(heap->dotfile,",\\n%s",n->transitionname);
	  n=n->same;
	}
      }
      fprintf(heap->dotfile,"\"]\n");
    } else {
      fprintf(heap->dotfile,"  %s -> %s [fontsize=10,style=dotted,label=\"%s", dot->role1,dot->role2, dot->transitionname);
      {
	struct dottransition *n=dot->same;
	while(n!=NULL) {
	  fprintf(heap->dotfile,",\\n%s",n->transitionname);
	  n=n->same;
	}
      }
      fprintf(heap->dotfile,"\"]\n");
    }
    dot=dot->next;
  }
  fprintf(heap->dotfile,"}\n");
}
