// ConstantValue.java, created Mon Jan 18 22:44:38 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * The <code>ConstantValue</code> interface provides a standard
 * way for constants to return a wrapped version of their values.
 * It is intended for 
 * <code>CONSTANT_Double</code>, <code>CONSTANT_Float</code>, 
 * <code>CONSTANT_Integer</code>, <code>CONSTANT_Long</code>,
 * and <code>CONSTANT_String</code>.
 * It is not applicable to CONSTANT_Class and the like, which do
 * not have an obvious object wrapper.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ConstantValue.java,v 1.2 2002-02-25 21:05:27 cananian Exp $
 * @see ConstantDouble
 * @see ConstantFloat
 * @see ConstantInteger
 * @see ConstantLong
 * @see ConstantString
 */
public abstract class ConstantValue extends Constant {
  protected ConstantValue(ClassFile parent) { super(parent); }
  public abstract Object value();
}
