package MCC.IR;

import java.util.*;

public class ImageSetExpr extends SetExpr {
    
    public static final boolean INVERSE = true;

    VarDescriptor vd;
    RelationDescriptor rd;
    boolean inverse;

    public ImageSetExpr(VarDescriptor vd, RelationDescriptor rd) {
        this.vd = vd;
        this.rd = rd;
        this.inverse = false;
    }

    public ImageSetExpr(boolean inverse, VarDescriptor vd, RelationDescriptor rd) {
        this.vd = vd;
        this.rd = rd;
        this.inverse = inverse;
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
        throw new IRException("not supported");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        throw new IRException("not supported");
    }

}
