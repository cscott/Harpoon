#include <stdlib.h>
#include <stdio.h>
#include "repair.h"
#include "model.h"
#include "normalizer.h"
#include "list.h"
#include "Action.h"
#include "processobject.h"
#include "ActionInSet.h"
#include "ActionNotInSet.h"
#include "ActionGEQ1.h"
#include "ActionEQ1.h"
#include "ActionAssign.h"
#include "ActionNormal.h"
#include "Relation.h"
#include "omodel.h"
#include "dmodel.h"
#include "set.h"
#include "common.h"
#include "fieldcheck.h"
#include "Hashtable.h"

Repair::Repair(model *m) {
  globalmodel=m;
  removedsentences=NULL;
  List *list=new List();
  /*  list->addobject(); --build action array */
  list->addobject(new ActionGEQ1(m->getdomainrelation(),m));
  list->addobject(new ActionEQ1(m->getdomainrelation(),m));
  list->addobject(new ActionAssign(m->getdomainrelation(),m));
  //  list->addobject(new ActionNotAssign(m->getdomainrelation()));
  list->addobject(new ActionInSet(m->getdomainrelation(),m));
  list->addobject(new ActionNotInSet(m->getdomainrelation(),m));
  numactions=list->size();
  repairactions=new Action *[list->size()];
  list->toArray((void **)repairactions);
  delete(list);
}


void Repair::repaireleexpr(Constraint *c, processobject *po, Elementexpr *ee, Hashtable *env) {
  /* We're here because we have a bad elementexpr */
  switch(ee->gettype()) {
  case ELEMENTEXPR_SUB:
  case ELEMENTEXPR_ADD:
  case ELEMENTEXPR_MULT:
    repaireleexpr(c,po,ee->getleft(),env);
    repaireleexpr(c,po,ee->getright(),env);
    return;
  case ELEMENTEXPR_RELATION: {
    /* Could have problem here */
    repaireleexpr(c,po,ee->getleft(),env);
    Element *e=evaluateexpr(ee,env,globalmodel);
    if (e==NULL) {
      /* found bad relation */
      FieldCheck *fc=globalmodel->getfieldcheck();
      char *set=NULL;
      
      {
	Elementexpr *left=ee->getleft();
	int tleft=left->gettype();
	if (tleft==ELEMENTEXPR_LABEL) {
	  set=getset(c, left->getlabel()->label());
	} else if (tleft==ELEMENTEXPR_RELATION) {
	  DRelation *drl=globalmodel->getdomainrelation()->getrelation(left->getrelation()->getname());
	  set=drl->getrange();
	} else {
	  ee->print();
	  printf("Can't determine domain-shouldn't be here because of static analysis...\n");
	  exit(-1);
	}
      }

      Element *e2=evaluateexpr(ee->getleft(),env,globalmodel);
      int index=fc->getindexthatprovides(ee->getrelation()->getname(), set);
      Hashtable *env2=new Hashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);
      env2->setparent(env);
      Constraint *crep=globalmodel->getconstraint(index);
      DomainRelation *dr=globalmodel->getdomainrelation();
      for(int j=0;j<crep->numquants();j++) {
	Quantifier *qrep=crep->getquant(j);
	char *label=qrep->getlabel()->label();
	char *setname=qrep->getset()->getname();
	DomainSet *dset=dr->getset(setname);
	if (dset->getset()->contains(e2)) {
	  env2->put(label,e2);
	} else {
	  env2->put(label,dset->getset()->firstelement()); /*This shouldn't happen right now*/
	  printf("Funnyness in repair.cc\n");
	}
      }
      this->repairconstraint(globalmodel->getconstraint(index),po,env2);
      delete(e2);
      delete(env2);
    } else
      delete(e);
    return;
  }
  default:
    return;
  }
}


void Repair::repairconstraint(Constraint *c, processobject *po, Hashtable *env) {
  NormalForm *nf=globalmodel->getnormalform(c);
  CoerceSentence *coercionstate=nf->closestmatch(removedsentences,po,env);
  /* Now we just need to coerce the predicates!!!*/
  for(int i=0;i<coercionstate->getnumpredicates();i++) {
    CoercePredicate *cp=coercionstate->getpredicate(i);
    int status=po->processpredicate(cp->getpredicate(),env);
    while (status==PFAIL) {
      /* Bad dereference...need to fix things*/
      repaireleexpr(c, po,cp->getpredicate()->geteleexpr(), env);
      status=po->processpredicate(cp->getpredicate(),env);
    }

    if (status!=cp->getcoercebool()) {
      /*Need to coerce*/
      Action *act=findrepairaction(cp);
      act->repairpredicate(env,cp);
      /* Data structure fixed - well eventually*/
    }
  }
}


// returns the action that can repair the given predicate
Action * Repair::findrepairaction(CoercePredicate *cp) {
  for(int i=0;i<numactions;i++) {
    if (repairactions[i]->canrepairpredicate(cp))
      return repairactions[i];
  }
  return NULL;
}



// returns the action that can break the given predicate
Action * Repair::findbreakaction(CoercePredicate *cp) 
{
  return findrepairaction(cp);
}


