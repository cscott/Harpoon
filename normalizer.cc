// converts constraints into disjunctive normal form

#include <stdlib.h>
#include "normalizer.h"
#include "omodel.h"
#include "processobject.h"
#include "set.h"
#include "amodel.h"
#include "common.h"



// class CoercePredicate

CoercePredicate::CoercePredicate(char *ts, char *lt, char *rs) {
  triggerset=ts;
  ltype=lt;
  rtype=NULL;
  relset=rs;
  rule=false;
  tuple=false;
  predicate=NULL;
}

CoercePredicate::CoercePredicate(char *ts, char *lt, char *rt,char *rs) {
  triggerset=ts;
  ltype=lt;
  rtype=rt;
  relset=rs;
  rule=false;
  tuple=true;
  predicate=NULL;
}

CoercePredicate::CoercePredicate(bool b, Predicate *p) {
  rule=true;
  coercebool=b;
  predicate=p;
#ifdef REPAIR
  if((p->gettype()==PREDICATE_EQ1||
      p->gettype()==PREDICATE_GTE1)&&coercebool==false) {
    printf("Possible forcing size predicate to be false.  Error!\n");
    exit(-1);
  }
#endif
}

Predicate * CoercePredicate::getpredicate() {
  return predicate;
}

bool CoercePredicate::isrule() {
  return rule;
}

bool CoercePredicate::istuple() {
  return tuple;
}

bool CoercePredicate::getcoercebool() {
  return coercebool;
}

char * CoercePredicate::gettriggerset() {
  return triggerset;
}

char * CoercePredicate::getrelset() {
  return relset;
}

char * CoercePredicate::getltype() {
  return ltype;
}

char * CoercePredicate::getrtype() {
  return rtype;
}




// class CoerceSentence

CoerceSentence::CoerceSentence(CoercePredicate **pred, int numpred) {
  predicates=pred;
  numpreds=numpred;
}


int CoerceSentence::getnumpredicates() {
  return numpreds;
}


CoercePredicate * CoerceSentence::getpredicate(int i) {
  return predicates[i];
}



// returns true iff the sentence is satisfied
bool CoerceSentence::issatisfied(processobject *po, Hashtable *env)
{
  for (int i=0; i<getnumpredicates(); i++)
    {
      CoercePredicate *cp = getpredicate(i);
      Predicate *p = cp->getpredicate();
      if (!po->processpredicate(cp->getpredicate(),env))
	return false;
    }
  return true;
}


// returns how much we pay if we satisfy this sentence 
int CoerceSentence::cost(processobject *po, Hashtable *env) {
  int cost=0;
  for(int i=0;i<numpreds;i++) {
    CoercePredicate *cp=predicates[i];
    bool pvalue;
    if (cp->getpredicate()!=NULL)
      pvalue=po->processpredicate(cp->getpredicate(),env);
    if (pvalue!=cp->getcoercebool())
      cost+=costfunction(cp);
  }
  return cost;
}


CoerceSentence::~CoerceSentence() {
  for(int i=0;i<numpreds;i++)
    delete(predicates[i]);
  delete predicates;
}




// class NormalForm

NormalForm::NormalForm(Constraint *c) {
  SentenceArray *sa=computesentences(c->getstatement(),true);
  this->length=sa->length;
  this->sentences=sa->sentences;
  this->c=c;
  delete(sa);
}

void NormalForm::fprint(FILE *f) {
  if (c!=NULL) 
    c->fprint(f);
}

NormalForm::NormalForm(Rule *r) {
  int count=-1;
  char *label, *triggerset;
  c=NULL;
  for(int i=0;i<r->numquants();i++) {
    AQuantifier *aq=r->getquant(i);
    switch(aq->gettype()) {
    case AQUANTIFIER_SING:
      triggerset=aq->getset()->getname();
      label=aq->getleft()->label();
      break;
    default:
      break;
    }
  }
  Statementb *sb=r->getstatementb();
  CoercePredicate *cp=NULL;
  if(sb->gettype()==STATEMENTB_SING) {
    cp=new CoercePredicate(triggerset,gettype(label,triggerset,sb->getleft()),sb->getsetlabel()->getname());
  } else {
    cp=new CoercePredicate(triggerset,gettype(label,triggerset,sb->getleft()),gettype(label,triggerset,sb->getright()),sb->getsetlabel()->getname());
  }
  CoercePredicate **cpa=new CoercePredicate*[1];
  cpa[0]=cp;
  CoerceSentence *cs=new CoerceSentence(cpa,1);
  sentences=new CoerceSentence*[1];
  sentences[0]=cs;
  length=1;
}


char * gettype(char *label, char *set,AElementexpr *ae) {
  switch(ae->gettype()) {
  case AELEMENTEXPR_SUB:
  case AELEMENTEXPR_ADD:
  case AELEMENTEXPR_MULT:
  case AELEMENTEXPR_DIV:
    return "int";
  case AELEMENTEXPR_NULL:
    return NULL;
  case AELEMENTEXPR_FIELD:
  case AELEMENTEXPR_FIELDARRAY:
    return NULL;
  case AELEMENTEXPR_LIT:
    switch(ae->getliteral()->gettype()) {
    case LITERAL_NUMBER:
      return "int";
    case LITERAL_TOKEN:
      return "token";
    default:
      printf("error in normalizer.gettype()\n");
      exit(-1);
    }
  case AELEMENTEXPR_LABEL:
    if (equivalentstrings(ae->getlabel()->label(),label))
      return set;
    return NULL;
  case AELEMENTEXPR_CAST:
    return gettype(label,set,ae->getleft());
  }
}


