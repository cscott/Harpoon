#include <stdlib.h>
#include "classlist.h"
#include "fieldcheck.h"
#include "common.h"
#include "set.h"
#include "dmodel.h"
#include "omodel.h"
#include "model.h"
#include "Hashtable.h"
#include "normalizer.h"

FieldCheck::FieldCheck(model *m) {
  cptoreqs=NULL;
  nftoprovides=NULL;
  globalmodel=m;
  propertytonf=NULL;
}

bool FieldCheck::testsatisfy(WorkSet *satisfies, FieldTuple *ft) {
  FieldTuple *copy=new FieldTuple(ft);
  bool sat=false;
  while(!sat&&copy!=NULL) {
    if(satisfies->contains(copy)) {
      sat=true;
      break;
    }
    copy->set=globalmodel->getdomainrelation()->getsuperset(globalmodel->getdomainrelation()->getset(copy->set))->getname();
  }
  delete(copy);
  return sat;
}

int FieldCheck::getindexthatprovides(char *set, char *relation) {
  FieldTuple *copy=new FieldTuple(set,relation);
  while(copy!=NULL) {
    if(propertytonf->contains(copy)) {
      /* Found!!! Do stuff */
      NormalForm *nf=(NormalForm *)propertytonf->get(copy);
      for(int i=0;i<globalmodel->getnumconstraints();i++) {
	if (nf==globalmodel->getnormalform(i)) {
	  delete(copy);
	  return i;
	}
      }
      printf("Error in getindexthatprovides\n");
      exit(-1);
    }
    copy->set=globalmodel->getdomainrelation()->getsuperset(globalmodel->getdomainrelation()->getset(copy->set))->getname();
  }
  delete(copy);
  return -1; /* Couldn't find...too bad */
}

void FieldCheck::analyze() {
  WorkSet *satisfied=new WorkSet();
  propertytonf=new Hashtable(NULL,NULL);
  bool change=true;
  bool allsatisfied=true;
  FieldTuple *badft=NULL;
  while(change) {
    allsatisfied=true;
    change=false;
    for(int i=0;i<globalmodel->getnumconstraints();i++) {
      NormalForm *nf=globalmodel->getnormalform(i);
      bool gotallreqs=true;
      for(int j=0;j<nf->getnumsentences();j++) {
	CoerceSentence *cs=nf->getsentence(j);
	for (int k=0;k<cs->getnumpredicates();k++) {
	  CoercePredicate *cp=cs->getpredicate(k);
	  WorkSet *reqs=(WorkSet *)cptoreqs->get(cp);
	  if (reqs!=NULL) {
	    for(FieldTuple *ft=(FieldTuple *)reqs->firstelement();ft!=NULL;
		ft=(FieldTuple*)reqs->getnextelement(ft)) {
	      if (!testsatisfy(satisfied,ft)) {
		gotallreqs=false;
		allsatisfied=false;
		badft=ft;
		break;
	      }
	    }
	  }
	  if(!gotallreqs)
	    break;
	}
	if (gotallreqs) {
	  WorkSet *provides=(WorkSet *)nftoprovides->get(nf);
	  if(provides!=NULL) {
	    for(FieldTuple *ft=(FieldTuple *)provides->firstelement();ft!=NULL;
		ft=(FieldTuple*)provides->getnextelement(ft)) {
	      if(!testsatisfy(satisfied,ft)) {
		/* something to add */
		satisfied->addobject(ft);
		propertytonf->put(ft,nf);
		change=true;
	      }
	    }
	  }
	}
      }
    }
  }
  if (!allsatisfied) {
    printf("Some relations can't be established:<%s,%s>",badft->relation,badft->set);
    exit(-1);
  }
  delete(satisfied);
}

void FieldCheck::buildmaps() {
  cptoreqs=new Hashtable(NULL,NULL);
  nftoprovides=new Hashtable(NULL,NULL);

  for(int i=0;i<globalmodel->getnumconstraints();i++) {
    NormalForm *nf=globalmodel->getnormalform(i);
    Constraint *c=globalmodel->getconstraint(i);
    WorkSet *old=NULL;
    for(int j=0;j<nf->getnumsentences();j++) {
      WorkSet *curr=new WorkSet();
      CoerceSentence *cs=nf->getsentence(j);
      for (int k=0;k<cs->getnumpredicates();k++) {
	CoercePredicate *cp=cs->getpredicate(k);
	WorkSet *wr=requireswhat(c,cp);
	if (wr!=NULL)
	  cptoreqs->put(cp,wr);
	FieldTuple *ft=provideswhat(c,cp);
	if(ft!=NULL)
	  curr->addobject(ft);
	else
	  delete(ft);
      }
      if (old==NULL)
	old=curr;
      else {
	for(FieldTuple *ft=(FieldTuple *)old->firstelement();ft!=NULL;
	    ft=(FieldTuple *)old->getnextelement(ft)) {
	  if (!curr->contains(ft))
	    old->removeobject(ft);
	}
	delete(curr);
      }
    }
    if(old!=NULL) {
      if(old->isEmpty())
	delete old;
      else
	nftoprovides->put(nf, old);
    }
  }
}

