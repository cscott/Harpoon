// Defines the Internal Constraint Language


#ifndef ObjectModel_H
#define ObjectModel_H
#include<unistd.h>
#include<stdio.h>
#include "classlist.h"


#define LITERAL_NUMBER 1
#define LITERAL_TOKEN 2
#define LITERAL_BOOL 3


class Literal {
 public:
  Literal(char *s);
  bool getbool();
  int gettype();
  int number();
  char * token();
  void print();

 private:
  char *str;
};




class Setlabel {
 public:
  Setlabel(char *s);
  char * getname();
  void print();
 private:
  char *str;
};





#define SET_label 0
#define SET_literal 1

class Set {
 public:
  Set(Setlabel *sl);
  Set(Literal **l, int nl);
  ~Set();
  void print();
  int gettype();
  char * getname();
  int getnumliterals();
  Literal * getliteral(int i);
 private:
  int type;
  Setlabel *setlabel;
  int numliterals;
  Literal **literals;
};





class Label {
 public:
  Label(char *s);
  void print();
  char* label() {
    return str;
  }

 private:
  char *str;
};





class Relation {
 public:
  Relation(char * r);
  void print();  
  char * getname();

 private:
  char *str;
};





class Quantifier {
 public:
  Quantifier(Label *l, Set *s);
  void print();
  Label * getlabel();
  Set * getset();

 private:
  Label *label;
  Set *set;
};






#define SETEXPR_LABEL 1
#define SETEXPR_REL 2
#define SETEXPR_INVREL 3

class Setexpr {
 public:
  Setexpr(Setlabel *sl);
  Setexpr(Label *l, bool invert, Relation *r);
  void print();
  Setlabel * getsetlabel();
  Relation * getrelation();
  Label * getlabel();
  int gettype();

 private:
  int type;
  Setlabel *setlabel;
  Label *label;
  Relation *relation;
};






class Valueexpr {
 public:
  Valueexpr(Label *l,Relation *r);
  void print();
  void print_value(model *m);

  Label * getlabel();
  Relation * getrelation();
  
 private:
  Label *label;
  Relation *relation;
};






#define ELEMENTEXPR_LABEL 1
#define ELEMENTEXPR_SUB 2
#define ELEMENTEXPR_ADD 3
#define ELEMENTEXPR_MULT 4
#define ELEMENTEXPR_LIT 5
#define ELEMENTEXPR_SETSIZE 6
#define ELEMENTEXPR_RELATION 11

class Elementexpr {
 public:
  Elementexpr(Setexpr *se);
  Elementexpr(Elementexpr *l, Elementexpr *r, int op);
  Elementexpr(Literal *lit);
  Elementexpr(Label *lab);
  Elementexpr(Elementexpr *l,Relation *r);
  void print();
  int gettype();
  Label * getlabel();
  Elementexpr *getleft();
  Elementexpr *getright();
  Literal * getliteral();
  Setexpr * getsetexpr();
  Relation * getrelation();

 private:
  int type;
  Relation *relation;
  Elementexpr *left, *right;
  Label *label;
  Literal *literal;
  Setexpr *setexpr;
};





#define PREDICATE_LT 1
#define PREDICATE_LTE 2
#define PREDICATE_EQUALS 3
#define PREDICATE_GTE 4
#define PREDICATE_GT 5
#define PREDICATE_SET 6
#define PREDICATE_EQ1 7
#define PREDICATE_GTE1 8

class Predicate {
 public:
  Predicate(Label *l,Setexpr *se);
  Predicate(Valueexpr *ve, int t, Elementexpr *ee);
  Predicate(bool greaterthan, Setexpr *se);
  void print();
  void print_sets(model *m);
  int gettype();
  Valueexpr * getvalueexpr();
  Elementexpr * geteleexpr();
  Label * getlabel();
  Setexpr * getsetexpr();
 private:
  int type;
  Valueexpr *valueexpr;
  Elementexpr *elementexpr;
  Label *label;
  Setexpr *setexpr;
};






#define STATEMENT_OR 1
#define STATEMENT_AND 2
#define STATEMENT_NOT 3
#define STATEMENT_PRED 4

class Statement {
 public:
  Statement(Statement *l, Statement *r, int t);
  Statement(Statement *l);
  Statement(Predicate *p);
  void print();
  void print_sets(model *m); // prints the sets and the relations involved in the statement
  int gettype();
  Statement *getleft();
  Statement *getright();
  Predicate *getpredicate();
 private:
  int type;
  Statement *left,*right;
  Predicate *pred;
};






class Constraint {
 public:
  Constraint();
  Constraint(Quantifier **q, int nq);
  void setstatement(Statement *s);
  void print();
  int numquants();
  Quantifier * getquant(int i);
  Statement * getstatement();
  void setcrash(bool c);
  bool getcrash();
 private:
  bool crash;
  int numquantifiers;
  Quantifier **quantifiers;
  Statement *statement;
};


#endif
