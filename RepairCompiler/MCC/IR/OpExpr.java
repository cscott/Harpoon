package MCC.IR;

import java.util.*;

public class OpExpr extends Expr {

    Expr left;
    Expr right;
    Opcode opcode;

    public Expr getUpper() {
	Expr lupper=left.getUpper();
	if (lupper==null)
	    return null;
	if (right!=null) {
	    Expr rupper=right.getUpper();
	    if (rupper==null)
		return null;
	    OpExpr oe=new OpExpr(this.opcode,lupper,rupper);
	    oe.td = ReservedTypeDescriptor.INT;
	    return oe;
	} else return lupper;
    }

    public Expr getLower() {
	Expr llower=left.getLower();
	if (llower==null)
	    return null;
	if (right!=null) {
	    Expr rlower=right.getLower();
	    if (rlower==null)
		return null;
	    OpExpr oe=new OpExpr(this.opcode,llower,rlower);
	    oe.td = ReservedTypeDescriptor.INT;
	    return oe;
	} else return llower;
    }


    public boolean isSafe() {
	if (right==null)
	    return left.isSafe();
	return left.isSafe()&&right.isSafe();
    }

    public boolean isInvariant(Set vars) {
	return left.isInvariant(vars)&&((right==null)||right.isInvariant(vars));
    }

    public Set findInvariants(Set vars) {
	if (isInt(this)) {
	    /* Don't hoist ints */
	    return new HashSet();
	} else if (isInvariant(vars)) {
	    Set s=new HashSet();
	    s.add(this);
	    return s;
	} else {
	    Set ls=left.findInvariants(vars);
	    if (right!=null)
		ls.addAll(right.findInvariants(vars));
	    return ls;
	}
    }

    public Set getfunctions() {
	Set leftfunctions=left.getfunctions();
	Set rightfunctions=null;
	if (right!=null) rightfunctions=right.getfunctions();
	if (leftfunctions!=null&&rightfunctions!=null) {
	    HashSet functions=new HashSet();
	    functions.addAll(leftfunctions);
	    functions.addAll(rightfunctions);
	    return functions;
	}
	if (leftfunctions!=null)
	    return leftfunctions;
	return rightfunctions;
    }

    public static boolean isInt(Expr e) {
	if (e==null)
	    return false;
	if ((e instanceof IntegerLiteralExpr)||
	    ((e instanceof OpExpr)&&(((OpExpr)e).opcode==Opcode.NOP)&&(((OpExpr)e).getLeftExpr() instanceof IntegerLiteralExpr)))
	    return true;
	return false;
    }

    public static int getInt(Expr e) {
	if (e instanceof IntegerLiteralExpr)
	    return ((IntegerLiteralExpr)e).getValue();
	else if ((e instanceof OpExpr) && (((OpExpr)e).getLeftExpr() instanceof IntegerLiteralExpr))
	    return ((IntegerLiteralExpr)((OpExpr)e).getLeftExpr()).getValue();
	else throw new Error();
    }

