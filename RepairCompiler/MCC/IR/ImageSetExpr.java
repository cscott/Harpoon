package MCC.IR;

import java.util.*;

public class ImageSetExpr extends SetExpr {
    static final public boolean INVERSE=true;
    VarDescriptor vd;
    RelationDescriptor rd;
    boolean inverse;
    ImageSetExpr ise;
    boolean isimageset=false;

    public ImageSetExpr(boolean inverse, VarDescriptor vd, RelationDescriptor rd) {
        this.vd = vd;
        this.rd = rd;
        this.inverse = inverse;
    }

    public ImageSetExpr(VarDescriptor vd, RelationDescriptor rd) {
        this.vd = vd;
        this.rd = rd;
        this.inverse = false;
    }

    public ImageSetExpr(boolean inverse, ImageSetExpr ise, RelationDescriptor rd) {
        this.ise = ise;
	this.isimageset=true;
        this.rd = rd;
        this.inverse = inverse;
    }

    public ImageSetExpr(ImageSetExpr ise, RelationDescriptor rd) {
	this.ise = ise;
	this.isimageset=true;
        this.rd = rd;
        this.inverse = false;
    }

    public Set freeVars() {
	HashSet hs=new HashSet();
	hs.add(vd);
	return hs;
    }

    public String name() {
	String name="";
	if (isimageset)
	    name+=ise.name()+".";
	else
	    name+=vd.toString()+".";
	if (inverse)
	    name+="~";
	name+=rd.toString();
	return name;
    }

    public boolean equals(Map remap, Expr e) {
	if (e==null||!(e instanceof ImageSetExpr))
	    return false;
	ImageSetExpr ise2=(ImageSetExpr)e;
	if (ise2.isimageset!=isimageset)
	    return false;
	if (ise2.inverse!=inverse)
	    return false;
	if (ise2.rd!=rd)
	    return false;

	if (isimageset) {
	    return ise.equals(remap,ise2.ise);
	} else {
	    VarDescriptor nvde=vd;
	    if (remap!=null&&remap.containsKey(nvde))
		nvde=(VarDescriptor)remap.get(nvde);
	    if (nvde!=ise2.vd)
		return false;
	    return true;
	}
    }

    public boolean inverted() {
	return inverse;
    }

    public VarDescriptor getVar() {
        return vd;
    }

    public ImageSetExpr getImageSetExpr() {
	return ise;
    }

    public RelationDescriptor getRelation() {
        return rd;
    }

    public Descriptor getDescriptor() {
	return rd;
    }

    public boolean usesDescriptor(Descriptor d) {
	if (isimageset)
	    return d==rd||ise.usesDescriptor(d);
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
        String hash = inverse ? "_hashinv, " : "_hash, " ;
	if (!isimageset) {
	    writer.addDeclaration("int", dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol() + " = SimpleHashcontainskeydata(" + rd.getSafeSymbol() + hash + vd.getSafeSymbol() + ", " + element.getSafeSymbol() + ");");
	} else {
	    VarDescriptor newset=VarDescriptor.makeNew("newset");
	    generate_set(writer,newset);
	    writer.addDeclaration("int", dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol()+"=SimpleHashcontainskey("+newset.getSafeSymbol()+","+element.getSafeSymbol()+");");
	    writer.outputline("freeSimpleHash("+newset.getSafeSymbol()+");");
	}
    }

    public void generate_size(CodeWriter writer, VarDescriptor dest) {
        assert dest != null;
        assert rd != null;
	if (!isimageset) {
	    String hash = inverse ? "_hashinv, " : "_hash, " ;
	    writer.addDeclaration("int", dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol() + " = SimpleHashcount(" + rd.getSafeSymbol() + hash + vd.getSafeSymbol() + ");");
	} else {
	    VarDescriptor newset=VarDescriptor.makeNew("newset");
	    generate_set(writer,newset);
	    writer.addDeclaration("int", dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol()+"=SimpleHashcountset("+newset.getSafeSymbol()+");");
	    writer.outputline("freeSimpleHash("+newset.getSafeSymbol()+");");
	}
    }

    public void generate_leftside(CodeWriter writer, VarDescriptor dest) {
	if (!isimageset) {
	    writer.addDeclaration(vd.getType().getGenerateType().toString(), dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol()+" = "+vd.getSafeSymbol()+";");
	} else {
	    VarDescriptor iseset=VarDescriptor.makeNew("set");
	    ise.generate_set(writer,iseset);
	    writer.addDeclaration("int",dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol()+" = SimpleHashfirstkey("+iseset.getSafeSymbol()+");");
	    writer.outputline("freeSimpleHash("+iseset.getSafeSymbol()+");");
	}
    }

    public void generate_set(CodeWriter writer, VarDescriptor dest) {
	if (!isimageset) {
	    String hash = inverse ? "_hashinv, " : "_hash, " ;
	    writer.addDeclaration("struct SimpleHash *",dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol()+"=SimpleHashimageSet("+rd.getSafeSymbol()+hash+vd.getSafeSymbol()+");");
	} else {
	    VarDescriptor iseset=VarDescriptor.makeNew("set");
	    ise.generate_set(writer,iseset);

	    VarDescriptor itvd=VarDescriptor.makeNew("iterator");
	    writer.addDeclaration("struct SimpleIterator",itvd.getSafeSymbol());
	    writer.outputline("SimpleHashiterator("+iseset.getSafeSymbol()+",&"+itvd.getSafeSymbol()+");");
	    writer.addDeclaration("struct SimpleHash *", dest.getSafeSymbol());
	    writer.outputline(dest.getSafeSymbol()+"=allocateSimpleHash(10);");
	    writer.outputline("while (hasNext(&"+itvd.getSafeSymbol()+")) {");

	    VarDescriptor keyvd=VarDescriptor.makeNew("key");

	    writer.addDeclaration("int",keyvd.getSafeSymbol());
	    writer.outputline(keyvd.getSafeSymbol()+"=next(&"+itvd.getSafeSymbol()+");");
	    String hash = inverse ? "_hashinv, " : "_hash, " ;
	    VarDescriptor newset=VarDescriptor.makeNew("newset");
	    writer.addDeclaration("struct SimpleHash *", newset.getSafeSymbol());
	    writer.outputline(newset.getSafeSymbol()+"=SimpleHashimageSet("+rd.getSafeSymbol()+hash+keyvd.getSafeSymbol()+");");
	    writer.outputline("SimpleHashaddAll("+dest.getSafeSymbol()+", "+ newset.getSafeSymbol()+");");
	    writer.outputline("freeSimpleHash("+newset.getSafeSymbol()+");");
	    writer.outputline("}");
	    writer.outputline("freeSimpleHash("+iseset.getSafeSymbol()+");");
	}
    }

    public void prettyPrint(PrettyPrinter pp) {
	if (!isimageset)
	    pp.output(vd.toString());
	else
	    ise.prettyPrint(pp);
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
