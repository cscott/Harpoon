package MCC.IR;

public class Opcode {

    private final String name;
    private Opcode(String name) { this.name = name; }
    
    public String toString() { return name; }       

    public static final Opcode ADD = new Opcode("+");
    public static final Opcode SUB = new Opcode("-");
    public static final Opcode MULT = new Opcode("*");
    public static final Opcode DIV = new Opcode("/");

    public static final Opcode GT = new Opcode(">");
    public static final Opcode GE = new Opcode(">=");
    public static final Opcode LT = new Opcode("<");
    public static final Opcode LE = new Opcode("<=");

    public static final Opcode EQ = new Opcode("==");
    public static final Opcode NE = new Opcode("!=");

    public static final Opcode AND = new Opcode("&&");
    public static final Opcode OR = new Opcode("||");
    public static final Opcode NOT = new Opcode("!");

    public static Opcode decodeFromString(String opname) {
        Opcode opcode;

        if (opname.equals("add")) {
            return Opcode.ADD;
        } else if (opname.equals("sub")) {
            return Opcode.SUB;
        } else if (opname.equals("mult")) {
            return Opcode.MULT;
        } else if (opname.equals("div")) {
            return Opcode.DIV;
        } else if (opname.equals("and")) {
            return Opcode.AND;
        } else if (opname.equals("or")) {
            return Opcode.OR;
        } else if (opname.equals("not")) {
            return Opcode.NOT;
        } else if (opname.equals("gt")) {
            return Opcode.GT;
        } else if (opname.equals("ge")) {
            return Opcode.GE;
        } else if (opname.equals("lt")) {
            return Opcode.LT;
        } else if (opname.equals("le")) {
            return Opcode.LE;
        } else if (opname.equals("eq")) {
            return Opcode.EQ;
        } else if (opname.equals("ne")) {
            return Opcode.NE;
        } else {
            return null;
        }
    }

}
