package MCC.IR;

import java.util.*;

public class SetExpr extends Expr {

    SetDescriptor sd;

    public SetExpr(SetDescriptor sd) {
        this.sd = sd;
    }

    public String name() {
	return sd.toString();
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof SetExpr))
	    return false;
	SetExpr se=(SetExpr)e;
	if (sd!=se.sd)
	    return false;
	return true;
    }

    public boolean usesDescriptor(Descriptor s) {
	return (s==sd);
    }

    public SetExpr() {
        this.sd = null;
    }

    public Set getInversedRelations() {
        return new HashSet();
    }

    public Descriptor getDescriptor() {
	return sd;
    }

    public Set getRequiredDescriptors() {
        HashSet v = new HashSet();
        v.add(sd);
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        throw new IRException("unsupported");
    }

    public void generate_inclusion(CodeWriter writer, VarDescriptor dest, VarDescriptor element) {
        writer.outputline("int " + dest.getSafeSymbol() + " = " + sd.getSafeSymbol() + "_hash->contains(" + element.getSafeSymbol() + ");");
    }    

    public void generate_size(CodeWriter writer, VarDescriptor dest) {
        writer.outputline("int " + dest.getSafeSymbol() + " = " + sd.getSafeSymbol() + "_hash->count();");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output(sd.getSafeSymbol());
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        this.td = sd.getType();
        return this.td;
    }

}


