#ifndef Cmodel_H
#define Cmodel_H
#include<unistd.h>
#include "classlist.h"
#include "amodel.h"

#define CAELEMENTEXPR_LABEL 1
#define CAELEMENTEXPR_SUB 2
#define CAELEMENTEXPR_ADD 3
#define CAELEMENTEXPR_MULT 4
#define CAELEMENTEXPR_LIT 5
#define CAELEMENTEXPR_NULL 8
#define CAELEMENTEXPR_SIZEOF 9
#define CAELEMENTEXPR_ELEMENT 10
#define CAELEMENTEXPR_RELATION 11
#define CAELEMENTEXPR_DIV 12
class CAElementexpr:public AElementexpr {
 public:
  CAElementexpr(CAElementexpr *index, Setexpr *se);
  CAElementexpr(Setexpr *se);
  CAElementexpr(CAElementexpr *,Relation *r);  
  CAElementexpr(CAElementexpr *l, CAElementexpr *r, int op);
  CAElementexpr(Literal *lit);
  CAElementexpr(Label *lab);
  CAElementexpr();
  CAElementexpr * getleft();
  CAElementexpr * getright();
  Setexpr * getsetexpr();
  void print();
  Relation * getrelation();
 private:
  Relation *rrelation;
  Setexpr *setexpr;
};

#define EXPR_LABEL 1
#define EXPR_FIELD 2
#define EXPR_ARRAY 3
#define EXPR_CAST 4

class Expr {
 public:
  Expr(Label *l);
  Expr(Expr *, Field *);
  Expr(Expr *, Field *, CAElementexpr *);
  Expr(char *, Expr *);
  int gettype();
  Expr * getexpr();
  Field * getfield();
  Label * getlabel();
  CAElementexpr * getindex();
  void print();
  char * getcasttype();

 private:
  int type;
  CAElementexpr *index;
  Expr *expr;
  Field *field;
  Label *label;
  char *casttype;
};

#define CSTATEMENTB_FIELDASSIGN 3
#define CSTATEMENTB_ARRAYASSIGN 4

class CStatementb:public Statementb {
 public:
  void print();
  CStatementb(Expr *l, Field *f, CAElementexpr *rvalue);
  CStatementb(Expr *l, Field *f, CAElementexpr *index, CAElementexpr *rvalue);
  Expr * getexpr();
  Field * getfield();
 protected:
  Expr *expr;
  Field *field;
};
#endif
