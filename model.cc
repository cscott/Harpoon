#include "model.h"
#include "token.h"
#include "typeparser.h"
#include "dparser.h"
#include "aparser.h"
#include "cparser.h"
#include "oparser.h"
#include "dmodel.h"
#include "omodel.h"
#include "amodel.h"
#include "tmodel.h"
#include "list.h"
#include "processabstract.h"
#include "processobject.h"
#include "processconcrete.h"
#include "normalizer.h"
#include <stdio.h>
#include <stdlib.h>
#include "Hashtable.h"
#include "element.h"
#include "bitreader.h"
#include "DefaultGuidance2.h"
#include "DefaultGuidance.h"
#include "DefaultGuidance3.h"
#include "repair.h"
#include "fieldcheck.h"
#include "tmap.h"

Hashtable * model::gethashtable() {
  return env;
}

bitreader * model::getbitreader() {
  return br;
}

Guidance * model::getguidance() {
  return guidance;
}

model::model(char * abstractfile, char * modelfile, char *spacefile,char *structfile,char * concretefile) {
  parsestructfile(structfile);
  //Hashtable first for lookups
  env=new Hashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);
  //  env->put(&bstring,new Element(&badstruct,getstructure("arrayofstructs")));//should be of badstruct
  parseabstractfile(abstractfile);
  parsemodelfile(modelfile);
  parsespacefile(spacefile);
  parseconcretefile(concretefile);
  br=new bitreader(this,env);
  guidance=new DefGuidance(this); //DefGuidance2
  repair=new Repair(this);
  if (!repair->analyzetermination()) {
    printf("Constraint set might not terminate and can't be repaired!\n");
    exit(-1);
  }
  fc=new FieldCheck(this);
  fc->buildmaps();
  fc->analyze();
  typmap=new typemap(this);
}

void model::reset() {
  typmap->reset();
  domainrelation->reset();
}

typemap * model::gettypemap() {
  return typmap;
}

Repair * model::getrepair() {
  return repair;
}

NormalForm * model::getnormalform(Constraint *c) {
  for (int i=0;i<numconstraint;i++) {
    if (c==constraintarray[i])
      return constraintnormal[i];
  }
  printf("Error finding normal form\n");
  exit(-1);
}

int model::getnumconstraints() {
  return numconstraint;
}

NormalForm * model::getnormalform(int i) {
  return constraintnormal[i];
}

Constraint * model::getconstraint(int i) {
  return constraintarray[i];
}

void model::doabstraction() {
  pa=new processabstract(this);
  bool clean=false;
  /* Process rules until we reach a fixpoint*/
  /* First the normal rules */
  do {
    clean=false;
    for(int i=0;i<numrules;i++) {
      if(!rulearray[i]->isdelayed()) {
	pa->setclean();
	pa->processrule(rulearray[i]);
	if (pa->dirtyflagstatus())
	  clean=true;
      }
    }
  } while(clean);
  /* Then the delayed rules */
  do {
    clean=false;
    for(int i=0;i<numrules;i++) {
      if(rulearray[i]->isdelayed()) {
	pa->setclean();
	pa->processrule(rulearray[i]);
	if (pa->dirtyflagstatus())
	  clean=true;
      }
    }
  } while(clean);
}

void model::triggerrule(Element *ele,char *set) {
  for(int i=0;i<numrules;i++) {
    if(rulearray[i]->isstatic()) {
      pa->processrule(rulearray[i], ele, set);
    }
  }
}

void model::doconcrete() {
  processconcrete *pr=new processconcrete(this);
  for(int i=0;i<numconcrete;i++) {
    pr->processrule(concretearray[i]);
  }
  delete(pr);
}

void model::docheck() {
  processobject *po=new processobject(this);
  bool t=false;
  do {
    t=false;
    /* Process rules until we reach a fixpoint*/
    for(int i=0;i<numconstraint;i++) {
      if (!po->processconstraint(constraintarray[i]))
	t=true; //Got to keep running
    }
  } while(t);
  delete(po);
}

DomainRelation * model::getdomainrelation() {
  return domainrelation;
}

