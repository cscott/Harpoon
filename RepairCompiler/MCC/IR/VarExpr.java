package MCC.IR;

import java.util.*;

public class VarExpr extends Expr {
    static boolean DOMEMCHECKS=false;
    static boolean DOTYPECHECKS=false;
    static boolean DONULL=false;

    String varname;
    VarDescriptor vd = null;
    boolean typechecked = false;

    public Set freeVars() {
	HashSet hs=new HashSet();
	hs.add(vd);
	return hs;
    }

    public Expr getLower() {
	return vd.getLower();
    }

    public Expr getUpper() {
	return vd.getUpper();
    }

    public SetDescriptor getSet() {
	if (vd==null)
	    return null;
	return vd.getSet();
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

    public Set useDescriptor(Descriptor d) {
	HashSet newset=new HashSet();
	if (d==vd)
	    newset.add(this);
	return newset;
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

    public boolean isValue() {
	return vd.isGlobal();
    }

    public boolean isInvariant(Set vars) {
	return vd.isGlobal()||vars.contains(vd);
    }

    public Set findInvariants(Set vars) {
	if (isInvariant(vars)) {
	    Set s=new HashSet();
	    s.add(this);
	    return s;
	} else
	    return new HashSet();
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        // #TBD#: bit of a hack, really should have been type checked properly
        assert vd != null;
        assert vd.getType() != null;
	this.td = vd.getType();

	if (writer.getInvariantValue()!=null&&
	    writer.getInvariantValue().isInvariant(this)) {
	    writer.outputline(vd.getType().getGenerateType().getSafeSymbol()+
			      " "+dest.getSafeSymbol()+"="+writer.getInvariantValue().getValue(this).getSafeSymbol()+";");
	    writer.outputline("maybe="+writer.getInvariantValue().getMaybe(this).getSafeSymbol()+";");
	    return;
	}

        writer.outputline(vd.getType().getGenerateType().getSafeSymbol() + " " + dest.getSafeSymbol() +
                          " = (" + vd.getType().getGenerateType().getSafeSymbol() + ") " + vd.getSafeSymbol() + "; /*varexpr*/");
	if (vd.isGlobal() && (DOTYPECHECKS||DOMEMCHECKS) && (td instanceof StructureTypeDescriptor)) {
	    VarDescriptor typevar=VarDescriptor.makeNew("typechecks");
	    writer.outputline("if ("+dest.getSafeSymbol()+")");
	    writer.startblock();
	    if (DOTYPECHECKS)
		writer.outputline("bool "+typevar.getSafeSymbol()+"=assertvalidtype(" + dest.getSafeSymbol() + ", " + td.getId() + ");");
	    else
		writer.outputline("bool "+typevar.getSafeSymbol()+"=assertvalidmemory(" + dest.getSafeSymbol() + ", " + td.getId() + ");");
	    writer.outputline("if (!"+typevar.getSafeSymbol()+")");
	    writer.startblock();
	    writer.outputline(dest.getSafeSymbol()+"=0;");
	    if (DONULL)
		writer.outputline(vd.getSafeSymbol()+"=0;");
	    writer.endblock();
	    writer.endblock();
	}
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output(varname);
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        typechecked = true;
        vd = (VarDescriptor) sa.getSymbolTable().get(varname);
        if (vd == null) {
            //System.out.println(varname);
            sa.getErrorReporter().report(null, "Undefined variable '" + varname + "'");
            return null;
        }
        assert vd.getType() != null;
        this.td = vd.getType();
        return this.td;
    }
}
