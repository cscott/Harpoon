#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "RoleInference.h"
#include "CalculateDominators.h"
#include "Role.h"
#include "Method.h"
#include "Effects.h"
#include "Fields.h"
#include "dot.h"
#include "Container.h"
#include "RoleRelation.h"
#include "web.h"
#include "FastScan.h"

#ifdef MDEBUG
#include <dmalloc.h>
#endif

static int programfd;
#define bufsize 1000
//#define DEBUG
long long pointerage=0;


int main(int argc, char **argv) {
  getfile();
  fastscan();
  return 0;
}

void getfile() {
  programfd=open("roleinfer.mem",O_RDONLY);
  if (programfd<0)
    perror("roleinfer.mem open failure\n");
}

void outputinfo(struct namer* namer, struct genhashtable *calltable) {
    FILE *classfile=fopen("fs-class","w");
    FILE *methodfile=fopen("fs-method","w");
    FILE *fieldfile=fopen("fs-field","w");
    FILE *callgraphfile=fopen("fs-callgraph","w");

    struct geniterator *it=gengetiterator(namer->classtable);
    while(1) {
	struct classname *cn=gennext(it);
	if (cn==NULL)
	    break;
	fprintf(classfile, "%s ", cn->classname);
    }
    genfreeiterator(it);

    it=gengetiterator(namer->methodtable);
    while(1) {
	struct methodname *mn=gennext(it);
	if (mn==NULL)
	    break;
	fprintf(methodfile, "%s.%s%s ", mn->classname->classname, mn->methodname, mn->signature);
    }
    genfreeiterator(it);

    it=gengetiterator(namer->fieldtable);
    while(1) {
	struct fieldname *fn=gennext(it);
	if (fn==NULL)
	    break;
	fprintf(fieldfile, "%s %s %s ", fn->classname->classname, fn->fieldname, fn->fielddesc->fielddesc);
    }
    genfreeiterator(it);

    it=gengetiterator(calltable);
    while(1) {
	struct methodname *mn=gennext(it);
	struct methodchain *mc=NULL;
	if (mn==NULL)
	    break;
	mc=gengettable(calltable, mn);
	while(mc!=NULL) {
	    fprintf(callgraphfile, "%s.%s%s ", mn->classname->classname, mn->methodname, mn->signature);	    
	    fprintf(callgraphfile, "%s.%s%s ", mc->method->classname->classname, mc->method->methodname, mc->method->signature);
	    mc=mc->caller;
	}
    }
    genfreeiterator(it);

    fclose(callgraphfile);
    fclose(classfile);
    fclose(methodfile);
    fclose(fieldfile);
}

char bufdata[bufsize];
unsigned long buflength=0;
unsigned long bufstart=0;

char * getline() {
  char *bufreturn=(char *)malloc(bufsize+1);
  int i,offset;
  if (bufstart==buflength) {
    /*Base Case*/
    buflength=read(programfd, bufdata, bufsize);
    bufstart=0;
    if (buflength<0)
      perror("Error reading line\n");
    if (buflength==0)
      return 0;
  }
  offset=0;

  while(1) {
    for(i=bufstart;i<buflength;i++)
      if (bufdata[i]==10) {
	if ((offset+i+1-bufstart)>bufsize)
	  printf("ERROR: Buffer overrun");
	memcpy(bufreturn+offset, bufdata+bufstart, i+1-bufstart);
	bufreturn[offset+i+1-bufstart]=0;
	bufstart=i+1;
	return bufreturn;
      }
    
    if ((offset+buflength-bufstart)>bufsize)
      printf("ERROR: Buffer overrun");

    memcpy(bufreturn+offset, bufdata+bufstart, buflength-bufstart);
    offset+=buflength-bufstart;
    buflength=read(programfd, bufdata, bufsize);
    if (buflength<0)
      perror("Error reading line\n");
    if (buflength==0)
      return 0;
    bufstart=0;
  }
}

void fastscan() {
    struct methodchain *methodstack=NULL;
    struct namer *namer=allocatenamer();
    struct genhashtable *calltable=genallocatehashtable((int (*)(void *)) &hashmethod, (int (*)(void *,void *)) &comparemethod);

    while(1) {
    char *line=getline();

#ifdef DEBUG
    printf("------------------------------------------------------\n");
#endif

    if (line==0) {
	outputinfo(namer, calltable);
	return;
    }
#ifdef DEBUG
    printf("[%s]\n",line);
#endif
    switch(line[0]) {
    case 'C': 
	break;
    case 'O':
	break;
    case 'N':
      {
	/* Natively created object...may not have pointer to it*/
	char buf[1000];
	sscanf(line,"NI: %s",buf);

	getclass(namer,buf);
      }
      break;
    case 'U':
      {
	/* New object*/

	char buf[1000];
	sscanf(line,"NI: %s",buf);
	getclass(namer,buf);
      }
      break;
    case 'K':
      break; 
    case 'L':
      /* Do Load */
      {
	struct localvars * lv=(struct localvars *) calloc(1, sizeof(struct localvars));
	long long uid, objuid;
	char fieldname[600], classname[600], fielddesc[600];

	sscanf(line,"LF: %s %ld %s %lld %s %s %s %lld",lv->name,&lv->linenumber, lv->sourcename, &objuid, classname, fieldname, fielddesc, &uid);
	getfield(namer,classname, fieldname,fielddesc);
	
      }
      break;
    case 'G':
      /* Do Array Load */
      break;
    case 'M':
      /* Mark Local*/
      break;
    case 'I':
      /* Enter Method*/
      {
	struct methodchain* methodchain=(struct methodchain *) calloc(1,sizeof(struct methodchain));
	char classname[600], methodname[600],signature[600];
	sscanf(line,"IM: %s %s %s", classname, methodname, signature);
	methodchain->method=getmethod(namer, classname, methodname, signature);
	methodchain->caller=methodstack;
	if (!gencontains(calltable, methodchain->method)) {
	    struct methodchain *mc=(struct methodchain *) calloc(1,sizeof(struct methodchain));
	    mc->method=methodchain->method;
	    genputtable(calltable, methodchain->method, mc);
	} else {
	    struct methodchain *tosearch=(struct methodchain *)gengettable(calltable, methodchain->method);
	    while(tosearch->method!=methodchain->method) {
		if (tosearch->caller==NULL) {
		    struct methodchain *mc=(struct methodchain *) calloc(1,sizeof(struct methodchain));
		    mc->method=methodchain->method;
		    tosearch->caller=mc;
		    break;
		}
	    }
	}
	methodstack=methodchain;
      }

      break;
    case 'R':
      /* Return from method */
      {
	struct methodchain* caller=methodstack->caller;
        free(methodstack);
        methodstack=caller;
      }
      break;
    case 'F':
      /* Field Assignment */
      {
	long long suid;
	long long duid;
	char classname[1000];
	char fieldname[1000];
	char descname[1000];
	sscanf(line,"FA: %lld %s %s %s %lld", &suid, classname, fieldname, descname, &duid);
	getfield(namer, classname, fieldname, descname);
      }
      break;
    case 'A':
      /* Array Assignment */
      {
      	long long suid;
	long long duid;
	long index;
	sscanf(line,"AA: %lld %ld %lld", &suid, &index, &duid);
      }
      break;
    }
    free(line);
  }
}
