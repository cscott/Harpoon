// LineNumberTable.java, created Mon Jan 18 22:44:38 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/** 
 * Each object indicates that the line number in the original Java
 * source file changes at a given point in the <code>code</code>
 * array. 
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LineNumberTable.java,v 1.2 2002-02-25 21:05:27 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7.6"
 * @see AttributeLineNumberTable
 */ 
public class LineNumberTable {
  /** The value of the <code>start_pc</code> item must indicate the
      index into the <code>code</code> array at which the code for a
      new line in the original Java source file begins.  The value
      of <code>start_pc</code> must be less than the value of the
      <code>code_length</code> item of the <code>Code</code>
      attribute of which this <code>LineNumberTable</code> is an
      attribute. */
  public int start_pc;
  /** The value of the <code>line_number</code> item must give the
      corresponding line number in the original Java source file. */
  public int line_number;

  /** Constructor. */
  LineNumberTable(ClassDataInputStream in) throws java.io.IOException {
    start_pc = in.read_u2();
    line_number = in.read_u2();
  }
  /** Constructor. */
  public LineNumberTable(int start_pc, int line_number) {
    this.start_pc = start_pc;
    this.line_number = line_number;
  }
  /** Writes to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(start_pc);
    out.write_u2(line_number);
  }
  /** Human-readable string: */
  public String toString() {
    return "line "+line_number+" starts at pc="+start_pc;
  }
}
