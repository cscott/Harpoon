#include "ActionInSet.h"
#include <assert.h>
#include "dmodel.h"
#include "model.h"
#include "normalizer.h"
#include "omodel.h"
#include "Relation.h"
#include "set.h"
#include "Hashtable.h"

ActionInSet::ActionInSet(DomainRelation *drel,model *m) {
  domrelation=drel;
  globalmodel=m;
}

void ActionInSet::repairpredicate(Hashtable *env,CoercePredicate *p) {
  Element *ele=(Element*) env->get(p->getpredicate()->getlabel()->label());
  switch(p->getpredicate()->getsetexpr()->gettype()) {
  case SETEXPR_LABEL:
    domrelation->addtoset(ele, domrelation->getset(p->getpredicate()->getsetexpr()->getsetlabel()->getname()),
			  globalmodel);
    break;
  case SETEXPR_REL: {
    Element *key=(Element*) env->get(p->getpredicate()->getsetexpr()->getlabel()->label());
    domrelation->getrelation(p->getpredicate()->getsetexpr()->getrelation()->getname())->getrelation()->put(key,ele);
    char *rangename=domrelation->getrelation(p->getpredicate()->getsetexpr()->getrelation()->getname())->getrange();
    if (!equivalentstrings(rangename,"int")) {
      DomainSet *range=domrelation->getset(rangename);
      if (!range->getset()->contains(ele))
	domrelation->addtoset(ele,range,globalmodel);
    }
  }
  break;
  case SETEXPR_INVREL: {
    Element *key=(Element*) env->get(p->getpredicate()->getsetexpr()->getlabel()->label());
    domrelation->getrelation(p->getpredicate()->getsetexpr()->getrelation()->getname())->getrelation()->put(ele,key);
    char *domainname=domrelation->getrelation(p->getpredicate()->getsetexpr()->getrelation()->getname())->getdomain();
    if (!equivalentstrings(domainname,"int")) {
      DomainSet *domain=domrelation->getset(domainname);
      if (!domain->getset()->contains(ele))
	domrelation->addtoset(ele,domain,globalmodel);
    }
  }
  break;
  }
}



void ActionInSet::breakpredicate(Hashtable *env,CoercePredicate *p) 
{
}



bool ActionInSet::conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2) {
  assert(canrepairpredicate(p1));
  if(comparepredicates(c1,p1,c2,p2))
    return false; /*same predicates don't conflict*/
  Setexpr *pse=p1->getpredicate()->getsetexpr();
  switch(pse->gettype()) {
  case SETEXPR_LABEL: {
    /* Compute bounding set if there is one */
    char *boundname=calculatebound(c1,p1->getpredicate()->getlabel());
    /* Check conflicts arrising from addition to set */
    {
      WorkSet *ws=domrelation->conflictaddsets(pse->getsetlabel()->getname(),boundname,globalmodel);
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
      WorkSet *ws=domrelation->conflictdelsets(pse->getsetlabel()->getname(), boundname);    
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
    return false;
  }
  case SETEXPR_REL: {

    /* Compute bounding set if there is one */
    char *boundname=calculatebound(c1,p1->getpredicate()->getlabel());
    DomainRelation *drel=globalmodel->getdomainrelation();
    char *insertset=drel->getrelation(pse->getrelation()->getname())->getrange();

    /* Check conflicts arrising from addition to set */
    {
      WorkSet *ws=domrelation->conflictaddsets(insertset,boundname,globalmodel);
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
      WorkSet *ws=domrelation->conflictdelsets(insertset, boundname);    
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
  
    /* we have a in v.r */
    /* add <v,a> to r */
    return testforconflict(getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()),
			   getset(c1,p1->getpredicate()->getlabel()->label()),
			   p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2);
  }
  case SETEXPR_INVREL: {
    /* Compute bounding set if there is one */
    char *boundname=calculatebound(c1,p1->getpredicate()->getlabel());
    DomainRelation *drel=globalmodel->getdomainrelation();
    char *insertset=drel->getrelation(pse->getrelation()->getname())->getdomain();
    
    /* Check conflicts arrising from addition to set */
    {
      WorkSet *ws=domrelation->conflictaddsets(insertset,boundname,globalmodel);
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
      WorkSet *ws=domrelation->conflictdelsets(insertset, boundname);    
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
    /* add <a,v> to r */
    return testforconflict(getset(c1,p1->getpredicate()->getlabel()->label()),
			   getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()),
			   p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2);
  }
  }
}

bool ActionInSet::canrepairpredicate(CoercePredicate *cp) {
  if (cp->getcoercebool()==false)
    return false;
  Predicate *p=cp->getpredicate();
  if (p==NULL)
    return false;
  if (p->gettype()!=PREDICATE_SET)
    return false;
  Setexpr *se=p->getsetexpr();
  int setexprtype=se->gettype();
  if (setexprtype==SETEXPR_REL||
      setexprtype==SETEXPR_INVREL) {
    DRelation *dr=domrelation->getrelation(se->getrelation()->getname());
    if (dr->isstatic())
      return false; /* Can't change static domain relations */
  }

  /* Coercing set membership */
  return true;
}
