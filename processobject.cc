// evaluates constraints in the ICL

#include "processobject.h"
#include "processabstract.h"
#include "omodel.h"
#include "Hashtable.h"
#include "element.h"
#include "model.h"
#include "dmodel.h"
#include "set.h"
#include "Relation.h"
#include "repair.h"
#include "normalizer.h"
#include "Action.h"

processobject::processobject(model *m) {
  globalmodel=m;
  repair=m->getrepair();
}


// evaluates the truth value of the given predicate
int processobject::processpredicate(Predicate *p, Hashtable *env) {
  switch(p->gettype()) {
  case PREDICATE_LT: {
    Element *left=evaluatevalueexpr(p->getvalueexpr(),env,globalmodel);
    Element *right=evaluateexpr(p->geteleexpr(),env,globalmodel);
    if (right==NULL) {
      return PFAIL;
    }
    if (left==NULL) {
      delete(right);
      return false;
    }
    int t=left->intvalue()<right->intvalue();
    delete(right);
    return t;
  }
  case PREDICATE_LTE: {
    Element *left=evaluatevalueexpr(p->getvalueexpr(),env,globalmodel);
    Element *right=evaluateexpr(p->geteleexpr(),env,globalmodel);
    if (right==NULL) {return PFAIL;}
    if (left==NULL) {
      delete(right);
      return false;
    }
    bool t=left->intvalue()<=right->intvalue();
    delete(right);
    return t;
  }
  case PREDICATE_EQUALS: {
    Element *left=evaluatevalueexpr(p->getvalueexpr(),env,globalmodel);
    Element *right=evaluateexpr(p->geteleexpr(),env,globalmodel);
    if (right==NULL) {return PFAIL;}
    if (left==NULL) {
      delete(right);
      return false;
    }
    /* Can have more than just int's here */
    bool t=left->equals(right); /*Just ask the equals method*/
    delete(right);
    return t;
  }
  case PREDICATE_GTE: {
    Element *left=evaluatevalueexpr(p->getvalueexpr(),env,globalmodel);
    Element *right=evaluateexpr(p->geteleexpr(),env,globalmodel);
    if (right==NULL) {return PFAIL;}
    if (left==NULL) {
      delete(right);
      return false;
    }
    bool t=left->intvalue()>=right->intvalue();
    delete(right);
    return t;
  }
  case PREDICATE_GT: {
    Element *left=evaluatevalueexpr(p->getvalueexpr(),env,globalmodel);
    Element *right=evaluateexpr(p->geteleexpr(),env,globalmodel);
    if (right==NULL) {return PFAIL;}
    if (left==NULL) {
      delete(right);
      return false;
    }
    bool t=left->intvalue()>right->intvalue();
    delete(right);
    return t;
  }
  case PREDICATE_SET: {
    Label *label=p->getlabel();
    Setexpr * setexpr=p->getsetexpr();
    Element *labelele=(Element *) env->get(label->label());
    switch(setexpr->gettype()) {
      case SETEXPR_LABEL:
	return globalmodel->getdomainrelation()->getset(setexpr->getsetlabel()->getname())->getset()->contains(labelele);
      case SETEXPR_REL:
	return globalmodel->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->getset(setexpr->getlabel()->label())->contains(labelele);
      case SETEXPR_INVREL:
	return globalmodel->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->invgetset(setexpr->getlabel()->label())->contains(labelele);
    }
  }
  case PREDICATE_EQ1:
  case PREDICATE_GTE1: {
    int setsize;
    Setexpr * setexpr=p->getsetexpr();
    switch(setexpr->gettype()) {
    case SETEXPR_LABEL:
      setsize=globalmodel->getdomainrelation()->getset(setexpr->getsetlabel()->getname())->getset()->size();
      break;
    case SETEXPR_REL: {
      Element *key=(Element *)env->get(setexpr->getlabel()->label());
      WorkSet *ws=globalmodel->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->getset(key);
      if (ws!=NULL)
	setsize=ws->size();
      else
	setsize=0;
      break;
    }
    case SETEXPR_INVREL: {
      Element *key=(Element *)env->get(setexpr->getlabel()->label());
      WorkSet *ws=globalmodel->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->invgetset(key);
      if (ws!=NULL)
	setsize=ws->size();
      else
	setsize=0;
      break;
    }
    }
    return ((p->gettype()==PREDICATE_EQ1)&&(setsize==1))||
      ((p->gettype()==PREDICATE_GTE1)&&(setsize>=1));
  }
  }
}


