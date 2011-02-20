package MCC.IR;
import MCC.Compiler;
import java.util.*;

public class RelationInclusion extends Inclusion {

    Expr leftelementexpr, rightelementexpr;
    RelationDescriptor relation;

    // #TBD#: this flag needs to be set by some static analysis
    boolean typesafe = true;
    public static boolean worklist = false;

    public RelationInclusion(Expr leftelementexpr, Expr rightelementexpr, RelationDescriptor relation) {
        this.leftelementexpr = leftelementexpr;
        this.rightelementexpr = rightelementexpr;
        this.relation = relation;
    }

    public String toString() {
	String str="<"+leftelementexpr.name()+","+rightelementexpr.name()+"> in "+relation.toString();
	return str;
    }

    public boolean usesDescriptor(Descriptor d) {
	if (d==relation)
	    return true;
	else
	    return(leftelementexpr.usesDescriptor(d)
		   ||rightelementexpr.usesDescriptor(d));
    }

    public Expr getLeftExpr() {
        return leftelementexpr;
    }

    public Expr getRightExpr() {
        return rightelementexpr;
    }

    public RelationDescriptor getRelation() {
        return relation;
    }

    public Set getTargetDescriptors() {
        HashSet v = new HashSet();
        v.add(relation);
        return v;
    }

    public Set getRequiredDescriptors() {
        Set v = leftelementexpr.getRequiredDescriptors();
        v.addAll(rightelementexpr.getRequiredDescriptors());

        if (!typesafe) { /* if not typesafe then domain and range are needed to make typesafety checks! */
            v.add(relation.getDomain());
            v.add(relation.getRange());
        }

        return v;
    }

    public void generate(CodeWriter writer) {
        VarDescriptor ld = VarDescriptor.makeNew("leftele");
        leftelementexpr.generate(writer, ld);
        VarDescriptor rd = VarDescriptor.makeNew("rightele");
        rightelementexpr.generate(writer, rd);

        /* typesafe checks! */
        String typesafecheck = (VarDescriptor.makeNew("typesafecheck")).getSafeSymbol();

        if (!typesafe) {
            String check = "int " + typesafecheck + " = " ;

            if (!(relation.getDomain() instanceof ReservedSetDescriptor)) {
                check += "SimpleHashcontainskey("+relation.getDomain().getSafeSymbol() + "_hash, "+ld.getSafeSymbol() + ") && ";
            }

            if (!(relation.getRange() instanceof ReservedSetDescriptor)) {
                check += "SimpleHashcontainskey("+relation.getRange().getSafeSymbol() +"_hash, "+ rd.getSafeSymbol() + ") && ";
            }

            check += "1;"; // terminate boolean expression

            writer.outputline(check);
            writer.outputline("if (" + typesafecheck + ")");
            writer.startblock();
        }

        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
        Repair.generate_dispatch(writer, relation, ld.getSafeSymbol(), rd.getSafeSymbol());

        if (RelationInclusion.worklist) {
            writer.outputline("if (" + addeditem + ")");
            writer.startblock(); {
                WorkList.generate_dispatch(writer, relation, rd.getSafeSymbol(), ld.getSafeSymbol());
            }
            writer.endblock();
        }

        if (!typesafe) {
            writer.endblock();
        }

        //writer.outputline("printf(\"" + relation.getSafeSymbol() + " (add): <%d, %d>\\n\", " + ld.getSafeSymbol() + ", " + rd.getSafeSymbol() + ");");
    }

    public boolean typecheck(SemanticAnalyzer sa) {
        TypeDescriptor ld = leftelementexpr.typecheck(sa);
        TypeDescriptor rd = rightelementexpr.typecheck(sa);

        if (ld == null || rd == null) {
            return false;
        }

        boolean ok = true;

        /* #ATTN#: this check makes sure that the types match up,
           a runtime check needs to made that the set relationships
           are correct */

        if (ld != relation.getDomain().getType()) {
            sa.getErrorReporter().report(null, "Type of left element '" + ld.getSymbol() + "' must match domain type '" + relation.getDomain().getType().getSymbol() + "'");
            ok = false;
        }

        if (rd != relation.getRange().getType()) {
            assert rd.getSymbol() != null;
            assert relation.getRange().getType() != null : relation.getRange().getSymbol();
            sa.getErrorReporter().report(null, "Type of right element '" + rd.getSymbol() + "' must match range type '" + relation.getRange().getType().getSymbol() + "'");
            ok = false;
        }
        return ok;
    }

}
