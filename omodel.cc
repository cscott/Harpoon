// Defines the Internal Constraint Language

#include <stdio.h>
#include "omodel.h"
#include "common.h"
#include "model.h"
#include "Hashtable.h"
#include "dmodel.h"
#include "element.h"
#include "set.h"
#include "Relation.h"


// class Literal

Literal::Literal(char *s) {
  str=s;
}


// there are three types of literals: numbers, bools, and tokens
int Literal::gettype() {
  int val;
  if (sscanf(str,"%d",&val)==1)
    return LITERAL_NUMBER;
  else {
    if (equivalentstrings(str,"true")||
	equivalentstrings(str,"false"))
      return LITERAL_BOOL;
    return LITERAL_TOKEN;
  }
}

bool Literal::getbool() {
  if (equivalentstrings(str,"true"))
    return true;
  else
    return false;
}

int Literal::number() {
  int val;
  sscanf(str,"%d",&val);
  return val;
}

char * Literal::token() {
  return str;
}

void Literal::print() {
  printf("%s",str);
}

void Literal::fprint(FILE *f) {
  fprintf(f,"%s",str);
}






// class Setlabel

Setlabel::Setlabel(char *s) {
  str=s;
}

char * Setlabel::getname() {
  return str;
}
  
void Setlabel::print() {
  printf("%s",str);
}

void Setlabel::fprint(FILE *f) {
  fprintf(f,"%s",str);
}





// class Set

// a set is either a label or a set of literals

Set::Set(Setlabel *sl) {
  type=SET_label;
  setlabel=sl;
}

int Set::getnumliterals() {
  return numliterals;
}

Literal * Set::getliteral(int i) {
  return literals[i];
}

Set::Set(Literal **l, int nl) {
  type=SET_literal;
  literals=l;
  numliterals=nl;
}

Set::~Set() {
  if (type==SET_label)
    delete(setlabel);
  if (type==SET_literal) {
    for(int i=0;i<numliterals;i++)
      delete(literals[i]);
    delete(literals);
  }
}

int Set::gettype() {
  return type;
}

char * Set::getname() {
  return setlabel->getname();
}

void Set::print() {
  switch(type) {
  case SET_label:
    setlabel->print();
    break;
  case SET_literal:
    printf("{");
    for(int i=0;i<numliterals;i++) {
      if (i!=0)
	printf(",");
      literals[i]->print();
    }
    printf("}");
    break;
  }
}

void Set::fprint(FILE *f) {
  switch(type) {
  case SET_label:
    setlabel->fprint(f);
    break;
  case SET_literal:
    fprintf(f,"{");
    for(int i=0;i<numliterals;i++) {
      if (i!=0)
	fprintf(f,",");
      literals[i]->fprint(f);
    }
    fprintf(f,"}");
    break;
  }
}






// class Label

Label::Label(char *s) {
  str=s;
}

void Label::print() {
  printf("%s",str);
}

void Label::fprint(FILE *f) {
  fprintf(f,"%s",str);
}





// class Relation

Relation::Relation(char * r) {
  str=r;
}

char * Relation::getname() {
  return str;
}

void Relation::print() {
  printf("%s",str);
}

void Relation::fprint(FILE *f) {
  fprintf(f,"%s",str);
}





// class Quantifier

Quantifier::Quantifier(Label *l, Set *s) {
  label=l;
  set=s;
}

Label * Quantifier::getlabel() {
  return label;
}

Set * Quantifier::getset() {
  return set;
}

void Quantifier::print() {
  printf("forall ");
  label->print();
  printf(" in ");
  set->print();
}

void Quantifier::fprint(FILE *f) {
  fprintf(f,"forall ");
  label->fprint(f);
  fprintf(f," in ");
  set->fprint(f);
}






// class Setexpr

Setexpr::Setexpr(Setlabel *sl) {
  setlabel=sl;
  type=SETEXPR_LABEL;
}

