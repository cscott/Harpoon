package MCC.IR;
import MCC.State;
import MCC.Compiler;

import java.util.*;

public class ImplicitSchema {
    State state;
    SetAnalysis setanalysis;
    public ImplicitSchema(State state) {
	this.state=state;
	this.setanalysis=state.setanalysis;
    }

    public void update() {
	if (Compiler.REPAIR) {
	    updaterules();
	}
	updateconstraints();
	updaterelationconstraints();
    }

    boolean needDomain(RelationDescriptor rd) {
	return needDR(rd, true);
    }

    boolean needDR(RelationDescriptor rd,boolean isdomain) {
	Vector rules=state.vRules;
	SetDescriptor sd=isdomain?rd.getDomain():rd.getRange();
	if (sd instanceof ReservedSetDescriptor)
	    return false;

	/* See if there is a rule that adds the corresponding range or domain
	   of the relation to the correct set */
	for(int i=0;i<rules.size();i++) {
	    Rule r=(Rule)rules.get(i);
	    if ((r.numQuantifiers()==1)&&
		(r.getQuantifier(0) instanceof RelationQuantifier)&&
		(((RelationQuantifier)r.getQuantifier(0)).getRelation()==rd)&&
		r.getInclusion().getTargetDescriptors().contains(sd)) {
		SetInclusion rinc=(SetInclusion)r.getInclusion();
		RelationQuantifier rq=(RelationQuantifier)r.getQuantifier(0);
		VarDescriptor vd=isdomain?rq.x:rq.y;
		if ((rinc.getExpr() instanceof VarExpr)&&
		    (((VarExpr)rinc.getExpr()).getVar()==vd)&&
		    (r.getGuardExpr() instanceof BooleanLiteralExpr)&&
		    (((BooleanLiteralExpr)r.getGuardExpr()).getValue()))
		    return false;
	    }
	}
	for(int i=0;i<rules.size();i++) {
	    Rule r=(Rule)rules.get(i);
	    Inclusion inc=r.getInclusion();
	    if (inc.getTargetDescriptors().contains(rd)) {
		/* Need to check this rule */
		boolean good=false;
		RelationInclusion rinc=(RelationInclusion)inc;
		Expr expr=isdomain?rinc.getLeftExpr():rinc.getRightExpr();
		/* Check for varexpr's and quantification over */
		if (expr instanceof VarExpr) {
		    VarDescriptor vd=((VarExpr)expr).getVar();
		    assert vd!=null;
		    /* See if the var is from an appropriate quantifier */
		    for (int j=0;j<r.numQuantifiers();j++) {
			Quantifier q=r.getQuantifier(j);
			if ((q instanceof SetQuantifier)&&
			    (((SetQuantifier)q).getVar()==vd)&&
			    (sd.allSubsets().contains(((SetQuantifier)q).getSet()))) {
			    good=true;
			    break;
			}
			if ((q instanceof RelationQuantifier)&&
			    (
			    ((((RelationQuantifier)q).x==vd)&&
			    (sd.allSubsets().contains(((RelationQuantifier)q).getRelation().getDomain())))
			    ||
			    ((((RelationQuantifier)q).y==vd)&&
			    (sd.allSubsets().contains(((RelationQuantifier)q).getRelation().getRange())))
			    )) {
			    good=true;
			    break;
			}
		    }
		    if (good)
			continue; /* Checked for this case */
		}
		if (checkguard(r,isdomain))
		    continue;
		for(int j=0;j<rules.size();j++) {
		    Rule r2=(Rule)rules.get(j);
		    Inclusion inc2=r2.getInclusion();
		    if (checkimplication(r,r2,isdomain)) {
			good=true;
			break;
		    }
		}
		if (good)
		    continue;

		return true; /* Couldn't verify we didn't need */
	    }
	}
	return false;
    }

