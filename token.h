#ifndef Token_H
#define Token_H

#include "common.h"
#include <iostream.h>
#include <stdio.h>
#include "classlist.h"

class Token {
 public:
  Token(char *s, int tt);
  Token(const Token & t);
   Token & operator=(const Token &right);
  Token();
  ~Token();
  int token_type;
  char* str;

 private:
};
void tokenname(int t);

#define TOKEN_OPENBRACK 1
#define TOKEN_CLOSEBRACK 2
#define TOKEN_FORALL 3
#define TOKEN_IN 4
#define TOKEN_OPENBRACE 5
#define TOKEN_CLOSEBRACE 6
#define TOKEN_COMMA 7
#define TOKEN_SIZEOF 8
#define TOKEN_OPENPAREN 9
#define TOKEN_CLOSEPAREN 10
#define TOKEN_LT 11
#define TOKEN_LTE 12
#define TOKEN_EQUALS 13
#define TOKEN_GTE 14
#define TOKEN_GT 15
#define TOKEN_ONE 16
#define TOKEN_DOT 17
#define TOKEN_DOTINV 18
#define TOKEN_NOT 19
#define TOKEN_LITERAL 20
#define TOKEN_PARAM 21
#define TOKEN_SUB 22
#define TOKEN_ADD 23
#define TOKEN_MULT 24
#define TOKEN_AND 25
#define TOKEN_OR 26
#define TOKEN_EOL 27
#define TOKEN_EOF 28
#define TOKEN_IMPLIES 29
#define TOKEN_TRUE 30
#define TOKEN_ISVALID 31
#define TOKEN_FOR 32
#define TOKEN_TO 33
#define TOKEN_STRUCTURE 34
#define TOKEN_RESERVED 35
#define TOKEN_LABEL 36
#define TOKEN_INT 37
#define TOKEN_BIT 38
#define TOKEN_BYTE 39
#define TOKEN_SUBTYPE 40
#define TOKEN_OF 41
#define TOKEN_SEMI 42
#define TOKEN_COLON 43
#define TOKEN_SET 44
#define TOKEN_ARROW 45
#define TOKEN_MANY 46
#define TOKEN_BAR 47
#define TOKEN_PARTITION 48
#define TOKEN_ELEMENT 49
#define TOKEN_DELAY 50
#define TOKEN_STATIC 51
#define TOKEN_DIV 52
#define TOKEN_CAST 53
#define TOKEN_SHORT 54
#define TOKEN_NULL 55



class Reader{
 public:
  Reader(istream * is);
  Token readnext();
  Token peakahead();
  void error();

 private:
  bool breakchar(int);
  Token checktoken();
  istream *readerin;
  char buf[200];
  int pos;
};

#endif
