// evaluates model definition rules

#include <stdlib.h>
#include <assert.h>
#include <stdio.h>

#include "processabstract.h"
#include "processconcrete.h"
#include "amodel.h"
#include "omodel.h"
#include "dmodel.h"
#include "Hashtable.h"
#include "element.h"
#include "common.h"
#include "bitreader.h"
#include "model.h"
#include "set.h"
#include "Relation.h"
#include "tmap.h"



// class processabstract

processabstract::processabstract(model *m) {
  globalmodel=m;
  br=new bitreader(globalmodel,m->gethashtable());
  dirtyflag=false;
}

bool processabstract::dirtyflagstatus() {
  return dirtyflag;
}

void processabstract::setclean() {
  dirtyflag=false;
}


bool processabstract::evaluatestatementa(Statementa *sa, Hashtable *env) {
  switch(sa->gettype()) {
  case STATEMENTA_OR:
    return evaluatestatementa(sa->getleft(),env)||evaluatestatementa(sa->getright(),env);
  case STATEMENTA_AND:
    return evaluatestatementa(sa->getleft(),env)&&evaluatestatementa(sa->getright(),env);
  case STATEMENTA_NOT:
    return !evaluatestatementa(sa->getleft(),env);
  case STATEMENTA_EQUALS: {
    Element *left=evaluateexpr(globalmodel,sa->getleftee(),env,true,true);
    Element *right=evaluateexpr(globalmodel,sa->getrightee(),env,true,true);
    bool tvalue=left->equals(right);
    delete(left);
    delete(right);
    return tvalue;
  }
  case STATEMENTA_SET: {
    Element *left=evaluateexpr(globalmodel,sa->getleftee(),env,true,true);
    Set *set=sa->getset();
    if (set->gettype()==SET_label) {
      DomainSet *ds=globalmodel->getdomainrelation()->getset(set->getname());
      if (ds->getset()->contains(left)) {
	delete(left);
	return true;
      } else {
	delete(left);
	return false;
      }
    } else if (set->gettype()==SET_literal) {
      for(int j=0;j<set->getnumliterals();j++) {
	Literal *l=set->getliteral(j);
	switch(l->gettype()) {
	case LITERAL_NUMBER:
	  if(left->isnumber()&&
	     left->intvalue()==l->number()) {
	    delete(left);
	    return true;
	  }
	case LITERAL_TOKEN:
	  if((left->type()==ELEMENT_TOKEN)&&
	     equivalentstrings(left->gettoken(),l->token())) {
	      delete(left);
	      return true;
	    }
	}
      }
      delete(left);
      return false;
    }
  }
  case STATEMENTA_VALID: {
    Element *left=evaluateexpr(globalmodel,sa->getleftee(),env,true,true);
    if (left->type()!=ELEMENT_OBJECT) {
      printf("ERROR in processabstract.cc\n");
    }
    if (left->getobject()==NULL) {
      delete(left);
      return new Element(false);
    }
    char *structuretype=sa->getvalidtype();
    structure *st=(structuretype==NULL)?left->getstructure():globalmodel->getstructure(structuretype);
    bool validity=globalmodel->gettypemap()->istype(left->getobject(),st);
    delete(left);
    return new Element(validity);
  }

  case STATEMENTA_LT: {
    Element *left=evaluateexpr(globalmodel,sa->getleftee(),env,true,true);
    Element *right=evaluateexpr(globalmodel,sa->getrightee(),env,true,true);
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


/* a Statementb is of the type "E in S" or "<E,E> in R" so we just add the 
   respective element to S or R if the statement is not satisfied */
void processabstract::satisfystatementb(Statementb *sb, Hashtable *env) {
  switch(sb->gettype()) {
  case STATEMENTB_SING: {
    Element *ele=evaluateexpr(globalmodel,sb->getleft(),env,true,true);
    /*if (sb->gettleft()!=NULL) {
      Element **earray=new Element *[sb->gettleft()->getnumexpr()];
      for(int i=0;i<sb->gettleft()->getnumexpr();i++) {
	earray[i]=evaluateexpr(br,sb->gettleft()->getexpr(i),env);
      }
      ele->setnewparams(earray,sb->gettleft()->getnumexpr());
      }*/
    if (ele==NULL)
      break;
    if (!globalmodel->getdomainrelation()->getset(sb->getsetlabel()->getname())->getset()->contains(ele)) {
      dirtyflag=true;
#ifdef DEBUGMANYMESSAGES
      printf("element: ");
      ele->print();
      printf(" into %s\n",sb->getsetlabel()->getname());
#endif
      globalmodel->getdomainrelation()->abstaddtoset(ele,globalmodel->getdomainrelation()->getset(sb->getsetlabel()->getname()),globalmodel);
    } else {
      delete(ele);
    }
    break;
  }
  case STATEMENTB_TUPLE:{
    Element *left=evaluateexpr(globalmodel,sb->getleft(),env,true,true);
    if (left==NULL)
      break;
    Element *right=evaluateexpr(globalmodel,sb->getright(),env,true,true);    
    if (right==NULL) {
      delete(left);
      break;
    }
    /*    if (sb->gettleft()!=NULL) {
      Element **earray=new Element *[sb->gettleft()->getnumexpr()];
      for(int i=0;i<sb->gettleft()->getnumexpr();i++) {
	earray[i]=evaluateexpr(br,sb->gettleft()->getexpr(i),env);
      }
      left->setnewparams(earray,sb->gettleft()->getnumexpr());
      }*/
    /*    if (sb->gettright()!=NULL) {
      Element **earray=new Element *[sb->gettright()->getnumexpr()];
      for(int i=0;i<sb->gettright()->getnumexpr();i++) {
	earray[i]=evaluateexpr(br,sb->gettright()->getexpr(i),env);
      }
      right->setnewparams(earray,sb->gettright()->getnumexpr());
      }*/
    if (!globalmodel->getdomainrelation()->getrelation(sb->getsetlabel()->getname())->getrelation()->contains(left,right)) {
      dirtyflag=true;
#ifdef DEBUGMANYMESSAGES
      printf("element: <");
      left->print();
      printf(",");
      right->print();
      printf("> into %s\n",sb->getsetlabel()->getname());
#endif
      globalmodel->getdomainrelation()->getrelation(sb->getsetlabel()->getname())->getrelation()->put(left,right);
    } else {
      delete(left);
      delete(right);
    }
    break;
  }
  }
}


void processabstract::processrule(Rule *r) {
  State *st=new State(r, globalmodel->gethashtable());
  if (st->initializestate(br, globalmodel)) {
    while(true) {
      if (evaluatestatementa(r->getstatementa(),st->env))
	satisfystatementb(r->getstatementb(),st->env);

      if (!st->increment(br, globalmodel))
	break; /* done */
    }
  }
  delete(st);
}

void processabstract::processrule(Rule *r, Element *ele, char *set) {
  int count=-1;


  for(int i=0;i<r->numquants();i++) {
    AQuantifier *aq=r->getquant(i);
    switch(aq->gettype()) {
    case AQUANTIFIER_SING:
      count=i;
      break;
    default:
      break;
    }
  }

  AQuantifier *aq=r->getquant(count);
  if (!equivalentstrings(aq->getset()->getname(),set))
    return;

  Hashtable *env=new Hashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);
  env->setparent(globalmodel->gethashtable());
    
  RelationSet **relset=new RelationSet*[r->numquants()-1];
  int c=0;
  for(int i=0;i<r->numquants();i++) {
    if (i!=count) {
      AQuantifier *aq=r->getquant(i);
      RelationSet *rs=new RelationSet(aq->getleft()->label(),aq->getlower(),aq->getupper());
      rs->incrementassignment(br,env,globalmodel);
      relset[c++]=rs;
    }
  }
  
  env->put(aq->getleft()->label(),ele);
  bool flag=true;
  while(flag) {
    if (evaluatestatementa(r->getstatementa(),env))
      satisfystatementb(r->getstatementb(),env);
    int i=r->numquants()-2;
    for(;i>=0;i--) {
      if (relset[i]->incrementassignment(br,env,globalmodel)) {
	break;
      } else {
	relset[i]->resetassignment(env);
	if (!relset[i]->incrementassignment(br,env,globalmodel)) {
	  flag=false;
	  break;
	}
      }
    }
    if (i==-1)
      flag=false;
  }
  for(int i=0;i<r->numquants()-1;i++) {
    delete(relset[i]);
  }
  delete(relset);
  delete(env);
}

processabstract::~processabstract() {
  delete(br);
}





// class State

State::State(Rule *r, Hashtable *oldenv) {
  env=new Hashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);;
  env->setparent(oldenv);
  numrelset=r->numquants();
  relset=new RelationSet*[numrelset];
  for(int i=0;i<r->numquants();i++) {
    AQuantifier *aq=r->getquant(i);

    switch(aq->gettype()) {
    case AQUANTIFIER_SING:
      relset[i]=new RelationSet(aq->getset(),aq->getleft()->label(),aq->gettleft());
      break;
    case AQUANTIFIER_TUPLE:
      relset[i]=new RelationSet(aq->getset(),aq->getleft()->label(),aq->gettleft(),aq->getright()->label(),aq->gettright());
      break;
    case AQUANTIFIER_RANGE:
      relset[i]=new RelationSet(aq->getleft()->label(),aq->getlower(),aq->getupper());
      break;
    }
  }
}



State::State(Constraint *c,Hashtable *oldenv) {
  env=new Hashtable((unsigned int (*)(void *)) & hashstring,(int (*)(void *,void *)) &equivalentstrings);;
  env->setparent(oldenv);
  numrelset=c->numquants();
  relset=new RelationSet*[numrelset];
  for(int i=0;i<c->numquants();i++) {
    Quantifier *q=c->getquant(i);
    relset[i]=new RelationSet(q->getset(),q->getlabel()->label(),NULL);
  }
}



State::~State() {
  delete(env);
  for(int i=0;i<numrelset;i++)
    delete(relset[i]);
  delete[](relset);
}


bool State::initializestate(bitreader *br, model * m) {
  for(int i=0;i<numrelset;i++) {
    if (!relset[i]->incrementassignment(br,env,m))
      return false;
  }
  return true;
}


/* initializes all quantifiers of this constraint and returns false
   if there exists a quantifier that cannot be initialized */
bool State::initializestate(model * m) {
  for(int i=0;i<numrelset;i++) {
    if (!relset[i]->incrementassignment(env,m))
      return false;
  }
  return true;
}

bool State::initializestate(processconcrete *pc,model * m) {
  for(int i=0;i<numrelset;i++) {
    if (!relset[i]->incrementassignment(pc,env,m))
      return false;
  }
  return true;
}

bool State::increment(bitreader *br, model *m) {
  for(int i=numrelset-1;i>=0;i--) {
      if (relset[i]->incrementassignment(br,env,m))
	return true;
      else {
	relset[i]->resetassignment(env);
	if (!relset[i]->incrementassignment(br,env,m))
	  return false;
      }
  }
  return false;
}

bool State::increment(model *m) {
  for(int i=numrelset-1;i>=0;i--) {
      if (relset[i]->incrementassignment(env,m))
	return true;
      else {
	relset[i]->resetassignment(env);
	if (!relset[i]->incrementassignment(env,m))
	  return false;
      }
  }
  return false;
}

bool State::increment(processconcrete *pc,model *m) {
  for(int i=numrelset-1;i>=0;i--) {
      if (relset[i]->incrementassignment(pc,env,m))
	return true;
      else {
	relset[i]->resetassignment(env);
	if (!relset[i]->incrementassignment(pc,env,m))
	  return false;
      }
  }
  return false;
}



Element * evaluateexpr(model *m,AElementexpr *ee, Hashtable *env, bool enforcetyping, bool compute) {
  bitreader *br=m->getbitreader();
  switch(ee->gettype()) {
  case AELEMENTEXPR_NULL:
    return new Element();
  case AELEMENTEXPR_LABEL: {
    Element *r=new Element((Element *)env->get(ee->getlabel()->label()));
    typemap *tm=m->gettypemap();
    if (compute)
      if(r->type()==ELEMENT_OBJECT&&
	 r->getobject()!=NULL&&
	 !tm->asserttype(r->getobject(),r->getstructure())) {
	delete(r);
	return NULL;
      }
    return r;
  }
  case AELEMENTEXPR_SUB: {
    AElementexpr *left=ee->getleft();
    AElementexpr *right=ee->getright();
    Element *leftval=evaluateexpr(m,left,env,true,compute);
    if (leftval==NULL)
      return NULL;
    Element *rightval=evaluateexpr(m,right,env,true,compute);
    if (rightval==NULL) {
      delete(leftval);
      return NULL;
    }
    Element *diff=new Element(leftval->intvalue()-rightval->intvalue());
    delete(leftval);delete(rightval);
    return diff;
  }
  case AELEMENTEXPR_ADD: {
    AElementexpr *left=ee->getleft();
    AElementexpr *right=ee->getright();
    Element *leftval=evaluateexpr(m,left,env,true,compute);
    if (leftval==NULL)
      return NULL;
    Element *rightval=evaluateexpr(m,right,env,true,compute);
    if (rightval==NULL) {
      delete(leftval);
      return NULL;
    }
    Element *sum=new Element(leftval->intvalue()+rightval->intvalue());
    delete(leftval);delete(rightval);
    return sum;
  }
  case AELEMENTEXPR_MULT: {
    AElementexpr *left=ee->getleft();
    AElementexpr *right=ee->getright();
    Element *leftval=evaluateexpr(m,left,env,true,compute);
    if (leftval==NULL)
      return NULL;
    Element *rightval=evaluateexpr(m,right,env,true,compute);
    if (rightval==NULL) {
      delete(leftval);
      return NULL;
    }
    Element *diff=new Element(leftval->intvalue()*rightval->intvalue());
    delete(leftval);delete(rightval);
    return diff;
  }
  case AELEMENTEXPR_DIV: {
    AElementexpr *left=ee->getleft();
    AElementexpr *right=ee->getright();
    Element *leftval=evaluateexpr(m,left,env,true,compute);
    if (leftval==NULL)
      return NULL;
    Element *rightval=evaluateexpr(m,right,env,true,compute);
    if (rightval==NULL) {
      delete(leftval);
      return NULL;
    }
    Element *diff=new Element(leftval->intvalue()/rightval->intvalue());
    delete(leftval);delete(rightval);
    return diff;
  }
  case AELEMENTEXPR_LIT: {
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
  case AELEMENTEXPR_FIELD: {
    Element *e=evaluateexpr(m,ee->getleft(),env,true,compute);
    if (e==NULL)
      return NULL;
    Element *r=br->readfieldorarray(e,ee->getfield(),NULL);
    delete(e);
    if (r==NULL)
      return NULL;
    if (enforcetyping&&compute) {
      typemap *tm=m->gettypemap();
      if(r->type()==ELEMENT_OBJECT&&
	 r->getobject()!=NULL&&
	 !tm->asserttype(r->getobject(),r->getstructure())) {
	delete(r);
	return NULL;
      }
    } else if (compute) {
      typemap *tm=m->gettypemap();
      if(r->type()==ELEMENT_OBJECT&&
	 r->getobject()!=NULL&&
	 !tm->istype(r->getobject(),r->getstructure())) {
	delete(r);
	return NULL;
      }
    }
    return r;
  }
  case AELEMENTEXPR_CAST: {
    Element *e=evaluateexpr(m,ee->getleft(),env,true,compute);
    if (e==NULL)
      return NULL;
    typemap *tm=m->gettypemap();
    if (e->getobject()!=NULL&&compute&&
	!tm->asserttype(e->getobject(),m->getstructure(ee->getcasttype()))) {
      delete(e);
      return NULL;
    }
    Element *r=new Element(e->getobject(),m->getstructure(ee->getcasttype()));
    delete(e);
    return r;
  }
  case AELEMENTEXPR_FIELDARRAY: {
    Element *e=evaluateexpr(m,ee->getleft(),env,true,compute);
    if (e==NULL)
      return NULL;
    Element *ind=evaluateexpr(m,ee->getright(),env,true,compute);
    if (ind==NULL) {
      delete(e);
      return NULL;
    }
    Element *r=br->readfieldorarray(e,ee->getfield(),ind);
    delete(ind);
    delete(e);
    if (r==NULL)
      return NULL;
    if (enforcetyping&&compute) {
      typemap *tm=m->gettypemap();
      if(r->type()==ELEMENT_OBJECT&&
	 r->getobject()!=NULL&&
	 !tm->asserttype(r->getobject(),r->getstructure())) {
	delete(r);
	return NULL;
      }
    } else if (compute) {
      typemap *tm=m->gettypemap();
      if(r->type()==ELEMENT_OBJECT&&
	 r->getobject()!=NULL&&
	 !tm->istype(r->getobject(),r->getstructure())) {
	delete(r);
	return NULL;
      }
    }
    return r;
  }
  }
}


// prints the current state
void State::print(model *m) 
{
  for(int i=0; i<numrelset; i++) 
    {
      relset[i]->print(env, m); 
      printf(" ");
    }
}




// class RelationSet

RelationSet::RelationSet(Set *s, char *l,Type *tl) {
  set=s;
  type=TYPE_SET;
  left=l;
  tleft=tl;
  tright=NULL;
  right=NULL;
}

RelationSet::RelationSet(Set *s,char *l, Type *tl,char *r,Type *tr) {
  set=s;
  type=TYPE_RELATION;
  left=l;
  tleft=tl;
  tright=tr;
  right=r;
}

RelationSet::RelationSet(char *l,AElementexpr *lower,AElementexpr*upper) {
  this->lower=lower;
  this->upper=upper;
  left=l;
  type=TYPE_RANGE;
}

int RelationSet::gettype() {
  return type;
}

void RelationSet::resetassignment(Hashtable *env) {
  switch(type) {
  case TYPE_SET:
    env->remove(left);
    break;
  case TYPE_RELATION:
    env->remove(left);
    env->remove(right);
    break;
  case TYPE_RANGE:
    env->remove(left);
    break;
  }
}

bool RelationSet::incrementassignment(bitreader *br,Hashtable *env, model *m) {
  switch(type) {
  case TYPE_SET: {
    if (set->gettype()==SET_label) {
      DomainSet *ds=m->getdomainrelation()->getset(set->getname());

      if (!env->contains(left)) {
	Element *fele=(Element *)ds->getset()->firstelement();
	if (fele==NULL)
	  return false;
	env->put(left,fele);
	/*	if (tleft!=NULL) {
	  assert(tleft->getnumlabels()==ele->getnumparams());
	  for (int i=0;i<tleft->getnumlabels();i++) {
	    env->put(tleft->getlabel(i)->label(),ele->paramvalue(i));
	  }
	  }*/
	return true;
      }
      Element *ele=(Element *)env->get(left);
      Element *nextele=(Element *)ds->getset()->getnextelement(ele);
      if (nextele==NULL)
	return false;
      else {
	/*	if (tleft!=NULL) {
	  assert(tleft->getnumlabels()==nextele->getnumparams());
	  for (int i=0;i<tleft->getnumlabels();i++) {
	    env->put(tleft->getlabel(i)->label(),nextele->paramvalue(i));
	  }
	  }*/
	env->put(left,nextele);
	return true;
      }
    } else if(set->gettype()==SET_literal) {

      if (!env->contains(left)) {
	Literal *l=set->getliteral(0);
	switch(l->gettype()) {
	case LITERAL_NUMBER:
	  env->put(left,new Element(l->number()));
	  break;
	case LITERAL_TOKEN:
	  env->put(left,new Element(l->token()));
	  break;
	}
	return true;
      }
      Element *ele=(Element *)env->get(left);
      for(int j=0;j<set->getnumliterals();j++) {
	Literal *l=set->getliteral(j);
	switch(l->gettype()) {
	case LITERAL_NUMBER:
	  if(ele->isnumber()&&
	    ele->intvalue()==l->number()) {
	    if ((j+1)<set->getnumliterals()) {
	      env->put(left,new Element(set->getliteral(j+1)->number()));
	      delete(ele);
	      return true;
	    }
	    else return false;
	  }
	case LITERAL_TOKEN:
	  if((ele->type()==ELEMENT_TOKEN)&&
	    equivalentstrings(ele->gettoken(),l->token())) {
	    if ((j+1)<set->getnumliterals()) {
	      env->put(left,new Element(set->getliteral(j+1)->token()));
	      delete(ele);
	      return true;
	    }
	    else return false;
	  }
	}
      }
    }
  }
  case TYPE_RELATION: {
    DRelation *dr=m->getdomainrelation()->getrelation(set->getname());
    Element *eleleft=(Element *)env->get(left);
    Element *eleright=(Element *)env->get(right);
    if ((eleleft==NULL)||(eleright==NULL)) {
      if((eleleft!=NULL)||(eleright!=NULL)) {
	printf("ERROR in TYPE_RELATION in processabstract.cc\n");
	exit(-1);
      }
      Tuple t=dr->getrelation()->firstelement();
      if (t.isnull())
	return false;
      env->put(left,t.left);
      env->put(right,t.right);
      /*      if (tleft!=NULL) {
	assert(tleft->getnumlabels()==((Element *)t.left)->getnumparams());
	for (int i=0;i<tleft->getnumlabels();i++) {
	  env->put(tleft->getlabel(i)->label(),((Element *)t.left)->paramvalue(i));
	}
	}*/
      /*      if (tright!=NULL) {
	assert(tright->getnumlabels()==((Element *)t.right)->getnumparams());
	for (int i=0;i<tright->getnumlabels();i++) {
	  env->put(tright->getlabel(i)->label(),((Element *)t.right)->paramvalue(i));
	}
	}*/

      return true;
    }
    Tuple nextele=dr->getrelation()->getnextelement(eleleft,eleright);
    if (nextele.isnull())
      return false;
    else {
      env->put(left,nextele.left);
      env->put(right,nextele.right);
      /*      if (tleft!=NULL) {
	assert(tleft->getnumlabels()==((Element *)nextele.left)->getnumparams());
	for (int i=0;i<tleft->getnumlabels();i++) {
	  env->put(tleft->getlabel(i)->label(),((Element *)nextele.left)->paramvalue(i));
	}
	}*/
      /*      if (tright!=NULL) {
	assert(tright->getnumlabels()==((Element *)nextele.right)->getnumparams());
	for (int i=0;i<tright->getnumlabels();i++) {
	  env->put(tright->getlabel(i)->label(),((Element *)nextele.right)->paramvalue(i));
	}
	}*/
      return true;
    }
  }
  case TYPE_RANGE: {

    if (!env->contains(left)) {
      Element *lowerele=(Element *)evaluateexpr(m,lower,env,true,true);
      env->put(left,lowerele);
      return true;
    }
    Element *val=(Element *)env->get(left);
    Element *upperele=evaluateexpr(m,upper,env,true,true);
    if (val->intvalue()>=upperele->intvalue()) {
      delete(upperele);
      return false;
    } else {
      Element *nval=new Element(val->intvalue()+1);
      env->put(left,nval);
      delete(val);
      return true;
    }
  }
  }
}

bool RelationSet::incrementassignment(processconcrete *pc,Hashtable *env, model *m) {
  switch(type) {
  case TYPE_SET: {
    if (set->gettype()==SET_label) {
      DomainSet *ds=m->getdomainrelation()->getset(set->getname());

      if (!env->contains(left)) {
	Element *fele=(Element *)ds->getset()->firstelement();
	if (fele==NULL)
	  return false;
	env->put(left,fele);
	/*	if (tleft!=NULL) {
	  assert(tleft->getnumlabels()==ele->getnumparams());
	  for (int i=0;i<tleft->getnumlabels();i++) {
	    env->put(tleft->getlabel(i)->label(),ele->paramvalue(i));
	  }
	  }*/
	return true;
      }
      Element *ele=(Element *)env->get(left);
      Element *nextele=(Element *)ds->getset()->getnextelement(ele);
      if (nextele==NULL)
	return false;
      else {
	/*	if (tleft!=NULL) {
	  assert(tleft->getnumlabels()==nextele->getnumparams());
	  for (int i=0;i<tleft->getnumlabels();i++) {
	    env->put(tleft->getlabel(i)->label(),nextele->paramvalue(i));
	  }
	  }*/
	env->put(left,nextele);
	return true;
      }
    } else if(set->gettype()==SET_literal) {

      if (!env->contains(left)) {
	Literal *l=set->getliteral(0);
	switch(l->gettype()) {
	case LITERAL_NUMBER:
	  env->put(left,new Element(l->number()));
	  break;
	case LITERAL_TOKEN:
	  env->put(left,new Element(l->token()));
	  break;
	}
	return true;
      }
      Element *ele=(Element *)env->get(left);
      for(int j=0;j<set->getnumliterals();j++) {
	Literal *l=set->getliteral(j);
	switch(l->gettype()) {
	case LITERAL_NUMBER:
	  if(ele->isnumber()&&
	    ele->intvalue()==l->number()) {
	    if ((j+1)<set->getnumliterals()) {
	      env->put(left,new Element(set->getliteral(j+1)->number()));
	      delete(ele);
	      return true;
	    }
	    else return false;
	  }
	case LITERAL_TOKEN:
	  if((ele->type()==ELEMENT_TOKEN)&&
	    equivalentstrings(ele->gettoken(),l->token())) {
	    if ((j+1)<set->getnumliterals()) {
	      env->put(left,new Element(set->getliteral(j+1)->token()));
	      delete(ele);
	      return true;
	    }
	    else return false;
	  }
	}
      }
    }
  }
  case TYPE_RELATION: {
    DRelation *dr=m->getdomainrelation()->getrelation(set->getname());
    Element *eleleft=(Element *)env->get(left);
    Element *eleright=(Element *)env->get(right);
    if ((eleleft==NULL)||(eleright==NULL)) {
      if((eleleft!=NULL)||(eleright!=NULL)) {
	printf("ERROR in TYPE_RELATION in processabstract.cc\n");
	exit(-1);
      }
      Tuple t=dr->getrelation()->firstelement();
      if (t.isnull())
	return false;
      env->put(left,t.left);
      env->put(right,t.right);
      /*      if (tleft!=NULL) {
	assert(tleft->getnumlabels()==((Element *)t.left)->getnumparams());
	for (int i=0;i<tleft->getnumlabels();i++) {
	  env->put(tleft->getlabel(i)->label(),((Element *)t.left)->paramvalue(i));
	}
	}*/
      /*      if (tright!=NULL) {
	assert(tright->getnumlabels()==((Element *)t.right)->getnumparams());
	for (int i=0;i<tright->getnumlabels();i++) {
	  env->put(tright->getlabel(i)->label(),((Element *)t.right)->paramvalue(i));
	}
	}*/

      return true;
    }
    Tuple nextele=dr->getrelation()->getnextelement(eleleft,eleright);
    if (nextele.isnull())
      return false;
    else {
      env->put(left,nextele.left);
      env->put(right,nextele.right);
      /*      if (tleft!=NULL) {
	assert(tleft->getnumlabels()==((Element *)nextele.left)->getnumparams());
	for (int i=0;i<tleft->getnumlabels();i++) {
	  env->put(tleft->getlabel(i)->label(),((Element *)nextele.left)->paramvalue(i));
	}
	}*/
      /*      if (tright!=NULL) {
	assert(tright->getnumlabels()==((Element *)nextele.right)->getnumparams());
	for (int i=0;i<tright->getnumlabels();i++) {
	  env->put(tright->getlabel(i)->label(),((Element *)nextele.right)->paramvalue(i));
	}
	}*/
      return true;
    }
  }
  case TYPE_RANGE: {

    if (!env->contains(left)) {
      env->put(left,pc->evaluateexpr((CAElementexpr *)lower, env));
      return true;
    }
    Element *val=(Element *)env->get(left);
    Element *upperele=pc->evaluateexpr((CAElementexpr *)upper,env);
    if (val->intvalue()>=upperele->intvalue()) {
      delete(upperele);
      return false;
    } else {
      Element *nval=new Element(val->intvalue()+1);
      env->put(left,nval);
      delete(val);
      return true;
    }
  }
  }
}


/* increments the value of "left" and returns "false" if this is not possible.
   When this method is called for the first time, it simply initializes
   the value of the quantifier ("left") */
bool RelationSet::incrementassignment(Hashtable *env, model *m) {
  switch(type) {
  case TYPE_SET: {
    if (set->gettype()==SET_label) {
      DomainSet *ds=m->getdomainrelation()->getset(set->getname());

      Element *ele=NULL;
      if (!env->contains(left)) {
	ele=(Element *)ds->getset()->firstelement();
	if (ele==NULL)
	  return false;
	env->put(left,ele);
	/*	if (tleft!=NULL) {
	  assert(tleft->getnumlabels()==ele->getnumparams());
	  for (int i=0;i<tleft->getnumlabels();i++) {
	    env->put(tleft->getlabel(i)->label(),ele->paramvalue(i));
	  }
	  }*/
	return true;
      }

      ele=(Element *) env->get(left);
      Element *nextele=(Element *)ds->getset()->getnextelement(ele);
      if (nextele==NULL)
	return false;
      else {
	/*	if (tleft!=NULL) {
	  assert(tleft->getnumlabels()==nextele->getnumparams());
	  for (int i=0;i<tleft->getnumlabels();i++) {
	    env->put(tleft->getlabel(i)->label(),nextele->paramvalue(i));
	  }
	  }*/
	env->put(left,nextele);
	return true;
      }
    }
  }
  }
}


// prints the quantifier and its current state
void RelationSet::print(Hashtable *env, model *m)
{
  switch(type) {
  case TYPE_SET: {
    if (set->gettype()==SET_label) 
      {
	printf("%s=", left);
	DomainSet *ds=m->getdomainrelation()->getset(set->getname());
	Element *ele = (Element *) env->get(left);
	ele->print();
	printf("\n");
      }
  }}
}
