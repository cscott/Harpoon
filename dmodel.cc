// defines the sets and the relations used

#include <stdlib.h>
#include <stdio.h>
#include "dmodel.h"
#include "set.h"
#include "Relation.h"
#include "Hashtable.h"
#include "model.h"
#include "Guidance.h"


// class DomainSet

DomainSet::DomainSet(char *name) {
  setname=name;
  flag=0;
  this->type=NULL;
  numsubsets=0;
  subsets=NULL;
  set=new WorkSet();
}

void DomainSet::reset() {
  delete(set);
  set=new WorkSet();
}

char * DomainSet::getelementtype() {
  return type;
}

int DomainSet::gettype() {
  return flag%DOMAINSET_TYPED;
}

void DomainSet::settype(char *type) {
  this->type=type;
  flag|=DOMAINSET_TYPED;
}
  
void DomainSet::setsubsets(char **subsets, int numsubsets) {
  this->subsets=subsets;
  this->numsubsets=numsubsets;
  flag=DOMAINSET_SUBSET;
}

void DomainSet::setpartition(char **subsets, int numsubsets) {
  this->subsets=subsets;
  this->numsubsets=numsubsets;
  flag=DOMAINSET_PARTITION;
}
 
void DomainSet::print() {
  printf("%s",setname);
  if (DOMAINSET_TYPED&flag)
    printf("(%s)",type);
  printf(":");
  if (DOMAINSET_PARTITION&flag)
    printf("partition ");
  for(int i=0;i<numsubsets;i++)
    if (i==0)
      printf("%s ",subsets[i]);
    else
      printf("| %s",subsets[i]);
  printf("Size: %d",set->size());
}

char * DomainSet::getname() {
  return setname;
}
 
WorkSet * DomainSet::getset() {
  return set;
}

int DomainSet::getnumsubsets() {
  return numsubsets;
}

char * DomainSet::getsubset(int i) {
  return subsets[i];
}




// class DRelation

DRelation::DRelation(char *n, char *d, char *r, int t, bool b) {
  domain=d;range=r;type=t;name=n;
  relation=new WorkRelation();
  staticrel=b;
  tokenrange = NULL;
}

void DRelation::reset() {
  delete(relation);
  relation=new WorkRelation();
}

bool DRelation::isstatic() {
  return staticrel;
}

char * DRelation::getdomain() {
  return domain;
}

char * DRelation::getrange() {
  return range;
}

WorkSet* DRelation::gettokenrange() {
  return tokenrange;
}

void DRelation::settokenrange(WorkSet *ws) {
  tokenrange = ws;
}



void DRelation::print() {
  printf("%s: %s -> %s (",name,domain,range);
  if (type&DRELATION_MANYDOMAIN)
    printf("M");
  else
    printf("1");
  printf("->");
  if (type&DRELATION_MANYRANGE)
    printf("M");
  else
    printf("1");
  printf(")");

}

char * DRelation::getname() {
  return name;
}

WorkRelation * DRelation::getrelation() {
  return relation;
}
 




// class DomainRelation

DomainRelation::DomainRelation(DomainSet **s, int ns, DRelation **r,int nr) {
  sets=s; numsets=ns;
  relations=r;numrelations=nr;
  settable=new Hashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);
  relationtable=new Hashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);
  for(int i=0;i<numsets;i++)
    settable->put(sets[i]->getname(),sets[i]);
  for(int i=0;i<numrelations;i++)
    relationtable->put(relations[i]->getname(),relations[i]);
}

void DomainRelation::reset() {
  for(int i=0;i<numsets;i++) {
    sets[i]->reset();
  }
  for(int i=0;i<numrelations;i++) {
    relations[i]->reset();
  }
}

bool DomainRelation::issupersetof(DomainSet *sub,DomainSet *super) {
  while(sub!=NULL) {
    if (sub==super)
      return true;
    sub=getsuperset(sub);
  }
  return false;
}

void DomainRelation::print() {
  for(int i=0;i<numsets;i++) {
    sets[i]->print();
    printf("\n");
  }
  printf("\n");
  for(int i=0;i<numrelations;i++) {
    relations[i]->print();
    printf("\n");
  }
}

DomainSet * DomainRelation::getset(char * setname) {
  if (setname!=NULL)
    return (DomainSet *)settable->get(setname);
  else return NULL;
}

DRelation * DomainRelation::getrelation(char * relationname) {
  if (relationname!=NULL)
    return (DRelation *)relationtable->get(relationname);
  else
    return NULL;
}

