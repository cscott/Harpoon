#include "Action.h"
#include <stdlib.h>
#include "omodel.h"
#include "model.h"
#include "dmodel.h"
#include "set.h"
#include "normalizer.h"

char * Action::calculatebound(Constraint *c, Label *clabel) {
  return getset(c,clabel->label());
}

char * Action::getset(Constraint *c, char *v) {
  char *set=NULL;
  if (c==NULL||v==NULL)
    return NULL;
  for(int i=0;i<c->numquants();i++) {
    if(equivalentstrings(c->getquant(i)->getlabel()->label(),v)) {
      if (c->getquant(i)->getset()->gettype()==SET_label)
	set=c->getquant(i)->getset()->getname();
      break;
    }
  }
  return set;
}

bool Action::possiblysameset(char *v1, Constraint *c1, char *v2, Constraint *c2) {
  return possiblysameset(getset(c1,v1),getset(c2,v2));
}

bool Action::comparepredicates(Constraint *c1, CoercePredicate *cp1, Constraint *c2, CoercePredicate *cp2) {
  if (cp1->getcoercebool()!=cp2->getcoercebool())
    return false;
  Predicate *p1=cp1->getpredicate();
  Predicate *p2=cp2->getpredicate();
  if (p1->gettype()!=p2->gettype())
    return false;
  int type=p1->gettype();
  if (type==PREDICATE_LT ||
      type==PREDICATE_LTE ||
      type==PREDICATE_EQUALS ||
      type==PREDICATE_GTE ||
      type==PREDICATE_GT) {
    char *v1=p1->getvalueexpr()->getlabel()->label();
    char *v2=p2->getvalueexpr()->getlabel()->label();
    if (!equivalentstrings(p1->getvalueexpr()->getrelation()->getname(),
			  p2->getvalueexpr()->getrelation()->getname()))
      return false;
    if (!(compareee(p1->geteleexpr(),v1,p2->geteleexpr(),v2)))
      return false;
    else
      return true;
  }
  if (type==PREDICATE_SET) {
    char *v1=p1->getlabel()->label();
    char *v2=p2->getlabel()->label();
    return comparesetexpr(p1->getsetexpr(),v1,p2->getsetexpr(),v2);
  }
  if (type==PREDICATE_EQ1||type==PREDICATE_GTE1) {
    if(p1->getsetexpr()->gettype()!=p2->getsetexpr()->gettype())
      return false;
    switch(p1->getsetexpr()->gettype()) {
    case SETEXPR_LABEL: {
      return equivalentstrings(p1->getsetexpr()->getsetlabel()->getname(), p2->getsetexpr()->getsetlabel()->getname());
    }
    case SETEXPR_REL:
    case SETEXPR_INVREL: {
      /* these can't interfere....*/
      return equivalentstrings(p1->getsetexpr()->getrelation()->getname(), p2->getsetexpr()->getrelation()->getname());
    }
    }
  }
  return false; /* by default */
}
 
bool Action::comparesetexpr(Setexpr *s1,char *v1,Setexpr *s2,char *v2) {
  if (s1->gettype()!=s2->gettype())
    return false;
  switch(s1->gettype()) {
  case SETEXPR_LABEL:
    if (!equivalentstrings(s1->getsetlabel()->getname(),
			   s2->getsetlabel()->getname()))
      return false;
    else return true; 
  case SETEXPR_REL:
  case SETEXPR_INVREL:
    if (!equivalentstrings(v1, s1->getlabel()->label()))
      return false;
    if (!equivalentstrings(v2, s2->getlabel()->label()))
      return false;
    if (!equivalentstrings(s1->getrelation()->getname(),
			   s2->getrelation()->getname()))
      return false;
    else return true;
  default:
    return false;
  }
}

