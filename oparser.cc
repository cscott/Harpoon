#include "oparser.h"
#include "omodel.h"
#include "list.h"
#include "common.h"
#include "token.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

Constraint * Parser::parseconstraint() {
  Token token=reader->peakahead();
  while(token.token_type==TOKEN_EOL) {
    skiptoken();
    token=reader->peakahead();
  }
  if (token.token_type==TOKEN_EOF)
    return NULL;
  Constraint *c;
  /*Get Quantifiers*/
  if (token.token_type==TOKEN_OPENBRACK) {
    skiptoken();
    c=parsequantifiers();
    needtoken(TOKEN_COMMA);
  } else c=new Constraint();
  /*Peek ahead to see if sizeof*/

  c->setstatement(parsestatement(false));
    
  return c;
}

Statement * Parser::parsestatement(bool flag) {
  Statement * oldst=NULL;
  int joinflag=-1;
  while(true) {
    Token token=reader->peakahead();
    switch(token.token_type) {
    case TOKEN_EOL:
      skiptoken();
      return oldst;
    case TOKEN_OPENPAREN:
      {
	skiptoken();
	Statement *st=parsestatement(false);
	
	if (oldst==NULL) {
	  oldst=st;
	} else {
	  if (joinflag==TOKEN_AND) {
	    oldst=new Statement(oldst, st, STATEMENT_AND);
	  } else if (joinflag==TOKEN_OR) {
	    oldst=new Statement(oldst, st, STATEMENT_OR);
	  } else {
	    error();
	  }
	  joinflag=-1;
	}
      }
      break;
    case TOKEN_CLOSEPAREN:
      skiptoken();
      return oldst;
    case TOKEN_AND:
      skiptoken();
      if (oldst==NULL) error();
      joinflag=TOKEN_AND;
      break;
    case TOKEN_OR:
      skiptoken();
      if (oldst==NULL) error();
      joinflag=TOKEN_OR;
      break;
    case TOKEN_NOT:
      {
	skiptoken();
	Statement * st=new Statement(parsestatement(true));
	
	if (oldst==NULL) {
	  oldst=st;
	} else {
	  if (joinflag==TOKEN_AND) {
	    oldst=new Statement(oldst, st, STATEMENT_AND);
	  } else if (joinflag==TOKEN_OR) {
	    oldst=new Statement(oldst, st, STATEMENT_OR);	  
	  } else {
	    error();
	  }
	  joinflag=-1;
	}
      }
      break;
    default:
      {
	Statement * st=new Statement(parsepredicate());
	if (flag) return st;
	if (oldst==NULL) {
	  oldst=st;
	} else {
	  if (joinflag==TOKEN_AND) {
	    oldst=new Statement(oldst, st, STATEMENT_AND);
	  } else if (joinflag==TOKEN_OR) {
	    oldst=new Statement(oldst, st, STATEMENT_OR);	  
	  }
	  joinflag=-1;
	}
      }
    }
  }
}

Elementexpr * Parser::parseelementexpr() {
  Elementexpr *oldee=NULL;
  int joinop=-1;
  while(true) {
  Token t=reader->peakahead();
  switch(t.token_type) {
  case TOKEN_SIZEOF:
    {
      if ((joinop==-1)&&(oldee!=NULL))
	return oldee;
      skiptoken();
      needtoken(TOKEN_OPENPAREN);
      Setexpr* se=parsesetexpr();
      needtoken(TOKEN_CLOSEPAREN);
      if (oldee==NULL)
	oldee=new Elementexpr(se);
      else {
	if (joinop!=-1) {
	  oldee=new Elementexpr(oldee,new Elementexpr(se),joinop);
	  joinop=-1;
	} else error();
      }
      break;
    }
  case TOKEN_LITERAL:
    {
      if ((joinop==-1)&&(oldee!=NULL))
	return oldee;
      skiptoken();
      needtoken(TOKEN_OPENPAREN);
      Token literal=reader->readnext();
      needtoken(TOKEN_CLOSEPAREN);
      if (oldee==NULL)
	oldee=new Elementexpr(new Literal(copystr(literal.str)));
      else {
	if (joinop!=-1) {
	  oldee=new Elementexpr(oldee,new Elementexpr(new Literal(copystr(literal.str))),joinop);
	  joinop=-1;
	} else error();
      }
      break;
    }
    /*  case TOKEN_PARAM:
	{
	if ((joinop==-1)&&(oldee!=NULL))
	return oldee;
	skiptoken();
	needtoken(TOKEN_OPENPAREN);
	Elementexpr *ee=parseelementexpr();
	needtoken(TOKEN_COMMA);
	Token number=reader->readnext();
	needtoken(TOKEN_CLOSEPAREN);
	
	if (oldee==NULL)
	oldee=new Elementexpr(ee,new Literal(copystr(number.str)));
	else {
	if (joinop!=-1) {
	oldee=new Elementexpr(oldee,new Elementexpr(ee,new Literal(copystr(number.str))),joinop);
	joinop=-1;
	} else error();
	}
	
	break;
	} */
  case TOKEN_OPENPAREN:
    {
      if ((joinop==-1)&&(oldee!=NULL))
	return oldee;
      skiptoken();
      Elementexpr *ee=parseelementexpr();
      if (oldee==NULL)
	oldee=ee;
      else {
	if (joinop!=-1) {
	  oldee=new Elementexpr(oldee,ee,joinop);
	  joinop=-1;
	} else error();
      }
      break;
    }
  case TOKEN_CLOSEPAREN:
    return oldee;
    break;
  case TOKEN_SUB:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=ELEMENTEXPR_SUB;
    else
      error();
    break;
  case TOKEN_ADD:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=ELEMENTEXPR_ADD;
    else
      error();
    break;
  case TOKEN_MULT:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=ELEMENTEXPR_MULT;
    else
      error();
    break;
  default:
    if ((joinop==-1)&&(oldee!=NULL))
      return oldee;
    skiptoken();
    if (oldee==NULL)
      oldee=checkdot(new Elementexpr(new Label(copystr(t.str))));
    else {
      if (joinop!=-1) {
	oldee=new Elementexpr(oldee,new Elementexpr(new Label(copystr(t.str))),joinop);
	joinop=-1;
      } else error();
    }
  }
  }
}