DomainRelation::~DomainRelation() {
  delete(settable);
  delete(relationtable);
  for(int i=0;i<numsets;i++)
    delete(sets[i]);
  for(int i=0;i<numrelations;i++)
    delete(relations[i]);
  delete(sets);
  delete(relations);
}

void DomainRelation::addallsubsets(DomainSet *ds, WorkSet *ws) {
  WorkSet *tmp=new WorkSet(true);
  tmp->addobject(ds);
  while(!tmp->isEmpty()) {
    DomainSet *s=(DomainSet *)tmp->firstelement();
    tmp->removeobject(s);
    ws->addobject(s);
    for(int j=0;j<s->getnumsubsets();j++) {
      tmp->addobject(getset(s->getsubset(j)));
    }
  }
  delete(tmp);
}

WorkSet * DomainRelation::conflictdelsets(char * setname, char * boundset) {
  /* Want to know what set removals insertion into "setname" could cause */
  if (equivalentstrings(setname,"int"))
    return new WorkSet(true);
  if (equivalentstrings(setname,"token"))
    return new WorkSet(true);
  DomainSet *bs=getset(boundset);
  WorkSet *wsret=new WorkSet(true);
  WorkSet *ws=new WorkSet(true);
  while(bs!=NULL) {
    ws->addobject(bs);
    bs=getsuperset(bs);
  }

  DomainSet *oldcs=getset(setname);
  DomainSet *cs=getsuperset(oldcs);
  

  while(cs!=NULL) {
    if (ws->contains(cs)) {
      if (cs->gettype()==DOMAINSET_PARTITION &&
	  !equivalentstrings(cs->getname(),boundset)) {
	delete(ws);
	delete(wsret);
	ws=new WorkSet(true);

	DomainSet *bs=getset(boundset);
	addallsubsets(bs,ws);
	DomainSet *oldbs=bs;
	bs=getsuperset(oldbs);
	while(bs!=cs) {
	  ws->addobject(bs);
	  if (bs->gettype()!=DOMAINSET_PARTITION) {
	    for(int i=0;i<bs->getnumsubsets();i++) {
	      DomainSet *tss=getset(bs->getsubset(i));
	      if (oldbs!=tss) {
		addallsubsets(tss,ws);
	      }
	    }
	  }
	  oldbs=bs;
	  bs=getsuperset(oldbs);
	}
	return ws;
      }
      break;
    }
    if (cs->gettype()==DOMAINSET_PARTITION) {
      /* We have a partition...got to look at all other subsets */
      for(int i=0;i<cs->getnumsubsets();i++) {
	if (!equivalentstrings(cs->getsubset(i),oldcs->getname())) {
	  addallsubsets(getset(cs->getsubset(i)),wsret);
	}
      }
    }
    oldcs=cs;
    cs=getsuperset(cs);
  }
  delete(ws);
  return wsret;
}

DomainSet * DomainRelation::getsuperset(DomainSet *s) {
  char *name=s->getname();
  for(int i=0;i<numsets;i++)
    for (int j=0;j<sets[i]->getnumsubsets();j++) {
      if(equivalentstrings(name,sets[i]->getsubset(j)))
	return sets[i];
    }
  return NULL;
}

WorkSet * DomainRelation::conflictaddsets(char * setname, char *boundset, model *m) {
  /* Want to know what set additions insertion into "setname" could cause */
  if (equivalentstrings(setname,"int"))
    return new WorkSet(true);
  if (equivalentstrings(setname,"token"))
    return new WorkSet(true);
  DomainSet *bs=getset(boundset);
  WorkSet *wsret=new WorkSet(true);
  WorkSet *ws=new WorkSet(true);
  while(bs!=NULL) {
    ws->addobject(bs);
    bs=getsuperset(bs);
  }

  Guidance *g=m->getguidance();
  DomainSet *ds=getset(g->insertiontoset(setname));
  while(ds!=NULL) {
    if (ws->contains(ds))
      break;
    wsret->addobject(ds);
    ds=getsuperset(ds);
  }

  delete(ws);
  return wsret;
}

WorkSet * DomainRelation::removeconflictdelsets(char *setname) {
  /* Obviously remove from all subsets*/
  WorkSet *tmp=new WorkSet(true);
  WorkSet *wsret=new WorkSet(true);
  tmp->addobject(getset(setname));
  while(!tmp->isEmpty()) {
    DomainSet *s=(DomainSet *)tmp->firstelement();
    tmp->removeobject(s);
    wsret->addobject(s);
    for(int j=0;j<s->getnumsubsets();j++)
      tmp->addobject(getset(s->getsubset(j)));
  }
  delete(tmp);
  return wsret;
}

