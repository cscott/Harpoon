package MCC.IR;

import java.util.*;

public class SizeofFunction extends Expr {

    public VarDescriptor vd;
    public RelationDescriptor rd;
    public Rule rule;

    public SizeofFunction(VarDescriptor vd, RelationDescriptor rd, Rule rule) {
        this.vd = vd;
        this.rd = rd;
        this.rule = rule;
    }

    public Set getInversedRelations() {
        return new HashSet();
    }

    public boolean equals(Map remap,Expr e) {
	throw new Error("UNIMPLEMENTED");
    }

    public Set getRequiredDescriptors() {
        // because we don't actually use rd for any generation, we return the empty set
        return new HashSet();
    }

    public TypeDescriptor getType() {
        throw new IRException("unsupported");
    }

    public void generate(CodeWriter cr, VarDescriptor dest) {

        // basically a sizeoffunction can have two values ... zero or one... so what we need to do
        // is expand the guard of the rule and if its true then its 1 otherwise 0

        String destname = dest.getSafeSymbol();
        cr.outputline("int " + destname + ";");

        // ok... destination is declared... we gotta expand this rule inplace... and instead of the inclusion we 
        // set the destination in the guard ... otherwise maybe!
        
        VarDescriptor domain = vd;

        cr.pushSymbolTable(rule.getSymbolTable());
        cr.startblock(); {

            // ok... symbol table is set up... lets bind that initial vardescriptor of the quantifier
            SetQuantifier sq = ((SetQuantifier) rule.quantifiers().next());
            VarDescriptor rulebinding = sq.getVar();
            String tempvar = (VarDescriptor.makeNew("tempvar")).getSafeSymbol();
            
            // this is to be safe about name overlap because int t = t; sets t to 0!
            cr.outputline("int " + tempvar + " = " + domain.getSafeSymbol() + ";");
            cr.outputline("int " + rulebinding.getSafeSymbol() + " = " + tempvar + ";");
            
            /* pretty print! */
            cr.outputline("// about to inbed relational function");
            cr.output("// ");
            rule.getGuardExpr().prettyPrint(cr);
            cr.outputline("");
            
            /* now we have to generate the guard test */
            VarDescriptor guardval = VarDescriptor.makeNew();
            rule.getGuardExpr().generate(cr, guardval);
            
            cr.outputline("if (" + guardval.getSafeSymbol() + ")");
            cr.startblock(); {
                
                cr.outputline(destname + " = 1;");
                
            } cr.endblock();
            cr.outputline("else");
            cr.startblock(); {

                cr.outputline(destname + " = 0;");

            } cr.endblock();

        } cr.endblock();



    }

    public void prettyPrint(PrettyPrinter pp) {
        pp.output("sizeoffunction(");
        pp.output(vd.toString() + "." + rd.toString());
        pp.output(")");
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        throw new IRException();
    }
        
}
