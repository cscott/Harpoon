package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;

/**
 * <code>Operand</code> represents the operands of a java bytecode
 * instruction.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Operand.java,v 1.4 1998-08-03 22:14:52 cananian Exp $
 * @see Instr
 * @see InGen
 */
public class Operand {
  Object value;
  HClass type;
  /** Make a new <code>Operand</code> with the specified value and type. */
  public Operand(Object value, HClass type) {
    this.value = value;  this.type=type;
    // assert that value matches type.
    HClass check = HClass.forClass(value.getClass());
    if ((type.isPrimitive() && check!=type) ||
	(!type.isPrimitive()&& check!=type.getWrapper()))
      throw new Error("value doesn't match type of Operand.");
  }
  /** Return the value of this <code>Operand</code>. */
  public Object getValue() { return value; }
  /** Return the <code>HClass</code> type of this <Code>Operand</code>. */
  public HClass getType()  { return type; }

  /** Return a human-readable representation of this Operand. */
  public String toString() {
    if (getType()==HClass.forName("java.lang.String"))
      return "(String) \""+escape(getValue().toString())+"\"";
    return "("+getType().getName()+") "+getValue().toString();
  }
  // copied from Constant.Utf8
  static String escape(String str) {
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<str.length(); i++) {
      char c = str.charAt(i);
      if (Character.isISOControl(c)) {
	String hexval=Integer.toHexString((int)c);
	while(hexval.length()<4) hexval="0"+hexval;
	sb.append('\\'); sb.append('u');
	sb.append(hexval);
      }
      else sb.append(c);
    }
    return sb.toString();
  }
}
