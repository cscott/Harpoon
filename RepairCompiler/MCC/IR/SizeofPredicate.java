package MCC.IR;

import java.util.*;

public class SizeofPredicate extends Predicate {
   
    SetExpr setexpr;
    Opcode opcode;
    IntegerLiteralExpr cardinality;

    public SizeofPredicate(SetExpr setexpr, Opcode opcode, IntegerLiteralExpr cardinality) {
        if (setexpr == null || opcode == null || cardinality == null) {
            throw new IllegalArgumentException();
        } else if (opcode != Opcode.EQ &&
                   opcode != Opcode.GE &&
                   opcode != Opcode.LE) {
            throw new IllegalArgumentException("invalid operator type");
        }

        this.setexpr = setexpr;
        this.opcode = opcode;
        this.cardinality = cardinality;
    }

    public Set getRequiredDescriptors() {
        assert setexpr != null;
        Set v = setexpr.getRequiredDescriptors();
        // v.add(cardinality.getRequiredDescriptors()); // will be null
        return v;
    }
     
    public void generate(CodeWriter writer, VarDescriptor dest) {

        // #TBD#: generate the set which should generate a name (variable) which is the pointer 
        // to a hash table iterator that we can dereference get something blah blah blah

        VarDescriptor size = VarDescriptor.makeNew("size");
        setexpr.generate_size(writer, size);
        
        writer.outputline("int " + dest.getSafeSymbol() + " = " + size.getSafeSymbol() + opcode.toString() + cardinality.getValue() + ";");                       
    }
       
}
    





