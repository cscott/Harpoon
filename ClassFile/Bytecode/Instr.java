package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;

/**
 * <code>Bytecode.Instr</code> is the base type for the specific
 * bytecode instruction classes.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Instr.java,v 1.3 1998-08-03 06:54:53 cananian Exp $
 * @see InGen
 * @see InCti
 * @see InMerge
 * @see Code
 */
public abstract class Instr implements HCodeElement {
  String sourcefile;
  int linenumber;
  int id;
  /** Constructor. */
  protected Instr(String sourcefile, int linenumber) {
    this.sourcefile = sourcefile;
    this.linenumber = linenumber;
    this.id = next_id++;
  }
  static int next_id = 0;
  /** Get the original source file name that this bytecode instruction 
   *  is derived from. */
  public String getSourceFile() { return sourcefile; }
  /** Get the line in the original source file that this bytecode 
   *  instruction can be traced to. */
  public int getLineNumber() { return linenumber; }
  /** Returns a unique numeric identifier for this element. */
  public int getID() { return id; }

  /** 
   * Return the type pushed on the stack by this instruction, 
   * or <code>null</code>.
   * The stack manipulation instructions
   * <code>pop</code>, <code>pop2</code>, <code>dup</code>, <code>dup2</code>,
   * <code>dup_x1</code>, <code>dup2_x1</code>, 
   * <code>dup_x2</code>, <code>dup2_x2</code>, and <code>swap</code>
   * do not fit intuitively into this framework; they should return
   * <code>null</code>.
   */
  public HClass getResultType() { return null; }
}
