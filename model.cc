#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <fstream.h>
#include <iostream.h>
#include "model.h"
#include "token.h"
#include "typeparser.h"
#include "dparser.h"
#include "aparser.h"
#include "cparser.h"
#include "oparser.h"
#include "rparser.h"
#include "dmodel.h"
#include "omodel.h"
#include "amodel.h"
#include "tmodel.h"
#include "list.h"
#include "processabstract.h"
#include "processobject.h"
#include "processconcrete.h"
#include "normalizer.h"
#include "Hashtable.h"
#include "element.h"
#include "bitreader.h"
#include "DefaultGuidance2.h"
#include "DefaultGuidance.h"
#include "DefaultGuidance3.h"
#include "repair.h"
#include "fieldcheck.h"
#include "tmap.h"
#include "string.h"
#include "set.h"


Hashtable * model::gethashtable() {
  return env;
}

bitreader * model::getbitreader() {
  return br;
}

Guidance * model::getguidance() {
  return guidance;
}

model::model(char *abstractfile, char *modelfile, char *spacefile, char *structfile, char *concretefile, char *rangefile) 
{
  parsestructfile(structfile);

  //Hashtable first for lookups
  env=new Hashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);
  //  env->put(&bstring,new Element(&badstruct,getstructure("arrayofstructs")));//should be of badstruct
  parseabstractfile(abstractfile);
  parsemodelfile(modelfile);
  parsespacefile(spacefile);
  parseconcretefile(concretefile);
  //  parserangefile(rangefile);
  // committing out because of memory problems

  br=new bitreader(this,env);
  guidance=new DefGuidance2(this);  // for the file system benchmark
  repair=new Repair(this);
  if (!repair->analyzetermination()) {
#ifdef DEBUGMESSAGES
    printf("Constraint set might not terminate and can't be repaired!\n");
#endif
    exit(-1);
  }
  fc=new FieldCheck(this);
  fc->buildmaps();
  fc->analyze();
  typmap=new typemap(this);
}

void model::reset() {
  //  typmap->reset(); Don't rebuild trees
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


// processes the model definition rules
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


// processes the external constraints
void model::doconcrete() {
  processconcrete *pr=new processconcrete(this);
  for(int i=0;i<numconcrete;i++) {
    pr->processrule(concretearray[i]);
  }
  delete(pr);
}



// inserts faults that break the specifications
void model::breakspec() 
{
  srandom((unsigned) time(NULL));

  processobject *po = new processobject(this);

  // takes each satisfied constraint and breaks it with probability prob_breakconstraint
  for (int i=0; i<numconstraint; i++)
    {
      Constraint *c = getconstraint(i);
      if (po->issatisfied(c))
	if (random()<prob_breakconstraint*RAND_MAX)
	  po->breakconstraint(c);
    }

  delete(po);
}



// inserts faults that don not break the specifications
void model::inserterrors()
{
  printf("\nmodel::inserterrors CALLED\n\n");

  srandom((unsigned) time(NULL));

  processobject *po = new processobject(this);

  // takes each satisfied constraint and modifies it with probability prob_modifyconstraint
  long int r;
  for (int i=0; i<numconstraint; i++)
    {
      Constraint *c = getconstraint(i);
#ifdef DEBUGMESSAGES
      printf("Constraint: ");
      c->print();
      if (po->issatisfied(c))
	printf("     is satisfied\n");
      else printf("     is not satisfied\n");
      fflush(NULL);
#endif
      if (po->issatisfied(c))
	{
	  r=random();
#ifdef DEBUGMESSAGES
	  printf("r=%ld\n", r);
#endif
	  if (r<prob_modifyconstraint*RAND_MAX)
	    po->modifyconstraint(c);
	}
    }

  delete(po);
}



// processes the internal constraints
// returns true only if no violated constraints were found
bool model::docheck() 
{
  bool found=false;
  processobject *po=new processobject(this);
  bool t=false;
  do {
    t=false;
    /* Process rules until we reach a fixpoint*/
    for(int i=0;i<numconstraint;i++) {
      if (!po->processconstraint(constraintarray[i]))	
	{
	  found=true;
	  t=true; //Got to keep running
	}
    }
  } while(t);
  delete(po);
  return found;
}


DomainRelation * model::getdomainrelation() {
  return domainrelation;
}



/* reads the testspace file, which keeps the sets and relations involved;
   these sets and relations are managed by "domainrelation" */
void model::parsespacefile(char *spacefile) {
  ifstream *ifs=new ifstream();
  ifs->open(spacefile);
  Reader *r=new Reader(ifs);
  Dparser *p=new Dparser(r);
  domainrelation=p->parsesetrelation(); 

#ifdef DEBUGMANYMESSAGES  
  domainrelation->print();
#endif

  ifs->close();
  delete(ifs);
}



/* reads the teststruct file, which keeps the structure definitions;
   these definitions are kept in "structurearray" */
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

#ifdef DEBUGMANYMESSAGES
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


/* parses the testabstract file, which contains the model definition rules 
   these rules are kept in "rulearray" */
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
#ifdef DEBUGMANYMESSAGES
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



/* parses the testconcrete file, which contains the external constraints;
   these constraints are kept in "concretearray" */
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
#ifdef DEBUGMANYMESSAGES
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



/* parses the testmodel file, which contains the internal constraints 
   these constraints are kept in constraintarray;  
   the constraints in normal form are kept in constraintnormal */
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
#ifdef DEBUGMANYMESSAGES
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



/* reads the testrange file, which keeps the ranges of the relations
   of the form "relation: A -> token".  
   This information is used only by the fault injection mechanism.  
   This file should be read only after the testspace file.
*/
void model::parserangefile(char *rangefile) 
{
  ifstream *ifs=new ifstream();
  ifs->open(rangefile);
  Reader *r = new Reader(ifs);
  RParser *rp = new RParser(r);

  do {
    char* relation = rp->parserelation();

    if (relation != NULL)
      {
#ifdef DEBUGMANYMESSAGES
	printf("Reading relation: %s\n", relation);
	fflush(NULL);
#endif

	// find the given relation, whose range should be of type "token"
	DRelation *drel = domainrelation->getrelation(relation);
	if (strcmp(drel->getrange(), "token") != 0)
	  {
	    printf("Error! Range of %s should be of type token.", relation);
	    exit(0);
	  }

	WorkSet *ws = rp->parseworkset();
#ifdef DEBUGMANYMESSAGES
	printf("The range for %s is:\n", relation);
	void *obj = ws->firstelement();
	while (obj)
	  {
	    printf("%s ", (char *) obj);
	    obj = ws->getnextelement(obj);
	  }
	fflush(NULL);
	printf("\n\n");
#endif
	drel->settokenrange(ws);
	delete(relation);
      }
    else 
      break;
  } while(true);

  ifs->close();
  delete(ifs);
  delete(r);
  delete(rp);
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