Setexpr::Setexpr(Label *l, bool invert, Relation *r) {
  label=l;
  type=invert?SETEXPR_INVREL:SETEXPR_REL;
  relation=r;
}

void Setexpr::print() {
  switch(type) {
  case SETEXPR_LABEL:
    setlabel->print();
    break;
  case SETEXPR_REL:
    label->print();
    printf(".");
    relation->print();
    break;
  case SETEXPR_INVREL:
    label->print();
    printf(".~");
    relation->print();
    break;
  }
}

void Setexpr::fprint(FILE *f) {
  switch(type) {
  case SETEXPR_LABEL:
    setlabel->fprint(f);
    break;
  case SETEXPR_REL:
    label->fprint(f);
    fprintf(f,".");
    relation->fprint(f);
    break;
  case SETEXPR_INVREL:
    label->fprint(f);
    fprintf(f,".~");
    relation->fprint(f);
    break;
  }
}


void Setexpr::print_size(Hashtable *stateenv, model *m) 
{
  switch(type) {
  case SETEXPR_LABEL:{
    printf("sizeof(");
    setlabel->print();
    printf(")=");  

    DomainRelation *dr = m->getdomainrelation();
    Hashtable *env = m->gethashtable();
    DomainSet *dset = dr->getset(setlabel->getname());
    WorkSet *ws = dset->getset();

    if (ws != NULL)
      printf("%d", ws->size());
    else printf("NULL");
    break;
  }

  case SETEXPR_REL:{
    printf("sizeof(");
    label->print();
    printf(".");
    relation->print();
    printf(")=");  

    Element *key = (Element *) stateenv->get(label->label());

    DomainRelation *dr = m->getdomainrelation();
    Hashtable *env = m->gethashtable();
    DRelation *rel = dr->getrelation(relation->getname());
    WorkRelation *wr = rel->getrelation();
   
    WorkSet *ws = wr->getset(key);

    if (ws != NULL)
      printf("%d", ws->size());
    else printf("NULL);
    break;
  }

  case SETEXPR_INVREL:{
    printf("sizeof(");
    label->print();
    printf(".~");
    relation->print();
    printf(")=");  

    Element *key = (Element *) stateenv->get(label->label());

    DomainRelation *dr = m->getdomainrelation();

    Hashtable *env = m->gethashtable();
    DRelation *rel = dr->getrelation(relation->getname());
    WorkRelation *wr = rel->getrelation();
   
    WorkSet *ws = wr->invgetset(key);

    if (ws != NULL)
      printf("%d", ws->size());
    else printf("NULL");
    break;
  }
  }
}


void Setexpr::print_value(Hashtable *stateenv, model *m) 
{
  switch(type) {
  case SETEXPR_LABEL:{
    //printf("   ");
    setlabel->print();
    printf("=");  

    DomainRelation *dr = m->getdomainrelation();
    Hashtable *env = m->gethashtable();
    DomainSet *dset = dr->getset(setlabel->getname());

    WorkSet *ws = dset->getset();
    ws->print();
    break;
  }

  case SETEXPR_REL:{
    /*
    printf("   sizeof(");
    label->print();
    printf(".");
    relation->print();
    printf(") = ");  

    Element *key = (Element *) stateenv->get(label->label());

    DomainRelation *dr = m->getdomainrelation();
    Hashtable *env = m->gethashtable();
    DRelation *rel = dr->getrelation(relation->getname());
    WorkRelation *wr = rel->getrelation();
   
    WorkSet *ws = wr->getset(key);

    printf("%d\n", ws->size());
    */
    break;
  }

  case SETEXPR_INVREL:{
    /*
    printf("   sizeof(");
    label->print();
    printf(".~");
    relation->print();
    printf(") = ");  

    Element *key = (Element *) stateenv->get(label->label());

    DomainRelation *dr = m->getdomainrelation();

    Hashtable *env = m->gethashtable();
    DRelation *rel = dr->getrelation(relation->getname());
    WorkRelation *wr = rel->getrelation();
   
    WorkSet *ws = wr->invgetset(key);

    printf("%d\n", ws->size());
    */
    break;
  }
  }
}


Setlabel * Setexpr::getsetlabel() {
  return setlabel;
}

Relation * Setexpr::getrelation() {
  return relation;
}

Label * Setexpr::getlabel() {
  return label;
}

int Setexpr::gettype() {
  return type;
}





// class Valueexpr

Label * Valueexpr::getlabel() {
  return label;
}

Relation * Valueexpr::getrelation() {
  return relation;
}

Valueexpr::Valueexpr(Label *l,Relation *r) {
  label=l;relation=r;
}

void Valueexpr::print() {
  label->print();
  printf(".");
  relation->print();
}

void Valueexpr::fprint(FILE *f) {
  label->fprint(f);
  fprintf(f,".");
  relation->fprint(f);
}

void Valueexpr::print_value(Hashtable *stateenv, model *m) {
  printf("  ");
  label->print();
  printf(".");
  relation->print();
  printf("=");

  Element *key = (Element *) stateenv->get(label->label());

  DomainRelation *dr = m->getdomainrelation();
  Hashtable *env = m->gethashtable();
  DRelation *rel = dr->getrelation(relation->getname());
  WorkRelation *wr = rel->getrelation();

  Element *elem = (Element *) wr->getobj(key);

  elem->print();
  //printf("\n");
}







// class Elementexpr

int Elementexpr::gettype() {
  return type;
}

Relation * Elementexpr::getrelation() {
  return relation;
}

Elementexpr::Elementexpr(Elementexpr *l,Relation *r) {
  type=ELEMENTEXPR_RELATION;
  left=l;
  relation=r;
}

Setexpr * Elementexpr::getsetexpr() {
  return setexpr;
}

Elementexpr::Elementexpr(Setexpr *se) {
  setexpr=se;
  type=ELEMENTEXPR_SETSIZE;
}

Label * Elementexpr::getlabel() {
  return label;
}

Elementexpr * Elementexpr::getleft() {
  return left;
}

Elementexpr * Elementexpr::getright() {
  return right;
}

Literal * Elementexpr::getliteral() {
  return literal;
}

Elementexpr::Elementexpr(Elementexpr *l, Elementexpr *r, int op) {
  left=l;right=r;type=op;
}
 
Elementexpr::Elementexpr(Literal *lit) {
  literal=lit;
  type=ELEMENTEXPR_LIT;
}

Elementexpr::Elementexpr(Label *lab) {
  label=lab;
  type=ELEMENTEXPR_LABEL;
}

void Elementexpr::print() {
  switch(type) {
  case ELEMENTEXPR_LABEL:
    label->print();
    break;
  case ELEMENTEXPR_SUB:
    left->print();
    printf("-");
    right->print();
    break;
  case ELEMENTEXPR_ADD:
    left->print();
    printf("+");
    right->print();
    break;
  case ELEMENTEXPR_MULT:
    left->print();
    printf("*");
    right->print();
    break;
  case ELEMENTEXPR_LIT:
    literal->print();
    break;
  case ELEMENTEXPR_SETSIZE:
    printf("sizeof(");
    setexpr->print();
    printf(")");
    break;
  case ELEMENTEXPR_RELATION:
    left->print();
    printf(".");
    relation->print();
    break;
  }  
}

void Elementexpr::fprint(FILE *f) {
  switch(type) {
  case ELEMENTEXPR_LABEL:
    label->fprint(f);
    break;
  case ELEMENTEXPR_SUB:
    left->fprint(f);
    fprintf(f,"-");
    right->fprint(f);
    break;
  case ELEMENTEXPR_ADD:
    left->fprint(f);
    fprintf(f,"+");
    right->fprint(f);
    break;
  case ELEMENTEXPR_MULT:
    left->fprint(f);
    fprintf(f,"*");
    right->fprint(f);
    break;
  case ELEMENTEXPR_LIT:
    literal->fprint(f);
    break;
  case ELEMENTEXPR_SETSIZE:
    fprintf(f,"sizeof(");
    setexpr->fprint(f);
    fprintf(f,")");
    break;
  case ELEMENTEXPR_RELATION:
    left->fprint(f);
    fprintf(f,".");
    relation->fprint(f);
    break;
  }  
}


void Elementexpr::print_value(Hashtable *stateenv, model *m) {
  switch(type) {
  case ELEMENTEXPR_SUB:
  case ELEMENTEXPR_ADD:
  case ELEMENTEXPR_MULT:
    left->print_value(stateenv, m);
    right->print_value(stateenv, m);
    break;
  case ELEMENTEXPR_SETSIZE:
    setexpr->print_size(stateenv, m);
    break;
  case ELEMENTEXPR_RELATION:
    /*
    left->print();
    printf(".");
    relation->print();
    */
    break;
  }  
}



// class Predicate 

Predicate::Predicate(Valueexpr *ve, int t, Elementexpr *ee) {
  valueexpr=ve;
  type=t;
  elementexpr=ee;
}

Predicate::Predicate(Label *l,Setexpr *se) {
  label=l;
  setexpr=se;
  type=PREDICATE_SET;
}

Predicate::Predicate(bool greaterthan, Setexpr *se) {
  if (greaterthan)
    type=PREDICATE_GTE1;
  else
    type=PREDICATE_EQ1;
  setexpr=se;
}

Valueexpr * Predicate::getvalueexpr() {
  return valueexpr;
}

Elementexpr * Predicate::geteleexpr() {
  return elementexpr;
}

Label * Predicate::getlabel() {
  return label;
}

Setexpr * Predicate::getsetexpr() {
  return setexpr;
}

int Predicate::gettype() {
  return type;
}

void Predicate::print() {
  switch(type) {
  case PREDICATE_LT:
    valueexpr->print();
    printf("<");
    elementexpr->print();
    break;
  case PREDICATE_LTE:
    valueexpr->print();
    printf("<=");
    elementexpr->print();
    break;
  case PREDICATE_EQUALS:
    valueexpr->print();
    printf("=");
    elementexpr->print();
    break;
  case PREDICATE_GTE:
    valueexpr->print();
    printf(">=");
    elementexpr->print();
    break;
  case PREDICATE_GT:
    valueexpr->print();
    printf(">");
    elementexpr->print();
    break;
  case PREDICATE_SET:
    label->print();
    printf(" in ");
    setexpr->print();
    break;
  case PREDICATE_EQ1:
  case PREDICATE_GTE1:
    printf("sizeof(");
    setexpr->print();
    if (type==PREDICATE_EQ1)
      printf(")=1");
    if (type==PREDICATE_GTE1)
      printf(")>=1");
    break;
  }
}

void Predicate::fprint(FILE *f) {
  switch(type) {
  case PREDICATE_LT:
    valueexpr->fprint(f);
    fprintf(f,"<");
    elementexpr->fprint(f);
    break;
  case PREDICATE_LTE:
    valueexpr->fprint(f);
    fprintf(f,"<=");
    elementexpr->fprint(f);
    break;
  case PREDICATE_EQUALS:
    valueexpr->fprint(f);
    fprintf(f,"=");
    elementexpr->fprint(f);
    break;
  case PREDICATE_GTE:
    valueexpr->fprint(f);
    fprintf(f,">=");
    elementexpr->fprint(f);
    break;
  case PREDICATE_GT:
    valueexpr->fprint(f);
    fprintf(f,">");
    elementexpr->fprint(f);
    break;
  case PREDICATE_SET:
    label->fprint(f);
    fprintf(f," in ");
    setexpr->fprint(f);
    break;
  case PREDICATE_EQ1:
  case PREDICATE_GTE1:
    fprintf(f,"sizeof(");
    setexpr->fprint(f);
    if (type==PREDICATE_EQ1)
      fprintf(f,")=1");
    if (type==PREDICATE_GTE1)
      fprintf(f,")>=1");
    break;
  }
}



void Predicate::print_sets(Hashtable *stateenv, model *m) {
  switch(type) {
  case PREDICATE_LT:
  case PREDICATE_LTE:
  case PREDICATE_EQUALS:
  case PREDICATE_GTE:
  case PREDICATE_GT:
    valueexpr->print_value(stateenv, m);
    printf("; ");
    elementexpr->print_value(stateenv, m);
    break;
  case PREDICATE_SET:    
    setexpr->print_value(stateenv, m);
    break;
  case PREDICATE_EQ1:
  case PREDICATE_GTE1:
    setexpr->print_size(stateenv, m); 
    break; 
  }
}





// class Statement

Statement::Statement(Statement *l, Statement *r, int t) {
  left=l;
  right=r;
  type=t;
}

Statement::Statement(Statement *l) {
  type=STATEMENT_NOT;
  left=l;
}
 
Statement::Statement(Predicate *p) {
  pred=p;
  type=STATEMENT_PRED;
}

void Statement::print() {
  switch(type) {
  case STATEMENT_OR:
    left->print();
    printf(" OR ");
    right->print();
    break;
  case STATEMENT_AND:
    left->print();
    printf(" AND ");
    right->print();
    break;
  case STATEMENT_NOT:
    printf("!");
    left->print();
    break;
  case STATEMENT_PRED:
    pred->print();
    break;
  }
}

void Statement::fprint(FILE *f) {
  switch(type) {
  case STATEMENT_OR:
    left->fprint(f);
    fprintf(f," OR ");
    right->fprint(f);
    break;
  case STATEMENT_AND:
    left->fprint(f);
    fprintf(f," AND ");
    right->fprint(f);
    break;
  case STATEMENT_NOT:
    fprintf(f,"!");
    left->fprint(f);
    break;
  case STATEMENT_PRED:
    pred->fprint(f);
    break;
  }
}


void Statement::print_sets(Hashtable *stateenv, model *m) {
  switch(type) {
  case STATEMENT_OR:
  case STATEMENT_AND:
    left->print_sets(stateenv, m);
    printf("; ");
    right->print_sets(stateenv, m);
    break;
  case STATEMENT_NOT:
    left->print_sets(stateenv, m);
    break;
  case STATEMENT_PRED:
    pred->print_sets(stateenv, m);
    break;
  }
  //printf("\n");
}


int Statement::gettype() {
  return type;
}

Statement* Statement::getleft() {
  return left;
}

Statement* Statement::getright() {
  return right;
}

Predicate * Statement::getpredicate() {
  return pred;
}






// class Constraint

Constraint::Constraint() {
  quantifiers=NULL;
  numquantifiers=0;
  statement=NULL;
}

void Constraint::setcrash(bool c) {
  crash=c;
}

bool Constraint::getcrash() {
  return crash;
}

Constraint::Constraint(Quantifier **q, int nq) {
  quantifiers=q;
  numquantifiers=nq;
  statement=NULL;
}

void Constraint::setstatement(Statement *s) {
  statement=s;
}

int Constraint::numquants() {
  return numquantifiers;
}

Quantifier * Constraint::getquant(int i) {
  return quantifiers[i];
}

Statement * Constraint::getstatement() {
  return statement;
}

void Constraint::print() {
  printf("[");
  for(int i=0;i<numquantifiers;i++) {
    if (i!=0)
      printf(",");
    quantifiers[i]->print();
  }
  printf("], ");
  if (statement!=NULL) {
    statement->print();
  }
  printf("\n");
}

void Constraint::fprint(FILE *f) {
  printf("[");
  for(int i=0;i<numquantifiers;i++) {
    if (i!=0)
      fprintf(f,",");
    quantifiers[i]->fprint(f);
  }
  printf("],");
  if (statement!=NULL) {
    statement->fprint(f);
  }
}
