package MCC.IR;

import java.util.*;

public class ImageSetExpr extends SetExpr {
    static final public boolean INVERSE=true;
    VarDescriptor vd;
    RelationDescriptor rd;
    boolean inverse;

    public ImageSetExpr(VarDescriptor vd, RelationDescriptor rd) {
        this.vd = vd;
        this.rd = rd;
        this.inverse = false;
    }

    public Set freeVars() {
	HashSet hs=new HashSet();
	hs.add(vd);
	return hs;
    }

    public String name() {
	String name=vd.toString()+".";
	if (inverse)
	    name+="~";
	name+=rd.toString();
	return name;
    }

    public ImageSetExpr(boolean inverse, VarDescriptor vd, RelationDescriptor rd) {
        this.vd = vd;
        this.rd = rd;
        this.inverse = inverse;
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof ImageSetExpr))
	    return false;
	ImageSetExpr ise=(ImageSetExpr)e;
	if (ise.inverse!=inverse)
	    return false;
	if (ise.rd!=rd)
	    return false;
	VarDescriptor nvde=vd;
	if (remap.containsKey(nvde))
	    nvde=(VarDescriptor)remap.get(nvde);
	if (nvde!=ise.vd)
	    return false;
	return true;
    }

    public boolean inverted() {
	return inverse;
    }

    public VarDescriptor getVar() {
        return vd;
    }

    public RelationDescriptor getRelation() {
        return rd;
    }

    public Descriptor getDescriptor() {
	return rd;
    }

    public boolean usesDescriptor(Descriptor d) {
	return (d==rd)||(d==vd);
    }

    public Set getInversedRelations() {
        HashSet set = new HashSet();
        if (inverse) {
            set.add(rd);
        }
        return set;
    }

    public Set getRequiredDescriptors() {
        HashSet v = new HashSet();
        v.add(rd);
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        throw new IRException("not supported");
    }

    public void generate_inclusion(CodeWriter writer, VarDescriptor dest, VarDescriptor element) {
        String hash = inverse ? "_hashinv->contains(" : "_hash->contains(" ;
        writer.outputline("int " + dest.getSafeSymbol() + " = " + rd.getSafeSymbol() + hash + vd.getSafeSymbol() + ", " + element.getSafeSymbol() + ");");
    }    

    public void generate_size(CodeWriter writer, VarDescriptor dest) {
        assert dest != null;
        assert vd != null;
        assert rd != null;
        String hash = inverse ? "_hashinv->count(" : "_hash->count(" ;
        writer.outputline("int " + dest.getSafeSymbol() + " = " + rd.getSafeSymbol() + hash + vd.getSafeSymbol() + ");");
    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output(vd.toString());
        pp.output(".");
        if (inverse) {
            pp.output("~");
        }
        pp.output(rd.toString());
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        throw new IRException("not supported");
    }

}
