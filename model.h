#ifndef MODEL_H
#define MODEL_H
#include <fstream.h>
#include <iostream.h>
#include "classlist.h"

class model {
 public:
  model(char * abstractfile, char * modelfile, char *spacefile,char *structfile,char * concretefile);
  DomainRelation * getdomainrelation();
  structure *getstructure(char * name);
  void doabstraction();
  void doconcrete();
  void docheck();
  NormalForm * getnormalform(Constraint *c);
  Hashtable * gethashtable();
  bitreader * getbitreader();
  Guidance * getguidance();
  int getnumconstraints();
  Constraint * getconstraint(int i);
  NormalForm * getnormalform(int i);
  Repair *getrepair();
  FieldCheck * getfieldcheck();
  void triggerrule(Element *,char *);
  int getnumrulenormal();
  NormalForm * getrulenormal(int i);
  bool subtypeof(structure *sub,structure *super);
  typemap * gettypemap();
  void reset();
 private:
  void parsespacefile(char *spacefile);
  void parsestructfile(char *structfile);
  void parseabstractfile(char *abstractfile);
  void parseconcretefile(char *concretefile);
  void parsemodelfile(char *modelfile);
  Hashtable *env;
  bitreader *br;
  Guidance * guidance;
  Repair *repair;
  FieldCheck *fc;
  processabstract *pa;
  DomainRelation *domainrelation;

  Constraint **constraintarray;
  int numconstraint;

  NormalForm **constraintnormal;

  structure **structurearray;
  int numstarray;

  Rule **concretearray;
  int numconcrete;

  Rule **rulearray;
  int numrules;
  NormalForm **rulenormal;
  int numrulenormal;

  typemap * typmap;
};
#endif