bool Action::compareee(Elementexpr *ee1,char *v1,Elementexpr *ee2,char *v2) {
  if (ee1->gettype()!=ee2->gettype())
    return false;
  switch(ee1->gettype()) {
  case ELEMENTEXPR_LABEL:
    if (!equivalentstrings(ee1->getlabel()->label(),v1))
      return false;
    if (!equivalentstrings(ee2->getlabel()->label(),v2))
      return false;
    return true;
  case ELEMENTEXPR_SUB:
    if (compareee(ee1->getleft(),v1,ee2->getleft(),v2) &&
	compareee(ee1->getright(),v1,ee2->getright(),v2))
      return true;
    else return false;
  case ELEMENTEXPR_RELATION:
    if (compareee(ee1->getleft(),v1,ee2->getleft(),v2) &&
	equivalentstrings(ee1->getrelation()->getname(),ee2->getrelation()->getname()))
      return true;
    else return false;
  case ELEMENTEXPR_ADD:
  case ELEMENTEXPR_MULT:
    if ((compareee(ee1->getleft(),v1,ee2->getleft(),v2) &&
	 compareee(ee1->getright(),v1,ee2->getright(),v2))||
	(compareee(ee1->getleft(),v1,ee2->getright(),v2) &&
	 compareee(ee1->getright(),v1,ee2->getleft(),v2)))
      return true;
    else return false;
  case ELEMENTEXPR_LIT:
    if (ee1->getliteral()->gettype()!=
	ee2->getliteral()->gettype())
      return false;
    switch(ee1->getliteral()->gettype()) {
    case LITERAL_NUMBER:
      return (ee1->getliteral()->number()==ee2->getliteral()->number());
    case LITERAL_TOKEN:
      return (equivalentstrings(ee1->getliteral()->token(),ee2->getliteral()->token()));
    default:
      return false;
    }
  case ELEMENTEXPR_SETSIZE:
    return comparesetexpr(ee1->getsetexpr(),v1,ee2->getsetexpr(),v2);
  default:
    return false;
  }
}

bool Action::possiblysameset(char *set1, char *v2, Constraint *c2) {
  return possiblysameset(set1,getset(c2,v2));
}

bool Action::possiblysameset(char *set1,char *set2) {
  if (set1==NULL||set2==NULL)
    return true; /* At least one variable isn't spanning over a set...could be from any set*/
  DomainSet *dset1=domrelation->getset(set1);
  DomainSet *dset2=domrelation->getset(set2);
  WorkSet *ws=new WorkSet(true);
  while(dset1!=NULL) {
    ws->addobject(dset1);
    dset1=domrelation->getsuperset(dset1);
  }
  bool sameset=false;
  if (!ws->contains(dset2)) {
    while(dset2!=NULL) {
      if (ws->contains(dset2)) {
	if (dset2->gettype()==DOMAINSET_PARTITION&&
	    dset2!=dset1)
	  sameset=false;
	else
	  sameset=true;
	break;
      }
      dset2=domrelation->getsuperset(dset2);
    }
  }
  delete(ws);
  return sameset;
}

bool Action::conflictwithaddtoset(char *set, Constraint *c, CoercePredicate *p) {
  /* Test for conflict with abstraction function */
  if (!p->isrule()&&
      equivalentstrings(p->gettriggerset(),set))
    return true;
      
  /* Test for add new constraint */
  if (c!=NULL) {
    for(int i=0;i<c->numquants();i++) {
      if (c->getquant(i)->getset()->gettype()==SET_label)
	if (equivalentstrings(c->getquant(i)->getset()->getname(),set)) {
	  return true; /*we're adding this constraint because of inclusion*/ 
	}
    }
  }
  /* Test for conflict with a \notin set */
  if (p->isrule()&&
      (p->getcoercebool()==false)&&
      (p->getpredicate()->gettype()==PREDICATE_SET)&&
      (p->getpredicate()->getsetexpr()->gettype()==SETEXPR_LABEL)) {
    char *psetname=p->getpredicate()->getsetexpr()->getsetlabel()->getname();
    if (equivalentstrings(psetname,set)) {
      return true;
    }
  }
  /* Test for conflict with setsize=1 */
  if (p->isrule()&&
      (p->getcoercebool()==true)&&
      (p->getpredicate()->gettype()==PREDICATE_EQ1)&&
      (p->getpredicate()->getsetexpr()->gettype()==SETEXPR_LABEL)) {
    char *psetname=p->getpredicate()->getsetexpr()->getsetlabel()->getname();
    if (equivalentstrings(psetname,set)) 
      return true;
  }
  return false;
}

