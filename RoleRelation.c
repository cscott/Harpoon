#include <stdlib.h>
#include "RoleRelation.h"
#include "Role.h"

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

void outputrolerelations(struct heap_state *heap) {
  struct geniterator *it=gengetiterator(heap->rolereferencetable);
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
      }
    }
  }
  fprintf(heap->rolediagramfile,"}\n");
  genfreeiterator(it);
}