void model::parsespacefile(char *spacefile) {
  ifstream *ifs=new ifstream();
  ifs->open(spacefile);
  Reader *r=new Reader(ifs);
  Dparser *p=new Dparser(r);
  domainrelation=p->parsesetrelation();
#ifdef DEBUGMESSAGES
  domainrelation->print();
#endif
  ifs->close();
  delete(ifs);
}

void model::parsestructfile(char *structfile) {
  ifstream *ifs=new ifstream();
  ifs->open(structfile);
  Reader *r=new Reader(ifs);
  Typeparser *p=new Typeparser(r);
  List *list=new List();
  
  do {
    structure *st=p->parsestructure();
    if (st!=NULL) {
      list->addobject(st);
#ifdef DEBUGMESSAGES
      st->print();
#endif
    }
    else
      break;
  } while(true);
  structurearray=new structure *[list->size()];
  list->toArray((void **)structurearray);
  numstarray=list->size();

  ifs->close();
  delete(list);
  delete(ifs);
}

void model::parseabstractfile(char *abstractfile) {
  ifstream *ifs=new ifstream();
  ifs->open(abstractfile);
  Reader *r=new Reader(ifs);
  AParser *p=new AParser(r);
  List *list=new List();
  int countstatic=0;
  do {
    Rule *ru=p->parserule();
    if (ru!=NULL) {
      list->addobject(ru);
      if(ru->isstatic())
	countstatic++;
#ifdef DEBUGMESSAGES
      ru->print();
#endif
    }
    else
      break;
  } while(true);
  rulearray=new Rule *[list->size()];
  list->toArray((void **)rulearray);
  numrules=list->size();
  numrulenormal=countstatic;
  rulenormal=new NormalForm *[countstatic];
  int count=0;
  for(int i=0;i<numrules;i++) {
    if(rulearray[i]->isstatic()) {
      rulenormal[count++]=new NormalForm(rulearray[i]);
    }
  }
  ifs->close();
  delete(ifs);
  delete(list);
}

void model::parseconcretefile(char *abstractfile) {
  ifstream *ifs=new ifstream();
  ifs->open(abstractfile);
  Reader *r=new Reader(ifs);
  CParser *p=new CParser(r);
  List *list=new List();
  do {
    Rule *ru=p->parserule();
    if (ru!=NULL) {
      list->addobject(ru);
#ifdef DEBUGMESSAGES
      ru->print();
#endif
    }
    else
      break;
  } while(true);
  concretearray=new Rule *[list->size()];
  list->toArray((void **)concretearray);
  numconcrete=list->size();
  
  ifs->close();
  delete(ifs);
  delete(list);
}

void model::parsemodelfile(char *modelfile) {
  ifstream* ifs=new ifstream();
  ifs->open(modelfile);
  Reader *r=new Reader(ifs);
  Parser *p=new Parser(r);
  List *list=new List();
  do {
    Constraint *c=p->parseconstraint();
    if (c!=NULL) {
      list->addobject(c);
#ifdef DEBUGMESSAGES
      c->print();
#endif
    } else
      break;
  } while(true);
  
  constraintarray=new Constraint *[list->size()];
  list->toArray((void **)constraintarray);
  numconstraint=list->size();
  constraintnormal=new NormalForm *[list->size()];
  for(int i=0;i<list->size();i++)
    constraintnormal[i]=new NormalForm(constraintarray[i]);
  
  ifs->close();
  delete(ifs);
  delete(list);
}

structure * model::getstructure(char *name) {
  for(int i=0;i<numstarray;i++) {
    if (equivalentstrings(name,structurearray[i]->getname())) {
      /* got match */
      return structurearray[i];
    }
  }
  return NULL;
}

FieldCheck * model::getfieldcheck() {
  return fc;
}

int model::getnumrulenormal() {
  return numrulenormal;
}

NormalForm * model::getrulenormal(int i) {
  return rulenormal[i];
}

bool model::subtypeof(structure *sub,structure *super) {
  while(sub!=NULL) {
    if (sub==super)
      return true;
    if (sub->getsubtype()==NULL)
      return false;
    sub=getstructure(sub->getsubtype()->getname());
  }
  printf("Error in subtypeof\n");
  return false; /* Should get here*/
}
