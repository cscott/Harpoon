#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include "rparser.h"
#include "list.h"
#include "common.h"
#include "token.h"
#include "set.h"
#include "classlist.h"


// class RParser

RParser::RParser(Reader *r) 
{
  reader=r;
}


// returns the name of the relation whose range is defined
char* RParser::parserelation()
{
  Token token = reader->peakahead();

  while(token.token_type==TOKEN_EOL) 
    {
      skiptoken();
      token = reader->peakahead();
    }
  
  if (token.token_type==TOKEN_EOF)
    return NULL;

  needtoken(0);
  needtoken(TOKEN_COLON); // we need a colon

  char* relation = new char[strlen(token.str)+1];
  strcpy(relation, token.str);
  return relation;
}



WorkSet* RParser::parseworkset()
{
#ifdef DEBUGMANYMESSAGES
  printf("Parsing a new workset... \n");
#endif

  WorkSet *wset = new WorkSet(true);
  needtoken(TOKEN_OPENBRACE);  // need an open brace

  Token token = reader->readnext();

  while (token.token_type != TOKEN_CLOSEBRACE)
    {      
#ifdef DEBUGMESSAGES
      //printf("Adding %s...\n", token.str);
      //fflush(NULL);
#endif
      char* newtoken = (char*) malloc(strlen(token.str));
      strcpy(newtoken, token.str);

      wset->addobject(newtoken);
      
      token = reader->readnext();
      
      if (token.token_type == TOKEN_COMMA)
	token = reader->readnext();
    }

  return wset;
}



void RParser::error() 
{
  printf("ERROR\n");
  reader->error();
  exit(-1);
}


void RParser::skiptoken() 
{
  reader->readnext();
}


void RParser::needtoken(int token) 
{
  Token t=reader->readnext();
  if (!(t.token_type==token)) 
    {
      printf("Needed token: ");
      tokenname(token);
      printf("\n Got token: %s ",t.str);
      tokenname(t.token_type);
      error();
    }
}
