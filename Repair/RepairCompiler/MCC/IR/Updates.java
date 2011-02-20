package MCC.IR;

class Updates {
    static public final int EXPR=0;
    static public final int POSITION=1;
    static public final int ACCESSPATH=2;
    static public final int ABSTRACT=3;

    int type=-1;
    int rightposition;
    Expr rightexpr;
    Expr leftexpr;
    Opcode opcode;
    boolean negate=false;
    ArrayAnalysis.AccessPath ap;
    int fieldnum;

    public Updates(Expr lexpr, int fieldnum, ArrayAnalysis.AccessPath ap, Expr rexpr, int slot) {
	this.leftexpr=lexpr;
	this.fieldnum=fieldnum;
	this.ap=ap;
	this.rightposition=slot;
	this.rightexpr=rexpr;
	this.type=Updates.ACCESSPATH;
	this.opcode=Opcode.EQ;
    }

    public Updates(Expr lexpr, Expr rexpr, Opcode op, boolean negate) {
	if (!lexpr.isValue(null))
	    System.out.println("Building invalid update");
	leftexpr=lexpr;
	type=Updates.EXPR;

	op=Opcode.translateOpcode(negate,op);

	opcode=op;
	rightexpr=rexpr;
    }

    public Updates(Expr lexpr, Expr rexpr) {
	if (!lexpr.isValue(null))
	    System.out.println("Building invalid update");
	leftexpr=lexpr;
	rightexpr=rexpr;
	type=Updates.EXPR;
	opcode=Opcode.EQ;
    }

    public Updates(Expr lexpr, int rpos, TypeDescriptor td) {
	if (!lexpr.isValue(td))
	    System.out.println("Building invalid update");
	leftexpr=lexpr;
	rightposition=rpos;
	type=Updates.POSITION;
	opcode=Opcode.EQ;
    }

    public Updates(Expr lexpr,boolean negates) {
	leftexpr=lexpr;
	type=Updates.ABSTRACT;
	negate=negates;
	opcode=null;
    }

    VarDescriptor getVar() {
	if (isGlobal()) {
	    return ((VarExpr)leftexpr).getVar();
	} else if (isField()) {
	    Expr e=leftexpr;
	    do {
		for(;e instanceof DotExpr;e=((DotExpr)e).getExpr()) ;
		if (e instanceof VarExpr)
		    break;
		if (e instanceof CastExpr)
		    e=((CastExpr)e).getExpr();
		else throw new Error("Unrecognized Expr:"+e.name());
	    } while(true);
	    return ((VarExpr)e).getVar();
	} else {
	    System.out.println(toString());
	    throw new Error("Unrecognized Update");
	}
    }

    Descriptor getDescriptor() {
	Expr lexpr=leftexpr;
	while (lexpr instanceof CastExpr)
	    lexpr=((CastExpr)lexpr).getExpr();

	if (isGlobal()) {
	    return ((VarExpr)lexpr).getVar();
	} else if (isField()) {
	    return ((DotExpr)lexpr).getField();
	} else {
	    System.out.println(toString());
	    throw new Error("Unrecognized Update");
	}
    }

    boolean isGlobal() {
	Expr lexpr=leftexpr;
	while (lexpr instanceof CastExpr)
	    lexpr=((CastExpr)lexpr).getExpr();

	if (lexpr instanceof VarExpr)
	    return true;
	else return false;
    }

    boolean isField() {
	Expr lexpr=leftexpr;
	while (lexpr instanceof CastExpr)
	    lexpr=((CastExpr)lexpr).getExpr();

	if (lexpr instanceof DotExpr) {
	    assert ((DotExpr)lexpr).getIndex()==null;
	    return true;
	} else
	    return false;
    }
    
    boolean isExpr() {
	return type==Updates.EXPR;
    }

    
    Opcode getOpcode() {
	return opcode;
    }

    boolean isAbstract() {
	return type==Updates.ABSTRACT;
    }

    public int getType() {
	return type;
    }

    public Expr getLeftExpr() {
	return leftexpr;
    }

    public int getRightPos() {
	assert type==Updates.POSITION||type==Updates.ACCESSPATH;
	return rightposition;
    }

    public Expr getRightExpr() {
	assert type==Updates.EXPR;
	return rightexpr;
    }

    public int getFieldNum() {
	assert type==Updates.ACCESSPATH;
	return fieldnum;
    }

    public ArrayAnalysis.AccessPath getAccessPath() {
	assert type==Updates.ACCESSPATH;
	return ap;
    }

    public String toString() {
	if (type==EXPR)
	    return leftexpr.name()+opcode.toString()+rightexpr.name();
	else if (type==POSITION)
	    return leftexpr.name()+opcode.toString()+"Position("+String.valueOf(rightposition)+")";
	else if (type==ACCESSPATH) {
	    return leftexpr.name()+opcode.toString()+"Field("+fieldnum+","+ap+") of Position("+String.valueOf(rightposition)+")";
	} else if (type==ABSTRACT) {
	    if (negate) return "!"+leftexpr.name();
	    else return leftexpr.name();
	} else throw new Error("Unrecognized type");
    }
}
