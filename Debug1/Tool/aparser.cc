#include "aparser.h"
#include "list.h"
#include "common.h"
#include "token.h"
#include "amodel.h"
#include "omodel.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "model.h"

AParser::AParser(Reader *r) {
  reader=r;
}

Rule * AParser::parserule() {
  Token token=reader->peakahead();
  while(token.token_type==TOKEN_EOL) {
    skiptoken();
    token=reader->peakahead();
  }
  if (token.token_type==TOKEN_EOF)
    return NULL;
  bool delay=false;bool staticrule=false;
  if (token.token_type==TOKEN_DELAY||token.token_type==TOKEN_STATIC) {
    /* both shouldn't be allowed..doesn't make sense...*/
    if(token.token_type==TOKEN_DELAY)
      delay=true;
    else
      staticrule=true;
    skiptoken();
    token=reader->peakahead();
  }
  Rule *c;
  /*Get Quantifiers*/
  if (token.token_type==TOKEN_OPENBRACK) {
    skiptoken();
    c=parsequantifiers();
    needtoken(TOKEN_COMMA);
  } else c=new Rule();
  if (delay)
    c->setdelay();
  if (staticrule) {
    int count=0;
    for(int i=0;i<c->numquants();i++) {
      AQuantifier *aq=c->getquant(i);
      switch(aq->gettype()) {
      case AQUANTIFIER_SING:
	if (count>=1)
	  error();
	count++;
	break;
      case AQUANTIFIER_RANGE:
	break;
      default:
	error();
      }
    }
    c->setstatic();
  }
  
  /*Peek ahead to see if sizeof*/
  c->setstatementa(parsestatementa(false));
  needtoken(TOKEN_IMPLIES);
  c->setstatementb(parsestatementb());
  return c;
}

TypeEle * AParser::parsetypeele() {
  Token type=reader->readnext();
  needtoken(TOKEN_OPENPAREN);
  List *list=new List();
  while(true) {
    Token token=reader->peakahead();
    switch(token.token_type) {
    case TOKEN_CLOSEPAREN: {
      skiptoken();
      AElementexpr** larray=new AElementexpr* [list->size()];
      list->toArray((void **)larray);
      TypeEle *t=new TypeEle(copystr(type.str),list->size(),larray);
      delete(list);
      return t;
    }
    default:
      list->addobject(parseaelementexpr(false));
      if (reader->peakahead().token_type!=TOKEN_CLOSEPAREN)
	needtoken(TOKEN_COMMA);
      break;
    }
  }
}

Type * AParser::parsetype() {
  Token type=reader->readnext();
  needtoken(TOKEN_OPENPAREN);
  List *list=new List();
  while(true) {
    Token token=reader->readnext();
    switch(token.token_type) {
    case TOKEN_CLOSEPAREN:
      {
      Label** larray=new Label* [list->size()];
      list->toArray((void **)larray);
      Type *t=new Type(copystr(type.str),list->size(),larray);
      delete(list);
      return t;
      }
    default:
      list->addobject(new Label(copystr(token.str)));
      if (reader->peakahead().token_type!=TOKEN_CLOSEPAREN)
	needtoken(TOKEN_COMMA);
      break;
    }
  }
}

