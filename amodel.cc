// Defines the Model Definition Language (MDL)

#include <stdio.h>
#include "amodel.h"
#include "omodel.h"


// class Field

Field::Field(char *s) {
  str=s;
}

void Field::print() {
  printf("%s",str);
}

char * Field::field() {
  return str;
}

// class AElementexpr

AElementexpr::AElementexpr(AElementexpr *l, AElementexpr *r, int op) {
  left=l;right=r;type=op;
}

AElementexpr::AElementexpr(char *ctype, AElementexpr *l) {
  type=AELEMENTEXPR_CAST;
  left=l;
  casttype=ctype;
}

AElementexpr::AElementexpr(Literal *lit) {
  literal=lit;
  type=AELEMENTEXPR_LIT;
}

AElementexpr::AElementexpr(Label *lab) {
  label=lab;
  type=AELEMENTEXPR_LABEL;
}
 
AElementexpr::AElementexpr(AElementexpr *l,Field *f) {
  field=f;
  left=l;
  type=AELEMENTEXPR_FIELD;
}

AElementexpr::AElementexpr() {
  type=AELEMENTEXPR_NULL;
}
 
AElementexpr::AElementexpr(AElementexpr *l,Field *f, AElementexpr * i) {
  field=f;
  left=l;
  right=i;
  type=AELEMENTEXPR_FIELDARRAY;
}
 
AElementexpr * AElementexpr::getleft() {
  return left;
}

char * AElementexpr::getcasttype() {
  return casttype;
}

AElementexpr * AElementexpr::getright() {
  return right;
}

int AElementexpr::gettype() {
  return type;
}

Label * AElementexpr::getlabel() {
  return label;
}

Literal * AElementexpr::getliteral() {
  return literal;
}

Field * AElementexpr::getfield() {
  return field;
}

void AElementexpr::print() {
  switch(type) {
  case AELEMENTEXPR_LABEL:
    label->print();
    break;
  case AELEMENTEXPR_SUB:
    left->print();
    printf("-");
    right->print();
    break;
  case AELEMENTEXPR_ADD:
    left->print();
    printf("+");
    right->print();
    break;
  case AELEMENTEXPR_MULT:
    left->print();
    printf("*");
    right->print();
    break;
  case AELEMENTEXPR_DIV:
    left->print();
    printf("/");
    right->print();
    break;
  case AELEMENTEXPR_LIT:
    literal->print();
    break;
  case AELEMENTEXPR_FIELD:
    left->print();
    printf(".");
    field->print();
    break;
  case AELEMENTEXPR_FIELDARRAY:
    left->print();
    printf(".");
    field->print();
    printf("[");
    right->print();
    printf("]");
    break;
  case AELEMENTEXPR_CAST:
    printf("cast(%s,",casttype);
    left->print();
    printf(")");
    break;
  case AELEMENTEXPR_NULL:
    printf("NULL");
    break;
  }
}

// class Type

Type::Type(char *s, int n, Label** l) {
  str=s;numlabels=n;labels=l;
}

void Type::print() {
  printf("(%s(",str);
  for(int i=0;i>numlabels;i++) {
    labels[i]->print();
  }
  printf("))");
}

int Type::getnumlabels() {
  return numlabels;
}

Label * Type::getlabel(int i) {
  return labels[i];
}

// class TypeEle

TypeEle::TypeEle(char *s, int n, AElementexpr** e) {
  str=s;numexpr=n;exprs=e;
}

void TypeEle::print() {
  printf("(%s(",str);
  for(int i=0;i<numexpr;i++) {
    exprs[i]->print();
  }
  printf("))");
}

int TypeEle::getnumexpr() {
  return numexpr;
}

AElementexpr * TypeEle::getexpr(int i) {
  return exprs[i];
}

// class AQuantifier

AQuantifier::AQuantifier(Label *l,Type *t, Set *s) {
  left=l;
  tleft=t;
  set=s;
  type=AQUANTIFIER_SING;
}

AQuantifier::AQuantifier(Label *l,Type *tl, Label *r, Type *tr, Set *s) {
  left=l;
  tleft=tl;
  right=r;
  tright=tr;
  set=s;
  type=AQUANTIFIER_TUPLE;
}

AQuantifier::AQuantifier(Label *l,AElementexpr *e1, AElementexpr *e2) {
  left=l;
  lower=e1;upper=e2;
  type=AQUANTIFIER_RANGE;
}
 
Label * AQuantifier::getleft() {
  return left;
}
 
Type * AQuantifier::gettleft() {
  return tleft;
}

Type * AQuantifier::gettright() {
  return tright;
}
 
Label * AQuantifier::getright() {
  return right;
}
 
Set * AQuantifier::getset() {
  return set;
}

AElementexpr * AQuantifier::getlower() {
  return lower;
}

AElementexpr * AQuantifier::getupper() {
  return upper;
}

int AQuantifier::gettype() {
  return type;
}