WorkSet * DomainRelation::removeconflictaddsets(char *setname, model *m) {
  /* Remove could cause addition to a new set...*/
  DomainSet *ds=getset(setname);
  Guidance *g=m->getguidance();
  char *settoputin=g->removefromset(setname);
  if (settoputin==NULL)
    return new WorkSet(true);
  return conflictaddsets(settoputin, setname, m);
}

DomainSet * DomainRelation::getsource(DomainSet *s) {
  return getsuperset(s);
}

void DomainRelation::addtoset(Element *ele, DomainSet *settoadd, model *m) {
  /* Assumption is that object is not in set*/
  if(settoadd->getset()->contains(ele)) /* Already in set-no worries */
    return;
  if(settoadd->gettype()==DOMAINSET_PARTITION) {
    /* Have to find subset to add to */
    char *subsettoadd=m->getguidance()->insertiontoset(settoadd->getname());
    DomainSet *setptr=getset(subsettoadd);
    while(setptr!=settoadd) {
      setptr->getset()->addobject(ele);
      m->triggerrule(ele,setptr->getname());
      setptr=getsuperset(setptr);
    }
  }
  settoadd->getset()->addobject(ele);
  m->triggerrule(ele,settoadd->getname());
  DomainSet *oldptr=settoadd;
  DomainSet *ptr=getsuperset(oldptr);
  while((ptr!=NULL)&&(!ptr->getset()->contains(ele))) {
    ptr->getset()->addobject(ele);
    m->triggerrule(ele,ptr->getname());
    oldptr=ptr;
    ptr=getsuperset(ptr);
  }
  if(ptr!=NULL&&
     ptr->gettype()==DOMAINSET_PARTITION) {
    /* may have to do removes....*/
    for(int i=0;i<ptr->getnumsubsets();i++) {
      char *subset=ptr->getsubset(i);
      DomainSet *ptrsubset=getset(subset);
      if (oldptr!=ptrsubset&&
	  ptrsubset->getset()->contains(ele)) {
	/* GOT THE ONE*/
	WorkSet *ws=new WorkSet(true);
	ws->addobject(ptrsubset);
	while(!ws->isEmpty()) {
	  DomainSet *ds=(DomainSet *)ws->firstelement();
	  ws->removeobject(ds);
	  if (ds->getset()->contains(ele)) {
	    for(int j=0;j<ds->getnumsubsets();j++) {
	      ws->addobject(getset(ds->getsubset(j)));
	    }
	    removefromthisset(ele, ds,m);
	  }
	}
	delete(ws);
	break;
      }
    }
  }
}

void DomainRelation::abstaddtoset(Element *ele, DomainSet *settoadd, model *m) {
  /* Assumption is that object is not in set*/
  if(settoadd->getset()->contains(ele)) /* Already in set-no worries */
    return;
  if(settoadd->gettype()==DOMAINSET_PARTITION) {
    /* Have to find subset to add to */
    char *subsettoadd=m->getguidance()->insertiontoset(settoadd->getname());
    DomainSet *setptr=getset(subsettoadd);
    while(setptr!=settoadd) {
      setptr->getset()->addobject(ele);
      m->triggerrule(ele,setptr->getname());
      setptr=getsuperset(setptr);
    }
  }
  settoadd->getset()->addobject(ele);
  m->triggerrule(ele,settoadd->getname());
  DomainSet *oldptr=settoadd;
  DomainSet *ptr=getsuperset(oldptr);
  while((ptr!=NULL)&&(!ptr->getset()->contains(ele))) {
    ptr->getset()->addobject(ele);
    m->triggerrule(ele,ptr->getname());
    oldptr=ptr;
    ptr=getsuperset(ptr);
  }
}

void DomainRelation::removefromthisset(Element *ele, DomainSet *ds, model *m) {
  ds->getset()->removeobject(ele); /*removed from set*/
  /* Next need to search relations */
  for(int i=0;i<numrelations;i++) {
    DRelation * relation=relations[i];
    if (equivalentstrings(relation->getdomain(),ds->getname()))
      for(Element *target=(Element *) relation->getrelation()->getobj(ele);target!=NULL;target=(Element *) relation->getrelation()->getobj(ele)) {
	relation->getrelation()->remove(ele,target);
	if (relation->isstatic()) {
	  /* Have to actually remove target*/
	  DomainSet *targetset=getset(relation->getrange());
	  delfromsetmovetoset(target,targetset,m);
	}
      }
    if (equivalentstrings(relation->getrange(),ds->getname()))
      for(Element *target=(Element *) relation->getrelation()->invgetobj(ele);target!=NULL;target=(Element *) relation->getrelation()->invgetobj(ele)) {
	relation->getrelation()->remove(target,ele);
	if (relation->isstatic()) {
	  DomainSet *targetset=getset(relation->getdomain());
	  delfromsetmovetoset(target,targetset,m);
	}
      }
  }
}

