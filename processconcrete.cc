#include <stdlib.h>
#include <assert.h>
#include "processconcrete.h"
#include "processabstract.h"
#include "amodel.h"
#include "omodel.h"
#include "dmodel.h"
#include "cmodel.h"
#include "Hashtable.h"
#include "element.h"
#include "common.h"
#include "bitreader.h"
#include "bitwriter.h"
#include "model.h"
#include "set.h"
#include "Relation.h"
static int concretecheck=0;
static int concretetrigger=0;

processconcrete::processconcrete(model *m) {
  globalmodel=m;
  bw=new bitwriter(globalmodel,m->gethashtable());
  br=new bitreader(globalmodel,m->gethashtable());
}

void processconcrete::printstats() {
  printf("Concretization Rules Checked: %d Triggered: %d\n",concretecheck,concretetrigger);
}


void processconcrete::processrule(Rule *r) {
  State *st=new State(r,globalmodel->gethashtable());
  if (st->initializestate(this, globalmodel)) {
    while(true) {
      concretecheck++;
      if (evaluatestatementa(r->getstatementa(),st->env)) {
	concretetrigger++;
	satisfystatementb((CStatementb *)r->getstatementb(),st->env);
      }

      if (!st->increment(this, globalmodel))
	break; /* done */
    }
  }
  delete(st);
}

processconcrete::~processconcrete() {
  delete(br);
}

bool processconcrete::evaluatestatementa(Statementa *sa, Hashtable *env) {
  switch(sa->gettype()) {
  case STATEMENTA_OR:
    return evaluatestatementa(sa->getleft(),env)||evaluatestatementa(sa->getright(),env);
  case STATEMENTA_AND:
    return evaluatestatementa(sa->getleft(),env)&&evaluatestatementa(sa->getright(),env);
  case STATEMENTA_NOT:
    return !evaluatestatementa(sa->getleft(),env);
  case STATEMENTA_EQUALS: {
    Element *left=evaluateexpr((CAElementexpr *)sa->getleftee(),env);
    Element *right=evaluateexpr((CAElementexpr *)sa->getrightee(),env);
    bool tvalue=left->equals(right);
    delete(left);
    delete(right);
    return tvalue;
  }
  case STATEMENTA_LT: {
    Element *left=evaluateexpr((CAElementexpr *)sa->getleftee(),env);
    Element *right=evaluateexpr((CAElementexpr *)sa->getrightee(),env);
    if (!left->isnumber()||
	!right->isnumber()) {
      printf("Bad lt compare\n");
      exit(-1);
    }
    bool tvalue=left->intvalue()<right->intvalue();
    delete(left);
    delete(right);
    return tvalue;
  }
  case STATEMENTA_TRUE:
    return true;
  }
}

void processconcrete::satisfystatementb(CStatementb *sb, Hashtable *env) {
  Element * rvalue=evaluateexpr((CAElementexpr *)sb->getright(),env);
  Element *index=NULL;
  if (sb->gettype()==CSTATEMENTB_ARRAYASSIGN)
    index=evaluateexpr((CAElementexpr *)sb->getleft(),env);
  Field *field=sb->getfield();
  Element *src=evaluateexpr(sb->getexpr(),env);
  bw->writefieldorarray(src,field,index,rvalue);
  delete(rvalue);
  if (index!=NULL)
    delete(index);
  delete(src);
}

Element * processconcrete::evaluateexpr(Expr *e, Hashtable *env) {
  switch(e->gettype()) {
  case EXPR_LABEL:
    return new Element((Element *)env->get(e->getlabel()->label()));
  case EXPR_FIELD: {
    Element *old=evaluateexpr(e->getexpr(),env);
    Element *newe=br->readfieldorarray(evaluateexpr(e->getexpr(),env),e->getfield(),NULL);
    delete(old);
    return newe;
  }
  case EXPR_CAST: {
    Element *old=evaluateexpr(e->getexpr(),env);
    char *type=e->getcasttype();
    structure *st=globalmodel->getstructure(type);
    Element *newe=new Element(old->getobject(),st);
    delete(old);
    return newe;
  }
  case EXPR_ARRAY: {
    Element *old=evaluateexpr(e->getexpr(),env);
    Element *index=evaluateexpr(e->getindex(),env);
    Element *newe=br->readfieldorarray(evaluateexpr(e->getexpr(),env),e->getfield(),index);
    delete(old);
    delete(index);
    return newe;
  }
  }
}

