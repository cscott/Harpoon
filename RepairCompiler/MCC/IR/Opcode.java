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
            opcode = Opcode.ADD;
        } else if (opname.equals("sub")) {
            opcode = Opcode.SUB;
        } else if (opname.equals("mult")) {
            opcode = Opcode.MULT;
        } else if (opname.equals("div")) {
            opcode = Opcode.DIV;
        } else if (opname.equals("and")) {
            opcode = Opcode.AND;
        } else if (opname.equals("or")) {
            opcode = Opcode.OR;
        } else if (opname.equals("not")) {
            opcode = Opcode.NOT;
        } else if (opname.equals("gt")) {
            opcode = Opcode.GT;
        } else if (opname.equals("ge")) {
            opcode = Opcode.GE;
        } else if (opname.equals("lt")) {
            opcode = Opcode.LT;
        } else if (opname.equals("le")) {
            opcode = Opcode.LE;
        } else if (opname.equals("eq")) {
            opcode = Opcode.EQ;
        } else if (opname.equals("ne")) {
            opcode = Opcode.NE;
        } else {
            return null;
        }
    }

}
