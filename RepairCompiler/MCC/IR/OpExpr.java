package MCC.IR;

import java.util.*;

public class OpExpr extends Expr {

    Expr left;
    Expr right;
    Opcode opcode;

    public OpExpr(Opcode opcode, Expr left, Expr right) {
        this.opcode = opcode;
        this.left = left;
        this.right = right;

        assert (right == null && opcode == Opcode.NOT) || (right != null);
    }

    public Expr getRightExpr() {
	return right;
    }

    public Expr getLeftExpr() {
	return left;
    }

    public Set freeVars() {
	Set lset=left.freeVars();
	Set rset=null;
	if (right!=null)
	    rset=right.freeVars();
	if (lset==null)
	    return rset;
	if (rset!=null)
	    lset.addAll(rset);
	return lset;
    }

    public String name() {
	if (opcode==Opcode.NOT)
	    return "!("+left.name()+")";
	String name=left.name()+opcode.toString();
	if (right!=null)
	    name+=right.name();
	return name;
    }

    public Opcode getOpcode() {
	return opcode;
    }




    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof OpExpr))
	    return false;
	OpExpr oe=(OpExpr)e;
	if (opcode!=oe.opcode)
	    return false;
	if (!left.equals(remap,oe.left))
	    return false;
	if (opcode!=Opcode.NOT)
	    if (!right.equals(remap,oe.right))
		return false;
	return true;
    }

    public DNFRule constructDNF() {
        if (opcode==Opcode.AND) {
            DNFRule leftd=left.constructDNF();
            DNFRule rightd=right.constructDNF();
            return leftd.and(rightd);
        } else if (opcode==Opcode.OR) {
            DNFRule leftd=left.constructDNF();
            DNFRule rightd=right.constructDNF();
            return leftd.or(rightd);
        } else if (opcode==Opcode.NOT) {
            DNFRule leftd=left.constructDNF();
            return leftd.not();
        } else return new DNFRule(this);
    }

    public boolean usesDescriptor(Descriptor d) {
	if (opcode==Opcode.GT||opcode==Opcode.GE||opcode==Opcode.LT||
	    opcode==Opcode.LE||opcode==Opcode.EQ||opcode==Opcode.NE)
	    return left.usesDescriptor(d)||(right!=null&&right.usesDescriptor(d));
	    //	    return right.usesDescriptor(d);
	else
	    return left.usesDescriptor(d)||(right!=null&&right.usesDescriptor(d));
    }
    

    public int[] getRepairs(boolean negated) {
	if (left instanceof RelationExpr)
	    return new int[] {AbstractRepair.MODIFYRELATION};
	if (left instanceof SizeofExpr) {
	    boolean isRelation=((SizeofExpr)left).setexpr instanceof ImageSetExpr;
	    if (isRelation) {
		if (opcode==Opcode.EQ)
		    if (((IntegerLiteralExpr)right).getValue()==0)
			return new int[] {AbstractRepair.REMOVEFROMRELATION};
		    else
			return new int[] {AbstractRepair.ADDTORELATION,
					      AbstractRepair.REMOVEFROMRELATION};
		if (((opcode==Opcode.GE)&&!negated)||
		    ((opcode==Opcode.LE)&&negated)) {
		    if (((IntegerLiteralExpr)right).getValue()==0)
			return new int[0];
		    else
			return new int[]{AbstractRepair.ADDTORELATION}; }
		else
		    return new int[]{AbstractRepair.REMOVEFROMRELATION};
	    } else {
		if (opcode==Opcode.EQ)
		    if (((IntegerLiteralExpr)right).getValue()==0)
			return new int[] {AbstractRepair.REMOVEFROMSET};			
		    else
			return new int[] {AbstractRepair.ADDTOSET,
					      AbstractRepair.REMOVEFROMSET};
		
		if (((opcode==Opcode.GE)&&!negated)||
		    ((opcode==Opcode.LE)&&negated)) {
		    if (((IntegerLiteralExpr)right).getValue()==0)
			return new int[0];
		    else
			return new int[] {AbstractRepair.ADDTOSET}; }
		else
		    return new int[] {AbstractRepair.REMOVEFROMSET};
	    }
	}
	throw new Error("BAD");
    }
    
    public Descriptor getDescriptor() {
	return left.getDescriptor();
    }

    public boolean inverted() {
	return left.inverted();
    }

    public Set getInversedRelations() {
        Set set = left.getInversedRelations();
        if (right != null) {
            set.addAll(right.getInversedRelations());
        }
        return set;
    }

    public Set getRequiredDescriptors() {
        Set v = left.getRequiredDescriptors();
     
        if (right != null) {
            v.addAll(right.getRequiredDescriptors());
        }

        return v;
    }   

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor ld = VarDescriptor.makeNew("leftop");
        left.generate(writer, ld);        
        VarDescriptor rd = null;

        if (right != null) {
            rd = VarDescriptor.makeNew("rightop");
            right.generate(writer, rd);
        }

        String code;
        if (opcode != Opcode.NOT) { /* two operands */
            assert rd != null;
            writer.outputline("int " + dest.getSafeSymbol() + " = " + 
                              ld.getSafeSymbol() + " " + opcode.toString() + " " + rd.getSafeSymbol() + ";");
        } else {
            writer.outputline("int " + dest.getSafeSymbol() + " = !" + ld.getSafeSymbol() + ";");
        }
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("(");
        if (opcode == Opcode.NOT) {
            left.prettyPrint(pp);
        } else {           
            left.prettyPrint(pp);
            pp.output(" " + opcode.toString() + " ");
            assert right != null;
            right.prettyPrint(pp);
        }
        pp.output(")");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        TypeDescriptor lt = left.typecheck(sa);
        TypeDescriptor rt = right == null ? null : right.typecheck(sa);

        if (lt == null) {
            return null;
        } else if (right != null && rt == null) {
            return null;
        }

        boolean ok = true;

        // #ATTN#: if we want node.next != literal(0) to represent a null check than we need to allow ptr arithmetic
        // either that or we use a isvalid clause to check for null

        /*
        if (lt != ReservedTypeDescriptor.INT) {
            sa.getErrorReporter().report(null, "Left hand side of expression is of type '" + lt.getSymbol() + "' but must be type 'int'");
            ok = false;
        }

        if (right != null) {
            if (rt != ReservedTypeDescriptor.INT) {
                sa.getErrorReporter().report(null, "Right hand side of expression is of type '" + rt.getSymbol() + "' but must be type 'int'");
                ok = false;
            }
        }
        */

        if (!ok) {
            return null;
        }

        this.td = ReservedTypeDescriptor.INT;
        return this.td;
    }

}





