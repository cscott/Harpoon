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

    public int[] getRepairs(boolean negated) {
	if (setexpr instanceof ImageSetExpr) {
	    if (opcode==Opcode.EQ)
		return new int[] {AbstractRepair.ADDTORELATION,
				      AbstractRepair.REMOVEFROMRELATION};
	    if (((opcode==Opcode.GE)&&!negated)||
		((opcode==Opcode.LE)&&negated))
		return new int[]{AbstractRepair.ADDTORELATION};
	    else
		return new int[]{AbstractRepair.REMOVEFROMRELATION};
	} else {
	    if (opcode==Opcode.EQ)
		return new int[] {AbstractRepair.ADDTOSET,
				      AbstractRepair.REMOVEFROMSET};

	    if (((opcode==Opcode.GE)&&!negated)||
		((opcode==Opcode.LE)&&negated))
		return new int[] {AbstractRepair.ADDTOSET};
	    else 
		return new int[] {AbstractRepair.REMOVEFROMSET};
	}
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
    