// evaluates the truth value of the given statement
int processobject::processstatement(Statement *s, Hashtable *env) {
  switch (s->gettype()) {
  case STATEMENT_OR: {
    int l=processstatement(s->getleft(),env);
    int r=processstatement(s->getright(),env);
    if (l==PFAIL&&(r==PFAIL||r==PFALSE)) return PFAIL;
    if ((l==PFAIL||l==PFALSE)&&r==PFAIL) return PFAIL;
    return l||r;
  }
  case STATEMENT_AND: {
    int l=processstatement(s->getleft(),env);
    int r=processstatement(s->getright(),env);
    if (l==PFAIL&&(r==PFAIL||r==PTRUE)) return PFAIL;
    if (r==PFAIL&&(l==PFAIL||l==PTRUE)) return PFAIL;
    return l&&r;
  }
  case STATEMENT_NOT: {
    int l=processstatement(s->getleft(),env);
    if (l==PFAIL) return PFAIL;
    return !l;
  }
  case STATEMENT_PRED:
    return processpredicate(s->getpredicate(),env);
  }
}


// returns true if and only if the given constraint is satisfied 
bool processobject::issatisfied(Constraint *c) 
{
  State *st=new State(c,globalmodel->gethashtable());
  bool satisfied = true;
  if (st->initializestate(globalmodel))
    while (true)
      {      
	if (c->getstatement()!=NULL)
	  if (processstatement(c->getstatement(),st->env)!=PTRUE) 
	    satisfied = false;
	if (!st->increment(globalmodel))
	    break;
      }

  delete(st);
  return satisfied;
}



/* processed the given constraint and if it's not satisfied, 
   displays a message and tries to repair the constraint 
   The function returns true only if the constraint was initially
   satisfied. */
bool processobject::processconstraint(Constraint *c) {
  State *st=new State(c,globalmodel->gethashtable());
  bool clean=true;
  if (st->initializestate(globalmodel)) {
    while(true) {
      if (c->getstatement()!=NULL) {
	if (processstatement(c->getstatement(),st->env)!=PTRUE) {
#ifdef DEBUGMESSAGES
	  printf("Predicate violation\n");
	  printf("   Statement: "); c->getstatement()->print(); printf("\n");
	  printf("   Curr. state: "); st->print(globalmodel);
	  printf("Repairing...\n");
#endif
	  if (c->getcrash()) {
	    printf("Fatal program error violating special constraint.\n");
	    exit(-1);
	  }
	  repair->repairconstraint(c,this,st->env);
	  clean=false;
	}
      }
      if (!st->increment(globalmodel))
	break; /* done */
    }
  }
  delete(st);
  return clean;
}



/* breaks the given constraint by invalidating each of its satisfied sentences */
void processobject::breakconstraint(Constraint *c)
{  
#ifdef DEBUGMESSAGES
  printf("Constraint to be broken: ");
  c->print();
  printf("\n");
  fflush(NULL);
#endif

  // first, get get the constraint in normal form
  NormalForm *nf = globalmodel->getnormalform(c);

  // for each CoerceSentence in nf, find if it's satisfied. If so, break it.
  for (int i=0; i<nf->getnumsentences(); i++)
    {
#ifdef DEBUGMESSAGES
      printf("In processobject::breakconstraint, i=%d \n", i);
      fflush(NULL);
#endif

      CoerceSentence *s = nf->getsentence(i);
      
      // find if s is satisfied
      bool satisfied = true;
      State *st = new State(c, globalmodel->gethashtable());
      if (st->initializestate(globalmodel))
	while(true) 
	  {
	    if (!s->issatisfied(this, st->env))
	      satisfied=false;

	    if (!st->increment(globalmodel))
	      break;
	  }  
      delete(st);
      

      // if s is satisfied, then break it

      if (satisfied)
	{
	  // first, select an arbitrary binding, for ex. the first one
	  st = new State(c, globalmodel->gethashtable());
	  
	  if (st->initializestate(globalmodel))
	    {
#ifdef DEBUGMESSAGES
	      printf("numpredicates = %d\n", s->getnumpredicates());
#endif
	      
	      for (int j=0; j<s->getnumpredicates(); j++)
		{
		  CoercePredicate *cp = s->getpredicate(j);
		  // break this predicate with probability prob_breakpredicate
		  if (random()<model::prob_breakpredicate*RAND_MAX)
		    {
#ifdef DEBUGMESSAGES
		      printf("po::breakconstraint:  We break predicate %d\n",j);
		      fflush(NULL);
#endif
		      
		      Action *action = repair->findbreakaction(cp);
		      action->breakpredicate(st->env, cp);
		      
#ifdef DEBUGMESSAGES
		      printf("After action->breakpredicate was called\n");
		      fflush(NULL);
#endif
		    }
		}
	    }
	  
	  delete(st);
	}
    }
}



