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
    int *nr=(int *)malloc(sizeof (int));
    *nr=newrole;
    puttable(h, origrole, nr);
  }
}

int remap(struct hashtable *h, int origrole) {
  while(contains(h,origrole)) {
    origrole=*((int *) gettable(h, origrole));
  }
  return origrole;
}

void outputrolerelations(struct heap_state *heap) {
  struct geniterator *it=gengetiterator(heap->rolereferencetable);
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
	int src=remap(roletable, rr->srcrole);
	int dst=remap(roletable, rr->dstrole);
	struct rolerelation trr={src,rr->field,dst};
	if (!gencontains(rrtable,&trr)) {
	  struct rolerelation *nrr=(struct rolerelation *) calloc(1,sizeof(struct rolerelation));
	  nrr->srcrole=src;
	  nrr->dstrole=dst;
	  nrr->field=rr->field;
	  genputtable(rrtable, nrr,nrr);
	  fprintf(heap->rolediagramfilemerge,"R%d -> R%d [label=\"%s\"]\n",src, dst,rr->field->fieldname);
	}
      }
    }
  }
  fprintf(heap->rolediagramfilemerge,"}\n");
  genfreeiterator(it);
  genfreekeyhashtable(rrtable);
  freedatahashtable(roletable, &free);
}

