#include "ActionGEQ1.h"
#include <assert.h>
#include "dmodel.h"
#include "normalizer.h"
#include "omodel.h"
#include "model.h"
#include "Relation.h"
#include "set.h"
#include "element.h"
#include "Hashtable.h"
#include "Guidance.h"

ActionGEQ1::ActionGEQ1(DomainRelation *drel, model *m) {
  domrelation=drel;
  globalmodel=m;
}

void ActionGEQ1::repair(Hashtable *env,CoercePredicate *p) {
  switch(p->getpredicate()->getsetexpr()->gettype()) {
  case SETEXPR_LABEL: {
    /* Set should be too small if we are doing a repair
       We need to add 1 element */
    Guidance *g=globalmodel->getguidance();
    char * newsetname=p->getpredicate()->getsetexpr()->getsetlabel()->getname();
    Source s=g->sourceforsetsize(newsetname);
    if (s.setname!=NULL) {
      /* just pick an element from s.setname */
      char *setname=s.setname;
      if (equivalentstrings(s.setname,"int")) {
	/* special case for ints*/
	WorkSet *wsnew=domrelation->getset(newsetname)->getset();
	for(int i=0;;i++) {
	  Element *e=new Element(i);
	  if (!wsnew->contains(e)) {
	    /* Got our element */
	    domrelation->addtoset(e,domrelation->getset(newsetname),globalmodel);
	    return;
	  }
	  delete(e);
	}
      } else {
	WorkSet *ws=domrelation->getset(setname)->getset();
	WorkSet *wsnew=domrelation->getset(newsetname)->getset();
	Element *e=(Element *)ws->firstelement();
	while(e!=NULL) {
	  if (!wsnew->contains(e)) {
	    /* Got our element */
	    domrelation->addtoset(e,domrelation->getset(newsetname),globalmodel);
	    return;
	  }
	  e=(Element *)ws->getnextelement(e);
	}
	printf("Error...set %s doesn't have enough elements for %s\n",setname,newsetname);
	exit(-1);
      }
    } else {
      /* call functionpointer */
      DomainSet *newset=domrelation->getset(newsetname);
      char *type=newset->getelementtype();
      structure *st=globalmodel->getstructure(type);
      Element *ele=s.functionptr(st,globalmodel);
      if (ele==NULL) {
	printf("Error...allocation function doesn't return structure for %s\n",newsetname);
	exit(-1);
      }
      domrelation->addtoset(ele,newset,globalmodel);
      return;
    }
  }
  break;
  case SETEXPR_REL: {
    /* Set should be too small if we are doing a repair
       We need to add 1 element */
    Guidance *g=globalmodel->getguidance();
    char * relationname=p->getpredicate()->getsetexpr()->getrelation()->getname();
    Element *key=(Element *)env->get(p->getpredicate()->getsetexpr()->getlabel()->label());
    DRelation *relation=domrelation->getrelation(relationname);
    char *rangeset=relation->getrange();
    Source s=g->sourceforrelation(rangeset);
    if (s.setname!=NULL) {
      /* just pick an element from s.setname */
      char *setname=s.setname;
      if (equivalentstrings(s.setname,"int")) {
	/* special case for ints*/
	for(int i=0;;i++) {
	  WorkSet *wsnew=domrelation->getrelation(relationname)->getrelation()->getset(key);
	  Element *e=new Element(i);
	  if (wsnew==NULL||!wsnew->contains(e)) {
	    /* Got our element */
	    domrelation->addtoset(e,domrelation->getset(rangeset),globalmodel);
	    relation->getrelation()->put(key,e);
	    return;
	  }
	  delete(e);
	}
      } else {
	WorkSet *ws=domrelation->getset(s.setname)->getset();
	Element *e=(Element *)ws->firstelement();
	while(e!=NULL) {
	  WorkSet *wsnew=domrelation->getrelation(relationname)->getrelation()->getset(key);
	  if (wsnew==NULL||!wsnew->contains(e)) {
	    /* Got our element */
	    domrelation->addtoset(e,domrelation->getset(rangeset),globalmodel);
	    relation->getrelation()->put(key,e);
	    return;
	  }
	  e=(Element *)ws->getnextelement(e);
	}
	printf("Error...set %s doesn't have enough elements for relation\n",setname);
	exit(-1);
      }
    } else {
      /* call functionpointer */
      DomainSet *newset=domrelation->getset(rangeset);
      char *type=newset->getelementtype();
      structure *st=globalmodel->getstructure(type);
      Element *ele=s.functionptr(st,globalmodel);
      if (ele==NULL) {
	printf("Error...allocation function doesn't return structure for %s\n",rangeset);
	exit(-1);
      }
      domrelation->addtoset(ele,domrelation->getset(rangeset),globalmodel);
      relation->getrelation()->put(key,ele);
      return;
    }
  }

  
  break;
  case SETEXPR_INVREL: {
    /* Set should be too small if we are doing a repair
       We need to add 1 element */
    Guidance *g=globalmodel->getguidance();
    char * relationname=p->getpredicate()->getsetexpr()->getrelation()->getname();
    Element *key=(Element *)env->get(p->getpredicate()->getsetexpr()->getlabel()->label());
    DRelation *relation=domrelation->getrelation(relationname);
    char *domainset=relation->getdomain();
    Source s=g->sourceforrelation(domainset);
    if (s.setname!=NULL) {
      /* just pick an element from s.setname */
      char *setname=s.setname;
      if (equivalentstrings(s.setname,"int")) {
	/* special case for ints*/
	for(int i=0;;i++) {
	  Element *e=new Element(i);
	  WorkSet *wsnew=domrelation->getrelation(relationname)->getrelation()->invgetset(key);
	  if (wsnew==NULL||!wsnew->contains(e)) {
	    /* Got our element */
	    domrelation->addtoset(e,domrelation->getset(domainset),globalmodel);
	    relation->getrelation()->put(e,key);
	    return;
	  }
	  delete(e);
	}
      } else {
	WorkSet *ws=domrelation->getset(s.setname)->getset();
	Element *e=(Element *)ws->firstelement();
	while(e!=NULL) {
	  WorkSet *wsnew=domrelation->getrelation(relationname)->getrelation()->invgetset(key);
	  if (wsnew==NULL||!wsnew->contains(e)) {
	    /* Got our element */
	    domrelation->addtoset(e,domrelation->getset(domainset),globalmodel);
	    relation->getrelation()->put(e,key);
	    return;
	  }
	  e=(Element *)ws->getnextelement(e);
	}
	printf("Error...set %s doesn't have enough elements for relation\n",setname);
	exit(-1);
      }
    } else {
      /* call functionpointer */
      DomainSet *newset=domrelation->getset(domainset);
      char *type=newset->getelementtype();
      structure *st=globalmodel->getstructure(type);
      Element *ele=s.functionptr(st,globalmodel);
      if (ele==NULL) {
	printf("Error...allocation function doesn't return structure for %s\n",domainset);
	exit(-1);
      }
      domrelation->addtoset(ele,domrelation->getset(domainset),globalmodel);
      relation->getrelation()->put(ele,key);
      return;
    }
  }
  break;
  }
}

bool ActionGEQ1::conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2) {
  assert(canrepair(p1));
  Setexpr *pse=p1->getpredicate()->getsetexpr();
  if(comparepredicates(c1,p1,c2,p2))
    return false; /*same predicates don't conflict*/
  char *fromname=NULL;
  DomainSet *fromset=domrelation->getsource(domrelation->getset(pse->getsetlabel()->getname()));
  if (fromset!=NULL)
    fromname=fromset->getname();
  /* FIXME: fromname should cycle through set of possibilities ...
     if it is null, it should be replaced with the type name the object will have for the REL tests...this is okay already for the label tests...
  */

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


    return testforconflict(getset(c1,p1->getpredicate()->getsetexpr()->getlabel()->label()),
			   fromname,
			   p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2);
  }
  case SETEXPR_INVREL: {
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
			   p1->getpredicate()->getsetexpr()->getrelation()->getname(),c2,p2);
  }
  }
}

bool ActionGEQ1::canrepair(CoercePredicate *cp) {
  if (cp->getcoercebool()==false)
    return false;
  Predicate *p=cp->getpredicate();
  
  if (p->gettype()!=PREDICATE_GTE1)
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