Statementa * AParser::parsestatementa(bool flag) {
  Statementa * oldst=NULL;
  AElementexpr *oldee=NULL;
  int eeflag=-1;
  int joinflag=-1;
  while(true) {
    Token token=reader->peakahead();
    switch(token.token_type) {
    case TOKEN_EOL:
      error();
    case TOKEN_OPENPAREN:
      {
	skiptoken();
	Statementa *st=parsestatementa(false);
	
	if (oldst==NULL) {
	  oldst=st;
	} else {
	  if (joinflag==TOKEN_AND) {
	    oldst=new Statementa(oldst, st, STATEMENTA_AND);
	  } else if (joinflag==TOKEN_OR) {
	    oldst=new Statementa(oldst, st, STATEMENTA_OR);	  
	  } else {
	    error();
	  }
	  joinflag=-1;
	}
      }
      break;
    case TOKEN_IMPLIES:
      return oldst;
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
    case TOKEN_TRUE:
      {
	skiptoken();
	Statementa *st=new Statementa();
	
	if (oldst==NULL) {
	  oldst=st;
	} else {
	  if (joinflag==TOKEN_AND) {
	    oldst=new Statementa(oldst, st, STATEMENTA_AND);
	  } else if (joinflag==TOKEN_OR) {
	    oldst=new Statementa(oldst, st, STATEMENTA_OR);	  
	  } else {
	    error();
	  }
	  joinflag=-1;
	}
      }
      break;
    case TOKEN_NOT:
      {
	skiptoken();
	Statementa * st=new Statementa(parsestatementa(true));
	
	if (oldst==NULL) {
	  oldst=st;
	} else {
	  if (joinflag==TOKEN_AND) {
	    oldst=new Statementa(oldst, st, STATEMENTA_AND);
	  } else if (joinflag==TOKEN_OR) {
	    oldst=new Statementa(oldst, st, STATEMENTA_OR);
	  } else {
	    error();
	  }
	  joinflag=-1;
	}
      }
      break;
    case TOKEN_IN: {
      skiptoken();
      Set *s=parseset();
      if (oldee==NULL||eeflag!=-1)
	error();
      Statementa * st=new Statementa(oldee,s);
      oldee=NULL;
      if (oldst==NULL) {
	oldst=st;
      } else {
	if (joinflag==TOKEN_AND) {
	  oldst=new Statementa(oldst, st, STATEMENTA_AND);
	} else if (joinflag==TOKEN_OR) {
	  oldst=new Statementa(oldst, st, STATEMENTA_OR);
	} else {
	  error();
	}
	joinflag=-1;
      }
    }
    break;
    case TOKEN_ISVALID: {
      if (oldee!=NULL) error();
      skiptoken();
      needtoken(TOKEN_OPENPAREN);
      AElementexpr *ae=parseaelementexpr(false);
      Token t=reader->peakahead();
      char *type=NULL;
      if (t.token_type==TOKEN_COMMA) {
	skiptoken();
	Token t2=reader->readnext();
	type=copystr(t2.str);
      }
      Statementa *st=new Statementa(ae, type);
      needtoken(TOKEN_CLOSEPAREN);
      if (oldst==NULL) {
	oldst=st;
      } else {
	if (joinflag==TOKEN_AND) {
	  oldst=new Statementa(oldst, st, STATEMENTA_AND);
	} else if (joinflag==TOKEN_OR) {
	  oldst=new Statementa(oldst, st, STATEMENTA_OR);
	} else {
	  error();
	}
	joinflag=-1;
      }
    }
      break;
    case TOKEN_EQUALS:
      skiptoken();
      if (oldee==NULL) error();
      eeflag=STATEMENTA_EQUALS;
      break;
    case TOKEN_LT:
      skiptoken();
      if (oldee==NULL) error();
      eeflag=STATEMENTA_LT;
      break;
    default:
      if ((oldee!=NULL) && (eeflag==-1))
	error();
      else if ((oldee==NULL)&&(eeflag==-1))
	oldee=parseaelementexpr(false);
      else {
	/*oldee!=NULL, and joinee!=-1*/
	Statementa * sa=new Statementa(oldee, parseaelementexpr(false), eeflag);
	eeflag=-1;
	oldee=NULL;
	if (oldst==NULL) {
	  oldst=sa;
	} else {
	  if (joinflag==TOKEN_AND) {
	    oldst=new Statementa(oldst, sa, STATEMENTA_AND);
	  } else if (joinflag==TOKEN_OR) {
	    oldst=new Statementa(oldst, sa, STATEMENTA_OR);	  
	  } else {
	    error();
	  }
	  joinflag=-1;
	}
      }
      break;
    }
  }
}

