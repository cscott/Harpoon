package MCC.IR;

import java.util.*;

public class VarExpr extends Expr {

    String varname;
    VarDescriptor vd = null;
    boolean typechecked = false;

    public Set freeVars() {
	HashSet hs=new HashSet();
	hs.add(vd);
	return hs;
    }

    public VarExpr(String varname) {
        this.varname = varname; 
    }

    public VarExpr(VarDescriptor vd) {
	this.vd=vd;
	varname=vd.getSymbol();
        this.td = vd.getType();	
    }

    public String name() {
	return varname;
    }

    public boolean usesDescriptor(Descriptor d) {
	if (d==vd)
	    return true;
	return false;
    }

    public boolean isNonNull() {
	return true;
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof VarExpr))
	    return false;
	VarExpr ve=(VarExpr)e;
	if (vd==null)
	    throw new Error("Uninitialized VarDescriptor");
	if (ve.vd==null)
	    throw new Error("e has uninitialized VarDescriptor");
	VarDescriptor nvd=vd;
	if (remap!=null&&remap.containsKey(nvd))
	    nvd=(VarDescriptor)remap.get(nvd);
	if (nvd!=ve.vd)
	    return false;
	return true;
    }

    public Set getInversedRelations() {
        return new HashSet();
    }

    public Set getRequiredDescriptors() {
        return new HashSet();
    }
    
    public VarDescriptor getVar() {
        return vd;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {        

        // #TBD#: bit of a hack, really should have been type checked properly 

        vd = (VarDescriptor) writer.getSymbolTable().get(varname);        
        assert vd != null;
        assert vd.getType() != null;
        this.td = vd.getType();

        writer.outputline(vd.getType().getGenerateType().getSafeSymbol() + " " + dest.getSafeSymbol() + 
                          " = (" + vd.getType().getGenerateType().getSafeSymbol() + ") " + vd.getSafeSymbol() + "; //varexpr");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output(varname);
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        typechecked = true;
        vd = (VarDescriptor) sa.getSymbolTable().get(varname);

        if (vd == null) {
            System.out.println(varname);
            sa.getErrorReporter().report(null, "Undefined variable '" + varname + "'");
            return null;
        }
        
        assert vd.getType() != null;

        this.td = vd.getType();
        return this.td;
    }
    
}
