#include "typeparser.h"
#include <stdlib.h>
#include "list.h"
#include "common.h"
#include "token.h"
#include "tmodel.h"
#include "amodel.h"
#include "omodel.h"

Typeparser::Typeparser(Reader *r) {
  reader=r;
}

structure * Typeparser::parsestructure() {
  Token t=reader->peakahead();
  while(true) {
    if (t.token_type==TOKEN_EOF)
      return NULL;
    if (t.token_type!=TOKEN_EOL)
      break;
    skiptoken();
    t=reader->peakahead();
  }
  needtoken(TOKEN_STRUCTURE);
  Token typenam=reader->readnext();
  structure *pstructure=new structure(copystr(typenam.str));
  /*  needtoken(TOKEN_OPENPAREN);
  bool continueparse=true;
  List *list=new List();
  while(continueparse) {
    Token t=reader->readnext();
    switch(t.token_type) {
    case TOKEN_CLOSEPAREN:
      continueparse=false;
      break;
    case TOKEN_INT:
      {
	Token name=reader->readnext();
	tparam * tpar=new tparam(new ttype(TTYPE_INT),copystr(name.str));
	list->addobject(tpar);
	commaorcloseparen();
	break;
      }
    case TOKEN_BIT:
      {
	Token name=reader->readnext();
	tparam * tpar=new tparam(new ttype(TTYPE_BIT),copystr(name.str));
	list->addobject(tpar);
	commaorcloseparen();
	break;
      }
    case TOKEN_BYTE:
      {
	Token name=reader->readnext();
	tparam * tpar=new tparam(new ttype(TTYPE_BYTE),copystr(name.str));
	list->addobject(tpar);
	commaorcloseparen();
	break;
      }
    default:
      {
	Token name=reader->readnext();
	tparam *tpar=new tparam(new ttype(copystr(typenam.str)),copystr(name.str));
	list->addobject(tpar);
	commaorcloseparen();
	break;
      }
    }
  }
  tparam **tp=new tparam*[list->size()];
  list->toArray((void**) tp);
  pstructure->setparams(tp,list->size());
  delete(list);*/

  Token t2=reader->peakahead();
  if (t2.token_type==TOKEN_SUBTYPE) {
    skiptoken();
    needtoken(TOKEN_OF);
    pstructure->setsubtype(parsettype());
  }

  List *list=new List();
  List *labellist=new List();
  needtoken(TOKEN_OPENBRACE);
  while(true) {
    while (reader->peakahead().token_type==TOKEN_EOL)
      skiptoken();
    Token t=reader->peakahead();
    if (t.token_type==TOKEN_CLOSEBRACE)
      break;
    if (t.token_type==TOKEN_LABEL) {
      labellist->addobject(parsetlabel());
    } else  
      list->addobject(parsetfield());
  }
  tfield **tarray=new tfield*[list->size()];
  tlabel **larray=new tlabel*[labellist->size()];
  list->toArray((void **)tarray);
  labellist->toArray((void**)larray);
  pstructure->setfields(tarray,list->size());
  pstructure->setlabels(larray,labellist->size());
  needtoken(TOKEN_CLOSEBRACE);
  delete(list);
  delete(labellist);
  return pstructure;
}

tlabel * Typeparser::parsetlabel() {
  needtoken(TOKEN_LABEL);
  Token fieldname=reader->readnext();
  AElementexpr *index=NULL;
  if (reader->peakahead().token_type==TOKEN_OPENBRACK) {
    skiptoken();
    index=parseaelementexpr(true);
    needtoken(TOKEN_CLOSEBRACK);
  }
  needtoken(TOKEN_COLON);
  tfield *tf=parsetfield();
  return new tlabel(tf,copystr(fieldname.str),index);
}

tfield * Typeparser::parsetfield() {
  static int rcount=0;
  Token t=reader->peakahead();
  bool reserved=false;
  if (t.token_type==TOKEN_RESERVED) {
    reserved=true;
    skiptoken();
  }
  ttype *tt=parsettype();
  while(true) {
    Token isptr=reader->peakahead();
    if (isptr.token_type==TOKEN_MULT) {
      tt->makeptr();
      skiptoken();
      break; /*Only support direct pointers right now*/
    } else
      break;
  }
  Token fieldname;
  if (!reserved)
    fieldname=reader->readnext();
  AElementexpr *size=parseindex();
  tt->setsize(size);
  needtoken(TOKEN_SEMI);
  if (reserved) 
    return new tfield(tt,copystr("RESERVED"+(rcount++)));
  else
    return new tfield(tt,copystr(fieldname.str));
}