AElementexpr * AParser::checkdot(AElementexpr * incoming) {
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

AElementexpr * AParser::parseaelementexpr(bool isquant) {
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
  case TOKEN_CAST:
    {
      if ((joinop==-1)&&(oldee!=NULL))
	return oldee;
      skiptoken();
      needtoken(TOKEN_OPENPAREN);
      Token casttype=reader->readnext();
      needtoken(TOKEN_COMMA);
      AElementexpr *ee=parseaelementexpr(false);
      AElementexpr *tee=checkdot(new AElementexpr(copystr(casttype.str),ee));
      if (oldee==NULL)
	oldee=tee;
      else {
	if (joinop!=-1) {
	  oldee=new AElementexpr(oldee,tee,joinop);
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
      AElementexpr *ee=checkdot(parseaelementexpr(false));
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
    if (isquant)
      return oldee;
    skiptoken();
    return oldee;
  case TOKEN_CLOSEPAREN:
    skiptoken();
    return oldee;
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
  case TOKEN_NULL:
    if ((joinop==-1)&&(oldee!=NULL))
      return oldee;
    skiptoken();
    if (oldee==NULL)
      oldee=checkdot(new AElementexpr());
    else {
      if (joinop!=-1) {
	oldee=new AElementexpr(oldee,checkdot(new AElementexpr()),joinop);
	joinop=-1;
      } else error();
    }
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

Statementb * AParser::parsestatementb() {
  Token t=reader->peakahead();
  if (t.token_type==TOKEN_LT) {
    skiptoken();
    Token tpeak=reader->peakahead();
    TypeEle *typel=NULL,*typer=NULL;
    if (tpeak.token_type==TOKEN_OPENPAREN) {
      skiptoken();
      typel=parsetypeele();
      needtoken(TOKEN_CLOSEPAREN);
    }
    AElementexpr *ael=parseaelementexpr(false);
    needtoken(TOKEN_COMMA);
    if (tpeak.token_type==TOKEN_OPENPAREN) {
      skiptoken();
      typer=parsetypeele();
      needtoken(TOKEN_CLOSEPAREN);
    }
    AElementexpr *aer=parseaelementexpr(false);
    needtoken(TOKEN_GT);
    needtoken(TOKEN_IN);
    Token setlabel=reader->readnext();
    return new Statementb(typel,ael,typer,aer,new Setlabel(copystr(setlabel.str)));
  }

  TypeEle *type=NULL;
  if (t.token_type==TOKEN_OPENPAREN) {
    skiptoken();
    type=parsetypeele();
    needtoken(TOKEN_CLOSEPAREN);
  }
  AElementexpr *ae=parseaelementexpr(false);
  needtoken(TOKEN_IN);
  Token setlabel=reader->readnext();
  return new Statementb(type,ae,new Setlabel(copystr(setlabel.str)));
}

void AParser::error() {
  printf("ERROR\n");
  reader->error();
  exit(-1);
}

void AParser::skiptoken() {
  reader->readnext();
}

void AParser::needtoken(int token) {
  Token t=reader->readnext();
  if (!(t.token_type==token)) {
    printf("Needed token: ");
    tokenname(token);
    printf("\n Got token: %s ",t.str);
    tokenname(t.token_type);
    error();
  }
}

Rule * AParser::parsequantifiers() {
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
    case TOKEN_FOR:
      list->addobject(parsequantifierfor());
    case TOKEN_COMMA:
      break;
    default:
      error();
    }
  } while(bool_continue);
  AQuantifier** qarray=new AQuantifier* [list->size()];
  list->toArray((void **)qarray);
  Rule *c=new Rule(qarray,list->size());
  delete(list);
  return c;
}

AQuantifier * AParser::parsequantifier() {
  Token token=reader->peakahead();
  if (token.token_type==TOKEN_LT) {
    skiptoken();
    Type *tl=NULL, *tr=NULL;
    if (token.token_type==TOKEN_OPENPAREN) {
      skiptoken();
      tl=parsetype();
      needtoken(TOKEN_CLOSEPAREN);
    }
    Token labell=reader->readnext();
    needtoken(TOKEN_COMMA);

    if (token.token_type==TOKEN_OPENPAREN) {
      skiptoken();
      tr=parsetype();
      needtoken(TOKEN_CLOSEPAREN);
    }
    Token labelr=reader->readnext();

    needtoken(TOKEN_GT);
    needtoken(TOKEN_IN);
    return new AQuantifier(new Label(copystr(labell.str)),tl,new Label(copystr(labelr.str)),tr,parseset());
  } else {
    Type *t=NULL;
    if (token.token_type==TOKEN_OPENPAREN) {
      skiptoken();
      t=parsetype();
      needtoken(TOKEN_CLOSEPAREN);
    }
    Token label=reader->readnext();
    needtoken(TOKEN_IN);
    return new AQuantifier(new Label(copystr(label.str)),t,parseset());
  }
}

AQuantifier * AParser::parsequantifierfor() {
  Token label=reader->readnext();
  needtoken(TOKEN_EQUALS);
  AElementexpr *lower=parseaelementexpr(false);
  needtoken(TOKEN_TO);
  AElementexpr *upper=parseaelementexpr(true);
  return new AQuantifier(new Label(copystr(label.str)),lower,upper);
}

Set * AParser::parseset() {
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

