package MCC.IR;
import java.util.*;

class ConcreteInterferes {
    static public boolean interferes(MultUpdateNode mun, Rule r, boolean satisfy) {
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
