package MCC.IR;
import MCC.State;
import java.util.*;

public class ImplicitSchema {
    State state;
    SetAnalysis setanalysis;
    public ImplicitSchema(State state) {
	this.state=state;
	this.setanalysis=new SetAnalysis(state);
    }

    public void update() {
	updaterules();
	updateconstraints();
	updaterelationconstraints();
    }

    void updaterelationconstraints() {
	Vector reldescriptors=state.stRelations.getAllDescriptors();
	for(int i=0;i<reldescriptors.size();i++) {
	    RelationDescriptor rd=(RelationDescriptor) reldescriptors.get(i);
	    Constraint c=new Constraint();
	    
	    /* Construct quantifier */
	    RelationQuantifier rq=new RelationQuantifier();
	    String varname1=new String("partitionvar1");
	    String varname2=new String("partitionvar2");
	    VarDescriptor var1=new VarDescriptor(varname1);
	    VarDescriptor var2=new VarDescriptor(varname2);
	    c.getSymbolTable().add(var1);
	    c.getSymbolTable().add(var2);
	    var1.setType(rd.getDomain().getType());
	    var2.setType(rd.getRange().getType());
	    rq.setTuple(var1,var2);
	    rq.setRelation(rd);
	    c.addQuantifier(rq);

	    VarExpr ve1=new VarExpr(varname1);
	    SetExpr se1=new SetExpr(rd.getDomain());
	    LogicStatement incpred1=new InclusionPredicate(ve1,se1);

	    VarExpr ve2=new VarExpr(varname2);
	    SetExpr se2=new SetExpr(rd.getRange());
	    LogicStatement incpred2=new InclusionPredicate(ve2,se2);
	    c.setLogicStatement(new LogicStatement(LogicStatement.AND,incpred1,incpred2));
	    state.vConstraints.add(c);
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
			VarExpr ve=new VarExpr(varname);
			SetExpr se=new SetExpr((SetDescriptor) sd.getSubsets().get(k));
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
	for(int i=0;i<oldrules.size();i++) {
	    Rule r=(Rule)oldrules.get(i);
	    if (r.inclusion instanceof SetInclusion) {
		SetDescriptor sd=((SetInclusion)r.inclusion).getSet();
		Set supersets=setanalysis.getSuperset(sd);
		if (supersets!=null)
		    for(Iterator superit=supersets.iterator();superit.hasNext();) {
			SetDescriptor sd1=(SetDescriptor)superit.next();
			Rule nr=new Rule();
			nr.guard=r.guard;
			nr.quantifiers=r.quantifiers;
			nr.isstatic=r.isstatic;
			nr.isdelay=r.isdelay;
			nr.inclusion=new SetInclusion(((SetInclusion)r.inclusion).elementexpr,sd1);
			nr.st=r.st;
			newrules.add(nr);
		    }
	    }
	}
	oldrules.addAll(newrules);
    }
}
