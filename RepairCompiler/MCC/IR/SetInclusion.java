package MCC.IR;

import java.util.*;

public class SetInclusion extends Inclusion {
    
    Expr elementexpr;
    SetDescriptor set;

    static boolean worklist = true;

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

        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
        writer.outputline("int " + addeditem + ";");
        
        writer.outputline(addeditem + " = " + set.getSafeSymbol() + "_hash->add((int)" + vd.getSafeSymbol() 
                          +  ", (int)" + vd.getSafeSymbol() + ");");

        if (SetInclusion.worklist) {
            writer.outputline("if (" + addeditem + ")");
            writer.startblock(); {                
                WorkList.generate_dispatch(writer, set, vd.getSafeSymbol());
            }
            writer.endblock();
        }
        
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