Elementexpr * Parser::checkdot(Elementexpr * incoming) {
  Token tdot=reader->peakahead();
  if (tdot.token_type!=TOKEN_DOT) return incoming;
  skiptoken();
  Token tfield=reader->readnext();
  Token tpeak=reader->peakahead();
  return checkdot(new Elementexpr(incoming, new Relation(copystr(tfield.str))));
}

Predicate * Parser::parsepredicate() {
  Token label=reader->readnext();

  if (label.token_type==TOKEN_SIZEOF) {
    needtoken(TOKEN_OPENPAREN);
    Setexpr * setexpr=parsesetexpr();
    needtoken(TOKEN_CLOSEPAREN);
    Token tokentest=reader->readnext();
    bool greaterthan=false;
    switch(tokentest.token_type) {
    case TOKEN_EQUALS:
      greaterthan=false;
      break;
    case TOKEN_GT:
      greaterthan=true;
      break;
    default:
      error();
    }
    needtoken(TOKEN_ONE);
    return new Predicate(greaterthan, setexpr);
  }

  Token nexttoken=reader->readnext();
  switch(nexttoken.token_type) {
  case TOKEN_DOT:
    {
      Token relation=reader->readnext();
      Token compareop=reader->readnext();
      Elementexpr * ee=parseelementexpr();
      switch(compareop.token_type) {
      case TOKEN_LT:
	return new Predicate(new Valueexpr(new Label(copystr(label.str)),
					   new Relation(copystr(relation.str))),PREDICATE_LT,ee);
      case TOKEN_LTE:
	return new Predicate(new Valueexpr(new Label(copystr(label.str)),
					   new Relation(copystr(relation.str))),PREDICATE_LTE,ee);
      case TOKEN_EQUALS:
	return new Predicate(new Valueexpr(new Label(copystr(label.str)),
					   new Relation(copystr(relation.str))),PREDICATE_EQUALS,ee);
      case TOKEN_GTE:
	return new Predicate(new Valueexpr(new Label(copystr(label.str)),
					   new Relation(copystr(relation.str))),PREDICATE_GTE,ee);
      case TOKEN_GT:
	return new Predicate(new Valueexpr(new Label(copystr(label.str)),
					   new Relation(copystr(relation.str))),PREDICATE_GT,ee);
      default:
	error();
      }
    }
  case TOKEN_IN:
    {
      Setexpr * se=parsesetexpr();
    return new Predicate(new Label(copystr(label.str)),se);
    }
  default:
    error();
  }
}

void Parser::error() {
  printf("ERROR\n");
  reader->error();
  exit(-1);
}

void Parser::skiptoken() {
  reader->readnext();
}

void Parser::needtoken(int token) {
  Token t=reader->readnext();
  if (!(t.token_type==token)) {
    printf("Needed token: ");
    tokenname(token);
    printf("\n Got token: %s ",t.str);
    tokenname(t.token_type);
    error();
  }
}

Constraint * Parser::parsequantifiers() {
  bool bool_continue=true;
  List * list=new List();
  do {
    Token token2=reader->readnext();
    switch(token2.token_type) {
    case TOKEN_CLOSEBRACK:
      bool_continue=false;
      break;
    case TOKEN_FORALL:
      list->addobject(parsequantifier());
      break;
    case TOKEN_COMMA:
      break;
    default:
      error();
    }
  } while(bool_continue);
  Quantifier** qarray=new Quantifier* [list->size()];
  list->toArray((void **)qarray);
  Constraint *c=new Constraint(qarray,list->size());
  delete(list);
  return c;
}

Quantifier * Parser::parsequantifier() {
  Token label=reader->readnext();
  needtoken(TOKEN_IN);
  return new Quantifier(new Label(copystr(label.str)),parseset());
}

Set * Parser::parseset() {
  Token label=reader->readnext();
  if (label.token_type==TOKEN_OPENBRACE) {
    bool bool_continue=true;
    List * list=new List();
    do {
      Token token2=reader->readnext();
      switch(token2.token_type) {
      case TOKEN_CLOSEBRACE:
	bool_continue=false;
	break;
      case TOKEN_COMMA:
	break;
      default:
      	list->addobject(new Literal(copystr(token2.str)));
	break;
      }
    } while(bool_continue);
    int size=list->size();
    Literal** qarray=new Literal* [size];
    list->toArray((void **)qarray);
    delete(list);
    return new Set(qarray,size);
  } else
    return new Set(new Setlabel(copystr(label.str)));
}

Setexpr * Parser::parsesetexpr() {
  Token label=reader->readnext();
  Token peak=reader->peakahead();
  if (peak.token_type==TOKEN_DOT) {
    skiptoken();
    return new Setexpr(new Label(copystr(label.str)),false,new Relation(copystr(reader->readnext().str)));
  } else if (peak.token_type==TOKEN_DOTINV) {
    skiptoken();
    return new Setexpr(new Label(copystr(label.str)),true,new Relation(copystr(reader->readnext().str)));
  } else
    return new Setexpr(new Setlabel(copystr(label.str)));
}

Parser::Parser(Reader *r) {
  reader=r;
}