Element * processconcrete::evaluateexpr(CAElementexpr *ee, Hashtable *env) {
  switch(ee->gettype()) {
  case CAELEMENTEXPR_LABEL:
    {
      return new Element((Element *)env->get(ee->getlabel()->label()));
    }
  case CAELEMENTEXPR_NULL:
    {
      return new Element();
    }
  case CAELEMENTEXPR_SUB:
    {
      CAElementexpr *left=ee->getleft();
      CAElementexpr *right=ee->getright();
      Element *leftval=evaluateexpr(left,env);
      Element *rightval=evaluateexpr(right,env);
      Element *diff=new Element(leftval->intvalue()-rightval->intvalue());
      delete(leftval);delete(rightval);
      return diff;
    }
  case CAELEMENTEXPR_ADD:
    {
      CAElementexpr *left=ee->getleft();
      CAElementexpr *right=ee->getright();
      Element *leftval=evaluateexpr(left,env);
      Element *rightval=evaluateexpr(right,env);
      Element *sum=new Element(leftval->intvalue()+rightval->intvalue());
      delete(leftval);delete(rightval);
      return sum;
    }
  case CAELEMENTEXPR_MULT:
    {
      CAElementexpr *left=ee->getleft();
      CAElementexpr *right=ee->getright();
      Element *leftval=evaluateexpr(left,env);
      Element *rightval=evaluateexpr(right,env);
      Element *diff=new Element(leftval->intvalue()*rightval->intvalue());
      delete(leftval);delete(rightval);
      return diff;
    }
  case CAELEMENTEXPR_DIV:
    {
      CAElementexpr *left=ee->getleft();
      CAElementexpr *right=ee->getright();
      Element *leftval=evaluateexpr(left,env);
      Element *rightval=evaluateexpr(right,env);
      Element *diff=new Element(leftval->intvalue()/rightval->intvalue());
      delete(leftval);delete(rightval);
      return diff;
    }
  case CAELEMENTEXPR_LIT:
    {
      Literal *l=ee->getliteral();
      switch(l->gettype()) {
      case LITERAL_NUMBER:
	return new Element(l->number());
      case LITERAL_TOKEN:
	return new Element(copystr(l->token()));
      case LITERAL_BOOL:
	return new Element(l->getbool());
      default:
	printf("ERROR with lit type\n");
	exit(-1);
      }
    }
  case CAELEMENTEXPR_SIZEOF:
    {
    Setexpr * setexpr=ee->getsetexpr();
    switch(setexpr->gettype()) {
    case SETEXPR_LABEL:
      return new Element(globalmodel->getdomainrelation()->getset(setexpr->getsetlabel()->getname())->getset()->size());
    case SETEXPR_REL: {
      Element *key=(Element *)env->get(setexpr->getlabel()->label());
      WorkSet *ws=globalmodel->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->getset(key);
      if (ws==NULL)
	return new Element(0);
      else
	return new Element(ws->size());
    }
    case SETEXPR_INVREL: {
      Element *key=(Element *)env->get(setexpr->getlabel()->label());
      WorkSet *ws=globalmodel->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->invgetset(key);
      if (ws==NULL)
	return new Element(0);
      else
	return new Element(ws->size());
    }
    }
    }
  case CAELEMENTEXPR_ELEMENT: {
    Element *index=evaluateexpr(ee->getleft(),env);
    int ind=index->intvalue();
    delete(index);

    Setexpr * setexpr=ee->getsetexpr();
    switch(setexpr->gettype()) {
    case SETEXPR_LABEL:
      return new Element((Element *)globalmodel->getdomainrelation()->getset(setexpr->getsetlabel()->getname())->getset()->getelement(ind));
    case SETEXPR_REL: {
      Element *key=(Element *)env->get(setexpr->getlabel()->label());
      return new Element((Element *)globalmodel->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->getset(key)->getelement(ind));
    }
    case SETEXPR_INVREL: {
      Element *key=(Element *)env->get(setexpr->getlabel()->label());
      return new Element((Element *)globalmodel->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->invgetset(key)->getelement(ind));
    }
    }
  }
  case CAELEMENTEXPR_RELATION:
    {
      Element *e=evaluateexpr(ee->getleft(),env);
      Element *r=(Element *)globalmodel->getdomainrelation()->getrelation(ee->getrelation()->getname())->getrelation()->getobj(e);
      return new Element(r);
    }
  }
}
