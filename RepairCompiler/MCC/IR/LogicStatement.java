package MCC.IR;

import java.util.*;

public class LogicStatement {

    public static final Operation AND = new Operation("AND");
    public static final Operation OR = new Operation("OR");
    public static final Operation NOT = new Operation("NOT");

    public String name() {
	if (op==NOT)
	    return "!"+left.name();
	String name=left.name();
	name+=" "+op.toString()+" ";
	if (right!=null)
	    name+=right.name();
	return name;
    }

    public Set getInversedRelations() {
        if (left == null) {
            throw new IRException();
        }
        Set set = left.getInversedRelations();
        if (right != null) {
            set.addAll(right.getInversedRelations());
        }
        return set;
    }
    
    public DNFConstraint constructDNF() {
	if (op==AND) {
	    DNFConstraint leftd=left.constructDNF();
	    DNFConstraint rightd=right.constructDNF();
	    return leftd.and(rightd);
	} else if (op==OR) {
	    DNFConstraint leftd=left.constructDNF();
	    DNFConstraint rightd=right.constructDNF();
	    return leftd.or(rightd);
	} else if (op==NOT) {
	    DNFConstraint leftd=left.constructDNF();
	    return leftd.not();
	} else throw new Error();
    }

    public static class Operation {
        private final String name;
        private Operation(String opname) { name = opname; }
        public String toString() { return name; }
    }

    Operation op;
    LogicStatement left;
    LogicStatement right;

    public LogicStatement(Operation op, LogicStatement left, LogicStatement right) {
        if (op == NOT) {
            throw new IllegalArgumentException("Must be a AND or OR expression.");
        }

        this.op = op;
        this.left = left;
        this.right = right;
    }

    public LogicStatement(Operation op, LogicStatement left) {
        if (op != NOT) {
            throw new IllegalArgumentException("Must be a NOT expression.");
        }

        this.op = op;
        this.left = left;
        this.right = null;
    }

    protected LogicStatement() {
        this.op = null;
        this.left = null;
        this.right = null;
    }

    public Set getRequiredDescriptors() {
        Set v = left.getRequiredDescriptors();
        if (right != null) {
            v.addAll(right.getRequiredDescriptors());
        }
        return v;
    }

    public void generate(CodeWriter writer, VarDescriptor dest) {

        writer.outputline("int " + dest.getSafeSymbol() + ";");

        if (op == NOT) {

            VarDescriptor leftd = VarDescriptor.makeNew("leftboolean");
            left.generate(writer, leftd);

            writer.outputline("// 3-valued NOT");
            writer.outputline("if (!maybe)");
            writer.startblock();
            writer.outputline(dest.getSafeSymbol() + " =  !" + leftd.getSafeSymbol() + ";");
            writer.endblock();

        } else { // two operands

            VarDescriptor leftd = VarDescriptor.makeNew("leftboolean");
            String lm = (VarDescriptor.makeNew("leftmaybe")).getSafeSymbol();
            left.generate(writer, leftd);
            writer.outputline("int " + lm + " = maybe;");
            
            VarDescriptor rightd = VarDescriptor.makeNew("rightboolean");
            String rm = (VarDescriptor.makeNew("rightmaybe")).getSafeSymbol();
            assert right != null;
            right.generate(writer, rightd);
            writer.outputline("int " + rm + " = maybe;");

            String l = leftd.getSafeSymbol();
            String r = rightd.getSafeSymbol();
            
            if (op == AND) {

                /* 
                 * 3-value AND LOGIC
                 *
                 * LRLR
                 * MM    M O
                 * ----  ---
                 * 0000  0 0
                 * 0001  0 0
                 * 0010  0 0
                 * 0011  0 1
                 * 0100  0 0
                 * 0101  0 0
                 * 0110  1 X
                 * 0111  1 X
                 * 1000  0 0
                 * 1001  1 X
                 * 1010  0 0
                 * 1011  1 X
                 * 1100  1 X
                 * 1101  1 X
                 * 1110  1 X
                 * 1111  1 X
                 *
                 * M = (L*RM) + (R*LM) + (LM*RM)                 
                 * O = (L*R)
                 */
               
                // maybe = (l && rm) || (r && lm) || (lm && rm)
                writer.outputline("maybe = (" + l + " && " + rm + ") || (" + r + " && " + lm + ") || (" + lm + " && " + rm + ");");
                writer.outputline(dest.getSafeSymbol() + " = " + l + " && " + r + ";");

            } else if (op == OR) {

                /* 
                 * 3-value OR LOGIC
                 *
                 * LRLR
                 * MM    M O
                 * ----  ---
                 * 0000  0 0
                 * 0001  0 1
                 * 0010  0 1
                 * 0011  0 1
                 * 0100  1 X
                 * 0101  1 X
                 * 0110  0 1
                 * 0111  0 1
                 * 1000  1 X
                 * 1001  0 1
                 * 1010  1 X
                 * 1011  0 1
                 * 1100  1 X
                 * 1101  1 X
                 * 1110  1 X
                 * 1111  1 X
                 *
                 * M = (!L*RM) + (!R*LM) + (LM*RM)
                 * O = L+R
                 */

                // maybe = (!l && rm) || (!r && lm) || (lm && rm)
                writer.outputline("maybe = (!" + l + " && " + rm + ") || (!" + r + " && " + lm + ") || (" + lm + " && " + rm + ");");
                writer.outputline(dest.getSafeSymbol() + " = " + l + " || " + r + ";");
            } else {
                throw new IRException();
            }        
        }
    }   
}













