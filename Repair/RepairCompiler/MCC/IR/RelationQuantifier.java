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
	writer.addDeclaration("struct SimpleIterator",x.getSafeSymbol()+"_iterator");
        writer.outputline("for (SimpleHashiterator("+relation.getSafeSymbol()+"_hash, &"+x.getSafeSymbol()+"_iterator); hasNext(&"+x.getSafeSymbol()+"_iterator); )");
        writer.startblock();
        // #ATTN#: key is called first because next() forwards ptr and key does not!
        writer.addDeclaration(x.getType().getGenerateType().toString(), x.getSafeSymbol());
	writer.outputline(x.getSafeSymbol() + " = (" + x.getType().getGenerateType() + ") key(&"+x.getSafeSymbol()+"_iterator);");

        writer.addDeclaration(y.getType().getGenerateType().toString(), y.getSafeSymbol());
	writer.outputline(y.getSafeSymbol() + " = (" + y.getType().getGenerateType() + ") next(&"+x.getSafeSymbol()+"_iterator);");

    }

    public void generate_open(CodeWriter writer, String type,int number, String left,String right) {
	VarDescriptor tmp=VarDescriptor.makeNew("flag");
        writer.addDeclaration("struct SimpleIterator", x.getSafeSymbol() + "_iterator");
        writer.outputline("SimpleHashiterator("+relation.getSafeSymbol()+"_hash,& "+x.getSafeSymbol()+"_iterator);");
	writer.addDeclaration("int",tmp.getSafeSymbol());
	writer.outputline(tmp.getSafeSymbol()+"=0;");
	writer.outputline("if ("+type+"=="+number+")");
	writer.outputline(tmp.getSafeSymbol()+"=1;");

	writer.outputline("while("+tmp.getSafeSymbol()+"||(("+type+"!="+number+")&& hasNext(&"+x.getSafeSymbol()+"_iterator)))");
        writer.startblock();
        writer.addDeclaration(x.getType().getGenerateType().toString(), x.getSafeSymbol());
        writer.addDeclaration(y.getType().getGenerateType().toString(), y.getSafeSymbol());
	writer.outputline("if ("+type+"=="+number+")");
	writer.startblock();
	writer.outputline(tmp.getSafeSymbol()+"=0;");
        writer.outputline(x.getSafeSymbol() + " = (" + x.getType().getGenerateType() + ") " + left + ";");
        writer.outputline(y.getSafeSymbol() + " = (" + y.getType().getGenerateType() + ") " + right + ";");
	writer.endblock();
	writer.outputline("else");
	writer.startblock();
        writer.outputline(x.getSafeSymbol() + " = (" + x.getType().getGenerateType() + ") key(&"+x.getSafeSymbol()+"_iterator);");
        writer.outputline(y.getSafeSymbol() + " = (" + y.getType().getGenerateType() + ") next(&"+x.getSafeSymbol()+"_iterator);");
	writer.endblock();
    }

    public int generate_worklistload(CodeWriter writer, int offset) {
        String varx = x.getSafeSymbol();
        String vary = y.getSafeSymbol();
        writer.outputline("int " + varx + " = wi->word" + offset + "; /* r1*/");
        writer.outputline("int " + vary + " = wi->word" + (offset + 1) + "; /*r2*/");
        return offset + 2;
    }

    public int generate_workliststore(CodeWriter writer, int offset) {
        String varx = x.getSafeSymbol();
        String vary = y.getSafeSymbol();
        writer.outputline("wi->word" + offset + " = " + varx + "; /* r1*/");
        writer.outputline("wi->word" + (offset+1) + " = " + vary + "; /* r2*/");
        return offset + 2;
    }


}