    boolean checkguard(Rule r,boolean isdomain) {
	RelationInclusion inc=(RelationInclusion) r.getInclusion();
	RelationDescriptor rd=inc.getRelation();
	SetDescriptor sd=isdomain?rd.getDomain():rd.getRange();
	Expr expr=isdomain?inc.getLeftExpr():inc.getRightExpr();
	DNFRule dnfGuard=r.getDNFGuardExpr();
	for(int i=0;i<dnfGuard.size();i++) {
	    RuleConjunction rconj=dnfGuard.get(i);
	    boolean foundcheck=false;
	    for(int j=0;j<rconj.size();j++) {
		DNFExpr dexpr=rconj.get(j);
		if (!dexpr.getNegation()&&
		    dexpr.getExpr() instanceof ElementOfExpr) {
		    ElementOfExpr eoe=(ElementOfExpr)dexpr.getExpr();
		    
		    if (eoe.set==sd&&
			eoe.element.equals(null,expr)) {
			foundcheck=true;
			break;
		    }
		}
	    }
	    if (!foundcheck) {
		return false;
	    }
	}
	return true;
    }

    boolean checkimplication(Rule r1, Rule r2, boolean isdomain) {
	/* r1 is the relation */
	/* See if this rule guarantees relation */
	/* Steps:
	   1. match up quantifiers
	   2. check inclusion condition
	   3. see if guard expr of set rule is more general */
	RelationInclusion inc1=(RelationInclusion) r1.getInclusion();
	RelationDescriptor rd=inc1.getRelation();
	SetDescriptor sd=isdomain?rd.getDomain():rd.getRange();
	Expr expr=isdomain?inc1.getLeftExpr():inc1.getRightExpr();
	
	Inclusion inc2=r2.getInclusion();
	if (!(inc2 instanceof SetInclusion))
	    return false;
	SetInclusion sinc2=(SetInclusion)inc2;
	if (sinc2.getSet()!=sd)
	    return false;

	/* Construct a mapping between quantifiers */
	int[] mapping=new int[r2.numQuantifiers()];
	HashMap map=new HashMap();
	for(int i=0;i<r1.numQuantifiers();i++) {
	    Quantifier q1=r1.getQuantifier(i);
	    for (int j=0;j<r2.numQuantifiers();j++) {
		if (mapping[j]==1)
		    continue; /* Its already used */
		Quantifier q2=r2.getQuantifier(j);
		if (q1 instanceof SetQuantifier && q2 instanceof SetQuantifier&&
		    ((SetQuantifier)q1).getSet()==((SetQuantifier)q2).getSet()) {
		    mapping[j]=1;
		    map.put(((SetQuantifier)q1).getVar(),((SetQuantifier)q2).getVar());
		    break;
		}
		if (q1 instanceof RelationQuantifier && q2 instanceof RelationQuantifier &&
		    ((RelationQuantifier)q1).getRelation()==((RelationQuantifier)q2).getRelation()) {
		    mapping[j]=1;
		    map.put(((RelationQuantifier)q1).x,((RelationQuantifier)q2).x);
		    map.put(((RelationQuantifier)q1).y,((RelationQuantifier)q2).y);
		    break;
		}
		if (q1 instanceof ForQuantifier && q2 instanceof ForQuantifier &&
		    ((ForQuantifier)q1).lower.equals(map,((ForQuantifier)q2).lower)&&
		    ((ForQuantifier)q1).upper.equals(map,((ForQuantifier)q2).upper)) {
		    mapping[j]=1;
		    map.put(((ForQuantifier)q1).getVar(),((ForQuantifier)q2).getVar());
		}
	    }
	}

	/* Make sure all bindings in the set rule are bound */
	for (int i=0;i<r2.numQuantifiers();i++) {
	    if (mapping[i]!=1)
		return false;
	}

	/* Built mapping */
	Expr sexpr=sinc2.getExpr();
	if (!expr.equals(map,sexpr))
	    return false;  /* This rule doesn't add the right thing */

	DNFRule drule1=r1.getDNFGuardExpr();
	DNFRule drule2=r2.getDNFGuardExpr();
	for (int i=0;i<drule1.size();i++) {
	    RuleConjunction rconj1=drule1.get(i);
	    boolean foundmatch=false;
	    for (int j=0;j<drule2.size();j++) {
		RuleConjunction rconj2=drule2.get(j);
		/* Need to show than rconj2 is true if rconj1 is true */
		if (implication(map,rconj1,rconj2,sinc2)) {
		    foundmatch=true;
		    break;
		}
	    }
	    if (!foundmatch)
		return false;
	}
	return true;
    }

