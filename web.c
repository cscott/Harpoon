#include "web.h"
#include "RoleRelation.h"
#include "Method.h"

void outputweb(struct heap_state *heap, struct genhashtable *dotmethodtable) {
  struct geniterator *it=gengetiterator(heap->methodtable);
  FILE *methodfile=fopen("webmethod","w");
  FILE *rolefile=fopen("webrole","w");
  FILE *transitions=fopen("webtransitions","w");

  while (1) {
    struct rolemethod *method=(struct rolemethod *) gennext(it);
    if (method==NULL)
      break;
    printwebmethod(methodfile, heap, method);
  }
  genfreeiterator(it);
  fprintf(methodfile, "~~\n");

  it=gengetiterator(heap->roletable);
  while(1) {
    struct role *role=(struct role *) gennext(it);
    char *rolename;
    if (role==NULL)
      break;
    rolename=gengettable(heap->roletable, role);
    printwebrole(rolefile, heap,role, rolename);
  }
  genfreeiterator(it);
  fprintf(rolefile, "~~\n");

  it=gengetiterator(dotmethodtable);
  while(1) {
    struct classname *class=(struct classname *)gennext(it);
    struct dotclass *dotclass=NULL;
    if (class==NULL)
      break;
    dotclass=(struct dotclass *) gengettable(dotmethodtable, class);
    printwebdot(transitions,heap,class, dotclass);
  }
  fprintf(transitions, "~~\n");
  genfreeiterator(it);
  outputwebrolerelations(heap);

  fclose(methodfile);
  fclose(rolefile);
  fclose(transitions);
}


void outputwebrolerelations(struct heap_state *heap) {
  struct geniterator *it=gengetiterator(heap->rolereferencetable);
  /* FREEME*/
  FILE * rolediagramfile=fopen("webdiagram","w");

  while(1) {
    struct rolerelation *rr=(struct rolerelation *)gennext(it);
    char srcname[30];
    if (rr==NULL)
      break;
    sprintf(srcname, "R%d", rr->srcrole);
    {
      struct role *srcrole=(struct role *)gengettable(heap->reverseroletable,srcname);
      struct rolefieldlist *rfl=srcrole->nonnullfields;
      struct rolearraylist *ral=srcrole->nonnullarrays;
      
      while(rfl!=NULL) {
	if ((rfl->field==rr->field)&&
	    (rfl->role==rr->dstrole))
	  break;
	rfl=rfl->next;
      }
      while(ral!=NULL) {
	if (ral->role==rr->dstrole)
	  break;
	ral=ral->next;
      }

      if (rfl==NULL&&ral==NULL) {
	fprintf(rolediagramfile,"%d %d 0 %s ",rr->srcrole, rr->dstrole,rr->field->fieldname);
      } else {
	fprintf(rolediagramfile,"%d %d 1 %s",rr->srcrole, rr->dstrole, rr->field->fieldname);
      }
    }
  }
  genfreeiterator(it);
  fclose(rolediagramfile);
}


