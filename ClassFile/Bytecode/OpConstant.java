package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;

/**
 * <code>OpConstant</code> represents a constant operands of a java bytecode
 * instruction.  This would typically be taken from the 
 * <code>constant_pool</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpConstant.java,v 1.1 1998-08-03 23:22:15 cananian Exp $
 * @see Operand
 * @see Instr
 */
public class OpConstant extends Operand {
  Object value;
  HClass type;
  /** Make a new <code>OpConstant</code> with the specified value and type. */
  public OpConstant(Object value, HClass type) {
    this.value = value;  this.type=type;
    // assert that value matches type.
    HClass check = HClass.forClass(value.getClass());
    if ((type.isPrimitive() && check!=type) ||
	(!type.isPrimitive()&& check!=type.getWrapper()))
      throw new Error("value doesn't match type of OpConstant.");
  }
  /** Return the value of this <code>Operand</code>. */
  public Object getValue() { return value; }
  /** Return the <code>HClass</code> type of this <Code>Operand</code>. */
  public HClass getType()  { return type; }

  /** Return a human-readable representation of this OpConstant. */
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
