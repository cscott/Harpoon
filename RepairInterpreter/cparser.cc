#include "cparser.h"
#include "token.h"
#include "amodel.h"
#include "omodel.h"
#include "cmodel.h"
#include "element.h"

CParser::CParser(Reader *r) {
  reader=r;
}

AElementexpr * CParser::parseaelementexpr(bool isquant) {
  CAElementexpr *oldee=NULL;
  int joinop=-1;
  while(true) {
  Token t=reader->peakahead();
  switch(t.token_type) {
  case TOKEN_LITERAL:
    {
      if ((joinop==-1)&&(oldee!=NULL))
	return oldee;
      skiptoken();
      needtoken(TOKEN_OPENPAREN);
      Token literal=reader->readnext();
      needtoken(TOKEN_CLOSEPAREN);
      if (oldee==NULL)
	oldee=new CAElementexpr(new Literal(copystr(literal.str)));
      else {
	if (joinop!=-1) {
	  oldee=new CAElementexpr(oldee,new CAElementexpr(new Literal(copystr(literal.str))),joinop);
	  joinop=-1;
	} else error();
      }
      break;
    }

  case TOKEN_OPENPAREN:
    {
      if ((joinop==-1)&&(oldee!=NULL))
	return oldee;
      skiptoken();
      CAElementexpr *ee=(CAElementexpr *)parseaelementexpr(false);
      if (oldee==NULL)
	oldee=ee;
      else {
	if (joinop!=-1) {
	  oldee=new CAElementexpr(oldee,ee,joinop);
	  joinop=-1;
	} else error();
      }
      break;
    }
    
  case TOKEN_CLOSEBRACK:
    if (isquant)
      return oldee;
    // Otherwise fall through
  case TOKEN_CLOSEPAREN:
    skiptoken();
    return checkdot(oldee);
    break;
  case TOKEN_SUB:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=CAELEMENTEXPR_SUB;
    else
      error();
    break;
  case TOKEN_ADD:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=CAELEMENTEXPR_ADD;
    else
      error();
    break;
  case TOKEN_SIZEOF: {
    skiptoken();
    needtoken(TOKEN_OPENPAREN);
    Setexpr *sae=parsesetexpr();
    needtoken(TOKEN_CLOSEPAREN);
    return new CAElementexpr(sae);
  }
  case TOKEN_ELEMENT: {
    skiptoken();
    CAElementexpr *cae=(CAElementexpr *)parseaelementexpr(false);
    needtoken(TOKEN_OF);
    Setexpr *sae=parsesetexpr();
    return new CAElementexpr(cae,sae);
  }
  case TOKEN_OF:
    return oldee;
  case TOKEN_MULT:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=CAELEMENTEXPR_MULT;
    else
      error();
    break;
  case TOKEN_DIV:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=CAELEMENTEXPR_DIV;
    else
      error();
    break;
  case TOKEN_NULL:
    if ((joinop==-1)&&(oldee!=NULL))
      return oldee;
    skiptoken();
    if (oldee==NULL)
      oldee=checkdot(new CAElementexpr());
    else {
      if (joinop!=-1) {
	oldee=new CAElementexpr(oldee,checkdot(new CAElementexpr()),joinop);
	joinop=-1;
      } else error();
    }
    break;
  default:
    if ((joinop==-1)&&(oldee!=NULL))
      return oldee;
    skiptoken();
    if (oldee==NULL)
      oldee=checkdot(new CAElementexpr(new Label(copystr(t.str))));
    else {
      if (joinop!=-1) {
	oldee=new CAElementexpr(oldee,checkdot(new CAElementexpr(new Label(copystr(t.str)))),joinop);
	joinop=-1;
      } else error();
    }
  }
  }
}

CAElementexpr * CParser::checkdot(CAElementexpr * incoming) {
  Token tdot=reader->peakahead();
  if (tdot.token_type!=TOKEN_DOT) return incoming;
  skiptoken();
  Token tfield=reader->readnext();
  Token tpeak=reader->peakahead();
  return new CAElementexpr(incoming, new Relation(copystr(tfield.str)));
}

Setexpr * CParser::parsesetexpr() {
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

Expr * CParser::parseexpr() {
  Expr * oldee=NULL;
  Field *oldf=NULL;
  CAElementexpr *oldindex=NULL;
  bool parselside=true;
  while(parselside) {
    Token t=reader->readnext();
    switch(t.token_type) {
    case TOKEN_DOT:
      if (oldf!=NULL) {
	/* do shift-pack stuff */
	if(oldindex==NULL)
	  oldee=new Expr(oldee,oldf);
	else
	  oldee=new Expr(oldee,oldf,oldindex);
	oldf=NULL;
	oldindex=NULL;
      }
      {
	Token name=reader->readnext();
	oldf=new Field(copystr(name.str));
      }
      break;
    case TOKEN_OPENBRACK:
      oldindex=(CAElementexpr *)parseaelementexpr(true);
      needtoken(TOKEN_CLOSEBRACK);
      break;
    case TOKEN_CAST: {
      if(oldee!=NULL)
	error();
      needtoken(TOKEN_OPENPAREN);
      Token fld=reader->readnext();
      needtoken(TOKEN_COMMA);
      Expr *ex=parseexpr();
      needtoken(TOKEN_CLOSEPAREN);
      oldee=new Expr(copystr(fld.str),ex);
      break;
    }
    case TOKEN_CLOSEPAREN:
      if (oldf!=NULL) {
	/* do shift-pack stuff */
	if(oldindex==NULL)
	  oldee=new Expr(oldee,oldf);
	else
	  oldee=new Expr(oldee,oldf,oldindex);
	oldf=NULL;
	oldindex=NULL;
      }
      return oldee;
    default:
      if(oldee!=NULL)
	error();
      oldee=new Expr(new Label(copystr(t.str)));
    }
  }
  return oldee;
}

Statementb * CParser::parsestatementb() {
  Expr * oldee=NULL;
  Field *oldf=NULL;
  CAElementexpr *oldindex=NULL;
  bool parselside=true;
  while(parselside) {
    Token t=reader->readnext();
    switch(t.token_type) {
    case TOKEN_DOT:
      if (oldf!=NULL) {
	/* do shift-pack stuff */
	if(oldindex==NULL)
	  oldee=new Expr(oldee,oldf);
	else
	  oldee=new Expr(oldee,oldf,oldindex);
	oldf=NULL;
	oldindex=NULL;
      }
      {
	Token name=reader->readnext();
	oldf=new Field(copystr(name.str));
      }
      break;
    case TOKEN_OPENBRACK:
      oldindex=(CAElementexpr *)parseaelementexpr(true);
      needtoken(TOKEN_CLOSEBRACK);
      break;
    case TOKEN_CAST: {
      if(oldee!=NULL)
	error();
      needtoken(TOKEN_OPENPAREN);
      Token fld=reader->readnext();
      needtoken(TOKEN_COMMA);
      Expr *ex=parseexpr();
      oldee=new Expr(copystr(fld.str),ex);
      break;
    }
    case TOKEN_EQUALS:
      parselside=false;
      if (oldf==NULL)
	error();
      break;
    default:
      if(oldee!=NULL)
	error();
      oldee=new Expr(new Label(copystr(t.str)));
    }
  }
  CAElementexpr *rside=(CAElementexpr *)parseaelementexpr(false);
  if (oldindex==NULL)
    return new CStatementb(oldee,oldf,rside);
  else
    return new CStatementb(oldee,oldf,oldindex,rside);
}
