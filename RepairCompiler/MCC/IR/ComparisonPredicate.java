package MCC.IR;

import java.util.*;

public class ComparisonPredicate extends Predicate {

    public static final Comparison GT = new Comparison("GT");
    public static final Comparison GE = new Comparison("GE");
    public static final Comparison LT = new Comparison("LT");
    public static final Comparison LE = new Comparison("LE");
    public static final Comparison EQ = new Comparison("EQ");
    private static final Comparison ALL[] = { GT, GE, LT, LE, EQ };               
    
    public static class Comparison {
        private final String name;
        private Comparison(String name) { this.name = name; }
        public String toString() { return name; }
        public static Comparison fromString(String name) {
            if (name == null) {
                throw new NullPointerException();
            }

            for (int i = 0; i < ALL.length; i++) {
                if (name.equalsIgnoreCase(ALL[i].toString())) {
                    return ALL[i];
                }
            }

            throw new IllegalArgumentException("Input not a valid comparison.");
        }                
    }
    
    Comparison comparison;
    Expr left, right;

    public ComparisonPredicate(String comparison, Expr left, Expr right) {
        this.comparison = Comparison.fromString(comparison);
        this.left = left;
        this.right = right;
    }

    public Set getRequiredDescriptors() {
        assert left != null;
        assert right != null;
        Set v = left.getRequiredDescriptors();
        v.addAll(right.getRequiredDescriptors());
        return v;
    }
            
}
    