void printwebrole(FILE *rolefile, struct heap_state *heap,struct role *r, char * rolename) {
  struct rolereferencelist *dominators=r->dominatingroots;

  fprintf(rolefile,"%d ",parserolestring(rolename));
  fprintf(rolefile,"%s ", r->class->classname);

  fprintf(rolefile, "%d ",r->contained);

  while(dominators!=NULL) {
    if (dominators->methodname!=NULL) {
      fprintf(rolefile,"0 %s %s %s.%s%s:%d ",dominators->lvname, dominators->sourcename, dominators->methodname->classname->classname, dominators->methodname->methodname, dominators->methodname->signature,dominators->linenumber);
    } else {
      fprintf(rolefile,"1 %s.%s ",dominators->globalname->classname->classname, dominators->globalname->fieldname);
    }
    dominators=dominators->next;
  }

  fprintf(rolefile, "~~ ");

  {
    struct rolefieldlist *fl=r->pointedtofl;
    struct rolearraylist *al=r->pointedtoal;
    while(fl!=NULL) {
      fprintf(rolefile,"%s %s %d ",fl->field->fieldname, fl->field->classname->classname, fl->duplicates+1);
      fl=fl->next;
    }
    fprintf(rolefile,"~~ ");
    while(al!=NULL) {
      fprintf(rolefile,"%s %d ", al->class->classname, al->duplicates+1);
      al=al->next;
    }
    fprintf(rolefile,"~~ ");
  }

  {
    struct identity_relation *ir=r->identities;
    print_webidentities(rolefile, heap,ir);
  }

  {
    struct rolefieldlist *fl=r->nonnullfields;
    while(fl!=NULL) {
      fprintf(rolefile,"%s %d ",fl->field->fieldname,fl->role);
      fl=fl->next;
    }
    fprintf(rolefile,"~~ ");
  }

  {
    struct rolearraylist *al=r->nonnullarrays;
    while(al!=NULL) {
      fprintf(rolefile,"%s %d %d ",al->class->classname, al->role, al->duplicates+1);
      al=al->next;
    }
    fprintf(rolefile,"~~ ");
  }

  {
    if (r->methodscalled!=NULL) {
      int i=0;
      for(i=0;i<heap->statechangesize;i++) {
	if(r->methodscalled[i]) {
	  char *methodname=(char *)gettable(heap->statechangereversetable, i);
	  fprintf(heap->rolefile,"%s ",methodname);
	}
      }
    }
    fprintf(rolefile,"~~\n");
  }
}

void printwebmethod(FILE *methodfile, struct heap_state *heap, struct rolemethod *method) {
  int i;
  struct rolereturnstate *rrs=method->returnstates;

  fprintf(methodfile," %s.%s%s ",method->methodname->classname->classname,method->methodname->methodname,method->methodname->signature);
  fprintf(methodfile," %d ",method->isStatic);
  fprintf(methodfile, " %d ",method->numobjectargs);

  for(i=0;i<method->numobjectargs;i++)
    fprintf(methodfile,"%d ",parserolestring(method->paramroles[i]));



  while(rrs!=NULL) {
    for(i=0;i<method->numobjectargs;i++)
      fprintf(methodfile,"%d ",parserolestring(rrs->paramroles[i]));
    fprintf(methodfile," %d ",parserolestring(rrs->returnrole));
    rrs=rrs->next;
  }

  fprintf(methodfile,"~~ ");

  printwebrolechanges(methodfile, heap,method);
  fprintf(methodfile," \n");
}

void printwebrolechanges(FILE *methodfile, struct heap_state *heap, struct rolemethod *rm) {
  struct genhashtable *rolechanges=rm->rolechanges;
  struct geniterator *it=gengetiterator(rolechanges);
  while(1) {
    struct rolechangesum *rcs=(struct rolechangesum *)gennext(it);
    if (rcs==NULL) break;
    fprintf(methodfile, "%d %d ",parserolestring(rcs->origrole), parserolestring(rcs->newrole));
  }
  genfreeiterator(it);
}

void print_webidentities(FILE *rolefile, struct heap_state *heap,struct identity_relation *irptr) {
  while(irptr!=NULL) {
    fprintf(rolefile," %s %s ",irptr->fieldname1->fieldname,irptr->fieldname2->fieldname);
    irptr=irptr->next;
  }
  fprintf(rolefile,"~~ ");
}

void printwebdot(FILE *dotfile,struct heap_state *heap,struct classname *class, struct dotclass *dotclass) {
  struct dottransition *dot=dotclass->transitions;
  fprintf(dotfile,"%s ",class->classname);

  while(dot!=NULL) {
    fprintf(dotfile,"%d %d %d %s ", parserolestring(dot->role1),parserolestring(dot->role2), dot->type, dot->transitionname);
    {
      struct dottransition *n=dot->same;
      while(n!=NULL) {
	fprintf(dotfile,"%s ",n->transitionname);
	n=n->same;
      }
      fprintf(dotfile,"~~ ");
    }
    dot=dot->next;
  }
  fprintf(dotfile,"~~\n");
}