int NormalForm::getnumsentences() {
  return length;
}


CoerceSentence * NormalForm::getsentence(int i) {
  return sentences[i];
}


/* returns the sentence in this constraint that can be satisfied 
   with a minimum cost, and which is different from any sentence 
   in the "badsentences" structure */
CoerceSentence * NormalForm::closestmatch(WorkSet *badsentences, processobject *po, Hashtable *env) {
  int totalcost=-1; int bestmatch=-1;
  for(int i=0;i<length;i++) {
    if (badsentences==NULL || !badsentences->contains(sentences[i])) 
      {
	int cost=sentences[i]->cost(po,env);
	if ((totalcost==-1)||(totalcost>cost)) {
	  totalcost=cost; 
	  bestmatch=i;
	}
      }
  }
  return sentences[bestmatch];
}

int costfunction(CoercePredicate *p) {
  return 1;
}


// computes the normal form of the given statement
SentenceArray * computesentences(Statement *st,bool stat) {
  switch(st->gettype()) {
  case STATEMENT_OR: {
    SentenceArray *left=computesentences(st->getleft(),stat);
    SentenceArray *right=computesentences(st->getright(),stat);
    if (stat) {
      CoerceSentence **combine=new CoerceSentence *[left->length+right->length];
      for(int i=0;i<left->length;i++)
	combine[i]=left->sentences[i];
      for(int i=0;i<right->length;i++)
	combine[i+left->length]=right->sentences[i];
      SentenceArray *sa=new SentenceArray(combine, left->length+right->length);
      delete[](left->sentences);delete[](right->sentences);
      delete(left);delete(right);
      return sa;
    } else {
      CoerceSentence **combine=new CoerceSentence *[left->length*right->length];
      for(int i=0;i<left->length;i++)
	for(int j=0;j<right->length;j++) {
	  CoerceSentence *leftsent=left->sentences[i];
	  CoerceSentence *rightsent=right->sentences[j];
	  CoercePredicate **preds=new CoercePredicate *[leftsent->getnumpredicates()+rightsent->getnumpredicates()];
	  for(int il=0;il<leftsent->getnumpredicates();il++)
	    preds[il]=new CoercePredicate(leftsent->getpredicate(il)->getcoercebool(),leftsent->getpredicate(il)->getpredicate());
	  for(int ir=0;ir<rightsent->getnumpredicates();ir++)
	    preds[ir+leftsent->getnumpredicates()]=new CoercePredicate(rightsent->getpredicate(ir)->getcoercebool(),rightsent->getpredicate(ir)->getpredicate());
	  combine[i*right->length+j]=new CoerceSentence(preds,left->length*right->length);
	}
      SentenceArray *sa=new SentenceArray(combine, left->length*right->length);
      for(int i=0;i<left->length;i++)
	delete(left->sentences[i]);
      for(int i=0;i<right->length;i++)
	delete(right->sentences[i]);
      delete(left->sentences);delete(right->sentences);
      delete(left);delete(right);
      return sa;
    }
  }
  case STATEMENT_AND: {
    SentenceArray *left=computesentences(st->getleft(),stat);
    SentenceArray *right=computesentences(st->getright(),stat);
    if (stat) {
      CoerceSentence **combine=new CoerceSentence *[left->length*right->length];
      for(int i=0;i<left->length;i++)
	for(int j=0;j<right->length;j++) {
	  CoerceSentence *leftsent=left->sentences[i];
	  CoerceSentence *rightsent=right->sentences[j];
	  CoercePredicate **preds=new CoercePredicate *[leftsent->getnumpredicates()+rightsent->getnumpredicates()];
	  for(int il=0;il<leftsent->getnumpredicates();il++)
	    preds[il]=new CoercePredicate(leftsent->getpredicate(il)->getcoercebool(),leftsent->getpredicate(il)->getpredicate());
	  for(int ir=0;ir<rightsent->getnumpredicates();ir++)
	    preds[ir+leftsent->getnumpredicates()]=new CoercePredicate(rightsent->getpredicate(ir)->getcoercebool(),rightsent->getpredicate(ir)->getpredicate());
	  combine[i*right->length+j]=new CoerceSentence(preds,left->length*right->length);
	}
      SentenceArray *sa=new SentenceArray(combine, left->length*right->length);
      for(int i=0;i<left->length;i++)
	delete(left->sentences[i]);
      for(int i=0;i<right->length;i++)
	delete(right->sentences[i]);
      delete(left->sentences);delete(right->sentences);
      delete(left);delete(right);
      return sa;
    } else {
      CoerceSentence **combine=new CoerceSentence *[left->length+right->length];
      for(int i=0;i<left->length;i++)
	combine[i]=left->sentences[i];
      for(int i=0;i<right->length;i++)
	combine[i+left->length]=right->sentences[i];
      SentenceArray *sa=new SentenceArray(combine, left->length+right->length);
      delete(left->sentences);delete(right->sentences);
      delete(left);delete(right);
      return sa;
    }
  }
  case STATEMENT_NOT:
    return computesentences(st->getleft(),!stat);
  case STATEMENT_PRED:
    CoercePredicate *cp=new CoercePredicate(stat, st->getpredicate());
    CoercePredicate **cparray=new CoercePredicate *[1];
    cparray[0]=cp;
    CoerceSentence *cs=new CoerceSentence(cparray,1);
    CoerceSentence **csarray=new CoerceSentence *[1];
    csarray[0]=cs;
    return new SentenceArray(csarray,1);
  }
}




// class SentenceArray

SentenceArray::SentenceArray(CoerceSentence **sentences, int l) {
  length=l;
  this->sentences=sentences;
}

