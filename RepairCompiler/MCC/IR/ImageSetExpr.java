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
	    name+=ise.name();
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
        String hash = inverse ? "_hashinv->contains(" : "_hash->contains(" ;
	if (!isimageset) {
	    writer.outputline("int " + dest.getSafeSymbol() + " = " + rd.getSafeSymbol() + hash + vd.getSafeSymbol() + ", " + element.getSafeSymbol() + ");");
	} else {
	    VarDescriptor newset=VarDescriptor.makeNew("newset");
	    generate_set(writer,newset);
	    writer.outputline("int "+dest.getSafeSymbol()+"="+newset.getSafeSymbol()+"->contains("+element.getSafeSymbol()+");");
	    writer.outputline("delete "+newset.getSafeSymbol()+";");
	}
    }

    public void generate_size(CodeWriter writer, VarDescriptor dest) {
        assert dest != null;
        assert rd != null;
	if (!isimageset) {
	    String hash = inverse ? "_hashinv->count(" : "_hash->count(" ;
	    writer.outputline("int " + dest.getSafeSymbol() + " = " + rd.getSafeSymbol() + hash + vd.getSafeSymbol() + ");");
	} else {
	    VarDescriptor newset=VarDescriptor.makeNew("newset");
	    generate_set(writer,newset);
	    writer.outputline("int "+dest.getSafeSymbol()+"="+newset.getSafeSymbol()+"->count();");
	    writer.outputline("delete "+newset.getSafeSymbol()+";");
	} 
    }

    public void generate_leftside(CodeWriter writer, VarDescriptor dest) {
	if (!isimageset) {
	    writer.outputline(vd.getType().getGenerateType()+" "+dest.getSafeSymbol()+" = "+vd.getSafeSymbol()+";");
	} else {
	    VarDescriptor iseset=VarDescriptor.makeNew("set");
	    ise.generate_set(writer,iseset);
	    writer.outputline("int "+dest.getSafeSymbol()+" = "+iseset.getSafeSymbol()+"->firstkey();");
	    writer.outputline("delete "+iseset.getSafeSymbol()+";");
	}
    }

    public void generate_set(CodeWriter writer, VarDescriptor dest) {
	if (!isimageset) {
	    String hash = inverse ? "_hashinv->imageSet(" : "_hash->imageSet(" ;
	    writer.outputline("SimpleHash * "+dest.getSafeSymbol()+"="+rd.getSafeSymbol()+hash+vd.getSafeSymbol()+");");
	} else {
	    VarDescriptor iseset=VarDescriptor.makeNew("set");
	    ise.generate_set(writer,iseset);

	    VarDescriptor itvd=VarDescriptor.makeNew("iterator");
	    writer.outputline("SimpleIterator "+itvd.getSafeSymbol()+";");
	    writer.outputline(iseset.getSafeSymbol()+"->iterator("+itvd.getSafeSymbol()+");");

	    writer.outputline("SimpleHash *"+dest.getSafeSymbol()+"=new SimpleHash(10);");
	    writer.outputline("while ("+itvd.getSafeSymbol()+".hasNext()) {");
	    
	    VarDescriptor keyvd=VarDescriptor.makeNew("key");
	    
	    writer.outputline("int "+keyvd.getSafeSymbol()+"="+itvd.getSafeSymbol()+".next();");
	    String hash = inverse ? "_hashinv->imageSet(" : "_hash->imageSet(" ;
	    VarDescriptor newset=VarDescriptor.makeNew("newset");
	    writer.outputline("SimpleHash * "+newset.getSafeSymbol()+"="+rd.getSafeSymbol()+hash+keyvd.getSafeSymbol()+");");
	    writer.outputline(dest.getSafeSymbol()+"->addAll("+newset.getSafeSymbol()+");");
	    writer.outputline("delete "+newset.getSafeSymbol()+";");
	    writer.outputline("}");
	    writer.outputline("delete "+iseset.getSafeSymbol()+";");
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
