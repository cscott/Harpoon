package MCC.IR;

import java.util.*;

public class SizeofExpr extends Expr {

    SetExpr setexpr;

    public SizeofExpr(SetExpr setexpr) {
        if (setexpr == null) {
            throw new NullPointerException();
        }

        this.setexpr = setexpr;
    }

    public String name() {
	return "size("+setexpr.name()+")";
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof SizeofExpr))
	    return false;
	SizeofExpr soe=(SizeofExpr)e;
	return setexpr.equals(remap,soe.setexpr);
    }

    public boolean usesDescriptor(Descriptor rd) {
	return setexpr.usesDescriptor(rd);
    }


    public Descriptor getDescriptor() {
	return setexpr.getDescriptor();
    }

    public boolean inverted() {
	return setexpr.inverted();
    }

    public SetExpr getSetExpr() {
        return setexpr;
    }

    public Set getRequiredDescriptors() {
        return setexpr.getRequiredDescriptors();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        setexpr.generate_size(writer, dest);
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("sizeof(");
        setexpr.prettyPrint(pp);
        pp.output(")");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        this.td = ReservedTypeDescriptor.INT;
        return this.td;        
    }

    public Set getInversedRelations() {
        return setexpr.getInversedRelations();
    }
        
}
