package MCC.IR;
import java.util.*;

class AbstractInterferes {
    Termination termination;

    public AbstractInterferes(Termination t) {
	termination=t;
    }

    /** Does performing the AbstractRepair ar satisfy (or falsify if satisfy=false)
     * Rule r. */

    static public boolean interfereswithrule(AbstractRepair ar, Rule r, boolean satisfy) {
	boolean mayadd=false;
	boolean mayremove=false;
	switch (ar.getType()) {
	case AbstractRepair.ADDTOSET:
	case AbstractRepair.ADDTORELATION:
	    if (interferesquantifier(ar.getDescriptor(), true, r, satisfy))
		return true;
	    mayadd=true;
	    break;
	case AbstractRepair.REMOVEFROMSET:
	case AbstractRepair.REMOVEFROMRELATION:
	    if (interferesquantifier(ar.getDescriptor(), false, r, satisfy))
		return true;
	    mayremove=true;
	    break;
	case AbstractRepair.MODIFYRELATION:
	    if (interferesquantifier(ar.getDescriptor(), true, r, satisfy))
		return true;
	    if (interferesquantifier(ar.getDescriptor(), false, r, satisfy))
		return true;
	    mayadd=true;
	    mayremove=true;
	break;
	default:
	    throw new Error("Unrecognized Abstract Repair");
	}
	DNFRule drule=null;
	if (satisfy)
	    drule=r.getDNFGuardExpr();
	else
	    drule=r.getDNFNegGuardExpr();
	
	for(int i=0;i<drule.size();i++) {
	    RuleConjunction rconj=drule.get(i);
	    for(int j=0;j<rconj.size();j++) {
		DNFExpr dexpr=rconj.get(j);
		Expr expr=dexpr.getExpr();
		if (expr.usesDescriptor(ar.getDescriptor())) {
		    /* Need to check */
		    if ((mayadd&&!dexpr.getNegation())||(mayremove&&dexpr.getNegation()))
			return true;
		}
	    }
	}
	return false;
    }

    public boolean checkrelationconstraint(AbstractRepair ar, Constraint c) {
	if (c.numQuantifiers()==1&&
	    (c.getQuantifier(0) instanceof RelationQuantifier)) {
	    RelationQuantifier rq=(RelationQuantifier)c.getQuantifier(0);
	    if (rq.getRelation()==ar.getDescriptor()) {
		Hashtable ht=new Hashtable();
		if (ar.getDomainSet()!=null)
		    ht.put(rq.x,ar.getDomainSet());
		if (ar.getRangeSet()!=null)
		    ht.put(rq.y,ar.getRangeSet());
		DNFConstraint dconst=c.dnfconstraint;
	    conjloop:
		for(int i=0;i<dconst.size();i++) {
		    Conjunction conj=dconst.get(i);
		predloop:
		    for(int j=0;j<conj.size();j++) {
			DNFPredicate dpred=conj.get(j);
			Predicate p=dpred.getPredicate();
			if ((p instanceof InclusionPredicate)&&(!dpred.isNegated())) {
			    InclusionPredicate ip=(InclusionPredicate)p;
			    if (ip.expr instanceof VarExpr&&
				ip.setexpr.getDescriptor() instanceof SetDescriptor) {
				VarDescriptor vd=((VarExpr)ip.expr).getVar();
				if (ht.containsKey(vd)) {
				    SetDescriptor td=(SetDescriptor)ip.setexpr.getDescriptor();
				    SetDescriptor s=(SetDescriptor)ht.get(vd);
				    if (td.isSubset(s))
					continue predloop;
				}
			    }
			}
			continue conjloop;
		    }
		    return true;
		}
	    }
	}
	return false;
    }

    /** Does performing the AbstractRepair ar falsify the predicate dp */