bool Repair::analyzetermination() {
  WorkRelation *wr=new WorkRelation(true);
  buildmap(wr);
  /* Map built */
  WorkSet *cycleset=searchcycles(wr);
  if (!cycleset->isEmpty()) {
    WorkSet *removeedges=new WorkSet(true);
    for(int i=1;i<=cycleset->size();i++) {
      if (breakcycles(removeedges, i, cycleset,wr)) {
	removedsentences=removeedges;
	printf("Modified constraints for repairability!\n");
	outputgraph(removeedges, wr, "cycle.dot");
	return true;
      }
    }
    delete(removeedges);
    return false;
  } else {
    outputgraph(NULL,wr, "cycle.dot");
    return true;
  }
}



bool Repair::breakcycles(WorkSet *removeedge, int number, WorkSet *cyclelinks, WorkRelation *wr) {
  for(CoerceSentence *cs=(CoerceSentence *)cyclelinks->firstelement();
      cs!=NULL;cs=(CoerceSentence *)cyclelinks->getnextelement(cs)) {
    if (!removeedge->contains(cs)) {
      removeedge->addobject(cs);
      if (number==1) {
	if (!checkforcycles(removeedge, wr))
	  return true;
      } else {
	if (breakcycles(removeedge,number-1,cyclelinks,wr))
	  return true;
      }
      removeedge->removeobject(cs);
    }
  }
  return false;
}

void Repair::outputgraph(WorkSet *removededges, WorkRelation *wr, char *filename) {
  FILE * dotfile=fopen(filename,"w");
  fprintf(dotfile,"digraph cyclegraph {\n");
  fprintf(dotfile,"ratio=auto\n");
  for(int i=0;i<globalmodel->getnumconstraints();i++) {
    NormalForm *nf=globalmodel->getnormalform(i);
    for(int j=0;j<nf->getnumsentences();j++) {
      CoerceSentence *cs=nf->getsentence(j);
      for(int i=0;i<cs->getnumpredicates();i++) {
	CoercePredicate *cp=cs->getpredicate(i);
	fprintf(dotfile,"%lu -> %lu [style=dashed]\n",nf,cp);
	WorkSet *setofnf=wr->getset(cp);
	if (setofnf!=NULL) {
	  NormalForm *nf2=(NormalForm*)setofnf->firstelement();
	  while(nf2!=NULL) {
	    /* cp interferes with nf2 */
	    bool removed=(removededges!=NULL)&&removededges->contains(cs); /*tells what color of edge to generate*/
	    if (removed)
	      fprintf(dotfile,"%lu -> %lu [style=dotted]\n",cp,nf2);
	    else
	      fprintf(dotfile,"%lu -> %lu\n",cp,nf2);
	    nf2=(NormalForm *)setofnf->getnextelement(nf2);
	  }
	}
      }
    }
  }
  fprintf(dotfile,"}\n");
  fclose(dotfile);
}

bool Repair::checkforcycles(WorkSet *removededges, WorkRelation *wr) {
  /* Check that there are no cycles and
     that system is solvable */
  for(int i=0;i<globalmodel->getnumconstraints();i++) {
    NormalForm *nf=globalmodel->getnormalform(i);
    bool good=false;
    for(int j=0;j<nf->getnumsentences();j++) {
      if (!removededges->contains(nf->getsentence(j))) {
	CoerceSentence *cs=nf->getsentence(j);
	bool allgood=true;
	for(int k=0;k<cs->getnumpredicates();k++) {
	  if(cs->getpredicate(k)->isrule()&&
	     findrepairaction(cs->getpredicate(k))==NULL) {
	    allgood=false;
	    break;
	  }
	}
	if(allgood) {
	  good=true;
	  break;
	}
      }
    }
    if (!good)
      return true;
  }
  WorkSet *tmpset=new WorkSet(true);
  for(int i=0;i<globalmodel->getnumconstraints();i++) {
    NormalForm *nf=globalmodel->getnormalform(i);
    bool good=false;
    for(int j=0;j<nf->getnumsentences();j++) {
      CoerceSentence *cs=nf->getsentence(j);
      if (checkcycles(cs,removededges,tmpset,wr)) {
	delete(tmpset);
	return true;
      }
    }
  }
  delete(tmpset);
  return false; /*yeah...no cycles*/
}



bool Repair::checkcycles(CoerceSentence *cs,WorkSet *removededges, WorkSet *searchset,WorkRelation *wr) {
  if (searchset->contains(cs)) {
    /* found cycle */
    return true;
  }
  if (removededges->contains(cs))
    return false; /* this edge is removed...don't consider it*/

  searchset->addobject(cs);
  for(int i=0;i<cs->getnumpredicates();i++) {
    CoercePredicate *cp=cs->getpredicate(i);
    WorkSet *setofnf=wr->getset(cp);
    if (setofnf!=NULL) {
      /* Might not mess up anything */
      NormalForm *nf=(NormalForm*)setofnf->firstelement();
      while(nf!=NULL) {
	for(int j=0;j<nf->getnumsentences();j++) {
	  CoerceSentence *cssearch=nf->getsentence(j);
	  if (checkcycles(cssearch, removededges, searchset, wr))
	    return true;
	}
	nf=(NormalForm *)setofnf->getnextelement(nf);
      }
    }
  }
  searchset->removeobject(cs);
  return false;
}



