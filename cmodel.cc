#include "cmodel.h"
#include "amodel.h"
#include "omodel.h"
#include <stdio.h>

CAElementexpr::CAElementexpr(CAElementexpr *index, Setexpr *se) {
  left=index;right=NULL;type=CAELEMENTEXPR_ELEMENT;
  setexpr=se;
}


CAElementexpr::CAElementexpr(CAElementexpr *l, CAElementexpr *r, int op) {
  left=l;right=r;type=op;
}

CAElementexpr::CAElementexpr(Literal *lit) {
  literal=lit;
  type=CAELEMENTEXPR_LIT;
}

CAElementexpr::CAElementexpr(Label *lab) {
  label=lab;
  type=CAELEMENTEXPR_LABEL;
}
CAElementexpr::CAElementexpr() {
  type=CAELEMENTEXPR_NULL;
}

CAElementexpr * CAElementexpr::getleft() {
  return (CAElementexpr *) left;
}

CAElementexpr * CAElementexpr::getright() {
  return (CAElementexpr *) right;
}

Relation * CAElementexpr::getrelation() {
  return rrelation;
}

CAElementexpr::CAElementexpr(CAElementexpr *index, Relation *r) {
  left=index;right=NULL;type=CAELEMENTEXPR_RELATION;
  rrelation=r;
}

CAElementexpr::CAElementexpr(Setexpr *se) {
  setexpr=se;
  type=CAELEMENTEXPR_SIZEOF;
}


void CAElementexpr::print() {
  switch(type) {
  case CAELEMENTEXPR_LABEL:
    label->print();
    break;
  case CAELEMENTEXPR_NULL:
    printf("NULL");
    break;
  case CAELEMENTEXPR_RELATION:
    left->print();
    printf(".");
    rrelation->print();
    break;
  case CAELEMENTEXPR_SUB:
    left->print();
    printf("-");
    right->print();
    break;
  case CAELEMENTEXPR_ADD:
    left->print();
    printf("+");
    right->print();
    break;
  case CAELEMENTEXPR_MULT:
    left->print();
    printf("*");
    right->print();
    break;
  case CAELEMENTEXPR_DIV:
    left->print();
    printf("/");
    right->print();
    break;
  case CAELEMENTEXPR_LIT:
    literal->print();
    break;
  case CAELEMENTEXPR_SIZEOF:
    printf("sizeof(");
    setexpr->print();
    printf(")");
    break;
  case CAELEMENTEXPR_ELEMENT:
    printf("element ");
    left->print();
    printf(" of ");
    setexpr->print();
    break;
  }
}

Setexpr * CAElementexpr::getsetexpr() {
  return setexpr;
}

Expr::Expr(Label *l) {
  label=l;
  type=EXPR_LABEL;
}

Expr::Expr(Expr *e, Field *f) {
  field=f;expr=e; type=EXPR_FIELD;
}

Expr::Expr(char *ctype, Expr *e) {
  expr=e;
  casttype=ctype;
  type=EXPR_CAST;
}

char * Expr::getcasttype() {
  return casttype;
}

Expr::Expr(Expr *e, Field *f, CAElementexpr *cae) {
  field=f;expr=e;index=cae;
  type=EXPR_ARRAY;
}

Expr * Expr::getexpr() {
  return expr;
}

int Expr::gettype() {
  return type;
}

Field * Expr::getfield() {
  return field;
}

Label * Expr::getlabel() {
  return label;
}

CAElementexpr * Expr::getindex() {
  return index;
}

void Expr::print() {
  switch(type) {
  case EXPR_LABEL:
    label->print();
    break;
  case EXPR_FIELD:
    expr->print();
    printf(".");
    field->print();
    break;
  case EXPR_CAST:
    printf("cast(%s,",casttype);
    expr->print();
    printf(")");
    break;
  case EXPR_ARRAY:
    expr->print();
    printf(".");
    field->print();
    printf("[");
    index->print();
    printf("]");
    break;
  }
}

void CStatementb::print() {
  switch(type) {
  case CSTATEMENTB_ARRAYASSIGN:
    expr->print();
    printf(".");
    field->print();
    printf("[");
    left->print();
    printf("]=");
    right->print();
    break;
  case CSTATEMENTB_FIELDASSIGN:
    expr->print();
    printf(".");
    field->print();
    printf("=");
    right->print();
    break;
  }
}

CStatementb::CStatementb(Expr *l, Field *f, CAElementexpr *rvalue) {
  expr=l;
  field=f;
  right=rvalue;
  type=CSTATEMENTB_FIELDASSIGN;
}

CStatementb::CStatementb(Expr *l, Field *f, CAElementexpr *index, CAElementexpr *rvalue) {
  expr=l;
  field=f;
  left=index;
  right=rvalue;
  type=CSTATEMENTB_ARRAYASSIGN;
}

Expr * CStatementb::getexpr() {
  return expr;
}

Field * CStatementb::getfield() {
  return field;
}
