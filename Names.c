#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <values.h>
#include "RoleInference.h"
#include "GenericHashtable.h"
#include "Role.h"
#ifdef MDEBUG
#include <dmalloc.h>
#endif
#include "Names.h"

struct namer * allocatenamer() {
  struct namer * namer=(struct namer *)calloc(1, sizeof(struct namer));
  namer->classtable=genallocatehashtable((int (*)(void *)) &hashclass, (int (*)(void *,void *)) &compareclass);
  namer->desctable=genallocatehashtable((int (*)(void *)) &hashdesc, (int (*)(void *,void *)) &comparedesc);
  namer->methodtable=genallocatehashtable((int (*)(void *)) &hashmethod, (int (*)(void *,void *)) &comparemethod);
  namer->fieldtable=genallocatehashtable((int (*)(void *)) &hashfield, (int (*)(void *,void *)) &comparefield);
  return namer;
}

struct classname * getclass(struct namer *n, char * classname) {
  struct classname cn={classname};
  if (gencontains(n->classtable, &cn))
    return gengettable(n->classtable, &cn);
  else {
    struct classname* ncn=(struct classname *)calloc(1,sizeof(struct classname));
    ncn->classname=copystr(classname);
    genputtable(n->classtable, ncn,ncn);
    return ncn;
  }
}

struct fielddesc * getdesc(struct namer *n, char * fielddesc) {
  struct fielddesc fd={fielddesc};
  if (gencontains(n->desctable, &fd))
    return gengettable(n->desctable, &fd);
  else {
    struct fielddesc* nfd=(struct fielddesc *)calloc(1,sizeof(struct fielddesc));
    nfd->fielddesc=copystr(fielddesc);
    genputtable(n->desctable, nfd,nfd);
    return nfd;
  }
}

struct methodname * getmethod(struct namer *n,char *classname, char *methodname, char *signature) {
  struct classname *cn=getclass(n,classname);
  struct methodname mn={cn, methodname, signature};
  if (gencontains(n->methodtable, &mn))
    return gengettable(n->methodtable, &mn);
  else {
    struct methodname *nmn=(struct methodname *)calloc(1,sizeof(struct methodname));
    nmn->classname=cn;
    nmn->methodname=copystr(methodname);
    nmn->signature=copystr(signature);
    genputtable(n->methodtable, nmn, nmn);
    return nmn;
  }
}

struct fieldname * getfieldc(struct namer *n,struct classname *cn, char *fieldname) {
  struct fieldname fn={cn, fieldname};
  if (gencontains(n->fieldtable, &fn))
    return gengettable(n->fieldtable, &fn);
  else {
    struct fieldname *nfn=(struct fieldname *)calloc(1,sizeof(struct fieldname));
    nfn->classname=cn;
    nfn->fieldname=copystr(fieldname);
    genputtable(n->fieldtable, nfn, nfn);
    return nfn;
  }
}

struct fieldname * getfield(struct namer *n,char *classname, char *fieldname) {
  struct classname *cn=getclass(n,classname);
  struct fieldname fn={cn, fieldname};
  if (gencontains(n->fieldtable, &fn))
    return gengettable(n->fieldtable, &fn);
  else {
    struct fieldname *nfn=(struct fieldname *)calloc(1,sizeof(struct fieldname));
    nfn->classname=cn;
    nfn->fieldname=copystr(fieldname);
    genputtable(n->fieldtable, nfn, nfn);
    return nfn;
  }
}
/* Hash and compare functions to make the hashtables work*/

int hashclass(struct classname *class) {
  return hashstring(class->classname);
}

int compareclass(struct classname *cl1, struct classname *cl2) {
  return equivalentstrings(cl1->classname, cl2->classname);
}

int hashdesc(struct fielddesc *fd) {
  return hashstring(fd->fielddesc);
}

int comparedesc(struct fielddesc *fd1, struct fielddesc *fd2) {
  return equivalentstrings(fd1->fielddesc, fd2->fielddesc);
}

int hashmethod(struct methodname *method) {
  int hashcode=hashclass(method->classname);
  hashcode^=hashstring(method->methodname);
  hashcode^=hashstring(method->signature);
  return hashcode;
}

int comparemethod(struct methodname *m1, struct methodname *m2) {
  if ((m1->classname==m2->classname)&&
      equivalentstrings(m1->methodname, m2->methodname)&&
      equivalentstrings(m1->signature, m2->signature))
    return 1;
  else
    return 0;
}

int hashfield(struct fieldname *fn) {
  int hashcode=hashclass(fn->classname);
  hashcode^=hashstring(fn->fieldname);
  return hashcode;
}

int comparefield(struct fieldname *fn1, struct fieldname *fn2) {
  if ((fn1->classname==fn2->classname)&&
      equivalentstrings(fn1->fieldname, fn2->fieldname))
    return 1;
  else return 0;
}

int hashptr(void *ptr) {
  return ((long int)ptr) % MAXINT;
}
