package MCC.IR;

import java.util.*;

public class RelationQuantifier extends Quantifier {

    RelationDescriptor relation;
    VarDescriptor x, y; // y = x.relation

    public RelationQuantifier() {}

    public void setRelation(RelationDescriptor rd) {
        relation = rd; 
    }

    public void setTuple(VarDescriptor x, VarDescriptor y) {
        this.x = x;
        this.y = y;
    }

    public Set getRequiredDescriptors() {
        HashSet v = new HashSet();
        v.add(relation);
        return v;
    }

    public String toString() {
        return "relation quantifier <" + x.getSymbol() + "," + y.getSymbol() + "> in " + relation.getSymbol();
    }

    public void generate_open(CodeWriter writer) {
        writer.outputline("for (SimpleIterator* " + x.getSafeSymbol() + "_iterator = " + relation.getSafeSymbol() + "_hash->iterator(); " + x.getSafeSymbol() + "_iterator->hasNext(); )");
        writer.startblock();
        writer.outputline(y.getType().getSafeSymbol() + " " + y.getSafeSymbol() + " = (" + y.getType().getSafeSymbol() + ") " + x.getSafeSymbol() + "_iterator->next();");        
        // #ATTN#: key is called second because next() forwards ptr and key does not! 
        writer.outputline(x.getType().getSafeSymbol() + " " + x.getSafeSymbol() + " = (" + x.getType().getSafeSymbol() + ") " + x.getSafeSymbol() + "_iterator->key();");
    }

}
