package MCC.IR;

import java.util.*;

public class RelationExpr extends Expr {

    Expr expr;
    RelationDescriptor relation;
    boolean inverse;

    public RelationExpr(Expr expr, RelationDescriptor relation, boolean inverse) {
        this.expr = expr;
        this.relation = relation;
        this.inverse = inverse;
    }

    public Set getRequiredDescriptors() {
        Set v = expr.getRequiredDescriptors();        
        v.add(relation);
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        VarDescriptor domain = VarDescriptor.makeNew("domain");
        String found = (VarDescriptor.makeNew("found")).getSafeSymbol();
        expr.generate(writer, domain);
        writer.outputline(relation.getRange().getType().getGenerateType().getSafeSymbol() + " " + dest.getSafeSymbol() + ";");
        writer.outputline("int " + found + " = " + relation.getSafeSymbol() + "_hash->get(" + domain.getSafeSymbol() + ", " + dest.getSafeSymbol() + ");");
        writer.outputline("if (!" + found + ") { maybe = 1; }");
    }

    // #TBD#: don't think this method is needed (or even called/referenced)
    /*
      public void generate_set(CodeWriter writer, VarDescriptor dest) {
      VarDescriptor domain = VarDescriptor.makeNew("domain");
      expr.generate(writer, domain);
      writer.outputline(relation.getRange().getType().getGenerateType().getSafeSymbol() + " " + dest.getSafeSymbol() + " = " + relation.getSafeSymbol() + "_hash->get(" + domain.getSafeSymbol() + ");");
      }
    */

    public void prettyPrint(PrettyPrinter pp) {
        expr.prettyPrint(pp);
        pp.output(".");
        
        if (inverse) {
            pp.output("~");
        }

        pp.output(relation.getSafeSymbol());
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {

        TypeDescriptor type = expr.typecheck(sa);
        
        if (type == null) {
            return null;
        }

        /* check to make sure that the types of the relation match up */
        if (inverse) {
            TypeDescriptor rangetype = relation.getRange().getType();
            
            if (rangetype != type) {
                sa.getErrorReporter().report(null, "Type of left side of relation operator '.' is '" + type.getSymbol() + 
                                    "' but must be '" + rangetype.getSymbol() + 
                                    "', the type of the range of the relation '" + relation.getSymbol() + "'");
                return null;
            }
            
            this.td = relation.getDomain().getType();
            return this.td;
        } else { /* not inverse */
            TypeDescriptor domaintype = relation.getDomain().getType();
            
            if (domaintype != type) {
                sa.getErrorReporter().report(null, "Type of left side of relation operator '.' is '" + type.getSymbol() + 
                                    "' but must be '" + domaintype.getSymbol() + 
                                    "', the type of the domain of the relation '" + relation.getSymbol() + "'");
                return null;
            }

            this.td = relation.getRange().getType();
            return this.td;
        }
    }

}