WorkSet * FieldCheck::requireswhat(Constraint *c,CoercePredicate *cp) {
  Predicate *p=cp->getpredicate();
  int type=p->gettype();
  if (type==PREDICATE_LT ||
      type==PREDICATE_LTE ||
      type==PREDICATE_EQUALS ||
      type==PREDICATE_GT ||
      type==PREDICATE_GTE) {
    WorkSet *ws=new WorkSet();
    Elementexpr *ee=p->geteleexpr();
    processee(ws,c,ee);
    if (ws->isEmpty()) {
      delete(ws);
      return NULL;
    } else return ws;
  } else return NULL;
}

void FieldCheck::processee(WorkSet *ws, Constraint *c, Elementexpr *ee) {
  switch(ee->gettype()) {
  case ELEMENTEXPR_SUB:
  case ELEMENTEXPR_ADD:
  case ELEMENTEXPR_MULT:
    processee(ws,c,ee->getleft());
    processee(ws,c,ee->getright());
    return;
  case ELEMENTEXPR_RELATION: {
    /* Interesting case */
    char *rel=ee->getrelation()->getname();
    Elementexpr *left=ee->getleft();
    int tleft=left->gettype();
    if (tleft==ELEMENTEXPR_LABEL) {
      ws->addobject(new FieldTuple(rel,getset(c, left->getlabel()->label())));
    } else if (tleft==ELEMENTEXPR_RELATION) {
      DRelation *drl=globalmodel->getdomainrelation()->getrelation(left->getrelation()->getname());
      char *rangeofl=drl->getrange();
      ws->addobject(new FieldTuple(rel,rangeofl));
    } else {
      ee->print();
      printf("Can't determine domain\n");
      exit(-1);
    }
  }
  return;
  default:
    return;
  }
}

bool FieldCheck::setok(Constraint *c, char *set) {
  for(int i=0;i<c->numquants();i++) {
    Quantifier *q=c->getquant(i);
    char *qs=q->getset()->getname();
    DomainRelation *dr=globalmodel->getdomainrelation();
    DomainSet *iset=dr->getset(set);
    DomainSet *qset=dr->getset(qs);
    if (dr->issupersetof(iset,qset))
      return false;
  }
  return true;
}

FieldTuple * FieldCheck::provideswhat(Constraint *c,CoercePredicate *cp) {
  if (cp->getcoercebool())
    return NULL;
  Predicate *p=cp->getpredicate();
  switch(p->gettype()) {
  case PREDICATE_LT:
  case PREDICATE_LTE:
  case PREDICATE_EQUALS:
  case PREDICATE_GTE:
  case PREDICATE_GT: {
    char *relation=p->getvalueexpr()->getrelation()->getname();
    char *var=p->getvalueexpr()->getlabel()->label();
    char *set=getset(c,var);
    if (setok(c,set)) {
      return new FieldTuple(relation,set);
    } else
      return NULL;
  }
  case PREDICATE_SET:
  case PREDICATE_EQ1:
  case PREDICATE_GTE1: {
    Setexpr *se=p->getsetexpr();
    if (se->gettype()==SETEXPR_REL) {
      char *relation=se->getrelation()->getname();
      char *var=se->getlabel()->label();
      char *set=getset(c,var);
      if (setok(c,set)) {
	return new FieldTuple(relation,set);
      } else return NULL;
    }
    return NULL;
  }
  default:
    return NULL;
  }
}

char *getset(Constraint *c, char *var) {
  for (int i=0;i<c->numquants();i++) {
    Quantifier *q=c->getquant(i);
    if (equivalentstrings(var,q->getlabel()->label())) {
      return q->getset()->getname();
    }
  }
  return NULL;
}

unsigned int FieldTuple::hashCode() {
  return hashstring(relation)^hashstring(set);
}

bool FieldTuple::equals(ElementWrapper *other) {
  if (other->type()!=ELEMENT_FTUPLE)
    return false;
  FieldTuple *oft=(FieldTuple *)other;
  if (equivalentstrings(relation, oft->relation)&&
      equivalentstrings(set,oft->set))
    return true;
  else
    return false;
}

int FieldTuple::type() {
  return ELEMENT_FTUPLE;
}

FieldTuple::FieldTuple(char * r, char *s) {
  relation=r;
  set=s;
}

FieldTuple::FieldTuple(FieldTuple *o) {
  this->relation=o->relation;
  this->set=o->set;
}
