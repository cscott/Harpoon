#include "ActionAssign.h"
#include <assert.h>
#include "dmodel.h"
#include "normalizer.h"
#include "omodel.h"
#include "Relation.h"
#include "set.h"
#include "Hashtable.h"
#include "model.h"
#include "processobject.h"
#include "element.h"
#include "common.h"

ActionAssign::ActionAssign(DomainRelation *drel, model *m) {
  domrelation=drel;
  globalmodel=m;
}

char * ActionAssign::gettype(Constraint *c,Elementexpr *ee) {
  switch(ee->gettype()) {
  case ELEMENTEXPR_LABEL:
    return getset(c,ee->getlabel()->label());
  case ELEMENTEXPR_SUB:
  case ELEMENTEXPR_ADD:
  case ELEMENTEXPR_MULT:
    return "int";
  case ELEMENTEXPR_LIT: {
    Literal *lit=ee->getliteral();
    switch(lit->gettype()) {
    case LITERAL_NUMBER:
      return "int";
    case LITERAL_TOKEN:
      return "token";
    case LITERAL_BOOL:
      printf("ERROR in gettype\n");
      exit(-1);
    }
  }
  case ELEMENTEXPR_SETSIZE:
    return "int";
  case ELEMENTEXPR_RELATION: {
    Relation *r=ee->getrelation();
    DomainRelation *drel=globalmodel->getdomainrelation();
    return drel->getrelation(r->getname())->getrange();
  }
  }
}

void ActionAssign::repair(Hashtable *env,CoercePredicate *cp) {
  Predicate *p=cp->getpredicate();
  Element *ele=evaluateexpr(p->geteleexpr(),env,globalmodel); //ele=E
  Element *index=(Element *) env->get(p->getvalueexpr()->getlabel()->label()); // index=V
  char *rel=p->getvalueexpr()->getrelation()->getname();
  WorkRelation *relation=domrelation->getrelation(rel)->getrelation();
  Element *old=(Element *)relation->getobj(index); // old=V.R
  if (old!=NULL)
    relation->remove(index,old);
  DRelation *drel=domrelation->getrelation(rel);
  
  if(!equivalentstrings(drel->getdomain(),"int")) {
    DomainSet *domain=domrelation->getset(drel->getdomain());
    if (!domain->getset()->contains(index))
      domrelation->addtoset(index,domain,globalmodel);
    }


  switch (p->gettype()) {
  case PREDICATE_LT: {
    Element *ele2=new Element(ele->intvalue()-1);
    delete(ele);
    relation->put(index,ele2);
    break;
  }
  case PREDICATE_LTE: {
    relation->put(index,ele);
    break;
  }
  case PREDICATE_EQUALS: {
    relation->put(index,ele);
    if(!equivalentstrings(drel->getrange(),"int")&&
       !equivalentstrings(drel->getrange(),"token")) {
      DomainSet *range=domrelation->getset(drel->getrange());
      if (!range->getset()->contains(ele))
	domrelation->addtoset(ele,range,globalmodel);
    }
    break;
  }
  case PREDICATE_GTE: {
    relation->put(index,ele);
    break;
  }
  case PREDICATE_GT: {
    Element *ele2=new Element(ele->intvalue()+1);
    delete(ele);
    relation->put(index,ele2);
    break;
  }
  }
}

bool ActionAssign::conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2) {
  assert(canrepair(p1));
  if(comparepredicates(c1,p1,c2,p2))
    return false; /*same predicates don't conflict*/
  /* we have v.r?a */
  /* add <v,?> to r */

  /* Compute bounding set if there is one */
  
  
  DomainRelation *drel=globalmodel->getdomainrelation();
  char *insertset=drel->getrelation(p1->getpredicate()->getvalueexpr()->getrelation()->getname())->getrange();
  
  char *boundset=gettype(c1,p1->getpredicate()->geteleexpr());
  
  /* Check conflicts arrising from addition to set */
  {
    WorkSet *ws=domrelation->conflictaddsets(insertset,NULL,globalmodel);
    DomainSet *ds=(DomainSet *) ws->firstelement();
    while(ds!=NULL) {
      if (conflictwithaddtoset(ds->getname(),c2,p2)) {
	delete(ws);
	return true;
      }
      ds=(DomainSet *) ws->getnextelement(ds);
    }
    delete(ws);
  }
  /* Check conflicts arrising from deletions from set */
  {
    WorkSet *ws=domrelation->conflictdelsets(insertset, NULL);    
    DomainSet *ds=(DomainSet *) ws->firstelement();
    while (ds!=NULL) {
      if (conflictwithremovefromset(NULL,ds->getname(),c2,p2)) {
	delete(ws);
	return true;
      }
      ds=(DomainSet *) ws->getnextelement(ds);
    }
    delete(ws);
  }
  return testforconflict(getset(c1,p1->getpredicate()->getvalueexpr()->getlabel()->label()), NULL,
			 p1->getpredicate()->getvalueexpr()->getrelation()->getname(),c2,p2)||
    testforconflictremove(getset(c1,p1->getpredicate()->getvalueexpr()->getlabel()->label()), NULL,
			  p1->getpredicate()->getvalueexpr()->getrelation()->getname(),c2,p2);
}

bool ActionAssign::canrepair(CoercePredicate *cp) {
  if (cp->getcoercebool()==false)
    return false;
  Predicate *p=cp->getpredicate();
  if (p==NULL)
    return false;
  if (p->gettype()==PREDICATE_LT||
      p->gettype()==PREDICATE_LTE||
      p->gettype()==PREDICATE_EQUALS||
      p->gettype()==PREDICATE_GTE||
      p->gettype()==PREDICATE_GT) {
    Valueexpr *ve=p->getvalueexpr();
    DRelation *dr=domrelation->getrelation(ve->getrelation()->getname());
    if (dr->isstatic()) /* can't change static relations */
      return false;
    else
      return true;
  }  
  /* Coercing set membership */
  return false;
}