    public boolean interfereswithpredicate(AbstractRepair ar, DNFPredicate dp) {
	if ((ar.getDescriptor()!=dp.getPredicate().getDescriptor()) &&
	    //	    ((ar.getDescriptor() instanceof SetDescriptor)||
	    // If the second predicate uses the size of the set, modifying the set size could falsify it...
	    !dp.getPredicate().usesDescriptor(ar.getDescriptor()))
	    //)
	    return false;

	/* This if handles all the c comparisons in the paper */
	if (ar.getDescriptor()==dp.getPredicate().getDescriptor()&&
	    (ar.getType()==AbstractRepair.ADDTOSET||ar.getType()==AbstractRepair.ADDTORELATION)&&
	    (ar.getPredicate().getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate().inverted()==ar.getPredicate().getPredicate().inverted())&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
	    boolean neg1=ar.getPredicate().isNegated();
	    Opcode op1=((ExprPredicate)ar.getPredicate().getPredicate()).getOp();
	    int size1=((ExprPredicate)ar.getPredicate().getPredicate()).rightSize();
	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    int size2=((ExprPredicate)dp.getPredicate()).rightSize();
	    op1=Opcode.translateOpcode(neg1,op1);
	    op2=Opcode.translateOpcode(neg2,op2);

	    if ((op1==Opcode.EQ)||(op1==Opcode.NE)||(op1==Opcode.GT)||op1==Opcode.GE) {
		int size1a=0;
		if((op1==Opcode.EQ)||(op1==Opcode.GE))
		    size1a=size1;
		if((op1==Opcode.GT)||(op1==Opcode.NE))
		    size1a=size1+1;

		if (((op2==Opcode.EQ)&&(size1a==size2))||
		    ((op2==Opcode.NE)&&(size1a!=size2))||
		    (op2==Opcode.GE)||
		    (op2==Opcode.GT)||
		    ((op2==Opcode.LE)&&(size1a<=size2))||
		    ((op2==Opcode.LT)&&(size1a<size2)))
		    return false;
	    }
	}
	if (ar.getDescriptor()==dp.getPredicate().getDescriptor()&&
	    (ar.getType()==AbstractRepair.ADDTOSET||ar.getType()==AbstractRepair.ADDTORELATION)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    int size2=((ExprPredicate)dp.getPredicate()).rightSize();

	    op2=Opcode.translateOpcode(neg2,op2);

	    int maxsize=termination.maxsize.getsize(dp.getPredicate().getDescriptor());

	    if ((maxsize!=-1)&&
		((op2==Opcode.LT&&size2>maxsize)||
		 (op2==Opcode.LE&&size2>=maxsize)||
		 (op2==Opcode.EQ&&size2>=maxsize)))
		return false;

	    if (((op2==Opcode.NE)&&(size2==0))||
		(op2==Opcode.GE)||
		(op2==Opcode.GT))
		return false;
	}
	if (ar.getDescriptor()==dp.getPredicate().getDescriptor()&&
	    (ar.getType()==AbstractRepair.MODIFYRELATION)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate().inverted()==ar.getPredicate().getPredicate().inverted())&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.COMPARISON)) {
	    
	    boolean neg1=ar.getPredicate().isNegated();
	    Opcode op1=((ExprPredicate)ar.getPredicate().getPredicate()).getOp();
	    boolean isInt1=((ExprPredicate)ar.getPredicate().getPredicate()).isRightInt();
	    int size1=isInt1?((ExprPredicate)ar.getPredicate().getPredicate()).rightSize():0;
	    Expr expr1=((OpExpr)((ExprPredicate)ar.getPredicate().getPredicate()).expr).right;


	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    boolean isInt2=((ExprPredicate)dp.getPredicate()).isRightInt();
	    int size2=isInt2?((ExprPredicate)dp.getPredicate()).rightSize():0;
	    Expr expr2=((OpExpr)((ExprPredicate)dp.getPredicate()).expr).right;

	    
	    /* If the left sides of the comparisons are both from
               different sets, the update is orthogonal to the expr dp */
	    {
		Expr lexpr1=((RelationExpr)((OpExpr)((ExprPredicate)ar.getPredicate().getPredicate()).expr).getLeftExpr()).getExpr();
		Expr lexpr2=((RelationExpr)((OpExpr)((ExprPredicate)dp.getPredicate()).expr).getLeftExpr()).getExpr();
		SetDescriptor sd1=lexpr1.getSet();
		SetDescriptor sd2=lexpr2.getSet();
		if (termination.mutuallyexclusive(sd1,sd2))
		    return false;
	    }

	    /* Translate the opcodes */
	    op1=Opcode.translateOpcode(neg1,op1);
	    op2=Opcode.translateOpcode(neg2,op2);
	    
	    /* Obvious cases which can't interfere */
	    if (((op1==Opcode.GT)||
		(op1==Opcode.GE))&&
		((op2==Opcode.GT)||
		 (op2==Opcode.GE)))
		return false;
	    if (((op1==Opcode.LT)||
		(op1==Opcode.LE))&&
		((op2==Opcode.LT)||
		 (op2==Opcode.LE)))
		return false;
	    // FIXME
	    if (((op1==Opcode.EQ)||(op1==Opcode.GE)||(op1==Opcode.LE))&&
		((op2==Opcode.EQ)||(op2==Opcode.GE)||(op2==Opcode.LE))&&
		expr1.equals(null,expr2)) {
		// Need to check free variables...
		return false;
	    }
	    if (isInt1&&isInt2) {
		if (((op1==Opcode.EQ)||(op1==Opcode.GE)||(op1==Opcode.LE))&&
		    ((op2==Opcode.EQ)||(op2==Opcode.GE)||(op2==Opcode.LE))&&
		    size1==size2)
		    return false;
		int size1a=size1;
		int size2a=size2;
		if (op1==Opcode.GT)
		    size1a++;
		if (op1==Opcode.LT)
		    size1a--;
		if (op1==Opcode.NE)
		    size1a++; /*FLAGNE this is current behavior for NE repairs */
		
		if (op2==Opcode.GT)
		    size2a++;
		if (op2==Opcode.LT)
		    size2a--;
		if (((op1==Opcode.GE)||(op1==Opcode.GT))&&
		    ((op2==Opcode.LE)||(op2==Opcode.EQ)||(op2==Opcode.LT))&&
		    (size1a<=size2a))
		    return false;
		if (((op1==Opcode.LE)||(op1==Opcode.LT))&&
		    ((op2==Opcode.GE)||(op2==Opcode.EQ)||(op2==Opcode.GT))&&
		    (size1a>=size2a))
		    return false;
		if ((op1==Opcode.EQ)&&(op2==Opcode.EQ)&&
		    (size1a==size2a))
		    return false;
		if ((op1==Opcode.EQ)&&
		    ((op2==Opcode.LE)||(op2==Opcode.LT))&&
		    (size1a<=size2a))
		    return false;
		if ((op1==Opcode.EQ)&&
		    ((op2==Opcode.GE)||(op2==Opcode.GT))&&
		    (size1a>=size2a))
		    return false;
		if (op2==Opcode.NE)  /*FLAGNE this is current behavior for NE repairs */
		    if (size1a!=size2)
			return false;
		if ((op1==Opcode.NE)&&
		    (op2==Opcode.EQ)&&
		    (size1!=size2))
		    return false;
		if ((op1==Opcode.NE)&& /* FLAGNE relies on increasing or decreasing by 1 */
		    ((op2==Opcode.GT)||(op2==Opcode.GE))&&
		    (size1!=size2a))
		    return false;
		if ((op1==Opcode.NE)&& /* FLAGNE relies on increasing or decreasing by 1 */
		    ((op2==Opcode.LT)||(op2==Opcode.LE))&&
		    (size1!=size2a))
		    return false;
	    }
	}
	/* This handles all the c comparisons in the paper */
	if (ar.getDescriptor()==dp.getPredicate().getDescriptor()&&
	    (ar.getType()==AbstractRepair.REMOVEFROMSET||ar.getType()==AbstractRepair.REMOVEFROMRELATION)&&
	    (ar.getPredicate().getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (dp.getPredicate().inverted()==ar.getPredicate().getPredicate().inverted())&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
	    boolean neg1=ar.getPredicate().isNegated();
	    Opcode op1=((ExprPredicate)ar.getPredicate().getPredicate()).getOp();
	    int size1=((ExprPredicate)ar.getPredicate().getPredicate()).rightSize();
	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    int size2=((ExprPredicate)dp.getPredicate()).rightSize();
	    /* Translate the opcodes */
	    op1=Opcode.translateOpcode(neg1,op1);
	    op2=Opcode.translateOpcode(neg2,op2);

	    if (((op1==Opcode.EQ)||(op1==Opcode.LT)||op1==Opcode.LE)||(op1==Opcode.NE)) {
		int size1a=0;

		if((op1==Opcode.EQ)||(op1==Opcode.LE))
		    size1a=size1;
		if((op1==Opcode.LT)||(op1==Opcode.NE))
		    size1a=size1-1;

		if (((op2==Opcode.EQ)&&(size1a==size2))||
		    ((op2==Opcode.NE)&&(size1a!=size2))||
		    (op2==Opcode.LE)||
		    (op2==Opcode.LT)||
   		    ((op2==Opcode.GE)&&(size1a>=size2))||
		    ((op2==Opcode.GT)&&(size1a>size2)))
		    return false;
	    }
	}

	if (ar.getDescriptor()==dp.getPredicate().getDescriptor()&&
	    (ar.getType()==AbstractRepair.REMOVEFROMSET||ar.getType()==AbstractRepair.REMOVEFROMRELATION)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    int size2=((ExprPredicate)dp.getPredicate()).rightSize();
	    op2=Opcode.translateOpcode(neg2,op2);

	    if (((op2==Opcode.EQ)&&(size2==0))||
		(op2==Opcode.LE)||
		(op2==Opcode.LT))
		return false;
	}
	if ((ar.getDescriptor()==dp.getPredicate().getDescriptor())&&
	    (ar.getType()==AbstractRepair.ADDTOSET||ar.getType()==AbstractRepair.ADDTORELATION)&&
	    (dp.getPredicate() instanceof InclusionPredicate)&&
	    (dp.isNegated()==false))
	    return false; /* Could only satisfy this predicate */

	if ((ar.getDescriptor()==dp.getPredicate().getDescriptor())&&
	    (ar.getType()==AbstractRepair.REMOVEFROMSET||ar.getType()==AbstractRepair.REMOVEFROMRELATION)&&
	    (dp.getPredicate() instanceof InclusionPredicate)&&
	    (dp.isNegated()==true))
	    return false; /* Could only satisfy this predicate */
	return true;
    }

    /** Does the increase (or decrease) in scope of a model defintion
     * rule represented by sn falsify the predicate dp. */

    public boolean scopeinterfereswithpredicate(ScopeNode sn, DNFPredicate dp) {
	if (!sn.getSatisfy()&&(sn.getDescriptor() instanceof SetDescriptor)) {
	    Rule r=sn.getRule();
	    Set target=r.getInclusion().getTargetDescriptors();
	    boolean match=false;
	    for(int i=0;i<r.numQuantifiers();i++) {
		Quantifier q=r.getQuantifier(i);
		if (q instanceof SetQuantifier) {
		    SetQuantifier sq=(SetQuantifier) q;
		    if (target.contains(sq.getSet())) {
			match=true;
			break;
		    }
		}
	    }
	    if (match&&
		sn.getDescriptor()==dp.getPredicate().getDescriptor()&&
		(dp.getPredicate() instanceof ExprPredicate)&&
		(((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
		boolean neg=dp.isNegated();
		Opcode op=((ExprPredicate)dp.getPredicate()).getOp();
		int size=((ExprPredicate)dp.getPredicate()).rightSize();
		op=Opcode.translateOpcode(neg,op);

		if ((op==Opcode.GE)&&
		    ((size==0)||(size==1)))
		    return false;
		if ((op==Opcode.GT)&&
		    (size==0))
		    return false;
	    }
	}
	return interferes(sn.getDescriptor(), sn.getSatisfy(),dp);
    }

    /** Does increasing (or decreasing if satisfy=false) the size of
     * the set or relation des falsify the predicate dp. */

    private boolean interferes(Descriptor des, boolean satisfy, DNFPredicate dp) {
	if ((des!=dp.getPredicate().getDescriptor()) &&
	    //((des instanceof SetDescriptor)||
	    !dp.getPredicate().usesDescriptor(des))//)
	    return false;

	/* This if handles all the c comparisons in the paper */
	if (des==dp.getPredicate().getDescriptor()&&
	    (satisfy)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    int size2=((ExprPredicate)dp.getPredicate()).rightSize();
	    op2=Opcode.translateOpcode(neg2,op2);


	    int maxsize=termination.maxsize.getsize(dp.getPredicate().getDescriptor());
	    {
		if ((maxsize!=-1)&&
		    ((op2==Opcode.LT&&size2>maxsize)||
		     (op2==Opcode.LE&&size2>=maxsize)||
		     (op2==Opcode.EQ&&size2>=maxsize)))
		    return false;

		if ((op2==Opcode.GE)||
		    (op2==Opcode.GT)||
		    (op2==Opcode.NE)&&(size2==0))
		    return false;
	    }
	}
	/* This if handles all the c comparisons in the paper */
	if (des==dp.getPredicate().getDescriptor()&&
	    (!satisfy)&&
	    (dp.getPredicate() instanceof ExprPredicate)&&
	    (((ExprPredicate)dp.getPredicate()).getType()==ExprPredicate.SIZE)) {
	    boolean neg2=dp.isNegated();
	    Opcode op2=((ExprPredicate)dp.getPredicate()).getOp();
	    int size2=((ExprPredicate)dp.getPredicate()).rightSize();
	    op2=Opcode.translateOpcode(neg2,op2);
	    {
		if (((op2==Opcode.EQ)&&(size2==0))||
		    (op2==Opcode.LE)||
		    (op2==Opcode.LT))
		    return false;
	    } 
	}
	if ((des==dp.getPredicate().getDescriptor())&&
	    satisfy&&
	    (dp.getPredicate() instanceof InclusionPredicate)&&
	    (dp.isNegated()==false))
	    return false; /* Could only satisfy this predicate */

	if ((des==dp.getPredicate().getDescriptor())&&
	    (!satisfy)&&
	    (dp.getPredicate() instanceof InclusionPredicate)&&
	    (dp.isNegated()==true))
	    return false; /* Could only satisfy this predicate */

	return true;
    }

    /** This method test whether satisfying (or falsifying depending
     * on the flag satisfy) a rule that adds an object(or tuple) to
     * the set(or relation) descriptor may increase (or decrease
     * depending on the flag satisfyrule) the scope of a constraint or
     * model defintion rule r.  */

    static private boolean interferesquantifier(Descriptor des, boolean satisfy, Quantifiers r, boolean satisfyrule) {
	for(int i=0;i<r.numQuantifiers();i++) {
	    Quantifier q=r.getQuantifier(i);
	    if (q instanceof RelationQuantifier||q instanceof SetQuantifier) {
		if (q.getRequiredDescriptors().contains(des)&&(satisfy==satisfyrule))
		    return true;
	    } else if (q instanceof ForQuantifier) {
		if (q.getRequiredDescriptors().contains(des))
		    return true;
	    } else throw new Error("Unrecognized Quantifier");
	}
	return false;
    }

    static public boolean interferesquantifier(AbstractRepair ar, Quantifiers q) {
	if (ar.getType()==AbstractRepair.ADDTOSET||ar.getType()==AbstractRepair.ADDTORELATION)
	    return interferesquantifier(ar.getDescriptor(),true,q,true);
	return false;
    }

    static public boolean interfereswithquantifier(Descriptor des, boolean satisfy, Quantifiers q) {
	return interferesquantifier(des, satisfy, q,true);
    }

    static public boolean interfereswithrule(Descriptor des, boolean satisfy, Rule r, boolean satisfyrule) {
	if (interferesquantifier(des,satisfy,r,satisfyrule))
	    return true;
	/* Scan DNF form */
	DNFRule drule=r.getDNFGuardExpr();
	for(int i=0;i<drule.size();i++) {
	    RuleConjunction rconj=drule.get(i);
	    for(int j=0;j<rconj.size();j++) {
		DNFExpr dexpr=rconj.get(j);
		Expr expr=dexpr.getExpr();
		boolean negated=dexpr.getNegation();
		/*
		  satisfy  negated
		  Yes      No             Yes
		  Yes      Yes            No
		  No       No             No
		  No       Yes            Yes
		*/
		boolean satisfiesrule=(satisfy^negated);/*XOR of these */
		if (satisfiesrule==satisfyrule) {
		    /* Effect is the one being tested for */
		    /* Only expr's to be concerned with are TupleOfExpr and
		       ElementOfExpr */
		    if (expr.getRequiredDescriptors().contains(des)) {
			if (((expr instanceof ElementOfExpr)||
			    (expr instanceof TupleOfExpr))&&
			    (expr.getRequiredDescriptors().size()==1))
			    return true;
			else
			    throw new Error("Unrecognized EXPR");
		    }
		}
	    }
	}
	return false;
    }
}
