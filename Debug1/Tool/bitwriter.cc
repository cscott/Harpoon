// Interface for writing structures

#include "bitwriter.h"
#include "bitreader.h"
#include <assert.h>
#include "element.h"
#include "Hashtable.h"
#include "tmodel.h"
#include "amodel.h"
#include "model.h"
#include "processabstract.h"

bitwriter::bitwriter(model *m, Hashtable *e) {
  globalmodel=m;
  bitread=new bitreader(m,e);
  env=e;
}

void bitwriter::writefieldorarray(Element *element, Field * field, Element * index, Element *target) {
  assert(element->type()==ELEMENT_OBJECT);
  
  //  Hashtable *env=new Hashtable((int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);;  
  structure *type=element->getstructure();
  //assert(type->getnumparams()==element->getnumparams());

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
      offset+=type->getfield(i)->gettype()->getbytes(bitread,globalmodel,env);
    }
  }
  if (index!=NULL||lindex!=NULL) {
    if (lindex!=NULL)
      index=evaluateexpr(globalmodel,lindex,env,true,true);
    if(typeoffield->gettype()==TTYPE_BIT) {
      offset+=(index->intvalue())/8;
      bitoffset=(index->intvalue())%8;
    } else {
      int size=typeoffield->basesize(bitread,globalmodel,env);
      offset+=size*index->intvalue();
    }
    if (lindex!=NULL)
      delete(index);
  }
  void *addr=(void *)(((char *) element->getobject())+offset);

  if (typeoffield->isptr())
    addr=*((void **)addr);
  switch(typeoffield->gettype()) {
  case TTYPE_INT:
    {
      *((int *) addr)=target->intvalue();
      break;
    }
  case TTYPE_SHORT:
    {
      *((short *) addr)=target->getshortvalue();
      break;
    }
  case TTYPE_BIT:
    {
      char c=*((char *) addr);
      bool b=target->getboolvalue();
      char mask=0xFF^(1<<bitoffset);
      if (b)
	c=(c&mask)|(1<<bitoffset);
      else
	c=c&mask;
      *((char *)addr)=c;
      break;
    }
  case TTYPE_BYTE:
    {
      *((char *)addr)=target->getbytevalue();
      break;
    }
  case TTYPE_STRUCT:
    {
      assert(typeoffield->isptr());
      *((void **)(((char *) element->getobject())+offset))=target->getobject();
      break;
    }
  }
}

