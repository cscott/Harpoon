#include <stdlib.h>
#include "RoleRelation.h"
#include "Role.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif

int rolerelationhashcode(struct rolerelation *rr) {
  int hashcode=rr->srcrole;
  hashcode^=hashptr(rr->field);
  hashcode^=rr->dstrole;
  return hashcode;
}

int equivalentrolerelations(struct rolerelation *rr1, struct rolerelation *rr2) {
  if ((rr1->srcrole==rr2->srcrole)&&
      (rr1->field==rr2->field)&&
      (rr1->dstrole==rr2->dstrole))
    return 1;
  else
    return 0;
}

void addrolerelation(struct heap_state *heap, struct heap_object *src, struct fieldname *field, struct heap_object *dst) {
  int srcrole=parserolestring(src->rc->origrole);
  int dstrole=parserolestring(dst->rc->origrole);
  struct rolerelation rr={srcrole,field,dstrole};
  if (!gencontains(heap->rolereferencetable,&rr)) {
    struct rolerelation *rnew=(struct rolerelation *)calloc(1,sizeof(struct rolerelation));
    rnew->srcrole=srcrole;
    rnew->field=field;
    rnew->dstrole=dstrole;
    genputtable(heap->rolereferencetable, rnew,NULL);
  }
}

void addentry(struct hashtable *h, int origrole, int newrole) {
  if (!contains(h,origrole)) {
    struct intlist *nr=(struct intlist *)calloc(1,sizeof (struct intlist));
    nr->element=newrole;
    puttable(h, origrole, nr);
  } else {
    struct intlist *nr=(struct intlist *)gettable(h, origrole);
    struct intlist *nrlast=NULL;
    while(nr!=NULL) {
      if (nr->element==newrole)
	return;
      nrlast=nr;
      nr=nr->next;
    }
    nr=(struct intlist *)calloc(1,sizeof (struct intlist));
    nr->element=newrole;
    nrlast->next=nr;
  }
}

struct intlist * remap(struct hashtable *h, int origrole) {
  struct intlist *retval=NULL;
  struct intlist *todo=NULL;
  struct intlist *tmp=NULL;
  if (contains(h,origrole))
    tmp=(struct intlist *) gettable(h,origrole);
  else {
    struct intlist *nl=(struct intlist *)calloc(1,sizeof(struct intlist));
    nl->element=origrole;
    return nl;
  }

  while(tmp!=NULL) {
    struct intlist *nl=(struct intlist *)calloc(1,sizeof(struct intlist));
    nl->element=tmp->element;
    nl->next=todo;
    todo=nl;
    tmp=tmp->next;
  }

  while(todo!=NULL) {
    struct intlist *todoold=todo;
    todo=todo->next;
    if (contains(h, todoold->element)) {
      struct intlist *tmp=(struct intlist *) gettable(h,todoold->element);
      while(tmp!=NULL) {
	struct intlist *searchptr=todoold;
	while(searchptr!=NULL) {
	  if(searchptr->element==tmp->element)
	    break;
	  searchptr=searchptr->next;
	}
	if (searchptr==NULL) {
	  struct intlist *nl=(struct intlist *)calloc(1,sizeof(struct intlist));
	  nl->element=tmp->element;
	  nl->next=todo;
	  todo=nl;
	}
	tmp=tmp->next;
      }
    } else {
      struct intlist *searchptr=retval;
      while(searchptr!=NULL) {
	if(searchptr->element==todoold->element)
	  break;
	searchptr=searchptr->next;
      }
      if(searchptr==NULL) {
	struct intlist *nl=(struct intlist *)calloc(1,sizeof(struct intlist));
	nl->element=todoold->element;
	nl->next=retval;
	retval=nl;
      }
    }
    free(todoold);
  }
  return retval;
}

void outputrolerelations(struct heap_state *heap) {
  struct geniterator *it=gengetiterator(heap->rolereferencetable);
  /* FREEME*/
  struct hashtable *roletable=allocatehashtable();
  struct genhashtable *rrtable=genallocatehashtable((int (*)(void *)) &rolerelationhashcode, (int (*)(void *,void *)) &equivalentrolerelations);

  fprintf(heap->rolediagramfile,"digraph \"Role Relation Diagram\" {\n");
  fprintf(heap->rolediagramfile,"ratio=auto\n");
  while(1) {
    struct rolerelation *rr=(struct rolerelation *)gennext(it);
    char srcname[30];
    if (rr==NULL)
      break;
    sprintf(srcname, "R%d", rr->srcrole);
    {
      struct role *srcrole=(struct role *)gengettable(heap->reverseroletable,srcname);
      struct rolefieldlist *rfl=srcrole->nonnullfields;
      while(rfl!=NULL) {
	if ((rfl->field==rr->field)&&
	    (rfl->role==rr->dstrole))
	  break;
	rfl=rfl->next;
      }
      if (rfl==NULL) {
	fprintf(heap->rolediagramfile,"R%d -> R%d [label=\"%s\"]\n",rr->srcrole, rr->dstrole,rr->field->fieldname);
      } else {
	fprintf(heap->rolediagramfile,"R%d -> R%d [style=dotted,label=\"%s\"]\n",rr->srcrole, rr->dstrole,rr->field->fieldname);
	addentry(roletable, rr->dstrole, rr->srcrole);
      }
    }
  }
  fprintf(heap->rolediagramfile,"}\n");
  genfreeiterator(it);



  it=gengetiterator(heap->rolereferencetable);
  fprintf(heap->rolediagramfilemerge,"digraph \"Merged Role Relation Diagram\" {\n");
  fprintf(heap->rolediagramfilemerge,"ratio=auto\n");
  while(1) {
    struct rolerelation *rr=(struct rolerelation *)gennext(it);
    char srcname[30];
    if (rr==NULL)
      break;
    sprintf(srcname, "R%d", rr->srcrole);
    {
      struct role *srcrole=(struct role *)gengettable(heap->reverseroletable,srcname);
      struct rolefieldlist *rfl=srcrole->nonnullfields;
      while(rfl!=NULL) {
	if ((rfl->field==rr->field)&&
	    (rfl->role==rr->dstrole))
	  break;
	rfl=rfl->next;
      }
      if (rfl==NULL) {
	struct intlist * isrc=remap(roletable, rr->srcrole);
	struct intlist * idst=remap(roletable, rr->dstrole);
	while (isrc!=NULL) {
	  struct intlist * idst2=idst;
	  while (idst2!=NULL) {
	    int src=isrc->element;
	    int dst=idst2->element;
	    struct rolerelation trr={src,rr->field,dst};
	    if ((src!=dst||rr->srcrole==rr->dstrole)&&!gencontains(rrtable,&trr)) {
	      struct rolerelation *nrr=(struct rolerelation *) calloc(1,sizeof(struct rolerelation));
	      nrr->srcrole=src;
	      nrr->dstrole=dst;
	      nrr->field=rr->field;
	      genputtable(rrtable, nrr,nrr);
	      fprintf(heap->rolediagramfilemerge,"R%d -> R%d [label=\"%s\"]\n",src, dst,rr->field->fieldname);
	    }
	    idst2=idst2->next;
   	  }
	  {
	    struct intlist *oldsrc=isrc;
	    isrc=isrc->next;
	    free(oldsrc);
	  }
	}
	while(idst!=NULL) {
	    struct intlist *olddst=idst;
	    idst=idst->next;
	    free(olddst);
	}
      }
    }
  }
  fprintf(heap->rolediagramfilemerge,"}\n");
  genfreeiterator(it);
  genfreekeyhashtable(rrtable);

}