WorkSet * Repair::searchcycles(WorkRelation *wr) {
  /* Do cycle search */
  WorkSet *cycleset=new WorkSet(true);
  WorkSet *searchset=new WorkSet(true);
  for(int i=0;i<globalmodel->getnumconstraints();i++) {
    NormalForm *nf=globalmodel->getnormalform(i);
    for(int j=0;j<nf->getnumsentences();j++) {
      detectcycles(nf->getsentence(j),searchset,cycleset,wr);
    }
  }
  delete(searchset);
  return cycleset;
}



void Repair::detectcycles(CoerceSentence *cs,WorkSet *searchset,WorkSet *cycleset, WorkRelation *wr) {
  if (searchset->contains(cs)) {
    /* found cycle */
    CoerceSentence *csptr=(CoerceSentence *)searchset->firstelement();
    while(csptr!=NULL) {
      cycleset->addobject(csptr);
      csptr=(CoerceSentence *)searchset->getnextelement(csptr);
    }
    return;
  }
  searchset->addobject(cs);
  for(int i=0;i<cs->getnumpredicates();i++) {
    CoercePredicate *cp=cs->getpredicate(i);
    WorkSet *setofnf=wr->getset(cp);
    if (setofnf!=NULL) {
      /* Might not have any interference edges*/
      NormalForm *nf=(NormalForm*)setofnf->firstelement();
      while(nf!=NULL) {
	for(int j=0;j<nf->getnumsentences();j++) {
	  CoerceSentence *cssearch=nf->getsentence(j);
	  detectcycles(cssearch, searchset, cycleset, wr);
	}
	nf=(NormalForm*)setofnf->getnextelement(nf);
      }
    }
  }
  searchset->removeobject(cs);
}



void Repair::buildmap(WorkRelation *wr) {
  for(int i=0;i<globalmodel->getnumconstraints();i++) {
    NormalForm *nf=globalmodel->getnormalform(i);
    for(int j=0;j<nf->getnumsentences();j++) {
      CoerceSentence *cs=nf->getsentence(j);
      for (int k=0;k<cs->getnumpredicates();k++) {
	Action *repairaction=findrepairaction(cs->getpredicate(k));
	if (repairaction==NULL) {
	  /* Nothing will repair this */
	  cs->getpredicate(k)->getpredicate()->print();
	  printf(" can't be repaired!!!");
	  exit(-1);
	}
	checkpredicate(repairaction,wr, globalmodel->getconstraint(i),cs->getpredicate(k));
      }
    }
  }
  ActionNormal *repairnormal=new ActionNormal(globalmodel);
  for(int i=0;i<globalmodel->getnumrulenormal();i++) {
    NormalForm *nf=globalmodel->getrulenormal(i);
    CoercePredicate *cpo=nf->getsentence(0)->getpredicate(0);
    /* Check for conflicts between abstraction functions */
    for(int j=0;j<globalmodel->getnumrulenormal();j++) {
      NormalForm *nfn=globalmodel->getrulenormal(j);
      CoercePredicate *cpn=nfn->getsentence(0)->getpredicate(0);
      if (!cpo->istuple()&&
	  equivalentstrings(cpo->getrelset(),cpn->gettriggerset()))
	wr->put(cpo,nfn);
    }

    /* Check for conflict between abstraction functions and model constraints */
    for(int i2=0;i2<globalmodel->getnumconstraints();i2++) {
      NormalForm *nf2=globalmodel->getnormalform(i2);
      for(int j2=0;j2<nf2->getnumsentences();j2++) {
	CoerceSentence *cs2=nf2->getsentence(j2);
	for (int k2=0;k2<cs2->getnumpredicates();k2++) {
	  if (repairnormal->conflict(NULL,cpo,globalmodel->getconstraint(i2),cs2->getpredicate(k2)))
	    wr->put(cpo,nf2);
	}
      }
    }
  }
  delete(repairnormal);
}



void Repair::checkpredicate(Action *repair,WorkRelation *wr, Constraint *c,CoercePredicate *cp) {
  for(int i=0;i<globalmodel->getnumconstraints();i++) {
    NormalForm *nf=globalmodel->getnormalform(i);
    for(int j=0;j<nf->getnumsentences();j++) {
      CoerceSentence *cs=nf->getsentence(j);
      for (int k=0;k<cs->getnumpredicates();k++) {
	if (repair->conflict(c,cp,globalmodel->getconstraint(i),cs->getpredicate(k)))
	  wr->put(cp, nf);
      }
    }
  }
  for(int i=0;i<globalmodel->getnumrulenormal();i++) {
    NormalForm *nf=globalmodel->getrulenormal(i);
    if (repair->conflict(c,cp,NULL, nf->getsentence(0)->getpredicate(0)))
      wr->put(cp,nf);
  }
}