bool Action::conflictwithremovefromset(WorkSet * checkother,char *set, Constraint *c, CoercePredicate *p) {
  /* Check for conflict with set size predicate */
  if (p->isrule() &&
      (p->getcoercebool()==true)&&
      ((p->getpredicate()->gettype()==PREDICATE_EQ1)||
       (p->getpredicate()->gettype()==PREDICATE_GTE1)) &&
      (p->getpredicate()->getsetexpr()->gettype()==SETEXPR_LABEL)) {
    char *psetname=p->getpredicate()->getsetexpr()->getsetlabel()->getname();
    if (equivalentstrings(psetname,set)) 
      return true;
  }

  /* Check for conflict with a in SET */
  if (p->isrule() &&
      (p->getcoercebool()==true)&&
      (p->getpredicate()->gettype()==PREDICATE_SET)&&
      (p->getpredicate()->getsetexpr()->gettype()==SETEXPR_LABEL)) {
    char *psetname=p->getpredicate()->getsetexpr()->getsetlabel()->getname();
    if (equivalentstrings(psetname,set))
      return true;
  }

  /* Check for domain's of relation */
  DomainRelation *domrelation=globalmodel->getdomainrelation();
  for(int i=0;i<domrelation->getnumrelation();i++) {
    DRelation *drelation=domrelation->getrelation(i);
    char *domain=drelation->getdomain();
    if(equivalentstrings(domain,set)) {
      testforconflictremove(set,NULL,drelation->getname(),c,p);
      if(drelation->isstatic()) {
	bool flag=false;
	if(checkother==NULL) {
	  flag=true;
	  checkother=new WorkSet((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);
	}
	if(!checkother->contains(drelation->getrange())) {
	  checkother->addobject(drelation->getrange());
	  if (conflictwithremovefromset(checkother, drelation->getrange(),c,p))
	    return true;
	  checkother->removeobject(drelation->getrange());
	}
	if(flag)
	  delete(checkother);
      }
    }
  }
  /* Check for range's of relation */
  for(int i=0;i<domrelation->getnumrelation();i++) {
    DRelation *drelation=domrelation->getrelation(i);
    char *range=drelation->getrange();
    if (equivalentstrings(range,set)) {
      testforconflictremove(NULL,set,drelation->getname(),c,p);
    }
  }
  return false;
}

/* remove <v,a> from r */
bool Action::testforconflictremove(char *setv, char *seta, char *rel, Constraint *c, CoercePredicate *p) {
  /* check for conflict with v.r=? */
  if (p->isrule()) {
  int type=p->getpredicate()->gettype();
  if ((type==PREDICATE_LT||type==PREDICATE_LTE||type==PREDICATE_EQUALS||type==PREDICATE_GT||type==PREDICATE_GTE) &&
      possiblysameset(setv,p->getpredicate()->getvalueexpr()->getlabel()->label(),c) &&
      equivalentstrings(rel,p->getpredicate()->getvalueexpr()->getrelation()->getname()))
    return true;

  /* check for conflict with |v'.r|?=1 */
  if ((p->getpredicate()->gettype()==PREDICATE_EQ1||p->getpredicate()->gettype()==PREDICATE_GTE1)&&
      p->getpredicate()->getsetexpr()->gettype()==SETEXPR_REL&&
      possiblysameset(setv,p->getpredicate()->getsetexpr()->getlabel()->label(),c)&&
    equivalentstrings(rel,p->getpredicate()->getsetexpr()->getrelation()->getname()))
    return true;

  /* check for conflict with |a'.~r|?=1 */
  if ((p->getpredicate()->gettype()==PREDICATE_EQ1||p->getpredicate()->gettype()==PREDICATE_GTE1) &&
      p->getpredicate()->getsetexpr()->gettype()==SETEXPR_INVREL &&
      possiblysameset(seta,p->getpredicate()->getsetexpr()->getlabel()->label(),c)&&
      equivalentstrings(rel,p->getpredicate()->getsetexpr()->getrelation()->getname()))
    return true;
  
  /* check for conflit with (a' in v'.r)*/
  if (p->getcoercebool()&&
      p->getpredicate()->gettype()==PREDICATE_SET&&
      p->getpredicate()->getsetexpr()->gettype()==SETEXPR_REL&&
      possiblysameset(seta,p->getpredicate()->getlabel()->label(),c)&&
      possiblysameset(setv,p->getpredicate()->getsetexpr()->getlabel()->label(),c)&&
      equivalentstrings(rel,p->getpredicate()->getsetexpr()->getrelation()->getname()))
    return true;
  
  /* check for conflict with (a' in v'.~r)*/
  if (p->getcoercebool()&&
      p->getpredicate()->gettype()==PREDICATE_SET&&
      p->getpredicate()->getsetexpr()->gettype()==SETEXPR_INVREL &&
      possiblysameset(seta,p->getpredicate()->getsetexpr()->getlabel()->label(),c) &&
      possiblysameset(setv,p->getpredicate()->getlabel()->label(),c) &&
      equivalentstrings(rel,p->getpredicate()->getsetexpr()->getrelation()->getname()))
    return true;
  }
  return false;
}


/* add <v,a> to r */
bool Action::testforconflict(char *setv, char *seta, char *rel, Constraint *c, CoercePredicate *p) {
  /* check for conflict with valueexpr*/
  if (p->isrule()&&
      (p->getpredicate()->gettype()==PREDICATE_LT ||
       p->getpredicate()->gettype()==PREDICATE_LTE ||
       p->getpredicate()->gettype()==PREDICATE_EQUALS ||
       p->getpredicate()->gettype()==PREDICATE_GTE ||
       p->getpredicate()->gettype()==PREDICATE_GT) &&
      possiblysameset(setv,p->getpredicate()->getvalueexpr()->getlabel()->label(),c) &&
      equivalentstrings(rel,p->getpredicate()->getvalueexpr()->getrelation()->getname()))
    return true;
  
  /* check for conflict with |v'.r'|=1 */
  if (p->isrule() &&
      p->getpredicate()->gettype()==PREDICATE_EQ1&&
      p->getpredicate()->getsetexpr()->gettype()==SETEXPR_REL&&
      possiblysameset(setv,p->getpredicate()->getsetexpr()->getlabel()->label(),c)&&
      equivalentstrings(rel,p->getpredicate()->getsetexpr()->getrelation()->getname()))
    return true;

  /* try to catch case of |a'.~r|=1 */
  if (p->isrule() &&
      p->getpredicate()->gettype()==PREDICATE_EQ1 &&
      p->getpredicate()->getsetexpr()->gettype()==SETEXPR_INVREL &&
      possiblysameset(seta,p->getpredicate()->getsetexpr()->getlabel()->label(),c)&&
      equivalentstrings(rel,p->getpredicate()->getsetexpr()->getrelation()->getname()))
    return true;

  /* try to catch case of !(a' in v'.r)*/
  if (p->isrule() &&
      !p->getcoercebool()&&
      p->getpredicate()->gettype()==PREDICATE_SET&&
      p->getpredicate()->getsetexpr()->gettype()==SETEXPR_REL&&
      possiblysameset(seta,p->getpredicate()->getlabel()->label(),c)&&
      possiblysameset(setv,p->getpredicate()->getsetexpr()->getlabel()->label(),c)&&
      equivalentstrings(rel,p->getpredicate()->getsetexpr()->getrelation()->getname()))
    return true;
  
  
  /* try to catch case of !(a' in v'.~r)*/
  if (p->isrule() &&
      !p->getcoercebool()&&
      p->getpredicate()->gettype()==PREDICATE_SET&&
      p->getpredicate()->getsetexpr()->gettype()==SETEXPR_INVREL&&
      possiblysameset(seta,p->getpredicate()->getsetexpr()->getlabel()->label(),c)&&
      possiblysameset(setv,p->getpredicate()->getlabel()->label(),c)&&
      equivalentstrings(rel,p->getpredicate()->getsetexpr()->getrelation()->getname()))
  return true;
  
  return false;
}