ttype * Typeparser::parsettype() {
  Token name=reader->readnext();
  switch(name.token_type) {
  case TOKEN_BIT:
    {
      return new ttype(TTYPE_BIT);
    }
  case TOKEN_INT:
    {
      return new ttype(TTYPE_INT);
    }
  case TOKEN_SHORT:
    {
      return new ttype(TTYPE_SHORT);
    }
  case TOKEN_BYTE:
    {
      return new ttype(TTYPE_BYTE);
    }
  default:
    {
      /*
      needtoken(TOKEN_OPENPAREN);
      List *list=new List();
      while(reader->peakahead().token_type!=TOKEN_CLOSEPAREN) {
	list->addobject(parseaelementexpr(false));
	commaorcloseparen();
      }
      skiptoken();
      AElementexpr **earray=new AElementexpr*[list->size()];
      list->toArray((void**)earray);*/
      ttype *ntt=new ttype(copystr(name.str),NULL);
      //delete(list);
      return ntt;
    }
  }
}

AElementexpr * Typeparser::parseindex() {
  Token t=reader->peakahead();
  if (t.token_type!=TOKEN_OPENBRACK)
    return NULL;
  skiptoken();
  AElementexpr * ae=parseaelementexpr(true);
  needtoken(TOKEN_CLOSEBRACK);
  return ae;
}

void Typeparser::commaorcloseparen() {
  Token t=reader->peakahead();
  if (t.token_type!=TOKEN_CLOSEPAREN)
    needtoken(TOKEN_COMMA);
}

AElementexpr * Typeparser::parseaelementexpr(bool isstruct) {
  AElementexpr *oldee=NULL;
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
	oldee=new AElementexpr(new Literal(copystr(literal.str)));
      else {
	if (joinop!=-1) {
	  oldee=new AElementexpr(oldee,new AElementexpr(new Literal(copystr(literal.str))),joinop);
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
      AElementexpr *ee=parseaelementexpr(false);
      if (oldee==NULL)
	oldee=ee;
      else {
	if (joinop!=-1) {
	  oldee=new AElementexpr(oldee,ee,joinop);
	  joinop=-1;
	} else error();
      }
      break;
    }
  case TOKEN_CLOSEBRACK:
    if (isstruct)
      return oldee;
  case TOKEN_CLOSEPAREN:
    skiptoken();
    return checkdot(oldee);
    break;
  case TOKEN_SUB:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=AELEMENTEXPR_SUB;
    else
      error();
    break;
  case TOKEN_ADD:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=AELEMENTEXPR_ADD;
    else
      error();
    break;
  case TOKEN_MULT:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=AELEMENTEXPR_MULT;
    else
      error();
    break;
  case TOKEN_DIV:
    skiptoken();
    if ((oldee!=NULL)&&(joinop==-1))
      joinop=AELEMENTEXPR_DIV;
    else
      error();
    break;
  default:
    if ((joinop==-1)&&(oldee!=NULL))
      return oldee;
    skiptoken();
    if (oldee==NULL)
      oldee=checkdot(new AElementexpr(new Label(copystr(t.str))));
    else {
      if (joinop!=-1) {
	oldee=new AElementexpr(oldee,checkdot(new AElementexpr(new Label(copystr(t.str)))),joinop);
	joinop=-1;
      } else error();
    }
  }
  }
}

AElementexpr * Typeparser::checkdot(AElementexpr * incoming) {
  Token tdot=reader->peakahead();
  if (tdot.token_type!=TOKEN_DOT) return incoming;
  skiptoken();
  Token tfield=reader->readnext();
  Token tpeak=reader->peakahead();
  if (tpeak.token_type==TOKEN_OPENBRACK) {
    skiptoken();
    AElementexpr *index=parseaelementexpr(false);
    return checkdot(new AElementexpr(incoming, new Field(copystr(tfield.str)),index));
  } else {
    return checkdot(new AElementexpr(incoming, new Field(copystr(tfield.str))));
  }
}

void Typeparser::error() {
  printf("ERROR\n");
  reader->error();
  exit(-1);
}

void Typeparser::skiptoken() {
  reader->readnext();
}

void Typeparser::needtoken(int token) {
  Token t=reader->readnext();
  if (!(t.token_type==token)) {
    printf("Needed token: ");
    tokenname(token);
    printf("\n Got token: %s ",t.str);
    tokenname(t.token_type);
    error();
  }
}
