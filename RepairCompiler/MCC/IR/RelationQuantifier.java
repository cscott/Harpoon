package MCC.IR;

import java.util.*;

public class RelationQuantifier extends Quantifier {

    RelationDescriptor relation;
    VarDescriptor x, y; // y = x.relation

    public RelationQuantifier() {}

    public void setRelation(RelationDescriptor rd) {
        relation = rd; 
    }

    public RelationDescriptor getRelation() {
	return relation;
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
        writer.outputline(y.getType().getGenerateType() + " " + y.getSafeSymbol() + " = (" + y.getType().getGenerateType() + ") " + x.getSafeSymbol() + "_iterator->next();");        
        // #ATTN#: key is called second because next() forwards ptr and key does not! 
        writer.outputline(x.getType().getGenerateType() + " " + x.getSafeSymbol() + " = (" + x.getType().getGenerateType() + ") " + x.getSafeSymbol() + "_iterator->key();");
    }

    public int generate_worklistload(CodeWriter writer, int offset) {        
        String varx = x.getSafeSymbol();
        String vary = y.getSafeSymbol();
        writer.outputline("int " + varx + " = wi->word" + offset + "; // r1"); 
        writer.outputline("int " + vary + " = wi->word" + (offset + 1) + "; //r2"); 
        return offset + 2;       
    }

    public int generate_workliststore(CodeWriter writer, int offset) {        
        String varx = x.getSafeSymbol();
        String vary = y.getSafeSymbol();
        writer.outputline("wi->word" + offset + " = " + varx + "; // r1");
        writer.outputline("wi->word" + (offset+1) + " = " + vary + "; // r2");
        return offset + 2;       
    }


}
