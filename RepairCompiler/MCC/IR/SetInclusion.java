package MCC.IR;

import java.util.*;

public class SetInclusion extends Inclusion {
    
    Expr elementexpr;
    SetDescriptor set;

    public SetInclusion(Expr elementexpr, SetDescriptor set) {
        this.elementexpr = elementexpr;
        this.set = set;
    }

    public Set getTargetDescriptors() {
        HashSet v = new HashSet();
        v.add(set);
        return v;
    }

    public Set getRequiredDescriptors() {
        return elementexpr.getRequiredDescriptors();
    }

    public void generate(CodeWriter writer) {
        VarDescriptor vd = VarDescriptor.makeNew("element");
        elementexpr.generate(writer, vd);
        writer.outputline(set.getSafeSymbol() + "_hash->add((int)" + vd.getSafeSymbol() +  ", (int)" + vd.getSafeSymbol() + ");");
        //writer.outputline("printf(\"" + set.getSafeSymbol() + " (add): %d\\n\", " + vd.getSafeSymbol() + ");");
    }

    public boolean typecheck(SemanticAnalyzer sa) {
        TypeDescriptor td = elementexpr.typecheck(sa);
        
        if (td == null) {
            return false;
        }

        TypeDescriptor settype = set.getType();

        if (!td.equals(settype)) {
            sa.getErrorReporter().report(null, "Type mismatch: attempting to test for types '" + td.getSymbol() + "' in set of type '" + settype.getSymbol() + "'");
            return false;
        }
        
        return true;
    }

}
