package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;

/**
 * <code>Operand</code> represents the operands of a java bytecode
 * instruction.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Operand.java,v 1.2 1998-08-03 22:06:17 cananian Exp $
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
	(!type.isPrimitive()&& check!=HClass.getWrapper(type)))
      throw new Error("value doesn't match type of Operand.");
  }
  /** Return the value of this <code>Operand</code>. */
  public Object getValue() { return value; }
  /** Return the <code>HClass</code> type of this <Code>Operand</code>. */
  public HClass getType()  { return type; }
}
