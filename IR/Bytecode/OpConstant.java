// OpConstant.java, created Sun Sep 13 22:49:22 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.IR.RawClass.Constant;
import harpoon.IR.RawClass.ConstantValue;
import harpoon.IR.RawClass.ConstantDouble;
import harpoon.IR.RawClass.ConstantFloat;
import harpoon.IR.RawClass.ConstantInteger;
import harpoon.IR.RawClass.ConstantLong;
import harpoon.IR.RawClass.ConstantString;
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
 * @version $Id: OpConstant.java,v 1.4 2003-10-03 17:41:47 cananian Exp $
 * @see Operand
 * @see Instr
 * @see harpoon.IR.RawClass.ConstantDouble
 * @see harpoon.IR.RawClass.ConstantFloat
 * @see harpoon.IR.RawClass.ConstantInteger
 * @see harpoon.IR.RawClass.ConstantLong
 * @see harpoon.IR.RawClass.ConstantString
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
    Linker l = type.getLinker(); // use a consistent linker, whichever that is.
    if (!Boolean.getBoolean("harpoon.runtime1.minilib")) {
	// skip this check for minilib, because it doesn't necessarily have
	// all the wrapper classes necessary for type.getWrapper() to work.
    HClass check = l.forClass(value.getClass());
    if ((!type.isPrimitive() && check!=type) ||
	( type.isPrimitive() && check!=type.getWrapper(l)))
      throw new Error("value doesn't match type of OpConstant: " + 
		      type + "/" + check);
    }
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
	this.type=parent.linker.forName("java.lang.String");
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
    if (getType().getName().equals("java.lang.String"))
      return "(String)\""+Util.escape(getValue().toString())+"\"";
    return "("+getType().getName()+")"+getValue().toString();
  }
}