/* satisfies the given satisfied contraint in another way */
void processobject::modifyconstraint(Constraint *c)
{
#ifdef DEBUGMESSAGES
  printf("Constraint to be modified: ");
  c->print();
  printf("\n");
  fflush(NULL);
#endif

  // first, get the constraint in normal form
  NormalForm *nf = globalmodel->getnormalform(c);

  /* for each CoerceSentence in nf, find if it's satisfied. 
     If it's satisfied, we break it with probability prob_breaksatisfiedsentence;
     If it's not satisfied, we repair it with probability prob_repairbrokensentence
  */

  bool still_valid = false;

  for (int i=0; i<nf->getnumsentences(); i++)
    {
#ifdef DEBUGMESSAGES
      printf("In processobject::modifyconstraint, i=%d \n", i);
      fflush(NULL);
#endif

      CoerceSentence *s = nf->getsentence(i);
      
      // find if s is satisfied
      bool satisfied = true;
      State *st = new State(c, globalmodel->gethashtable());
      if (st->initializestate(globalmodel))
	while(true) 
	  {
	    if (!s->issatisfied(this, st->env))
	      satisfied=false;

	    if (!st->increment(globalmodel))
	      break;
	  }  
      delete(st);
      
      // if s is satisfied, then break it
      if  (satisfied)
	if (random()<model::prob_breaksatisfiedsentence*RAND_MAX) // then break it
	  {
	    // first, select an arbitrary binding, for ex. the first one
	    st = new State(c, globalmodel->gethashtable());
	    
	    if (st->initializestate(globalmodel))
	      {
		for (int j=0; j<s->getnumpredicates(); j++)
		  {
		    CoercePredicate *cp = s->getpredicate(j);
		    // break this predicate with probability prob_breakpredicate
		    if (random()<model::prob_breakpredicate*RAND_MAX)
		      {
#ifdef DEBUGMESSAGES
			printf("po::modifyconstraint:  We break predicate %d\n",j);
			fflush(NULL);
#endif
			
			Action *action = repair->findbreakaction(cp);
			action->breakpredicate(st->env, cp);
			
#ifdef DEBUGMESSAGES
			printf("After action->modifypredicate was called\n");
			fflush(NULL);
#endif
		      }
		  }
	      }
	    delete(st);
	  }
	else still_valid = true;
      else // if not satisfied, then repair it with prob_repairbrokensentence
	if (random()<model::prob_repairbrokensentence)
	  {
	    // first, select an arbitrary binding, for ex. the first one
	    st = new State(c, globalmodel->gethashtable());
	    
	    if (st->initializestate(globalmodel))
	      {
		for (int j=0; j<s->getnumpredicates(); j++)
		  {
		    CoercePredicate *cp = s->getpredicate(j);
		    Action *action = repair->findrepairaction(cp);
		    action->repairpredicate(st->env, cp);
			
#ifdef DEBUGMESSAGES
			printf("After action->repairpredicate was called\n");
			fflush(NULL);
#endif
		  }
	      }
	    
	    delete(st);	    
	    still_valid = true;
	  }
    }

  if (!still_valid) // if all sentences are broken, repair the first one
    {
      CoerceSentence *saux = nf->getsentence(0);
      // first, select an arbitrary binding, for ex. the first one
      State *st = new State(c, globalmodel->gethashtable());
      
      if (st->initializestate(globalmodel))
	{
	  for (int j=0; j<saux->getnumpredicates(); j++)
	    {
	      CoercePredicate *cp = saux->getpredicate(j);
	      Action *action = repair->findrepairaction(cp);
	      action->repairpredicate(st->env, cp);
	      
#ifdef DEBUGMESSAGES
	      printf("After action->repairpredicate was called\n");
	      fflush(NULL);
#endif
	    }
	}
      
      delete(st);	    
    }
  
}




