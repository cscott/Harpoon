package MCC.IR;

import java.util.*;

public class ElementOfExpr extends Expr {

    Expr element;
    SetDescriptor set;

    public Set freeVars() {
	return element.freeVars();
    }

    public ElementOfExpr(Expr element, SetDescriptor set) {
        if (element == null || set == null) {
            throw new NullPointerException();
        }
        this.element = element;
        this.set = set;
    }
    public boolean usesDescriptor(Descriptor d) {
	if (d==set)
	    return true;
	return element.usesDescriptor(d);
    }
    public Set useDescriptor(Descriptor d) {
	HashSet newset=new HashSet();
	if (d==set)
	    newset.add(this);
	newset.addAll(element.useDescriptor(d));
	return newset;
    }

    public String name() {
	return element.name()+" in "+set.toString();
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof ElementOfExpr))
	    return false;
	ElementOfExpr eoe=(ElementOfExpr)e;
	if (eoe.set!=set)
	    return false;
	if (!element.equals(remap,eoe.element))
	    return false;
	return true;
    }

    public Set getRequiredDescriptors() {
        Set v = element.getRequiredDescriptors();
        v.add(set);
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor ed = VarDescriptor.makeNew("element");
        element.generate(writer, ed);
        writer.outputline("int " + dest.getSafeSymbol() + " = " + 
                          set.getSafeSymbol() + "_hash->contains(" + ed.getSafeSymbol() + ");");
    }
    
    public void prettyPrint(PrettyPrinter pp) {
        element.prettyPrint(pp);
        pp.output(" in? " + set.getSafeSymbol());
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        TypeDescriptor td = element.typecheck(sa);
        
        if (td == null) {
            return null;
        }

        TypeDescriptor settype = set.getType();

        if (!td.equals(settype)) {
            sa.getErrorReporter().report(null, "Type mismatch: attempting to test for types '" + td.getSymbol() + "' in set of type '" + settype.getSymbol() + "'");
            return null;
        }
        
        this.td = ReservedTypeDescriptor.INT;
        return this.td;
    }

}