void DomainRelation::delfromsetmovetoset(Element *ele,DomainSet *deletefromset,model *m) {
  WorkSet *ws=new WorkSet(true);
  ws->addobject(deletefromset);
  while(!ws->isEmpty()) {
    DomainSet *ds=(DomainSet *)ws->firstelement();
    ws->removeobject(ds);
    if (ds->getset()->contains(ele)) {
      for(int j=0;j<ds->getnumsubsets();j++) {
	ws->addobject(getset(ds->getsubset(j)));
      }
      removefromthisset(ele, ds,m);
    }
  }
  delete(ws);
  char *mts=m->getguidance()->removefromset(deletefromset->getname());
  DomainSet *movetoset=getset(mts);
  addtoset(ele, movetoset, m); //Add to the movetoset now...
}

int DomainRelation::getnumrelation() {
  return numrelations;
}

DRelation * DomainRelation::getrelation(int i) {
  return relations[i];
}


bool DomainRelation::fixstuff() {
  bool anychange=false;
  /* Guaranteed fixpoint because we keep removing items...finite # of items */
  while(true) {
    bool changed=false;
    for(int i=0;i<numsets;i++) {
      if(checksubset(sets[i])) {
	changed=true;
	anychange=true;
      }
    }
    for(int i=0;i<numrelations;i++) {
      if(checkrelations(relations[i])) {
	changed=true;
	anychange=true;
      }
    }
#ifdef REPAIR
    /* Fix point only necessary if repairing */
    if(!changed)
#endif
      break;
  }
  return anychange;
}




/* propagate the changes so that all the subset inclusion and partition
   constraints are satisfied. */
bool DomainRelation::checksubset(DomainSet *ds) {
  // remove all elements in ds that are not contained by its superset
  bool changed=false;
  DomainSet *superset=getsuperset(ds);
  WorkSet *ws=ds->getset();
  WorkSet *wssuper=ds->getset();

  void *ele=ws->firstelement();
  while(ele!=NULL) {
    if (!wssuper->contains(ele)) {
      void *old=ele;
      ele=ws->getnextelement(ele);
      changed=true;
#ifdef REPAIR
      ws->removeobject(old);
#endif
    } else
      ele=ws->getnextelement(ele);
  }
  /* Superset inclusion property guaranteed */


  /* If an element is contained by more than one subset, remove it from
     all subsets but the first one.  If an element is not contained by 
     any subset, remove it from the superset */
  if (ds->gettype()==DOMAINSET_PARTITION) {
    ele=ws->firstelement();
    while(ele!=NULL) {
      int inccount=0;
      for(int i=0;i<ds->getnumsubsets();i++) {
	char *subsetname=ds->getsubset(i);
	DomainSet *subset=getset(subsetname);
	if (subset->getset()->contains(ele)) {
	  if (inccount==0)
	    inccount++;
	  else {
	    /* Partition exclusion property */
	    changed=true;
#ifdef REPAIR
	    subset->getset()->removeobject(ele);
#endif
	  }
	}
      }
      if (inccount==0) {
	/* Partition inclusion property */
	changed=true;
#ifdef REPAIR
	ws->removeobject(ele);
#endif
      }
      ele=ws->getnextelement(ele);
    }
  }
  return changed;
}



bool DomainRelation::checkrelations(DRelation *dr) {
  DomainSet  *range=getset(dr->getrange());
  DomainSet *domain=getset(dr->getdomain());
  WorkSet *rangeset=NULL,*domainset=NULL;
  bool changed=false;
  if (range!=NULL)
    rangeset=range->getset();
  if (domain!=NULL)
    domainset=domain->getset();
  WorkRelation *rel=dr->getrelation();
  Tuple ele=rel->firstelement();
  while(!ele.isnull()) {
    if((domainset!=NULL&&!domainset->contains(ele.left))||
	 (rangeset!=NULL&&!rangeset->contains(ele.right))) {
      void *l=ele.left;
      void *r=ele.right;
      ele=rel->getnextelement(l,r);
      changed=true;
#ifdef REPAIR
      rel->remove(l,r);
#endif
    } else {
      ele=rel->getnextelement(ele.left,ele.right);
    }
  }
  /* Relation is clean now also */
  return changed;
}
