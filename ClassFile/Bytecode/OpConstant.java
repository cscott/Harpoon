package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;
import harpoon.ClassFile.Raw.Constant.*;

/**
 * <code>OpConstant</code> represents a constant operand of a java bytecode
 * instruction.  This would typically be taken from the 
 * <code>constant_pool</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpConstant.java,v 1.4 1998-08-04 02:14:45 cananian Exp $
 * @see Operand
 * @see Instr
 */
public class OpConstant extends Operand {
  Object value;
  HClass type;
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
		      type + "/" + check+":"+(check==type)+":"+type.isPrimitive());
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
      return "(String)\""+escape(getValue().toString())+"\"";
    return "("+getType().getName()+")"+getValue().toString();
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
