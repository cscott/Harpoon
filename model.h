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

  structure **structurearray;     // the structure definitions
  int numstarray;

  DomainRelation *domainrelation; // the sets and relations

  Rule **rulearray;               // the model definition rules 
  int numrules;              
  NormalForm **rulenormal;        // the model definition rules in normal form
  int numrulenormal;
 
  Constraint **constraintarray;   // the internal constraints
  int numconstraint;
  NormalForm **constraintnormal;  // the internal constraints in normal form

  Rule **concretearray;           // the external constraints
  int numconcrete;


  typemap * typmap;
};
#endif
