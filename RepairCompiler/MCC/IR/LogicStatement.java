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
   
}
