#include "ActionNotInSet.h"
#include <assert.h>
#include "dmodel.h"
#include "normalizer.h"
#include "omodel.h"
#include "Relation.h"
#include "set.h"
#include "Hashtable.h"
#include "ActionInSet.h"

ActionNotInSet::ActionNotInSet(DomainRelation *drel, model *m) {
  domrelation=drel;
  globalmodel=m;
}

void ActionNotInSet::repairpredicate(Hashtable *env,CoercePredicate *p) {
  Element *ele=(Element*) env->get(p->getpredicate()->getlabel()->label());
  switch(p->getpredicate()->getsetexpr()->gettype()) {
  case SETEXPR_LABEL:
    domrelation->delfromsetmovetoset(ele, domrelation->getset(p->getpredicate()->getsetexpr()->getsetlabel()->getname()),globalmodel);
    break;
  case SETEXPR_REL: {
    Element *key=(Element*) env->get(p->getpredicate()->getsetexpr()->getlabel()->label());
    domrelation->getrelation(p->getpredicate()->getsetexpr()->getrelation()->getname())->getrelation()->remove(key,ele);
  }
  break;
  case SETEXPR_INVREL: {
    Element *key=(Element*) env->get(p->getpredicate()->getsetexpr()->getlabel()->label());
    domrelation->getrelation(p->getpredicate()->getsetexpr()->getrelation()->getname())->getrelation()->remove(ele,key);
  }
  break;
  }
}




void ActionNotInSet::breakpredicate(Hashtable *env, CoercePredicate *p)
{
#ifdef DEBUGMESSAGES
  printf("ActionNotInSet::breakpredicate CALLED\n");
  p->getpredicate()->print(); printf("\n");
#endif

  ActionInSet *a = new ActionInSet(domrelation, globalmodel);
  a->repairpredicate(env, p);
}




bool ActionNotInSet::conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2) {
  assert(canrepairpredicate(p1));
  if(comparepredicates(c1,p1,c2,p2))
    return false; /*same predicates don't conflict*/
  Setexpr *pse=p1->getpredicate()->getsetexpr();
  switch(pse->gettype()) {
  case SETEXPR_LABEL: {
    /* Go through the sets we add something to */
    {
      WorkSet *ws=domrelation->removeconflictaddsets(pse->getsetlabel()->getname(),globalmodel);
      DomainSet *ds=(DomainSet *)ws->firstelement();
      while(ds!=NULL) {
	if (conflictwithaddtoset(ds->getname(),c2,p2)) {
	  delete(ws);
	  return true;
	}
	ds=(DomainSet *)ws->getnextelement(ds);
      }
      delete(ws);
    }
    {
      WorkSet *ws=domrelation->removeconflictdelsets(pse->getsetlabel()->getname());
      DomainSet *ds=(DomainSet *)ws->firstelement();
      while(ds!=NULL) {
	if (conflictwithremovefromset(NULL,ds->getname(),c2,p2)) {
	  delete(ws);
	  return true;
	}
	ds=(DomainSet *)ws->getnextelement(ds);
      }
      delete(ws);
    }
    return false;

  }
  case SETEXPR_REL:
    /* we have !a in v.r */
    /* remove <v,a> to r */
    return testforconflictremove(getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()),
				 getset(c1,p1->getpredicate()->getlabel()->label()),
				 p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2);
  case SETEXPR_INVREL:
    /* we have !a in v.r */
    /* remove <v,a> to r */
    return testforconflictremove(getset(c1,p1->getpredicate()->getlabel()->label()),
				 getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()),
				 p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2);
  }
}

bool ActionNotInSet::canrepairpredicate(CoercePredicate *cp) {
  if (cp->getcoercebool()==true)
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
