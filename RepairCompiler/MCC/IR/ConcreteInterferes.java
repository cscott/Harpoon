package MCC.IR;
import java.util.*;

class ConcreteInterferes {
    static public boolean interferes(MultUpdateNode mun, Rule r, boolean satisfy) {
	if (!initialinterferes(mun,r,satisfy)) /* Can't falsify a rule adding something to a set on an initial addition*/
	    return false;
	for(int i=0;i<mun.numUpdates();i++) {
	    UpdateNode un=mun.getUpdate(i);
	    for (int j=0;j<un.numUpdates();j++) {
		Updates update=un.getUpdate(j);
		
		DNFRule drule=r.getDNFGuardExpr();
		if (satisfy)
		    drule=r.getDNFNegGuardExpr();


		if (!update.isAbstract()) {
		    Descriptor updated_des=update.getDescriptor();
		    assert updated_des!=null;
		    /* Update is local to this rule, and the effect is intentional */
		    /* If we're adding something, a side effect could be to falsify some other binding
		       If we're removing something, there is no similar side effect */
		    if ((un.getRule()==r)&&
			(((mun.op==MultUpdateNode.ADD)&&satisfy)||(mun.op==MultUpdateNode.REMOVE))&&
			(r.numQuantifiers()==1)&&
			(r.getQuantifier(0) instanceof SetQuantifier)&&
			update.isField()&&
			(((DotExpr)update.getLeftExpr()).getExpr() instanceof VarExpr)&&
			((SetQuantifier)r.getQuantifier(0)).getVar()==((VarExpr)((DotExpr)update.getLeftExpr()).getExpr()).getVar())
			continue;
    		    if ((un.getRule()==r)&&
			(((mun.op==MultUpdateNode.ADD)&&satisfy)||(mun.op==MultUpdateNode.REMOVE))&&
			(r.numQuantifiers()==0))
			continue;


		    if (r.getInclusion().usesDescriptor(updated_des))
			return true; /* Interferes with inclusion condition */
		    
		    for(int k=0;k<drule.size();k++) {
			RuleConjunction rconj=drule.get(k);
			for(int l=0;l<rconj.size();l++) {


			    DNFExpr dexpr=rconj.get(l);
			    /* See if update interferes w/ dexpr */
			    if (interferes(un,update, r,dexpr))
				return true;
			}
		    }
		}
	    }
	}
	return false;
    }

    static private boolean initialinterferes(MultUpdateNode mun, Rule r, boolean satisfy) {
	AbstractRepair ar=mun.getRepair();
	if (satisfy)
	    return true;
	if (ar==null)
	    return true;
	if (ar.getType()!=AbstractRepair.ADDTOSET)
	    return true;
	//	if (mun.op!=MultUpdateNode.ADD)  (Redundant)
	//    return true;
	if (!r.getInclusion().getTargetDescriptors().contains(ar.getDescriptor()))
	    return true;
	boolean negated=ar.getPredicate().isNegated();
	Predicate p=ar.getPredicate().getPredicate();
	if (!(p instanceof ExprPredicate))
	    return true;
	ExprPredicate ep=(ExprPredicate)p;
	if (ep.getType()!=ExprPredicate.SIZE)
	    return true;
	if ((ep.getOp()==Opcode.EQ)&&(ep.leftsize()==1)&&!negated)
	    return false;
	if ((ep.getOp()==Opcode.NE)&&(ep.leftsize()==1)&&negated)
	    return false;

	if ((ep.getOp()==Opcode.NE)&&(ep.leftsize()==0)&&!negated)
	    return false;
	if ((ep.getOp()==Opcode.EQ)&&(ep.leftsize()==0)&&negated)
	    return false;



	if ((ep.getOp()==Opcode.GT)&&(ep.leftsize()==0)&&!negated)
	    return false;
	if ((ep.getOp()==Opcode.LE)&&(ep.leftsize()==0)&&negated)
	    return false;

	if ((ep.getOp()==Opcode.GE)&&(ep.leftsize()==1)&&!negated)
	    return false;
	if ((ep.getOp()==Opcode.LT)&&(ep.leftsize()==1)&&negated)
	    return false;
	
	return true;

	
    }

    static private boolean interferes(UpdateNode un,Updates update, Rule r,DNFExpr dexpr) {
	Descriptor descriptor=update.getDescriptor();
	if (!dexpr.getExpr().usesDescriptor(descriptor))
	    return false;
	    
	/* We need to pair the variables */
	if (update.isExpr()) {
	    Set vars=update.getRightExpr().freeVars();
	    Opcode op1=update.getOpcode();
	    Expr lexpr1=update.getLeftExpr();
	    Expr rexpr1=update.getRightExpr();
	    boolean good=true;
	    for(Iterator it=vars.iterator();it.hasNext();) {
		VarDescriptor vd=(VarDescriptor) it.next();
		if (un.getBinding(vd)!=null) {
		    /* VarDescriptor isn't a global */
		    if (update.getVar()!=vd) {
			good=false;
			break;
		    }
		}
	    }
	    if (good&&(dexpr.getExpr() instanceof OpExpr)) {
		OpExpr expr=(OpExpr)dexpr.getExpr();
		Expr lexpr2=expr.getLeftExpr();
		Expr rexpr2=expr.getRightExpr();
		Opcode op2=expr.getOpcode();
		if (dexpr.getNegation()) {
		    /* remove negation through opcode translation */
		    if (op2==Opcode.GT)
			op2=Opcode.LE;
		    else if (op2==Opcode.GE)
			op2=Opcode.LT;
		    else if (op2==Opcode.EQ)
			op2=Opcode.NE;
		    else if (op2==Opcode.NE)
			op2=Opcode.EQ;
		    else if (op2==Opcode.LT)
			op2=Opcode.GE;
		    else if (op2==Opcode.LE)
			op2=Opcode.GT;
		}
		good=true;
		vars=rexpr2.freeVars();
		VarDescriptor leftdescriptor=null;
		if (lexpr2 instanceof VarExpr)
		    leftdescriptor=((VarExpr)lexpr2).getVar();
		else if (lexpr2 instanceof DotExpr) {
		    Expr e=lexpr2;
		    for(;e instanceof DotExpr;e=((DotExpr)e).getExpr()) ;
		    leftdescriptor=((VarExpr)e).getVar();
		} else throw new Error("Bad Expr");
		
		for(Iterator it=vars.iterator();it.hasNext();) {
		    VarDescriptor vd=(VarDescriptor) it.next();
		    if (un.getBinding(vd)!=null) {
			/* VarDescriptor isn't a global */
			if (leftdescriptor!=vd) {
			    good=false;
			    break;
			}
		    }
		}
		if (good) {
		    HashMap remap=new HashMap();
		    remap.put(update.getVar(),leftdescriptor);
		    if ((op1==op2)&&
			lexpr1.equals(remap,lexpr2)&&
			rexpr1.equals(remap,rexpr2))
			return false;
		}
	    }
	}
	return true;
    }
}
