// OpLocalVariable.java, created Sun Sep 13 22:49:23 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Bytecode;

/**
 * <code>OpLocalVariable</code> represents an operand of a java bytecode
 * instruction which indicates a local variable index.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OpLocalVariable.java,v 1.3 2002-02-25 21:04:17 cananian Exp $
 */
public final class OpLocalVariable extends Operand {
  final int index;
  /** Create an <code>OpLocalVariable</code> given the index of the 
   *  local variable referenced. */
  public OpLocalVariable(int local_variable_index) {
    this.index = local_variable_index;
  }
  /** Get the local variable index */
  public int getIndex() { return index; }
  /** Return a human-readable representation of this Operand. */
  public String toString() { return "lv_"+index; }
}
