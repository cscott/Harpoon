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

}
