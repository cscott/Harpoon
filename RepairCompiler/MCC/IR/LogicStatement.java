package MCC.IR;

import java.util.*;

public class LogicStatement {

    public static final Operation AND = new Operation("AND");
    public static final Operation OR = new Operation("OR");
    public static final Operation NOT = new Operation("NOT");
    
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
        VarDescriptor leftd = VarDescriptor.makeNew("leftboolean");
        left.generate(writer, leftd);        
        VarDescriptor rightd = VarDescriptor.makeNew("rightboolean");

        if (right != null) {
            right.generate(writer, rightd);
        }

        if (op == NOT) {
            writer.outputline("// 3-valued NOT");
            writer.outputline("int " + dest.getSafeSymbol() + " = " + leftd.getSafeSymbol() + " == -1 ? -1 : !" + leftd.getSafeSymbol() + ";");
        } else if (op == AND) {
            writer.outputline("// 3-valued AND");
            // !a || !b ? 0 : either maybe ? maybe : AND;
            String a = leftd.getSafeSymbol();
            String b = rightd.getSafeSymbol();
            String expr = a + " == 0 || " + b + " == 0 ? 0 : " + a + " == -1 || " + b + " == -1 ? -1 : 1;";
            writer.outputline("int " + dest.getSafeSymbol() + " = " + expr);
        } else if (op == OR) {
            writer.outputline("// 3-valued OR");
            // a == 1 || b == 1 ? 1 : a == -1 || b == -1 ? -1 : 0;
            String a = leftd.getSafeSymbol();
            String b = rightd.getSafeSymbol();
            String expr = a + " == 1 || " + b + " == 1 ? 1 : " + a + " == -1 || " + b + " == -1 ? -1 : 0;";
            writer.outputline("int " + dest.getSafeSymbol() + " = " + expr);
        } else {
            throw new IRException();
        }        
    }
   
}













