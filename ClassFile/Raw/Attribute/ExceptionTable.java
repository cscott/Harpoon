package harpoon.ClassFile.Raw;

/** 
 * Each <code>ExceptionTable</code> object describes one exception
 * handler in the <code>code</code> array of an
 * <code>AttributeCode</code>. 
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ExceptionTable.java,v 1.4 1998-07-31 05:51:10 cananian Exp $
 * @see "The Java Virtual Machine Specification, section 4.7.4"
 * @see AttributeCode
 */
class ExceptionTable {
  /** ClassFile in which this attribute information is found. */
  public ClassFile parent;

  /** The values of the two items <code>start_pc</code> and
      <code>end_pc</code> indicate the ranges in the <code>code</code>
      array at which the exception handler is active.  The value of
      <code>start_pc</code> must be a valid index into the
      <code>code</code> array of the opcode of an instruction.  The
      value of <code>start_pc</code> must be less than the value of
      <code>end_pc</code>. <p> The <code>start_pc</code> is
      inclusive. */
  int start_pc;
  /** The values of the two items <code>start_pc</code> and
      <code>end_pc</code> indicate the ranges in the <code>code</code>
      array at which the exception handler is active.  The value of
      <code>end_pc</code> either must be a valid index into the
      <code>code</code> array of the opcode of an instruction, or must
      be equal to <code>code_length</code>, the length of the
      <code>code</code> array.  The value of <code>start_pc</code>
      must be less than the value of <code>end_pc</code>. 
      <p> The <code>end_pc</code> is exclusive. */
  int end_pc;
  /** The value of the <code>handler_pc</code> item indicates the
      start of the exception handler.  The value of the item must be a
      valid index into the <code>code</code> array, must be the index
      of the opcode of an instruction, and must be less than the value
      of the <code>code_length</code> item. */
  int handler_pc;
  /** If the value of the <code>catch_type</code> item is nonzero, it
      must be a valid index into the <code>constant_pool</code>
      table.  the <code>constant_pool</code> entry at that index must
      be a <code>CONSTANT_Class_info</code> structure representing a
      class of exceptions that this exception handler is designated to
      catch.  This class must be the class <code>Throwable</code> of
      one of its subclasses.  The exception handler will be called
      only is the thrown exception is an instance of the given class
      or one of its subclasses.
      <p>
      If the value of the <code>catch_type</code> item is zero, this
      exception handler is called for all exceptions.  This is used to
      implement <code>finally</code>. */
  int catch_type;

  /** Constructor. */
  ExceptionTable(ClassFile parent, ClassDataInputStream in)
       throws java.io.IOException 
  {
    this.parent = parent;

    start_pc = in.read_u2();
    end_pc = in.read_u2();
    handler_pc = in.read_u2();
    catch_type = in.read_u2();
  }

  /** Constructor. */
  public ExceptionTable(ClassFile parent, 
			int start_pc, int end_pc, 
			int handler_pc, int catch_type) {
    this.parent = parent;

    this.start_pc = start_pc;
    this.end_pc = end_pc;
    this.handler_pc = handler_pc;
    this.catch_type = catch_type;
  }

  /** Write to bytecode stream. */
  void write(ClassDataOutputStream out) throws java.io.IOException {
    out.write_u2(start_pc);
    out.write_u2(end_pc);
    out.write_u2(handler_pc);
    out.write_u2(catch_type);
  }

  // convenience
  ConstantClass catch_type() 
  { return (ConstantClass) parent.constant_pool[catch_type]; }
}
