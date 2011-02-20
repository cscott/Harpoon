#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "common.h"
#include "token.h"


// class Token

Token::Token(char *s, int tt) {
  str=s;
  token_type=tt;
}

Token::Token(const Token & t) {
  token_type=t.token_type;
  str=copystr(t.str);
}

Token::Token() {
  token_type=-1;
  str=NULL;
}


Token& Token::operator=(const Token &right) {
  if (&right != this) {
    token_type=right.token_type;
    if (str!=NULL)
    delete[](str);
    str=copystr(right.str);
  }
  return *this;
}


Token::~Token() {
  if (str!=NULL)
    delete[](str);
}




// class Reader

Reader::Reader(istream * is) {
  readerin=is;
  pos=0;
}


Token Reader::peakahead() {
  Token t=checktoken();
  if (t.token_type!=-1) {
    return t;
  }
  while(true) {
    int nextchar=readerin->get();
    switch(nextchar) {
    case ' ':
      break;
    case '\t':
      break;
    case '/':
      if (readerin->peek()=='*') {
	/*have comment region */
	readerin->get();
	int state=0;
	for(int ch=readerin->get();ch!=EOF&&state!=2;ch=readerin->get()) {
	  switch(ch) {
	  case '*':
	    state=1;
	    break;
	  case '/':
	    if (state==1)
	      state=2;
	    break;
	  default:
	    state=0;
	  }
	}
	if (state!=2) error();
	break;
      }
    default:
      buf[pos++]=nextchar;
      Token t=checktoken();
      if (t.token_type!=-1)
	return t;
    }
  }
}


Token Reader::readnext() {
  Token t=peakahead();  
  pos=0;
  return t;
}

Token Reader::checktoken() {
  buf[pos]=0;
  if (pos==0) return Token();
  switch(buf[0]) {
  case '[':
    return Token(copystr(buf),TOKEN_OPENBRACK);
  case ']':
    return Token(copystr(buf),TOKEN_CLOSEBRACK);
  case '{':
    return Token(copystr(buf),TOKEN_OPENBRACE);
  case '}':
    return Token(copystr(buf),TOKEN_CLOSEBRACE);
  case '(':
    return Token(copystr(buf),TOKEN_OPENPAREN);
  case ')':
    return Token(copystr(buf),TOKEN_CLOSEPAREN);
  case ',':
    return Token(copystr(buf),TOKEN_COMMA);
  case ';':
    return Token(copystr(buf),TOKEN_SEMI);
  case ':':
    return Token(copystr(buf),TOKEN_COLON);
  case '=':
    if (pos==1) {
      if (readerin->peek()!='>')
	return Token(copystr(buf),TOKEN_EQUALS);
    } else
      return Token(copystr(buf),TOKEN_IMPLIES);
    break;
  case '<':
    if (pos==1) {
      if (readerin->peek()!='=')
	return Token(copystr(buf),TOKEN_LT);
    } else 
      return Token(copystr(buf),TOKEN_LTE);
    break;
  case '>':
    if (pos==1) {
      if (readerin->peek()!='=')
	return Token(copystr(buf),TOKEN_GT);
    } else 
      return Token(copystr(buf),TOKEN_GTE);
    break;
  case '.':
    if (pos==1) {
      if (readerin->peek()!='~')
	return Token(copystr(buf),TOKEN_DOT);
    } else 
      return Token(copystr(buf),TOKEN_DOTINV);
    break;
  case '|':
    return Token(copystr(buf),TOKEN_BAR);
  case '!':
    return Token(copystr(buf),TOKEN_NOT);
  case '-':
    if (pos==1) {
      if (readerin->peek()!='>')
	return Token(copystr(buf),TOKEN_SUB);
    } else
      return Token(copystr(buf),TOKEN_ARROW);
    break;
  case '+':
    return Token(copystr(buf),TOKEN_ADD);
  case '*':
    return Token(copystr(buf),TOKEN_MULT);
  case '/':
    return Token(copystr(buf),TOKEN_DIV);
  case '\n':
    return Token(copystr(buf),TOKEN_EOL);
  case EOF:
    return Token(copystr(buf),TOKEN_EOF);
  default:
    if(breakchar(readerin->peek())) {
      /*we've got token*/
      if (strcmp(buf,"in")==0)
	return Token(copystr(buf),TOKEN_IN);
      if (strcmp(buf,"isvalid")==0)
	return Token(copystr(buf),TOKEN_ISVALID);
      if (strcmp(buf,"and")==0)
	return Token(copystr(buf),TOKEN_AND);
      if (strcmp(buf,"or")==0)
	return Token(copystr(buf),TOKEN_OR);
      if (strcmp(buf,"crash")==0)
	return Token(copystr(buf),TOKEN_CRASH);
      if (strcmp(buf,"cast")==0)
	return Token(copystr(buf),TOKEN_CAST);
      if (strcmp(buf,"NULL")==0)
	return Token(copystr(buf),TOKEN_NULL);
      if (strcmp(buf,"partition")==0)
	return Token(copystr(buf),TOKEN_PARTITION);
      if (strcmp(buf,"many")==0)
	return Token(copystr(buf),TOKEN_MANY);
      if (strcmp(buf,"set")==0)
	return Token(copystr(buf),TOKEN_SET);
      if (strcmp(buf,"structure")==0)
	return Token(copystr(buf),TOKEN_STRUCTURE);
      if (strcmp(buf,"reserved")==0)
	return Token(copystr(buf),TOKEN_RESERVED);
      if (strcmp(buf,"label")==0)
	return Token(copystr(buf),TOKEN_LABEL);
      if (strcmp(buf,"int")==0)
	return Token(copystr(buf),TOKEN_INT);
      if (strcmp(buf,"short")==0)
	return Token(copystr(buf),TOKEN_SHORT);
      if (strcmp(buf,"bit")==0)
	return Token(copystr(buf),TOKEN_BIT);
      if (strcmp(buf,"byte")==0)
	return Token(copystr(buf),TOKEN_BYTE);
      if (strcmp(buf,"subtype")==0)
	return Token(copystr(buf),TOKEN_SUBTYPE);
      if (strcmp(buf,"of")==0)
	return Token(copystr(buf),TOKEN_OF);
      if (strcmp(buf,"element")==0)
	return Token(copystr(buf),TOKEN_ELEMENT);
      if (strcmp(buf,"forall")==0)
	return Token(copystr(buf),TOKEN_FORALL);
      if (strcmp(buf,"for")==0)
	return Token(copystr(buf),TOKEN_FOR);
      if (strcmp(buf,"sizeof")==0)
	return Token(copystr(buf),TOKEN_SIZEOF);
      if (strcmp(buf,"literal")==0)
	return Token(copystr(buf),TOKEN_LITERAL);
      if (strcmp(buf,"param")==0)
	return Token(copystr(buf),TOKEN_PARAM);
      if (strcmp(buf,"1")==0)
	return Token(copystr(buf),TOKEN_ONE);
      if (strcmp(buf,"true")==0)
	return Token(copystr(buf),TOKEN_TRUE);
      if (strcmp(buf,"to")==0)
	return Token(copystr(buf),TOKEN_TO);
      if (strcmp(buf,"delay")==0)
	return Token(copystr(buf),TOKEN_DELAY);
      if (strcmp(buf,"static")==0)
	return Token(copystr(buf),TOKEN_STATIC);
      return Token(copystr(buf),0);
    }
  }
  return Token();
}