    boolean implication(HashMap map, RuleConjunction rc1, RuleConjunction rc2,SetInclusion si) {
	for(int i=0;i<rc2.size();i++) {
	    /* Check that rc1 has all predicates that rc2 has */
	    DNFExpr de2=rc2.get(i);
	    /* Predicates for objects that aren't in set */
	    if (de2.getNegation()&&
		(de2.getExpr() instanceof ElementOfExpr)) {
		ElementOfExpr eoe=(ElementOfExpr)de2.getExpr();
		if (si.getSet().isSubset(eoe.set)&&
		    si.getExpr().equals(null,eoe.element))
		    continue; /* This predicate isn't a problem */
	    }
	    boolean havematch=false;
	    for(int j=0;j<rc1.size();j++) {
		DNFExpr de1=rc1.get(i);
		if (de1.getNegation()!=de2.getNegation())
		    continue;
		if (de1.getExpr().equals(map,de2.getExpr())) {
		    havematch=true;
		    break;
		}
	    }
	    if (!havematch)
		return false;
	}
	return true;
    }

    boolean needRange(RelationDescriptor rd) {
	return needDR(rd, false);
    }

    void updaterelationconstraints() {
	Vector reldescriptors=state.stRelations.getAllDescriptors();
	for(int i=0;i<reldescriptors.size();i++) {
	    RelationDescriptor rd=(RelationDescriptor) reldescriptors.get(i);
	    if (needDomain(rd)||needRange(rd)) {
		
		Constraint c=new Constraint();
		/* Construct quantifier */
		LogicStatement ls=null;

		RelationQuantifier rq=new RelationQuantifier();
		String varname1=new String("relationvar1");
		VarDescriptor var1=new VarDescriptor(varname1);
		String varname2=new String("relationvar2");
		VarDescriptor var2=new VarDescriptor(varname2);
		rq.setTuple(var1,var2);
		rq.setRelation(rd);
		c.addQuantifier(rq);
		c.getSymbolTable().add(var1);
		c.getSymbolTable().add(var2);
		var1.setType(rd.getDomain().getType());
		var2.setType(rd.getRange().getType());

		if (needDomain(rd)) {
		    VarExpr ve1=new VarExpr(var1);
		    SetExpr se1=new SetExpr(rd.getDomain());
		    se1.td=rd.getDomain().getType();
		    ls=new InclusionPredicate(ve1,se1);
		}


		if (needRange(rd)) {
		    VarExpr ve2=new VarExpr(var2);
		    SetExpr se2=new SetExpr(rd.getRange());
		    se2.td=rd.getRange().getType();
		    LogicStatement incpred2=new InclusionPredicate(ve2,se2);
		    if (ls==null) ls=incpred2;
		    else ls=new LogicStatement(LogicStatement.AND,ls,incpred2);
		}
		rd.addUsage(RelationDescriptor.IMAGE);

		c.setLogicStatement(ls);
		state.vConstraints.add(c);
	    }
	}
    }

