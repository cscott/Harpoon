#include "ActionEQ1.h"
#include <assert.h>
#include "dmodel.h"
#include "normalizer.h"
#include "omodel.h"
#include "model.h"
#include "Relation.h"
#include "set.h"
#include "Hashtable.h"
#include "Guidance.h"

ActionEQ1::ActionEQ1(DomainRelation *drel, model *m) {
  domrelation=drel;
  globalmodel=m;
}

void ActionEQ1::repair(Hashtable *env,CoercePredicate *p) {
  switch(p->getpredicate()->getsetexpr()->gettype()) {
  case SETEXPR_LABEL: {
    char *setname=p->getpredicate()->getsetexpr()->getsetlabel()->getname();
    DomainSet *ds=domrelation->getset(setname);
    if (ds->getset()->size()>1) {
      Guidance *g=globalmodel->getguidance();
      WorkSet *ws=ds->getset();
      while(ws->size()>1) {
	Element *e=(Element *)ws->firstelement();
	domrelation->delfromsetmovetoset(e,domrelation->getset(setname), globalmodel);
      }
    } else
      this->ActionGEQ1::repair(env,p);
  }
  break;
  case SETEXPR_REL: {
    DRelation *dr=domrelation->getrelation(p->getpredicate()->getsetexpr()->getrelation()->getname());
    WorkRelation *wr=dr->getrelation();
    Element *key=(Element *)env->get(p->getpredicate()->getsetexpr()->getlabel()->label());
    WorkSet *ws=wr->getset(key);

    if (ws!=NULL&&ws->size()>1) {
      //Remove elements
      int size=ws->size();
      for(int i=0;i<(size-1);i++) {
	void *objtoremove=ws->firstelement();
	wr->remove(key,objtoremove);
      }
    } else
      this->ActionGEQ1::repair(env,p);
  }
  break;
  case SETEXPR_INVREL: {
    DRelation *dr=domrelation->getrelation(p->getpredicate()->getsetexpr()->getrelation()->getname());
    WorkRelation *wr=dr->getrelation();
    Element *key=(Element *)env->get(p->getpredicate()->getsetexpr()->getlabel()->label());
    WorkSet *ws=wr->invgetset(key);

    if (ws!=NULL&&ws->size()>1) {
      //Remove elements
      int size=ws->size();
      for(int i=0;i<(size-1);i++) {
	void *objtoremove=ws->firstelement();
	wr->remove(objtoremove,key);
      }
    } else 
      this->ActionGEQ1::repair(env,p);
  }
  break;
  }
}

bool ActionEQ1::conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2) {
  assert(canrepair(p1));
  Setexpr *pse=p1->getpredicate()->getsetexpr();
  if(comparepredicates(c1,p1,c2,p2))
    return false; /*same predicates don't conflict*/

  switch(pse->gettype()) {
  case SETEXPR_LABEL: {
    char *boundname=NULL;
    Guidance *g=globalmodel->getguidance();
    DomainSet *fromset=domrelation->getsource(domrelation->getset(pse->getsetlabel()->getname()));

    if (fromset!=NULL) {
      Source s=g->sourceforsetsize(fromset->getname());
      if (s.setname!=NULL)
	boundname=s.setname;
    }
    char *setname=pse->getsetlabel()->getname();

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
    {
      WorkSet *ws=domrelation->removeconflictaddsets(pse->getsetlabel()->getname(),globalmodel);
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
    {
      WorkSet *ws=domrelation->removeconflictdelsets(pse->getsetlabel()->getname());
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
    char *fromname=NULL;
    /* we have a in v.r */
    /* add <v,a> to r */
    if (p2->isrule()&&
	(p2->getpredicate()->gettype()==PREDICATE_GTE1||
	 p2->getpredicate()->gettype()==PREDICATE_EQ1)&&
	p2->getpredicate()->getsetexpr()->gettype()==SETEXPR_REL) {
      return false; 
    }

    /* Compute bounding set if there is one */
    

    DomainRelation *drel=globalmodel->getdomainrelation();
    char *insertset=drel->getrelation(pse->getrelation()->getname())->getrange();
    Guidance *g=globalmodel->getguidance();
    Source s=g->sourceforsetsize(insertset);


    /* Check conflicts arrising from addition to set */
    {
      WorkSet *ws=domrelation->conflictaddsets(insertset,s.setname,globalmodel);
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
      WorkSet *ws=domrelation->conflictdelsets(insertset, s.setname);    
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


    return testforconflict(getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()), fromname,
			   p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2)||testforconflictremove(getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()), fromname,
														    p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2);
  }
  case SETEXPR_INVREL: {
    char *fromname=NULL;
    /* we have a in v.r */
    /* add <a,v> to r */
    if (p2->isrule()&&
	(p2->getpredicate()->gettype()==PREDICATE_GTE1||
	 p2->getpredicate()->gettype()==PREDICATE_EQ1)&&
	p2->getpredicate()->getsetexpr()->gettype()==SETEXPR_INVREL) {
      return false;
    }

    /* Compute bounding set if there is one */
    

    DomainRelation *drel=globalmodel->getdomainrelation();
    char *insertset=drel->getrelation(pse->getrelation()->getname())->getdomain();
    Guidance *g=globalmodel->getguidance();
    Source s=g->sourceforsetsize(insertset);


    /* Check conflicts arrising from addition to set */
    {
      WorkSet *ws=domrelation->conflictaddsets(insertset,s.setname,globalmodel);
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
      WorkSet *ws=domrelation->conflictdelsets(insertset, s.setname);    
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

    return testforconflict(fromname,
			   getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()),
			   p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2)||
      testforconflictremove(fromname,
			    getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()),
			    p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2);
  }
  }
}

bool ActionEQ1::canrepair(CoercePredicate *cp) {
  if (cp->getcoercebool()==false)
    return false;
  Predicate *p=cp->getpredicate();


  if (p->gettype()!=PREDICATE_EQ1)
    return false;
  /* Coercing set membership */
  Setexpr *se=p->getsetexpr();
  int setexprtype=se->gettype();
  if (setexprtype==SETEXPR_REL||
      setexprtype==SETEXPR_INVREL) {
    DRelation *dr=domrelation->getrelation(se->getrelation()->getname());
    if (dr->isstatic())
      return false; /* Can't change static domain relations */
  }

  return true;
}
