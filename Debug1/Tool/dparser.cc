#include "dparser.h"
#include <stdlib.h>
#include "list.h"
#include "common.h"
#include "token.h"
#include "dmodel.h"

Dparser::Dparser(Reader *r) {
  reader=r;
}

DomainRelation *Dparser::parsesetrelation() {
  List *sets=new List();
  List *relations=new List();
  for(Token t=reader->peakahead();t.token_type!=TOKEN_EOF;t=reader->peakahead()) {
    switch(t.token_type) {
    case TOKEN_EOL:
      skiptoken();
      break;
    case TOKEN_SET:
      sets->addobject(parseset());
      break;
    default:
      relations->addobject(parserelation());
    }
  }
  DomainSet **dsarray=new DomainSet*[sets->size()];
  DRelation **drarray=new DRelation*[relations->size()];
  sets->toArray((void **)dsarray);
  relations->toArray((void **)drarray);
  DomainRelation *dr=new DomainRelation(dsarray,sets->size(),
					drarray,relations->size());
  delete(sets);
  delete(relations);
  return dr;
}

DomainSet * Dparser::parseset() {
  needtoken(TOKEN_SET);
  Token name=reader->readnext();
  DomainSet *ds=new DomainSet(copystr(name.str));
  //  Token istype=reader->peakahead();
  //  if (istype.token_type==TOKEN_OPENPAREN) {
  //skiptoken();
  needtoken(TOKEN_OPENPAREN);
  ds->settype(copystr(reader->readnext().str));
  needtoken(TOKEN_CLOSEPAREN);
    //}
  needtoken(TOKEN_COLON);
  bool ispart=(reader->peakahead().token_type==TOKEN_PARTITION);
  if (ispart)
    skiptoken();
  bool needset=false;
  bool cont=true;
  List *subsets=new List();
  while(cont) {
    Token t=reader->peakahead();
    switch(t.token_type) {
    case TOKEN_EOF:
      if (!needset) {
	printf("ERROR: Need set name");
	exit(-1);
      }
      cont=false;
      break;
    case TOKEN_EOL:
      if (!needset)
	cont=false;
      skiptoken();
      break;
    case TOKEN_BAR:
      needset=true;
      skiptoken();
      break;
    default:
      subsets->addobject(copystr(t.str));
      skiptoken();
      needset=false;
    }
  }
  char **carray=new char*[subsets->size()];
  subsets->toArray((void **)carray);
  if (ispart)
    ds->setpartition(carray,subsets->size());
  else 
    ds->setsubsets(carray,subsets->size());
  delete(subsets);
  return ds;
}

DRelation * Dparser::parserelation() {
  Token name=reader->readnext();
  bool isstat=false;
  if (name.token_type==TOKEN_STATIC) {
    isstat=true;
    name=reader->readnext();
  }
  needtoken(TOKEN_COLON);
  Token domain=reader->readnext();
  needtoken(TOKEN_ARROW);
  Token range=reader->readnext();
  needtoken(TOKEN_OPENPAREN);
  Token domainmult=reader->readnext();
  needtoken(TOKEN_ARROW);
  Token rangemult=reader->readnext();
  needtoken(TOKEN_CLOSEPAREN);
  int type=0;
  if (domainmult.token_type==TOKEN_ONE)
    type+=0x1;
  else if (domainmult.token_type==TOKEN_MANY)
    type+=0x2;
  else error();

  if (rangemult.token_type==TOKEN_ONE)
    type+=0x10;
  else if (rangemult.token_type==TOKEN_MANY)
    type+=0x20;
  else error();

  return new DRelation(copystr(name.str),copystr(domain.str),
		       copystr(range.str),type,isstat);
}

void Dparser::error() {
  printf("ERROR\n");
  reader->error();
  exit(-1);
}

void Dparser::skiptoken() {
  reader->readnext();
}

void Dparser::needtoken(int token) {
  Token t=reader->readnext();
  if (!(t.token_type==token)) {
    printf("Needed token: ");
    tokenname(token);
    printf("\n Got token: %s ",t.str);
    tokenname(t.token_type);
    error();
  }
}
