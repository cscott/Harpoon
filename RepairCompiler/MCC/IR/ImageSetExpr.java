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

    public void generate(CodeWriter writer, VarDescriptor vd) {
        throw new IRException("not supported");
    }

    public void prettyPrint(PrettyPrinter pp) {
        throw new IRException("not supported");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        throw new IRException("not supported");
    }

}
