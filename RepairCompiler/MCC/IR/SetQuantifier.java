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

    public VarDescriptor getVar() {
        return var;
    }

    public SetDescriptor getSet() {
        return set;
    }

    public String toString() {
        return "set quantifier " + var.getSymbol() + " in " + set.getSymbol();
    }

    public void generate_open(CodeWriter writer) {
	writer.outputline("struct SimpleIterator "+var.getSafeSymbol()+"_iterator;");
        writer.outputline("for (SimpleHashiterator("+set.getSafeSymbol()+"_hash, & "+ var.getSafeSymbol()+"_iterator); hasNext(&"+var.getSafeSymbol()+"_iterator); )");
        writer.startblock();
        writer.outputline(var.getType().getGenerateType() + " " + var.getSafeSymbol() + " = (" + var.getType().getGenerateType() + ") next(&"+var.getSafeSymbol()+"_iterator);");
    }

    public void generate_open(CodeWriter writer, String type,int number, String left,String right) {
	VarDescriptor tmp=VarDescriptor.makeNew("flag");
	writer.outputline("struct SimpleIterator "+var.getSafeSymbol()+"_iterator;");
        writer.outputline("SimpleHashiterator("+set.getSafeSymbol()+"_hash, &"+var.getSafeSymbol()+"_iterator);");
	writer.outputline("int "+tmp.getSafeSymbol()+"=0;");
	writer.outputline("if ("+type+"=="+number+")");
	writer.outputline(tmp.getSafeSymbol()+"=1;");

	writer.outputline("while("+tmp.getSafeSymbol()+"||(("+type+"!="+number+")&&hasNext(&"+var.getSafeSymbol()+"_iterator)))");
        writer.startblock();
        writer.outputline(var.getType().getGenerateType() + " " + var.getSafeSymbol() + ";");
	writer.outputline("if ("+type+"=="+number+")");
	writer.startblock();
	writer.outputline(tmp.getSafeSymbol()+"=0;");
        writer.outputline(var.getSafeSymbol() + " = (" + var.getType().getGenerateType() + ") " + left + ";");
	writer.endblock();
	writer.outputline("else");
        writer.outputline(var.getSafeSymbol() + " = (" + var.getType().getGenerateType() + ") next(&"+var.getSafeSymbol()+"_iterator);");
    }

    public int generate_worklistload(CodeWriter writer, int offset) {
        String varname = var.getSafeSymbol();
        writer.outputline("int " + varname + " = wi->word" + offset + ";");
        return offset + 1;
    }

    public int generate_workliststore(CodeWriter writer, int offset) {
        String varname = var.getSafeSymbol();
        writer.outputline("wi->word" + offset + " = " + varname + ";");
        return offset + 1;
    }


}
