package MCC.IR;

import java.util.*;

public class RelationExpr extends Expr {

    // #TBD#: right now i'm not sure if this is the best way to organize the relationexpr... it may be better
    // to have one class represent each a.B.B'.B'' expression with a VarDescriptor for the a and a vector 
    // for the list of B
    RelationExpr subdomain;
    VarDescriptor domain;

    RelationDescriptor relation;
    boolean inverse;

    public RelationExpr() {
        this.domain = null;
        this.subdomain = null;
        this.relation = null;
        this.inverse = false;
    }

    public RelationExpr(RelationExpr subdomain) {
        this.subdomain = subdomain;
        this.domain = null;
        this.relation = null;
        this.inverse = false;
    }

    public void setRelation(RelationDescriptor rd, boolean inverse) {
        this.relation = rd;
        this.inverse = inverse;
    }

    public void setDomain(VarDescriptor vd) {
        this.domain = vd;
    }

    public Set getRequiredDescriptors() {
        HashSet v = new HashSet();
        v.add(relation);
        if (subdomain != null) {
            v.addAll(subdomain.getRequiredDescriptors());
        }
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {
        if (domain != null) { /* base case */
            writer.outputline(relation.getRange().getType().getSafeSymbol() + " " + dest.getSymbol() + " = " + relation.getSafeSymbol() + "_hash.getFirst(" + domain.getSafeSymbol() + ");");
        } else {
            VarDescriptor ld = VarDescriptor.makeNew();
            subdomain.generate(writer, ld);
            writer.outputline(relation.getRange().getType().getSafeSymbol() + " " + dest.getSymbol() + " = " + relation.getSafeSymbol() + "_hash.getFirst(" + ld.getSafeSymbol() + ");");
        }            

    }

    public void generate_set(CodeWriter writer, VarDescriptor dest) {
        if (domain != null) { /* base case */
            writer.outputline("Set " + dest.getSymbol() + " = " + relation.getSafeSymbol() + "_hash.get(" + domain.getSafeSymbol() + ");");
        } else {
            VarDescriptor ld = VarDescriptor.makeNew();
            subdomain.generate(writer, ld);
            writer.outputline("Set " + dest.getSymbol() + " = " + relation.getSafeSymbol() + "_hash.get(" + domain.getSafeSymbol() + ");");
        }     
    }

    public void prettyPrint(PrettyPrinter pp) {
        if (subdomain != null) {
            subdomain.prettyPrint(pp);
        } else {
            pp.output(domain.getSafeSymbol());
        }

        pp.output(".");
        
        if (inverse) {
            pp.output("~");
        }

        pp.output(relation.getSafeSymbol());
    }

    public TypeDescriptor typecheck(SemanticAnalyzer sa) {
        TypeDescriptor type = null;
        
        if (domain != null) {
            type = domain.getType();            
        } else {
            type = subdomain.typecheck(sa);
        }

        if (type == null) {
            return null;
        }

        /* check to make sure that the types of the relation match up */
        if (inverse) {
            TypeDescriptor rangetype = relation.getRange().getType();
            
            if (rangetype != type) {
                sa.getErrorReporter().report(null, "Type of left side of relation operator '.' is '" + type.getSymbol() + 
                                    "' but must be the '" + rangetype.getSymbol() + 
                                    "', the type of the range of the relation '" + relation.getSymbol() + "'");
                return null;
            }
            
            this.td = relation.getDomain().getType();
            return this.td;
        } else { /* not inverse */
            TypeDescriptor domaintype = relation.getDomain().getType();
            
            if (domaintype != type) {
                sa.getErrorReporter().report(null, "Type of left side of relation operator '.' is '" + type.getSymbol() + 
                                    "' but must be the '" + domaintype.getSymbol() + 
                                    "', the type of the range of the relation '" + relation.getSymbol() + "'");
                return null;
            }

            this.td = relation.getRange().getType();
            return this.td;
        }
    }

}
