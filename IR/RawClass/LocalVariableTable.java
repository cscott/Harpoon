// LocalVariableTable.java, created Mon Jan 18 22:44:38 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/** 
 * Each object indicates a range of <code>code</code> array offsets
 * within which a local variable has a value.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LocalVariableTable.java,v 1.2 2002-02-25 21:05:27 cananian Exp $
 * @see AttributeLocalVariableTable
 */
public class LocalVariableTable {
  /** ClassFile in which this attribute information is found. */
  protected ClassFile parent;

  /** The given local variable must have a value at indices into the
      <code>code</code> array in the closed interval
      <code>[start_pc, start_pc + length]</code>.
      <p> The value of <code>start_pc</code> must be a valid index
      into the <code>code</code> array of this <code>Code</code>
      attribute of the opcode of an instruction. */
  public int start_pc;
  /** The given local variable must have a value at indices into the
      <code>code</code> array in the closed interval
      <code>[start_pc, start_pc + length]</code>.
      <p> The value of <code>start_pc+length</code> must be either a 
      valid index into the <code>code</code> array of this
      <code>Code</code> attribute of the opcode of an instruction,
      or the first index beyond the end of that <code>code</code>
      array.  */
  public int length;
  /** The value of the <code>name_index</code> item must be a valid
	index into the <code>constant_pool</code> table.  The
	<code>constant_pool</code> entry at that index must contain a
	<code>CONSTANT_Utf8_info</codE> structure representing a valid
	Java local variable name stored as a simple name. */
  public int name_index;
  /** The value of the <code>descriptor_index</code> item must be a
	valid index into the <code>constant_pool</code> table.  The
	<code>constant_pool</code> entry at that index must contain a
	<code>CONSTANT_Utf8_info</code> structure representing a valid
	descriptor for a Java local variable.  Java local variable
	descriptors have the same form as field descriptors. */
  public int descriptor_index;
  /** The given local variable must be at <code>index</code> in its
	method's local variables.  If the local variable at
	<code>index</code> is a two-word type (<code>double</code> or
	<code>long</code>), it occupies both <code>index</code> and
	<code>index+1</code>. */
  public int index;

  /** Constructor. */
  LocalVariableTable(ClassFile parent, ClassDataInputStream in) 
    throws java.io.IOException {
    this.parent = parent;

    this.start_pc = in.read_u2();
    this.length   = in.read_u2();
      
    this.name_index       = in.read_u2();
    this.descriptor_index = in.read_u2();
      
    this.index = in.read_u2();
  }
  /** Constructor. */
  public LocalVariableTable(ClassFile parent,
			    int start_pc, int length,
			    int name_index, int descriptor_index,
			    int index) {
    this.parent = parent;

    this.start_pc = start_pc;
    this.length = length;
    this.name_index = name_index;
    this.descriptor_index = descriptor_index;
    this.index = index;
  }
  /** Writes to bytecode stream. */
  public void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(start_pc);
    out.write_u2(length);

    out.write_u2(name_index);
    out.write_u2(descriptor_index);
      
    out.write_u2(index);
  }
  // convenience functions.
  public ConstantUtf8 name_index()
  { return (ConstantUtf8) parent.constant_pool[name_index]; }
  public ConstantUtf8 descriptor_index()
  { return (ConstantUtf8) parent.constant_pool[descriptor_index]; }

  public String name() { return name_index().val; }
  public String descriptor() { return descriptor_index().val; }

  public int end_pc() { return start_pc + length; }
  
  /** Pretty-print the contents of this attribute.
   *  @param indent the indentation level to use.
   */
  public void print(java.io.PrintWriter pw, int indent) {
    int in=indent;
    indent(pw, in, "Valid in pc=["+start_pc+","+(start_pc+length)+"]");
    indent(pw, in, "Variable index: "+index);
    indent(pw, in, "Variable name:  "+name()+" {"+name_index+"}");
    indent(pw, in, "Descriptor:     "+descriptor()+" {"+descriptor_index+"}");
  }
  private static void indent(java.io.PrintWriter pw, int indent, String s) {
    ClassFile.indent(pw,indent,s);
  }
}