    void updateconstraints() {
	Vector setdescriptors=state.stSets.getAllDescriptors();
	for(int i=0;i<setdescriptors.size();i++) {
	    SetDescriptor sd=(SetDescriptor) setdescriptors.get(i);
	    if(sd.isPartition()) {
		Constraint c=new Constraint();
		/* Construct quantifier */
		SetQuantifier sq=new SetQuantifier();
		String varname=new String("partitionvar");
		VarDescriptor var=new VarDescriptor(varname);
		c.getSymbolTable().add(var);
		var.setType(sd.getType());
		sq.setVar(var);
		sq.setSet(sd);
		c.addQuantifier(sq);

		/*Construct logic statement*/
		LogicStatement ls=null;
		for(int j=0;j<sd.getSubsets().size();j++) {
		    LogicStatement conj=null;
		    for(int k=0;k<sd.getSubsets().size();k++) {
			VarExpr ve=new VarExpr(var);
			SetExpr se=new SetExpr((SetDescriptor) sd.getSubsets().get(k));
			se.td=sd.getType();
			LogicStatement incpred=new InclusionPredicate(ve,se);
			if (j!=k) {
			    incpred=new LogicStatement(LogicStatement.NOT ,incpred);
			}
			if (conj==null)
			    conj=incpred;
			else 
			    conj=new LogicStatement(LogicStatement.AND, conj, incpred);
		    }
		    if (ls==null)
			ls=conj;
		    else 
			ls=new LogicStatement(LogicStatement.OR, ls, conj);
		}
		c.setLogicStatement(ls);
		state.vConstraints.add(c);
	    }
	}
    }
    
    void updaterules() {
	Vector oldrules=state.vRules;
	Vector newrules=new Vector();
	Vector allrules=new Vector();
	allrules.addAll(oldrules);
	for(int i=0;i<oldrules.size();i++) {
	    Rule r=(Rule)oldrules.get(i);
	    if (r.inclusion instanceof SetInclusion) {
		SetDescriptor sd=((SetInclusion)r.inclusion).getSet();
		Set supersets=setanalysis.getSuperset(sd);
		if (supersets!=null)
		    for(Iterator superit=supersets.iterator();superit.hasNext();) {
			SetDescriptor sd1=(SetDescriptor)superit.next();
			Expr e=((SetInclusion)r.inclusion).getExpr();
			while(e instanceof CastExpr) {
			    e=((CastExpr)e).getExpr();
			}
			if (e instanceof VarExpr) {
			    VarDescriptor vde=((VarExpr)e).getVar();
			    boolean ok=false;
			    for (int j=0;j<r.numQuantifiers();j++) {
				Quantifier tmp=r.getQuantifier(j);
				if (tmp instanceof SetQuantifier&&
				    ((SetQuantifier)tmp).getVar()==vde)
				    ok=true; /* Need to make sure we don't have a relation quantifier. */
			    }

			    SetDescriptor currentset=e.getSet();
			    if (ok&&currentset!=null&&currentset.isSubset(sd1))
				continue; /* This rule doesn't add item to
					     this set, as item is already
					     in this set. */
			}
			if (isRedundant(allrules,r,sd1))
			    continue;

			Rule nr=new Rule();
			nr.setGuardExpr(r.getGuardExpr());
			nr.quantifiers=r.quantifiers;
			nr.isstatic=r.isstatic;
			nr.isdelay=r.isdelay;
			nr.inclusion=new SetInclusion(((SetInclusion)r.inclusion).elementexpr,sd1);
			nr.st=r.st;
			nr.setnogenerate();
			nr.num=r.num;
			newrules.add(nr);
			allrules.add(nr);
			state.implicitrule.put(nr,r);
			if (!state.implicitruleinv.containsKey(r))
			    state.implicitruleinv.put(r,new HashSet());
			((Set)state.implicitruleinv.get(r)).add(nr);
		    }
	    }
	}
	oldrules.addAll(newrules);
    }

