package MCC.IR;

import java.util.*;

public class SetInclusion extends Inclusion {
    
    Expr elementexpr;
    SetDescriptor set;

    public String generatedresult = null;
    public String generatedaddeditem = null;

    static boolean worklist = false;
    public boolean dostore = true;

    public SetInclusion(Expr elementexpr, SetDescriptor set) {
        this.elementexpr = elementexpr;
        this.set = set;
    }

    public boolean usesDescriptor(Descriptor d) {
	if (d==set)
	    return true;
	else
	    return elementexpr.usesDescriptor(d);
    }

    public SetDescriptor getSet() {
        return set;
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

        // allows access to the value of this set addition later
        generatedresult = vd.getSafeSymbol();

        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
        generatedaddeditem = addeditem; // allows access to the result of the set addition later.

        // we set equal to one so that if dostore == false the guard in teh 
        // metainclusion generation for the subrules and sub quantifiers will go on        
        writer.outputline("int " + addeditem + " = 1;");

        if (dostore) {
        
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
