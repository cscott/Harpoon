// Defines the Model Definition Language (MDL)

#ifndef Abstract_H
#define Abstract_H
#include<unistd.h>
#include "classlist.h"


class Field {
 public:
  Field(char *s);
  void print();
  char * field();
 private:
  char *str;
};





#define AELEMENTEXPR_LABEL 1
#define AELEMENTEXPR_SUB 2
#define AELEMENTEXPR_ADD 3
#define AELEMENTEXPR_MULT 4
#define AELEMENTEXPR_LIT 5
#define AELEMENTEXPR_FIELD 6
#define AELEMENTEXPR_FIELDARRAY 7
#define AELEMENTEXPR_NULL 8
#define AELEMENTEXPR_DIV 12
#define AELEMENTEXPR_CAST 13

class AElementexpr {
 public:
  AElementexpr(AElementexpr *l, AElementexpr *r, int op);
  AElementexpr(Literal *lit);
  AElementexpr(Label *lab);
  AElementexpr(char *ctype, AElementexpr *l);
  AElementexpr(AElementexpr *l,Field *f);
  AElementexpr();
  AElementexpr(AElementexpr *l,Field *f, AElementexpr * i);
  AElementexpr * getleft();
  AElementexpr * getright();
  int gettype();
  Label * getlabel();
  Literal * getliteral();
  Field * getfield();
  virtual void print();
  char * getcasttype();

 protected:
  int type;
  char *casttype;
  AElementexpr *left, *right;
  Label *label;
  Literal *literal;
  Field *field;
};





class Type {
 public:
  Type(char *s, int n, Label** l);
  void print();
  int getnumlabels();
  Label * getlabel(int i);
    
 private:
  char *str;
  int numlabels;
  Label **labels;
};





class TypeEle {
 public:
  TypeEle(char *s, int n, AElementexpr ** e);
  void print();
  int getnumexpr();
  AElementexpr * getexpr(int i);
 private:
  char *str;
  int numexpr;
  AElementexpr **exprs;
};





#define AQUANTIFIER_SING 1
#define AQUANTIFIER_TUPLE 2
#define AQUANTIFIER_RANGE 3

class AQuantifier {
 public:
  AQuantifier(Label *l,Type *t, Set *s);
  AQuantifier(Label *l,Type *tl, Label *r, Type *tr, Set *s);
  AQuantifier(Label *l,AElementexpr *e1, AElementexpr *e2);
  void print();
  int gettype();
  Label *getleft();
  Type *gettleft();
  Type *gettright();
  Label *getright();
  Set *getset();
  AElementexpr * getlower();
  AElementexpr * getupper();

 private:
  int type;
  AElementexpr *lower, *upper;
  Label *left, *right;
  Type *tleft,*tright;
  Set *set;
};






#define STATEMENTA_OR 1
#define STATEMENTA_AND 2
#define STATEMENTA_NOT 3
#define STATEMENTA_EQUALS 4
#define STATEMENTA_LT 5
#define STATEMENTA_TRUE 6
#define STATEMENTA_SET 7
#define STATEMENTA_VALID 8

class Statementa {
 public:
  Statementa(Statementa *l, Statementa *r, int t);
  Statementa(Statementa *l);
  Statementa();
  Statementa(AElementexpr *l, AElementexpr *r, int t);
  Statementa(AElementexpr *l, Set *s);
  Statementa(AElementexpr *, char *);
  void print();
  int gettype();
  Statementa * getleft();
  Statementa * getright();
  AElementexpr * getleftee();
  AElementexpr * getrightee();
  Set * getset();
  char *getvalidtype();

 private:
  int type;
  Set *set;
  Statementa *left,*right;
  AElementexpr *leftee, *rightee;
  char *validtype;
};






#define STATEMENTB_SING 1
#define STATEMENTB_TUPLE 2

class Statementb {
 public:
  Statementb(TypeEle *tl,AElementexpr *l, Setlabel *sl);
  Statementb(TypeEle *tl,AElementexpr *l, TypeEle *tr,AElementexpr *r, Setlabel *sl);
  Statementb() {}
  virtual void print();
  int gettype();
  AElementexpr *getleft();
  AElementexpr *getright();
  Setlabel *getsetlabel();
  TypeEle * gettleft();
  TypeEle * gettright();

 protected:
  TypeEle *tleft, *tright;
  AElementexpr *left, *right;
  Setlabel *setlabel;
  int type;
};






class Rule {
 public:
  Rule();
  Rule(AQuantifier **q, int nq);
  void setstatementa(Statementa *sa);
  void setstatementb(Statementb *sb);
  void setdelay();
  void print();
  int numquants();
  AQuantifier* getquant(int i);
  Statementa *getstatementa();
  Statementb *getstatementb();
  bool isdelayed();
  bool isstatic();
  void setstatic();

 private:
  bool staticrule;
  bool delay;
  int numquantifiers;
  AQuantifier **quantifiers;
  Statementa *statementa;
  Statementb *statementb;
};


#endif
