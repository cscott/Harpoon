package MCC.IR;

class Updates {
    static public final int EXPR=0;
    static public final int POSITION=1;
    static public final int ABSTRACT=2;
    int type=-1;
    int rightposition;
    Expr rightexpr;
    Expr leftexpr;
    Opcode opcode;
    boolean negate;

    public String toString() {
	String st="type="+type+"\n";
	st+="rightposition="+rightposition+"\n";
	if (rightexpr!=null)
	    st+="rightexpr="+rightexpr.name()+"\n";
	if (leftexpr!=null)
	    st+="leftexpr="+leftexpr.name()+"\n";
	st+="opcode="+opcode+"\n";
	st+="negate="+negate+"\n";
	return st;
    }

    public Updates(Expr lexpr, Expr rexpr, Opcode op, boolean negate) {
	leftexpr=lexpr;
	type=Updates.EXPR;
	if (negate) {
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

	opcode=Opcode.EQ;
	/* Get rid of everything but NE */
	if (op==Opcode.GT) {
	    rightexpr=new OpExpr(Opcode.ADD,rexpr,new IntegerLiteralExpr(1));
	} else if (op==Opcode.GE) {
	    rightexpr=rexpr;
	} else if (op==Opcode.LT) {
	    rightexpr=new OpExpr(Opcode.SUB,rexpr,new IntegerLiteralExpr(1));
	} else if (op==Opcode.LE) {
	    rightexpr=rexpr;
	} else if (op==Opcode.EQ) {
	    rightexpr=rexpr;
	} else if (op==Opcode.NE) {
	    opcode=Opcode.NE;
	}
    }

    boolean isGlobal() {
	if (leftexpr instanceof VarExpr)
	    return true;
	else return false;
    }



    Descriptor getDescriptor() {
	if (isGlobal()) {
	    return ((VarExpr)leftexpr).getVar();
	} else if (isField()) {
	    return ((DotExpr)leftexpr).getField();
	} else {
	    System.out.println(toString());
	    throw new Error("Unrecognized Update");
	}
    }

    boolean isField() {
	if (leftexpr instanceof DotExpr) {
	    assert ((DotExpr)leftexpr).getIndex()==null;
	    return true;
	} else
	    return false;
    }
    
    
    Opcode getOpcode() {
	return opcode;
    }

    public Updates(Expr lexpr, Expr rexpr) {
	leftexpr=lexpr;
	rightexpr=rexpr;
	type=Updates.EXPR;
	opcode=Opcode.EQ;
    }

    public Updates(Expr lexpr, int rpos) {
	leftexpr=lexpr;
	rightposition=rpos;
	type=Updates.POSITION;
	opcode=null;
    }

    boolean isAbstract() {
	return type==Updates.ABSTRACT;
    }

    public Updates(Expr lexpr,boolean negates) {
	leftexpr=lexpr;
	type=Updates.ABSTRACT;
	negate=negates;
	opcode=null;
    }

    public int getType() {
	return type;
    }
    public Expr getLeftExpr() {
	return leftexpr;
    }
    public int getRightPos() {
	assert type==Updates.POSITION;
	return rightposition;
    }
    public Expr getRightExpr() {
	assert type==Updates.EXPR;
	return rightexpr;
    }
}