// OpConstant.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.Constant.*;
import harpoon.Util.Util;

/**
 * <code>OpConstant</code> represents a constant operand of a java bytecode
 * instruction.  This would typically be taken from the 
 * <code>constant_pool</code>.<p>
 * <code>OpConstant</code> represents constant pool entries of type
 * <code>CONSTANT_Double</code>, <code>CONSTANT_Float</code>,
 * <code>CONSTANT_Integer</code>, <code>CONSTANT_Long</code>,
 * and <code>CONSTANT_String</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpConstant.java,v 1.2.2.1 1998-12-21 21:21:28 cananian Exp $
 * @see Operand
 * @see Instr
 * @see harpoon.ClassFile.Raw.Constant.ConstantDouble
 * @see harpoon.ClassFile.Raw.Constant.ConstantFloat
 * @see harpoon.ClassFile.Raw.Constant.ConstantInteger
 * @see harpoon.ClassFile.Raw.Constant.ConstantLong
 * @see harpoon.ClassFile.Raw.Constant.ConstantString
 */
public final class OpConstant extends Operand {
  final Object value;
  final HClass type;
  /** Make a new <code>OpConstant</code> with the specified value and type. */
  public OpConstant(Object value, HClass type) {
    this.value = value;  this.type=type; check();
  }
  private void check() {
    // assert that value matches type.
    HClass check = HClass.forClass(value.getClass());
    if ((!type.isPrimitive() && check!=type) ||
	( type.isPrimitive() && check!=type.getWrapper()))
      throw new Error("value doesn't match type of OpConstant: " + 
		      type + "/" + check);
  }
  /** Make a new <code>OpConstant</code> from a 
   *  <code>constant_pool</code> entry. */
  public OpConstant(Code parent, int constant_pool_index) {
    Constant c = parent.getConstant(constant_pool_index);
    if (c instanceof ConstantValue) {
      this.value=((ConstantValue)c).value();
      if (c instanceof ConstantDouble)       this.type=HClass.Double;
      else if (c instanceof ConstantFloat)   this.type=HClass.Float;
      else if (c instanceof ConstantInteger) this.type=HClass.Int;
      else if (c instanceof ConstantLong)    this.type=HClass.Long;
      else if (c instanceof ConstantString)  
	this.type=HClass.forName("java.lang.String");
      else throw new Error("Unknown ConstantValue type: "+c);
    } else throw new Error("Unknown constant pool entry: "+c);
    check();
  }
  /** Return the value of this <code>Operand</code>. */
  public Object getValue() { return value; }
  /** Return the <code>HClass</code> type of this <Code>Operand</code>. */
  public HClass getType()  { return type; }

  /** Return a human-readable representation of this OpConstant. */
  public String toString() {
    if (getType()==HClass.forName("java.lang.String"))
      return "(String)\""+Util.escape(getValue().toString())+"\"";
    return "("+getType().getName()+")"+getValue().toString();
  }
}
