#include <assert.h>
#include "ActionNormal.h"
#include "dmodel.h"
#include "normalizer.h"
#include "omodel.h"
#include "Relation.h"
#include "set.h"
#include "Hashtable.h"
#include "model.h"

ActionNormal::ActionNormal(model *m) {
  globalmodel=m;
  domrelation=m->getdomainrelation();
}

void ActionNormal::repairpredicate(Hashtable *env,CoercePredicate *p) {
  /* Don't actually repair stuff */
}


void ActionNormal::breakpredicate(Hashtable *env, CoercePredicate *p)
{
  /* Don't actually break stuff */
}


bool ActionNormal::conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2) {
  if(!p1->isrule()&&
     !p1->istuple()) {
    /* Compute bounding set if there is one */
    char *boundname=p1->getltype();
    /* Check conflicts arrising from addition to set */
    {
      WorkSet *ws=domrelation->conflictaddsets(p1->getrelset(),boundname,globalmodel);
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
      WorkSet *ws=domrelation->conflictdelsets(p1->getrelset(), boundname);
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

  if(!p1->isrule()&&
     p1->istuple()) {
    
    {
    /* Compute bounding set if there is one */
    char *boundname=getset(c1,p1->getrtype());
    DomainRelation *drel=globalmodel->getdomainrelation();
    char *insertset=drel->getrelation(p1->getrelset())->getrange();
    
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
    }
    /* Compute bounding set if there is one */
    char *boundname=getset(c1,p1->getltype());
    DomainRelation *drel=globalmodel->getdomainrelation();
    char *insertset=drel->getrelation(p1->getrelset())->getdomain();
    
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
    return testforconflict(getset(c1,p1->getltype()),
			   getset(c1,p1->getrtype()),
			   p1->getrelset(),c2,p2);
  }
}


bool ActionNormal::canrepairpredicate(CoercePredicate *cp) {
  return false; /* Doesn't repair stuff */
}
