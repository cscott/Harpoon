package MCC.IR;

import java.util.*;

public class ComparisonPredicate extends Predicate {

    VarDescriptor quantifier;
    RelationDescriptor relation;
    Opcode opcode;
    Expr expr;

    public ComparisonPredicate(VarDescriptor quantifier, RelationDescriptor relation, Opcode opcode, Expr expr) {
        if (quantifier == null || relation == null || opcode == null || expr == null) {
            throw new IllegalArgumentException();
        } else if (opcode != Opcode.EQ &&
                   opcode != Opcode.NE &&
                   opcode != Opcode.GT &&
                   opcode != Opcode.GE &&
                   opcode != Opcode.LT &&
                   opcode != Opcode.LE) {
            throw new IllegalArgumentException("invalid operator type");
        }
       
        this.quantifier = quantifier;
        this.relation = relation;
        this.opcode = opcode;
        this.expr = expr;
    }

    public Set getRequiredDescriptors() {
        assert expr != null;
        Set v = expr.getRequiredDescriptors();
        v.add(relation);
        return v;
    }

    public int[] getRepairs(boolean negated) {
	return new int[] {AbstractRepair.MODIFYRELATION};
    }

    public void generate(CodeWriter writer, VarDescriptor vd) {
        // get (first) value for quantifer.relation ... then do comparison with expr... 
        // can this be maybe? i guess if quantifer.relation is empty

        String rv = (VarDescriptor.makeNew("relval")).getSafeSymbol();
        writer.outputline("int " + rv + " = " + relation.getSafeSymbol() + "_hash->get(" + quantifier.getSafeSymbol() + ");");

        // #TBD# deal with maybe (catch exception?)

        VarDescriptor ev = VarDescriptor.makeNew("exprval");
        expr.generate(writer, ev);

        writer.outputline("int " + vd.getSafeSymbol() + " = " + rv + opcode.toString() + ev.getSafeSymbol() + ";");       
    }

}
    