void AQuantifier::print() {
  switch(type) {
  case AQUANTIFIER_SING:
    printf("forall ");
    if (tleft!=NULL)
      tleft->print();
    left->print();
    printf(" in ");
    set->print();
    break;
  case AQUANTIFIER_TUPLE:
    printf("forall <");
    if (tleft!=NULL)
      tleft->print();
    left->print();
    if (tright!=NULL)
      tright->print();
    right->print();
    printf("> in ");
    set->print();
    break;
  case AQUANTIFIER_RANGE:
    printf("for ");
    left->print();
    printf("=");
    lower->print();
    printf(" to ");
    upper->print();
    break;
  }
}

// class Statementa

Statementa::Statementa(AElementexpr *l, char *vt) {
  leftee=l;
  validtype=vt;
  type=STATEMENTA_VALID;
}

char * Statementa::getvalidtype() {
  return validtype;
}

Statementa::Statementa(AElementexpr *l, Set *s) {
  leftee=l;
  set=s;
  type=STATEMENTA_SET;
}

Statementa::Statementa(Statementa *l, Statementa *r, int t) {
  left=l;
  right=r;
  type=t;
}

int Statementa::gettype() {
  return type;
}

Statementa * Statementa::getleft() {
  return left;
}

Statementa * Statementa::getright() {
  return right;
}

AElementexpr * Statementa::getleftee() {
  return leftee;
}

AElementexpr * Statementa::getrightee() {
  return rightee;
}

Statementa::Statementa(Statementa *l) {
  type=STATEMENTA_NOT;
  left=l;
}

Statementa::Statementa() {
  type=STATEMENTA_TRUE;
}

Statementa::Statementa(AElementexpr *l, AElementexpr *r, int t) {
  leftee=l;
  rightee=r;
  type=t;
}

Set * Statementa::getset() {
  return set;
}

void Statementa::print() {
  switch(type) {
  case STATEMENTA_SET:
    leftee->print();
    printf(" in ");
    set->print();
    break;
  case STATEMENTA_OR:
    left->print();
    printf(" OR ");
    right->print();
    break;
  case STATEMENTA_AND:
    left->print();
    printf(" AND ");
    right->print();
    break;
  case STATEMENTA_NOT:
    printf("!");
    left->print();
    break;
  case STATEMENTA_EQUALS:
    leftee->print();
    printf("=");
    rightee->print();
    break;
  case STATEMENTA_LT:
    leftee->print();
    printf("<");
    rightee->print();
    break;
  case STATEMENTA_TRUE:
    printf("true");
    break;
  }
}

// class Statementb

TypeEle * Statementb::gettleft() {
  return tleft;
}

TypeEle * Statementb::gettright() {
  return tright;
}

AElementexpr * Statementb::getleft() {
  return left;
}

AElementexpr * Statementb::getright() {
  return right;
}

Setlabel * Statementb::getsetlabel() {
  return setlabel;
}

Statementb::Statementb(TypeEle *tl,AElementexpr *l, Setlabel *sl) {
  left=l;setlabel=sl;tleft=tl;
  type=STATEMENTB_SING;
}

int Statementb::gettype() {
  return type;
}

Statementb::Statementb(TypeEle *tl,AElementexpr *l, TypeEle *tr,AElementexpr *r, Setlabel *sl) {
  left=l;right=r;
  tleft=tl;tright=tr;
  setlabel=sl;
  type=STATEMENTB_TUPLE;
}

void Statementb::print() {
  switch(type) {
  case STATEMENTB_SING:
    left->print();
    printf(" in ");
    setlabel->print();
    break;
  case STATEMENTB_TUPLE:
    printf("<");
    left->print();
    printf(",");
    right->print();
    printf("> in ");
    setlabel->print();
    break;
  }
}

// class Rule

Rule::Rule() {
  quantifiers=NULL;
  numquantifiers=0;
  statementa=NULL;
  statementb=NULL;
  delay=false;
  staticrule=false;
}

Rule::Rule(AQuantifier **q, int nq) {
  quantifiers=q;
  numquantifiers=nq;
  statementa=NULL;
  statementb=NULL;
  delay=false;
  staticrule=false;
}

void Rule::setdelay() {
  delay=true;
}

bool Rule::isdelayed() {
  return delay;
}

bool Rule::isstatic() {
  return staticrule;
}
void Rule::setstatic() {
  staticrule=true;
}

void Rule::setstatementa(Statementa *sa) {
  statementa=sa;
}
 
void Rule::setstatementb(Statementb *sb) {
  statementb=sb;
}

Statementa * Rule::getstatementa() {
  return statementa;
}
 
Statementb * Rule::getstatementb() {
  return statementb;
}
 
int Rule::numquants() {
  return numquantifiers;
}

AQuantifier* Rule::getquant(int i) {
  return quantifiers[i];
}

void Rule::print() {
  printf("[");
  for(int i=0;i<numquantifiers;i++) {
    if (i!=0)
      printf(",");
    quantifiers[i]->print();
  }
  printf("],");
  statementa->print();
  printf(" => ");
  statementb->print();
  printf("\n");
}
