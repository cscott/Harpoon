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
  for (i=0;i<rm->numobjectargs;i++) {
    struct rolereturnstate *rs=rm->returnstates;
    if (rm->paramroles[i]!=NULL)
      while(rs!=NULL) {
	char buf[600],buf1[100];
	int place=convertnumberingobjects(rm->signature, rm->isStatic, i);
	int total=convertnumberingobjects(rm->signature, rm->isStatic, -1);
	int ii=0,j=0;
	struct role* role=(struct role *)gengettable(reverseroletable, rm->paramroles[i]);
	for(ii=0;ii<place;ii++) 
	  buf1[j++]=',';
	buf1[j++]='t';	buf1[j++]='h';	buf1[j++]='i';	buf1[j++]='s';
	for(;ii<(total-1);ii++)
	  buf1[j++]=',';
	buf1[j++]=0;
	sprintf(buf, " %s.%s\\n%s(%s)", rm->classname, rm->methodname, rm->signature,buf1);
	
	addtransition(htable, role->class, rm->paramroles[i],
		      buf ,rs->paramroles[i],0);
	rs=rs->next;
      }
  }
}

void dotrolechange(struct genhashtable *htable, struct heap_state *hs,
		   struct rolechange *rc) {
  char *role1=rc->origrole;
  char *role2=rc->newrole;
  int depth=0,bdepth;
  char buf[600];
  int i;

  struct dynamiccallmethod *dcm=(struct dynamiccallmethod *) gettable(hs->dynamiccallchain, rc->origmethod-1); 
  
  struct role* role=(struct role *)gengettable(hs->reverseroletable, role1);
  /*rc is filled in now...*/
  /*We should store it...*/
  if (dcm->status==0) {
    depth=1;
    bdepth=1;
    sprintf(buf,"%s.%s\\n%s", dcm->classname, dcm->methodname, dcm->signature);
  } else {
    depth=-1;
    bdepth=-1;
    sprintf(buf,"%s.%s\\n%s", dcm->classnameto, dcm->methodnameto, dcm->signatureto);
  }
  

  for(i=rc->origmethod;i<rc->newmethod;i++) {
      struct dynamiccallmethod * dcm=(struct dynamiccallmethod *) gettable(hs->dynamiccallchain, i);
      if (dcm->status==0)
	depth++;
      else {
	depth--;
	if (depth<bdepth)
	  sprintf(buf,"%s.%s\\n%s", dcm->classnameto, dcm->methodnameto, dcm->signatureto);
      }
  }
  addtransition(htable, role->class, role1, buf, role2, 1);
}


void addtransition(struct genhashtable *htable, char *class, char *role1, char * transitionname, char *role2, int type) {
  struct dotclass *dot=NULL;
  struct dottransition *trans=NULL,*oldtrans=NULL;
  if (gencontains(htable, class))
    dot=(struct dotclass *)gengettable(htable, class);
  else {
    dot=(struct dotclass *) calloc(1, sizeof(struct dotclass));
    genputtable(htable, copystr(class), dot);
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

void printdot(char *class, struct dotclass *dotclass) {
  struct dottransition *dot=dotclass->transitions;
  printf("digraph \"%s\" {\n",class);
  printf("ratio=auto\n");
  while(dot!=NULL) {
    if (dot->type==0) {
      printf("  %s -> %s [fontsize=10,label=\"%s", dot->role1,dot->role2, dot->transitionname);
      {
	struct dottransition *n=dot->same;
	while(n!=NULL) {
	  printf(",\\n%s",n->transitionname);
	  n=n->same;
	}
      }
      printf("\"]\n");
    } else {
      printf("  %s -> %s [fontsize=10,style=dotted,label=\"%s", dot->role1,dot->role2, dot->transitionname);
      {
	struct dottransition *n=dot->same;
	while(n!=NULL) {
	  printf(",\\n%s",n->transitionname);
	  n=n->same;
	}
      }
      printf("\"]\n");
    }
    dot=dot->next;
  }
  printf("}\n");
}
