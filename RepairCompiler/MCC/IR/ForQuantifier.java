package MCC.IR;

import java.util.*;

public class ForQuantifier extends Quantifier {

    VarDescriptor var = null;
    Expr lower = null;
    Expr upper = null;

    public ForQuantifier() {}

    public void setVar(VarDescriptor vd) {
        this.var = vd;
    }

    public VarDescriptor getVar() {
	return var;
    }

    public void setBounds(Expr lower, Expr upper) {
        this.lower = lower;
        this.upper = upper;
    }

    public Set getRequiredDescriptors() {
        HashSet set=new HashSet();
	set.addAll(lower.getRequiredDescriptors());
	set.addAll(upper.getRequiredDescriptors());
	return set;
    }

    public String toString() {
        return "for quantifier " + var.getSymbol() + " = " + lower.name() + " to " + upper.name();
    }

    public void generate_open(CodeWriter writer) {
        VarDescriptor ld = VarDescriptor.makeNew();
        VarDescriptor ud = VarDescriptor.makeNew();
        lower.generate(writer, ld);
        upper.generate(writer, ud);
        writer.addDeclaration("int",var.getSafeSymbol());
        writer.outputline("for (" + var.getSafeSymbol() + " = " + ld.getSafeSymbol() + "; " + var.getSafeSymbol() + " <= " + ud.getSafeSymbol() + "; " + var.getSafeSymbol() + "++)");
        writer.startblock();
    }

    public void generate_open(CodeWriter writer, String type,int number, String
left,String right) {
	generate_open(writer);
    }

    public int generate_worklistload(CodeWriter writer, int offset) {        
        String varname = var.getSafeSymbol();
	writer.addDeclaration("int",varname);
        writer.outputline(varname + " = wi->word" + offset + ";"); 
        return offset + 1;       
    }

    public int generate_workliststore(CodeWriter writer, int offset) {        
        String varname = var.getSafeSymbol();
        writer.outputline("wi->word" + offset + " = " + varname + ";");
        return offset + 1;       
    }


    public boolean typecheck(SemanticAnalyzer sa) {
        TypeDescriptor lt = lower.typecheck(sa);
        TypeDescriptor ut = upper.typecheck(sa);
        
        if (lt == null || ut == null) {
            return false;
        }

        boolean ok = true;

        if (lt != ReservedTypeDescriptor.INT) {
            sa.getErrorReporter().report(null, "Lower bound of for quantifier must be of type 'int'");
            ok = false;
        }

        if (ut != ReservedTypeDescriptor.INT) {
            sa.getErrorReporter().report(null, "Upper bound of for quantifier must be of type 'int'");
            ok = false;
        }

        return ok;       
    } 

}










