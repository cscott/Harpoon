// handles prediate of the following forms: VE<E, VE<=E, VE=E, VE>=E, VE>E

#include <stdlib.h>
#include <assert.h>
#include "ActionAssign.h"
#include "dmodel.h"
#include "normalizer.h"
#include "omodel.h"
#include "Relation.h"
#include "set.h"
#include "Hashtable.h"
#include "model.h"
#include "processobject.h"
#include "element.h"
#include "common.h"


ActionAssign::ActionAssign(DomainRelation *drel, model *m) {
  domrelation=drel;
  globalmodel=m;
}

char * ActionAssign::gettype(Constraint *c,Elementexpr *ee) {
  switch(ee->gettype()) {
  case ELEMENTEXPR_LABEL:
    return getset(c,ee->getlabel()->label());
  case ELEMENTEXPR_SUB:
  case ELEMENTEXPR_ADD:
  case ELEMENTEXPR_MULT:
    return "int";
  case ELEMENTEXPR_LIT: {
    Literal *lit=ee->getliteral();
    switch(lit->gettype()) {
    case LITERAL_NUMBER:
      return "int";
    case LITERAL_TOKEN:
      return "token";
    case LITERAL_BOOL:
      printf("ERROR in gettype\n");
      exit(-1);
    }
  }
  case ELEMENTEXPR_SETSIZE:
    return "int";
  case ELEMENTEXPR_RELATION: {
    Relation *r=ee->getrelation();
    DomainRelation *drel=globalmodel->getdomainrelation();
    return drel->getrelation(r->getname())->getrange();
  }
  }
}


// repairs the given predicate
void ActionAssign::repairpredicate(Hashtable *env, CoercePredicate *cp) {
  Predicate *p=cp->getpredicate();
  Element *ele=evaluateexpr(p->geteleexpr(),env,globalmodel); //ele=E
  Element *index=(Element *) env->get(p->getvalueexpr()->getlabel()->label()); // index=V
  char *rel=p->getvalueexpr()->getrelation()->getname(); // rel=R
  WorkRelation *relation=domrelation->getrelation(rel)->getrelation();
  Element *old=(Element *)relation->getobj(index); // old=V.R
  if (old!=NULL)
    relation->remove(index,old);
  DRelation *drel=domrelation->getrelation(rel);
  
  if(!equivalentstrings(drel->getdomain(),"int")) {
    DomainSet *domain=domrelation->getset(drel->getdomain());
    if (!domain->getset()->contains(index))
      domrelation->addtoset(index,domain,globalmodel);
    }


  switch (p->gettype()) {
  case PREDICATE_LT: {
    Element *ele2=new Element(ele->intvalue()-1);
    delete(ele);
    relation->put(index,ele2);
    break;
  }
  case PREDICATE_LTE: {
    relation->put(index,ele);
    break;
  }
  case PREDICATE_EQUALS: {
    relation->put(index,ele);
    if(!equivalentstrings(drel->getrange(),"int")&&
       !equivalentstrings(drel->getrange(),"token")) {
      DomainSet *range=domrelation->getset(drel->getrange());
      if (!range->getset()->contains(ele))
	domrelation->addtoset(ele,range,globalmodel);
    }
    break;
  }
  case PREDICATE_GTE: {
    relation->put(index,ele);
    break;
  }
  case PREDICATE_GT: {
    Element *ele2=new Element(ele->intvalue()+1);
    delete(ele);
    relation->put(index,ele2);
    break;
  }
  }
}