processobject::~processobject() {
}






// computes ve = V.R
Element * evaluatevalueexpr(Valueexpr *ve, Hashtable *env, model *m) {
  Element * e=(Element *) env->get(ve->getlabel()->label());
  return (Element *)m->getdomainrelation()->getrelation(ve->getrelation()->getname())->getrelation()->getobj(e);
}



// evaluates E = V | number | string | E-E | E+E | E*E | E/E | E.R |size(SE)
Element * evaluateexpr(Elementexpr *ee, Hashtable *env, model *m) {
  switch(ee->gettype()) {
  case ELEMENTEXPR_LABEL: {
    return new Element((Element *)env->get(ee->getlabel()->label()));
  }
  case ELEMENTEXPR_SUB: {
    Elementexpr *left=ee->getleft();
    Elementexpr *right=ee->getright();
    Element *leftval=evaluateexpr(left,env,m);
    if(leftval==NULL) return NULL;
    Element *rightval=evaluateexpr(right,env,m);
    if(rightval==NULL) {delete(leftval);return NULL;}
    Element *diff=new Element(leftval->intvalue()-rightval->intvalue());
    delete(leftval);delete(rightval);
    return diff;
  }
  case ELEMENTEXPR_ADD: {
    Elementexpr *left=ee->getleft();
    Elementexpr *right=ee->getright();
    Element *leftval=evaluateexpr(left,env,m);
    if(leftval==NULL) return NULL;
    Element *rightval=evaluateexpr(right,env,m);
    if(rightval==NULL) {delete(leftval);return NULL;}
    Element *sum=new Element(leftval->intvalue()+rightval->intvalue());
    delete(leftval);delete(rightval);
    return sum;
  }
  case ELEMENTEXPR_RELATION: {
    Elementexpr *left=ee->getleft();
    Relation *rel=ee->getrelation();
    Element *leftval=evaluateexpr(left,env,m);
    if(leftval==NULL) return NULL;
    Element *retval=(Element *)m->getdomainrelation()->getrelation(rel->getname())->getrelation()->getobj(leftval);
    delete(leftval);
    return new Element(retval);
  }
  case ELEMENTEXPR_MULT: {
    Elementexpr *left=ee->getleft();
    Elementexpr *right=ee->getright();
    Element *leftval=evaluateexpr(left,env,m);
    if(leftval==NULL) return NULL;
    Element *rightval=evaluateexpr(right,env,m);
    if(rightval==NULL) {delete(leftval);return NULL;}
    Element *diff=new Element(leftval->intvalue()*rightval->intvalue());
    delete(leftval);delete(rightval);
    return diff;
  }
  case ELEMENTEXPR_LIT: {
    Literal *l=ee->getliteral();
    switch(l->gettype()) {
    case LITERAL_NUMBER:
      return new Element(l->number());
    case LITERAL_TOKEN:
      return new Element(copystr(l->token()));
    default:
      printf("ERROR with lit type\n");
      exit(-1);
    }
  }
  /*  case ELEMENTEXPR_PARAM: {
    Element *ele=evaluateexpr(ee->getleft(),env,m);
    Element *eec=ele->paramvalue(ee->getliteral()->number());
    Element *retval=new Element(eec);
    delete(ele);
    return eec;
    }*/ //NO OBJECT PARAMETERS
  case ELEMENTEXPR_SETSIZE:
    Setexpr * setexpr=ee->getsetexpr();
    switch(setexpr->gettype()) {
    case SETEXPR_LABEL:
      return new Element(m->getdomainrelation()->getset(setexpr->getsetlabel()->getname())->getset()->size());
    case SETEXPR_REL: {
      Element *key=(Element *)env->get(setexpr->getlabel()->label());
      WorkSet *ws=m->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->getset(key);
      if (ws==NULL)
	return new Element(0);
      else
	return new Element(ws->size());
    }
    case SETEXPR_INVREL: {
      Element *key=(Element *)env->get(setexpr->getlabel()->label());
      WorkSet *ws=m->getdomainrelation()->getrelation(setexpr->getrelation()->getname())->getrelation()->invgetset(key);
      if (ws==NULL)
	return new Element(0);
      else
	return new Element(ws->size());
    }
    }
    break;
  }
}


