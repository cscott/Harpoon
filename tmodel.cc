#include "tmodel.h"
#include <stdlib.h>
#include "processabstract.h"
#include "element.h"
#include "model.h"
#include "Hashtable.h"
#include "common.h"

void structure::print() {
}

void ttype::print() {
}

/*void tparam::print() {
  }*/

void tlabel::print() {
}

void tfield::print() {
}

AElementexpr * ttype::getsize() {
  return asize;
}


int ttype::getbytes(bitreader *br,model *m,Hashtable *env) {
  int basesizeval=basesize(br,m,env);
  int mult=1;
  if (asize!=NULL) {
    Element * number=evaluateexpr(m,asize,env,true,false);
    mult=number->intvalue();
    delete(number);
  }
  if (gettype()==TTYPE_BIT) {
    return ((mult-1)/8)+1;
  }
  return mult*basesizeval;
}

int ttype::basesize(bitreader *br,model *m,Hashtable *env) {
  if ((primtype/TTYPE_PTR)>0)
    return pointersize;
  if (gettype()==TTYPE_INT)
    return intsize;
  if (gettype()==TTYPE_SHORT)
    return shortsize;
  if (gettype()==TTYPE_BYTE)
    return 1;
  if (gettype()==TTYPE_BIT)
    return 0;
  if (gettype()==TTYPE_STRUCT) {
    /*    Element **earray=new Element *[numparamvalues];
	  for(int i=0;i<numparamvalues;i++) {
	  earray[i]=evaluateexpr(br,paramvalues[i],env);
	  }*/
    structure *st=m->getstructure(type);
    int size=st->getsize(br,m,env);
    /*    for(int i=0;i<numparamvalues;i++)
	  delete(earray[i]);
	  delete earray;*/
    return size;
  }
}

int structure::getsize(bitreader *br,model *m, Hashtable *env) {
  int totalsize=0;
  /* build parameter mapping */
  /*  for(int i=0;i<getnumparams();i++) {
    env->put(getparam(i)->getname(),earray[i]);
    }*/
  /* loop through fields */
  if (getsubtype()!=NULL) {
    return getsubtype()->getbytes(br, m,env);
  }
  for(int j=0;j<getnumfields();j++)
    totalsize+=getfield(j)->gettype()->getbytes(br,m,env);
  //delete(env);
  return totalsize;
}
char * tlabel::getname() {
  return specifictype->getname();
}
ttype * tlabel::gettype() {
  return specifictype->gettype();
}

structure::structure(char *nam) {
  subtype=NULL;
  name=nam;
}

void structure::settype(int t) {
  type=t;
}

/*void structure::setparams(tparam **tp,int n) {
  params=tp;numparams=n;
  }*/

void structure::setsubtype(ttype *sub) {
  subtype=sub;
}

ttype * structure::getsubtype() {
  return subtype;
}

void structure::setfields(tfield **fieldarray, int n) {
  fields=fieldarray;
  numfields=n;
}

void structure::setlabels(tlabel **lab,int n) {
  labels=lab;
  numlabels=n;
}

/*int structure::getnumparams() {
  return numparams;
  }*/

/*tparam * structure::getparam(int i) {
  return params[i];
  }*/

int structure::getnumlabels() {
  return numlabels;
}

tlabel * structure::getlabel(int i) {
  return labels[i];
}

int structure::getnumfields() {
  return numfields;
}

tfield * structure::getfield(int i) {
  return fields[i];
}

char * structure::getname() {
  return name;
}

ttype::ttype(int typ) {
  primtype=typ;
  intlength=0;
  type=NULL;
  //  paramvalues=NULL;
  //numparamvalues=0;
  asize=NULL;
}

ttype::ttype(char *t) {
  primtype=TTYPE_STRUCT;
  type=t;
  intlength=0;
  //paramvalues=NULL;
  //numparamvalues=0;
  asize=NULL;
}

ttype::ttype(char *t, AElementexpr *size) {
  primtype=TTYPE_STRUCT;
  type=t;
  intlength=0;
  //paramvalues=param;
  //numparamvalues=numparam;
  asize=size;
}

ttype::ttype(int type, AElementexpr * size) {
  primtype=type;
  intlength=0;
  this->type=NULL;
  //paramvalues=NULL;
  //numparamvalues=0;
  asize=size;
}

void ttype::makeptr() {
  primtype+=TTYPE_PTR;
}
 
void ttype::setsize(AElementexpr *size) {
  asize=size;
}

bool ttype::isptr() {
  if (numderef()>0)
    return true;
  else
    return false;
}

int ttype::numderef() {
  return primtype/TTYPE_PTR;
}

int ttype::gettype() {
  return primtype%TTYPE_PTR;
}
 
/*int ttype::getnumparamvalues() {
  return numparamvalues;
}

AElementexpr * ttype::getparamvalues(int i) {
  return paramvalues[i];
  }*/
 
char * ttype::getname() {
  return type;
}

/*tparam::tparam(ttype *t,char * n) {
  type=t;name=n;
  }
  char * tparam::getname() {
  return name;
  }
*/
tlabel::tlabel(tfield *f, char *fld,AElementexpr *a) {
  index=a;
  field=fld;
  specifictype=f;
}

char *tlabel::getfield() {
  return field;
}
 
AElementexpr * tlabel::getindex() {
  return index;
}

tfield::tfield(ttype *tt, char *n) {
  type=tt;name=n;
}

ttype * tfield::gettype() {
  return type;
}

char * tfield::getname() {
  return name;
}