void ActionAssign::breakpredicate(Hashtable *env, CoercePredicate *cp)
{
#ifdef DEBUGMESSAGES
  printf("ActionAssign::breakpredicate CALLED\n");
  cp->getpredicate()->print(); printf("\n");
#endif

  Predicate *p = cp->getpredicate();
  Element *ele = evaluateexpr(p->geteleexpr(),env,globalmodel); //ele=E
  Element *index = (Element *) env->get(p->getvalueexpr()->getlabel()->label()); // index=V


#ifdef DEBUGMESSAGES
  printf("index=%s\n", p->getvalueexpr()->getlabel()->label());
  if (index == NULL)    
    printf("index - bad\n");
  else printf("index - ok\n");
#endif
  
  char *rel = p->getvalueexpr()->getrelation()->getname(); // rel=R
  WorkRelation *relation = domrelation->getrelation(rel)->getrelation();

#ifdef DEBUGMESSAGES
  if (relation == NULL)    
    printf("relation - bad\n");
  else printf("relation - ok\n");
  fflush(NULL);
#endif

  Element *old_ve = (Element *)relation->getobj(index); // old_ve=V.R

  if (old_ve!=NULL)
    relation->remove(index,old_ve);
  DRelation *drel = domrelation->getrelation(rel);
  
  if(!equivalentstrings(drel->getdomain(),"int")) 
    {
      DomainSet *domain = domrelation->getset(drel->getdomain());
      if (!domain->getset()->contains(index))
	domrelation->addtoset(index,domain,globalmodel);
    }

#ifdef DEBUGMESSAGES
  printf("p->gettype() = %d\n", p->gettype());
  fflush(NULL);
#endif


  switch (p->gettype()) {
  // VE<E
  case PREDICATE_LT: 
    {
      // set VE=E which breaks VE<E
      Element *newele=new Element(ele->intvalue());
      delete(ele);
      relation->put(index,newele);
      break;
    }

  // VE<=E
  case PREDICATE_LTE: 
    {
      // set VE=E+1, which breaks VE<=E
      Element *newele=new Element(ele->intvalue()+1);
      delete(ele);
      relation->put(index,newele);
      break;
    }

  // VE=E
  case PREDICATE_EQUALS: 
    {      
      DRelation *drel=domrelation->getrelation(rel);      

      // if the V.R is an integer, set VE=E+1, which breaks VE=E
      if (equivalentstrings(drel->getrange(),"int")) 
	{
	  Element *newele=new Element(ele->intvalue()+1);
	  delete(ele);
	  relation->put(index,newele);
	}
      else 
	{
	  Element *newele = NULL;
	  printf("PREDICATE_EQUALS for tokens\n");
	  //printf("range name = %s\n", drel->getrange()); fflush(NULL);
	  //printf("Current value: ");  old_ve->print();  printf("\n");

	  /* find a value in the actual range that is different from the
	     current value of V.R */
	  char* old_token = old_ve->gettoken();
	  WorkSet *ws = drel->gettokenrange();
	  bool found = false;
	  char *token = (char*) ws->firstelement();
	  while (token)
	    {
	      printf("Token: %s\n", token);
	      if (!equivalentstrings(token, old_token))
		{
		  found = true;
		  newele = new Element(token);
		  break;
		}	      
	      token = (char*) ws->getnextelement(token);
	    }

	  if (!found)
	    {
	      printf("The following predicate cannot be broken:");
	      cp->getpredicate()->print(); printf("\n");
	    }
	  else relation->put(index, newele);

	  fflush(NULL);
	  printf("\n\n");  	  	 	
	}
      break;
    }

  // VE>=E
  case PREDICATE_GTE: 
    {
      // set VE=E-1, which breaks VE>=E
      Element *newele=new Element(ele->intvalue()-1);
      delete(ele);
      relation->put(index,newele);
      break;
    }

  // VE>E
  case PREDICATE_GT: 
    {
      // set VE=E, which breaks VE>E
      Element *newele=new Element(ele->intvalue());
      delete(ele);
      relation->put(index,newele);
      break;
    }
  }
}



bool ActionAssign::conflict(Constraint *c1, CoercePredicate *p1,Constraint *c2, CoercePredicate *p2) {
  assert(canrepairpredicate(p1));
  if(comparepredicates(c1,p1,c2,p2))
    return false; /*same predicates don't conflict*/
  /* we have v.r?a */
  /* add <v,?> to r */

  /* Compute bounding set if there is one */
  
  
  DomainRelation *drel=globalmodel->getdomainrelation();
  char *insertset=drel->getrelation(p1->getpredicate()->getvalueexpr()->getrelation()->getname())->getrange();
  
  char *boundset=gettype(c1,p1->getpredicate()->geteleexpr());
  
  /* Check conflicts arrising from addition to set */
  {
    WorkSet *ws=domrelation->conflictaddsets(insertset,NULL,globalmodel);
    DomainSet *ds=(DomainSet *) ws->firstelement();
    while(ds!=NULL) {
      if (conflictwithaddtoset(ds->getname(),c2,p2)) {
	delete(ws);
	return true;
      }
      ds=(DomainSet *) ws->getnextelement(ds);
    }
    delete(ws);
  }
  /* Check conflicts arrising from deletions from set */
  {
    WorkSet *ws=domrelation->conflictdelsets(insertset, NULL);    
    DomainSet *ds=(DomainSet *) ws->firstelement();
    while (ds!=NULL) {
      if (conflictwithremovefromset(NULL,ds->getname(),c2,p2)) {
	delete(ws);
	return true;
      }
      ds=(DomainSet *) ws->getnextelement(ds);
    }
    delete(ws);
  }
  return testforconflict(getset(c1,p1->getpredicate()->getvalueexpr()->getlabel()->label()), NULL,
			 p1->getpredicate()->getvalueexpr()->getrelation()->getname(),c2,p2)||
    testforconflictremove(getset(c1,p1->getpredicate()->getvalueexpr()->getlabel()->label()), NULL,
			  p1->getpredicate()->getvalueexpr()->getrelation()->getname(),c2,p2);
}

bool ActionAssign::canrepairpredicate(CoercePredicate *cp) {
  if (cp->getcoercebool()==false)
    return false;
  Predicate *p=cp->getpredicate();
  if (p==NULL)
    return false;
  if (p->gettype()==PREDICATE_LT||
      p->gettype()==PREDICATE_LTE||
      p->gettype()==PREDICATE_EQUALS||
      p->gettype()==PREDICATE_GTE||
      p->gettype()==PREDICATE_GT) {
    Valueexpr *ve=p->getvalueexpr();
    DRelation *dr=domrelation->getrelation(ve->getrelation()->getname());
    if (dr->isstatic()) /* can't change static relations */
      return false;
    else
      return true;
  }  
  /* Coercing set membership */
  return false;
}