    public OpExpr(Opcode opcode, Expr left, Expr right) {
	if ((isInt(left)&&isInt(right))||
	    (isInt(left)&&(opcode==Opcode.NOT))||
	    (isInt(left)&&(opcode==Opcode.RND))) {
	    this.opcode=Opcode.NOP;
	    this.right=null;
	    int lint=isInt(left)?getInt(left):0;
	    int rint=isInt(right)?getInt(right):0;
	    int value=0;
	    if (opcode==Opcode.ADD) {
		value=lint+rint;
	    } else if (opcode==Opcode.SUB) {
		value=lint-rint;
	    } else if (opcode==Opcode.SHL) {
		value=lint<<rint;
	    } else if (opcode==Opcode.SHR) {
		value=lint>>rint;
	    } else if (opcode==Opcode.MULT) {
		value=lint*rint;
	    } else if (opcode==Opcode.DIV) {
		value=lint/rint;
	    } else if (opcode==Opcode.GT) {
		if (lint>rint)
		    value=1;
	    } else if (opcode==Opcode.GE) {
		if (lint>=rint)
		    value=1;
	    } else if (opcode==Opcode.LT) {
		if (lint<rint)
		    value=1;
	    } else if (opcode==Opcode.LE) {
		if (lint<=rint)
		    value=1;
	    } else if (opcode==Opcode.EQ) {
		if (lint==rint)
		    value=1;
	    } else if (opcode==Opcode.NE) {
		if (lint!=rint)
		    value=1;
	    } else if (opcode==Opcode.AND) {
		if ((lint!=0)&&(rint!=0))
		    value=1;
	    } else if (opcode==Opcode.OR) {
		if ((lint!=0)||(rint!=0))
		    value=1;
	    } else if (opcode==Opcode.NOT) {
		if (lint==0)
		    value=1;
	    } else if (opcode==Opcode.RND) {
		value=((lint>>3)<<3);
		if ((lint % 8)!=0)
		    value+=8;
	    } else throw new Error("Unrecognized Opcode");
	    this.left=new IntegerLiteralExpr(value);
	} else if ((opcode==Opcode.MULT)&&
		   ((isInt(left)&&(getInt(left)==0))
		    ||(isInt(right)&&(getInt(right)==0)))) {
	    this.opcode=Opcode.NOP;
	    this.right=null;
	    this.left=new IntegerLiteralExpr(0);
	} else {
	    this.opcode = opcode;
	    this.left = left;
	    this.right = right;
	    assert (right == null && (opcode == Opcode.NOT||opcode==Opcode.RND)) || (right != null);
	}
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
	if (opcode==Opcode.NOP)
	    return left.name();
	if (opcode==Opcode.RND)
	    return "Round("+left.name()+")";
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
	if ((opcode!=Opcode.NOT)&&(opcode!=Opcode.RND)&&(opcode!=Opcode.NOP))
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
	return left.usesDescriptor(d)||(right!=null&&right.usesDescriptor(d));
    }

    public void findmatch(Descriptor d, Set  s) {
	left.findmatch(d,s);
	if (right!=null)
	    right.findmatch(d,s);
    }

    public Set useDescriptor(Descriptor d) {
	HashSet newset=new HashSet();
	newset.addAll(left.useDescriptor(d));
	if (right!=null)
	    newset.addAll(right.useDescriptor(d));
	return newset;
    }

