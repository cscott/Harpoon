#include "bitreader.h"
#include <assert.h>
#include "element.h"
#include "Hashtable.h"
#include "tmodel.h"
#include "amodel.h"
#include "model.h"
#include "processabstract.h"

bitreader::bitreader(model *m, Hashtable *e) {
  globalmodel=m;
  env=e;
} 

Element * bitreader::readfieldorarray(Element *element, Field * field, Element * index) {
  assert(element->type()==ELEMENT_OBJECT);
  //  Hashtable *env=new Hashtable((int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);;  
  if (element->getobject()==NULL)
    return NULL;
  structure *type=element->getstructure();
  //  assert(type->getnumparams()==element->getnumparams());
  /* build parameter mapping */
  /*  for(int i=0;i<type->getnumparams();i++) {
      env->put(type->getparam(i)->getname(),element->paramvalue(i));
      }*/
  char *fieldname=field->field();
  ttype *typeoffield=NULL;
  AElementexpr *lindex=NULL;
  for(int i=0;i<type->getnumlabels();i++) {
    if (equivalentstrings(type->getlabel(i)->getname(),fieldname)) {
      /* got label not field... */
      typeoffield=type->getlabel(i)->gettype();
      fieldname=type->getlabel(i)->getfield();
      lindex=type->getlabel(i)->getindex();
      break;
    }
  }
  int offset=0;
  int bitoffset=0;
  for(int i=0;i<type->getnumfields();i++) {
    if(equivalentstrings(type->getfield(i)->getname(), fieldname)) {
      /* got fieldname */
      if (typeoffield==NULL)
	typeoffield=type->getfield(i)->gettype();
      break;
    } else {
      offset+=type->getfield(i)->gettype()->getbytes(this,globalmodel,env);
    }
  }
  if (index!=NULL||lindex!=NULL) {
    if (lindex!=NULL)
      index=evaluateexpr(globalmodel,lindex,env,true,true);
    if(typeoffield->gettype()==TTYPE_BIT) {
      offset+=(index->intvalue())/8;
      bitoffset=(index->intvalue())%8;
    } else {
      if (index->intvalue()!=0) {
	/* Don't want to force computation of basesize unless we really need to...
	   we'd like to handle the filesystem example...:) */
	int size=typeoffield->basesize(this,globalmodel,env);
	offset+=size*index->intvalue();
      }
    }
    if (lindex!=NULL)
      delete(index);
  }
  Element *ele=NULL;
  void *addr=(void *)(((char *) element->getobject())+offset);
  if (typeoffield->isptr())
    addr=*((void **)addr);
  switch(typeoffield->gettype()) {
  case TTYPE_INT:
    {
      int i=*((int *) addr);
      ele=new Element(i);
      break;
    }
  case TTYPE_SHORT:
    {
      short i=*((short *) addr);
      ele=new Element(i);
      break;
    }
  case TTYPE_BIT:
    {
      char c=*((char *) addr);
      ele=new Element((bool)((c&(1<<bitoffset))!=0));
      break;
    }
  case TTYPE_BYTE:
    {
      char c=*((char *)addr);
      ele=new Element(c);
      break;
    }
  case TTYPE_STRUCT:
    {
      /*      Element **earray=new Element *[typeoffield->getnumparamvalues()];
	      for(int i=0;i<typeoffield->getnumparamvalues();i++) {
	      earray[i]=evaluateexpr(this,typeoffield->getparamvalues(i),env);
	      }*/
      ele=new Element(addr,globalmodel->getstructure(typeoffield->getname()));
      break;
    }
  }
  return ele;
}

