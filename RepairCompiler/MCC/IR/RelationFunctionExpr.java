package MCC.IR;

import java.util.*;

public class RelationFunctionExpr extends Expr {

    // #WHAT I WAS DOING: about to define relationfunctionexpr thich should take a expr, relation and rule and generated
    // the functional value or "maybe" if not there!

    Expr expr;
    RelationDescriptor relation;
    Rule rule;

    public RelationFunctionExpr(Expr expr, RelationDescriptor relation, Rule rule) {
        this.expr = expr;
        this.relation = relation;
        this.rule = rule;
    }

    public boolean equals(Map remap, Expr e) {
	throw new Error("UNIMPLEMENTED!!!");
    }

    public Descriptor getDescriptor() {
	return relation;
    }

    public RelationDescriptor getRelation() {
        return relation;
    }

    public Set getInversedRelations() {
        return expr.getInversedRelations();
    }

    public Set getRequiredDescriptors() {
        Set v = expr.getRequiredDescriptors();
        v.add(relation);
        return v;
    }

    public boolean usesDescriptor(Descriptor rd) {
	if (rd==relation)
	    return true;
	else
	    return expr.usesDescriptor(rd);
    }

    public void generate(CodeWriter cr, VarDescriptor dest) {

        String destname = dest.getSafeSymbol();
        cr.addDeclaration("int", destname);

        // ok... destination is declared... we gotta expand this rule inplace... and instead of the inclusion we
        // set the destination in the guard ... otherwise maybe!

        VarDescriptor domain = VarDescriptor.makeNew("domain");
        expr.generate(cr, domain);

        cr.pushSymbolTable(rule.getSymbolTable());
        cr.startblock(); {

            // ok... symbol table is set up... lets bind that initial vardescriptor of the quantifier
            SetQuantifier sq = ((SetQuantifier) rule.quantifiers().next());
            VarDescriptor rulebinding = sq.getVar();
            String tempvar = (VarDescriptor.makeNew("tempvar")).getSafeSymbol();

            // this is to be safe about name overlap because int t = t; sets t to 0!
            cr.addDeclaration("int", tempvar);
            cr.outputline(tempvar + " = " + domain.getSafeSymbol() + ";");
            cr.addDeclaration("int", rulebinding.getSafeSymbol());
            cr.outputline(rulebinding.getSafeSymbol() + " = " + tempvar + ";");

            /* pretty print! */
            cr.outputline("/* about to inbed relational function*/");
            cr.output("/* ");
            rule.getGuardExpr().prettyPrint(cr);
            cr.outputline("*/");

            /* now we have to generate the guard test */
            VarDescriptor guardval = VarDescriptor.makeNew();
            rule.getGuardExpr().generate(cr, guardval);

            cr.outputline("if (" + guardval.getSafeSymbol() + ")");
            cr.startblock(); {

                /* now we have to generate the inclusion code */
                RelationInclusion ri = (RelationInclusion) rule.getInclusion();

                // basically, destname = righthandside<r, r.field>
                VarDescriptor tempdest = VarDescriptor.makeNew("tempdest");
                Expr rhs = ri.getRightExpr();
                rhs.generate(cr, tempdest);

                cr.outputline(destname + " = " + tempdest.getSafeSymbol() + ";");

            } cr.endblock();
            cr.outputline("else");
            cr.startblock(); {

                // three valued logic. if the relation (which is a partial function)
                // fails its guard, then we have a "maybe" condition, which must
                // propagate

                cr.outputline("maybe = 1;");

            } cr.endblock();

        } cr.endblock();

    }

    public void prettyPrint(PrettyPrinter pp) {
        expr.prettyPrint(pp);
        pp.output(".");
        pp.output(relation.getSafeSymbol());
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        throw new IRException();
    }

}