    public int[] getRepairs(boolean negated, Termination t) {
	if (left instanceof RelationExpr)
	    return new int[] {AbstractRepair.MODIFYRELATION};
	if (left instanceof SizeofExpr) {
	    Opcode op=opcode;
	    if (negated) {
		/* remove negation through opcode translation */
		if (op==Opcode.GT)
		    op=Opcode.LE;
		else if (op==Opcode.GE)
		    op=Opcode.LT;
		else if (op==Opcode.EQ)
		    op=Opcode.NE;
		else if (op==Opcode.NE)
		    op=Opcode.EQ;
		else if (op==Opcode.LT)
		    op=Opcode.GE;
		else if (op==Opcode.LE)
		    op=Opcode.GT;
	    }

	    int maxsize=t.maxsize.getsize(getDescriptor());
	    int size=getInt(right);


	    boolean isRelation=((SizeofExpr)left).setexpr instanceof ImageSetExpr;
	    if (isRelation) {
		if (op==Opcode.EQ) {
		    if (size==0)
			return new int[] {AbstractRepair.REMOVEFROMRELATION};
		    else {
			if ((maxsize!=-1)&&maxsize<=size)
			    return new int[] {AbstractRepair.ADDTORELATION};
			return new int[] {AbstractRepair.ADDTORELATION,
					  AbstractRepair.REMOVEFROMRELATION};
		    }
		} else if (op==Opcode.GE||op==Opcode.GT) {
		    return new int[]{AbstractRepair.ADDTORELATION};
		} else if (op==Opcode.LE||op==Opcode.LT) {
		    if ((op==Opcode.LT&&maxsize!=-1&&maxsize<size)||(op==Opcode.LE&&maxsize!=-1&&maxsize<=size))
			return new int[0];
		    return new int[]{AbstractRepair.REMOVEFROMRELATION};
		} else if (op==Opcode.NE) {
		    if (maxsize<size&&maxsize!=-1)
			return new int[0];
		    return new int[]{AbstractRepair.ADDTORELATION};
		} else throw new Error();
	    } else {
		if (op==Opcode.EQ) {
		    if (size==0)
			return new int[] {AbstractRepair.REMOVEFROMSET};
		    else {
			if (maxsize<=size&&maxsize!=-1)
			    return new int[] {AbstractRepair.ADDTOSET};
			return new int[] {AbstractRepair.ADDTOSET,
					      AbstractRepair.REMOVEFROMSET};
		    }
		} else if (op==Opcode.GE||op==Opcode.GT) {
		    return new int[] {AbstractRepair.ADDTOSET};
		} else if (op==Opcode.LE||op==Opcode.LT) {
		    if ((op==Opcode.LT&&maxsize<size&&maxsize!=-1)||(op==Opcode.LE&&maxsize<=size&&maxsize!=-1))
			return new int[0];
		    return new int[] {AbstractRepair.REMOVEFROMSET};
		} else if (op==Opcode.NE) {
		    if (maxsize<size&&maxsize!=-1)
			return new int[0];
		    return new int[] {AbstractRepair.ADDTOSET};
		} else throw new Error();
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
	/* Check for loop invariant hoisting. */
	if (writer.getInvariantValue()!=null&&
	    writer.getInvariantValue().isInvariant(this)) {
	    writer.addDeclaration("int",dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol()+"="+writer.getInvariantValue().getValue(this).getSafeSymbol()+";");
	    writer.outputline("maybe="+writer.getInvariantValue().getMaybe(this).getSafeSymbol()+";");
	    return;
	}

	left.generate(writer, ld);
        VarDescriptor rd = null;
	VarDescriptor lm=VarDescriptor.makeNew("lm");
	VarDescriptor rm=VarDescriptor.makeNew("rm");

        if (right != null) {
	    if ((opcode==Opcode.OR)||
		(opcode==Opcode.AND)) {
		writer.addDeclaration("int",lm.getSafeSymbol());
		writer.outputline(lm.getSafeSymbol()+"=maybe;");
		writer.outputline("maybe=0;");
	    }

            rd = VarDescriptor.makeNew("rightop");
            right.generate(writer, rd);
        }

        String code;
	if (opcode == Opcode.RND) {
	    writer.addDeclaration("int",dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol() + " = (" +
			      ld.getSafeSymbol() + ">>3)<<3; ");
	    writer.outputline("if ("+ld.getSafeSymbol()+" % 8) "+dest.getSafeSymbol()+"+=8;");
	} else if (opcode == Opcode.NOP) {
	    writer.addDeclaration("int", dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol() + " = " +
			      ld.getSafeSymbol() +"; ");
        } else if (opcode == Opcode.AND) {
	    writer.addDeclaration("int",rm.getSafeSymbol());
	    writer.outputline(rm.getSafeSymbol()+"=maybe;");
	    writer.outputline("maybe = (" + ld.getSafeSymbol() + " && " + rm.getSafeSymbol() + ") || (" + rd.getSafeSymbol() + " && " + lm.getSafeSymbol() + ") || (" + lm.getSafeSymbol() + " && " + rm.getSafeSymbol() + ");");
	    writer.addDeclaration("int",dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol() + " = " + ld.getSafeSymbol() + " && " + rd.getSafeSymbol() + ";");
	} else if (opcode == Opcode.OR) {
	    writer.addDeclaration("int",rm.getSafeSymbol());
	    writer.outputline(rm.getSafeSymbol()+"=maybe;");
	    writer.outputline("maybe = (!" + ld.getSafeSymbol() + " && " + rm.getSafeSymbol() + ") || (!" + rd.getSafeSymbol() +
			      " && " + lm.getSafeSymbol() + ") || (" + lm.getSafeSymbol() + " && " + rm.getSafeSymbol() + ");");
	    writer.addDeclaration("int",dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol() + " = " + ld.getSafeSymbol() + " || " + rd.getSafeSymbol() + ";");
	} else if (opcode != Opcode.NOT) { /* two operands */
            assert rd != null;
	    writer.addDeclaration("int", dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol() + " = " +
			      ld.getSafeSymbol() + " " + opcode.toString() + " " + rd.getSafeSymbol() + ";");
        } else if (opcode == Opcode.NOT) {
            writer.addDeclaration("int", dest.getSafeSymbol());
            writer.outputline(dest.getSafeSymbol() + " = !" + ld.getSafeSymbol() + ";");
        }
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("(");
        if (opcode == Opcode.NOT) {
	    pp.output("!");
            left.prettyPrint(pp);
	} else if (opcode == Opcode.NOP) {
            left.prettyPrint(pp);
	} else if (opcode == Opcode.RND) {
	    pp.output("RND ");
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

        if (!ok) {
            return null;
        }

        this.td = ReservedTypeDescriptor.INT;
        return this.td;
    }

}