    private boolean isRedundant(Vector allrules,Rule r,SetDescriptor sd) {
    outerloop:
	for(int i=0;i<allrules.size();i++) {
	    Rule r2=(Rule)allrules.get(i);
	    if (!(r2.getInclusion() instanceof SetInclusion))
		continue;
	    //old rule to the same set as the new rule's inclusion condition
	    if (sd!=((SetInclusion)r2.getInclusion()).getSet())
		continue;
	    //old rule quantifiers over superset of new rule's quantification
	    Hashtable varmap=buildvarmap(state,r2,r,true);
	    if (varmap==null)
		continue;
	    Expr ei1=((SetInclusion)r.getInclusion()).getExpr();
	    Expr ei2=((SetInclusion)r2.getInclusion()).getExpr();
	    if (!ei2.stripCastExpr().equals(varmap,ei1.stripCastExpr())) //adds same expression
		continue;
	    DNFRule dr1=r.getDNFGuardExpr();
	    DNFRule dr2=r2.getDNFGuardExpr();

	    //need to show that whenever the guard in r is satisfied, some guard in r2 is satisfied
	innerloop1:
	    for(int j=0;j<dr1.size();j++) {
		RuleConjunction rc1=dr1.get(j);
	    innerloop2:
		for(int k=0;k<dr2.size();k++) {
		    RuleConjunction rc2=dr2.get(k);
		    //if rc1 being true implies rc2 being true continue to innerloop1
		innerloop3:
		    for(int l=0;l<rc2.size();l++) {
			DNFExpr de2=rc2.get(l);
			for(int m=0;m<rc1.size();m++) {
			    DNFExpr de1=rc1.get(m);
			    if (de1.getNegation()==de2.getNegation()&&
				de2.getExpr().equals(varmap,de1.getExpr()))
				continue innerloop3;
			}
			continue innerloop2; //see if we can satisfy some other conjunction
		    }
		    continue innerloop1; //all of the expr's in this conjunction are satisfied
		}
		continue outerloop;
	    }
	    return true;
	}
	return false;
    }

    public static Hashtable buildvarmap(State state,Rule r1,Rule r2,boolean subsetting) {
	// Building a map between quantifier variables
	Hashtable varmap=new Hashtable();
	if (r1.numQuantifiers()!=r2.numQuantifiers())
	    return null;
	Set usedQuantifiers=new HashSet();
    outerloop:
	for(int i=0;i<r1.numQuantifiers();i++) {
	    Quantifier q1=r1.getQuantifier(i);

	    if (q1 instanceof SetQuantifier) {
		for(int j=0;j<r2.numQuantifiers();j++) {
		    Quantifier q2=r2.getQuantifier(j);
		    if (!(q2 instanceof SetQuantifier))
			continue;;
		    if (usedQuantifiers.contains(q2))
			continue;
		    if (((SetQuantifier)q1).getSet()==((SetQuantifier)q2).getSet()||
			(subsetting&&((SetQuantifier)q1).getSet().isSubset(((SetQuantifier)q2).getSet()))) {
			varmap.put(((SetQuantifier)q1).getVar(),((SetQuantifier)q2).getVar());
			usedQuantifiers.add(q2);
			continue outerloop;
		    }
		}
		return null;
	    } else if (q1 instanceof RelationQuantifier) {
		for(int j=0;j<r2.numQuantifiers();j++) {
		    Quantifier q2=r2.getQuantifier(j);
		    if (!(q2 instanceof RelationQuantifier))
			continue;
		    if (usedQuantifiers.contains(q2))
			continue;
		    if (((RelationQuantifier)q1).getRelation()==((RelationQuantifier)q2).getRelation()) {
			varmap.put(((RelationQuantifier)q1).x,((RelationQuantifier)q2).x);
			varmap.put(((RelationQuantifier)q1).y,((RelationQuantifier)q2).y);
			usedQuantifiers.add(q2);
			continue outerloop;
		    }
		}
		return null;
	    }
	}
	return varmap;
    }

}
