package MCC.IR;

import java.util.*;

public class SetQuantifier extends Quantifier {

    VarDescriptor var;
    SetDescriptor set;

    public SetQuantifier() {
        this.var = null;
    }

    public void setVar(VarDescriptor vd) {
        this.var = vd;
    }

    public void setSet(SetDescriptor sd) {
        this.set = sd;
    }

    public Set getRequiredDescriptors() {
        HashSet v = new HashSet();
        v.add(set);
        return v;
    }

    public SetDescriptor getSet() {
        return set;
    }

    public String toString() {
        return "set quantifier " + var.getSymbol() + " in " + set.getSymbol();
    }

    public void generate_open(CodeWriter writer) {
        writer.outputline("for (SimpleIterator* " + var.getSafeSymbol() + "_iterator = " + set.getSafeSymbol() + "_hash->iterator(); " + var.getSafeSymbol() + "_iterator->hasNext(); )");
        writer.startblock();
        writer.outputline(var.getType().getGenerateType() + " " + var.getSafeSymbol() + " = (" + var.getType().getGenerateType() + ") " + var.getSafeSymbol() + "_iterator->next();");
    }
  
}