// return true if the given char is a separator
bool Reader::breakchar(int chr) {
  switch(chr) {
  case ' ':
    return true;
  case '|':
    return true;
  case '-':
    return true;
  case '+':
    return true;
  case '*':
    return true;
  case '/':
    return true;
  case ']':
    return true;
  case ')':
    return true;
  case ';':
    return true;
  case ':':
    return true;
  case '}':
    return true;
  case '[':
    return true;
  case '(':
    return true;
  case '{':
    return true;
  case '<':
    return true;
  case '=':
    return true;
  case '\n':
    return true;
  case '>':
    return true;
  case '.':
    return true;
  case ',':
      return true;
  default:
    return false;
  }
}



void Reader::error() {
  printf("%s\n",buf);
}



void tokenname(int t) {
  switch(t) {
  case TOKEN_OPENBRACK:
    printf("[");
    break;
  case TOKEN_CLOSEBRACK:
    printf("]");
    break;
  case TOKEN_FORALL:
    printf("forall");
    break;
  case TOKEN_IN:
    printf("in");
    break;
  case TOKEN_OPENBRACE:
    printf("{");
    break;
  case TOKEN_CLOSEBRACE:
    printf("}");
    break;
  case TOKEN_COMMA:
    printf(",");
    break;
  case TOKEN_SIZEOF:
    printf("sizeof");
    break;
  case TOKEN_OPENPAREN:
    printf("(");
    break;
  case TOKEN_CLOSEPAREN:
    printf(")");
    break;
  case TOKEN_LT:
    printf("<");
    break;
  case TOKEN_LTE:
    printf("<=");
    break;
  case TOKEN_EQUALS:
    printf("=");
    break;
  case TOKEN_GTE:
    printf(">=");
    break;
  case TOKEN_GT:
    printf(">");
    break;
  case TOKEN_ONE:
    printf("1");
    break;
  case TOKEN_DOT:
    printf(".");
    break;
  case TOKEN_DOTINV:
    printf(".~");
    break;
  case TOKEN_NOT:
    printf("!");
    break;
  case TOKEN_LITERAL:
    printf("literal");
    break;
  case TOKEN_PARAM:
    printf("param");
    break;
  case TOKEN_SUB:
    printf("-");
    break;
  case TOKEN_ADD:
    printf("+");
    break;
  case TOKEN_MULT:
    printf("*");
    break;
  case TOKEN_AND:
    printf("and");
    break;
  case TOKEN_OR:
    printf("or");
    break;
  case TOKEN_EOL:
    printf("EOL");
    break;
  case TOKEN_EOF:
    printf("EOF");
    break;
  case TOKEN_IMPLIES:
    printf("=>");
    break;
  case TOKEN_TRUE:
    printf("true");
    break;
  case TOKEN_FOR:
    printf("for");
    break;
  case TOKEN_TO:
    printf("to");
    break;
  case TOKEN_STRUCTURE:
    printf("structure");
    break;
  case TOKEN_RESERVED:
    printf("reserved");
    break;
  case TOKEN_LABEL:
    printf("label");
    break;
  case TOKEN_INT:
    printf("int");
    break;
  case TOKEN_BIT:
    printf("bit");
    break;
  case TOKEN_BYTE:
    printf("byte");
    break;
  case TOKEN_SUBTYPE:
    printf("subtype");
    break;
  case TOKEN_OF:
    printf("of");
    break;
  case TOKEN_SEMI:
    printf(";");
    break;
  case TOKEN_COLON:
    printf(":");
    break;
  case TOKEN_SET:
    printf("set");
    break;
  case TOKEN_ARROW:
    printf("->");
    break;
  case TOKEN_MANY:
    printf("many");
    break;
  case TOKEN_BAR:
    printf("|");
    break;
  case TOKEN_PARTITION:
    printf("partition");
    break;
  case TOKEN_ELEMENT:
    printf("element");
    break;
  default:
    printf("undefined token");
  }
}
