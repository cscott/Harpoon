package MCC.IR;

import java.util.*;

public class SumExpr extends Expr {

    SetDescriptor sd;
    RelationDescriptor rd;


    public SumExpr(SetDescriptor sd, RelationDescriptor rd) {
        if (sd == null||rd==null) {
            throw new NullPointerException();
        }
        this.sd=sd;
        this.rd=rd;
    }

    public String name() {
	return "sum("+sd.getSafeSymbol()+"."+rd.getSafeSymbol()+")";
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof SumExpr))
	    return false;
	SumExpr se=(SumExpr)e;
	return (se.sd==sd)&&(se.rd==rd);
    }

    public boolean usesDescriptor(Descriptor d) {
        return (sd==d)||(rd==d);
    }

    public Set useDescriptor(Descriptor d) {
        HashSet newset=new HashSet();
        if ((d==sd)||(d==rd))
            newset.add(this);
        return newset;
    }

    public Descriptor getDescriptor() {
        throw new Error();
    }

    public boolean inverted() {
	return false;
    }

    public Set getRequiredDescriptors() {
        HashSet v=new HashSet();
        v.add(sd);
        v.add(rd);
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        throw new Error();
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("sum(");
        pp.output(sd.getSafeSymbol());
        pp.output(".");
        pp.output(rd.getSafeSymbol());
        pp.output(")");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        this.td = ReservedTypeDescriptor.INT;
        return this.td;
    }

    public Set getInversedRelations() {
        return new HashSet();
    }

}
