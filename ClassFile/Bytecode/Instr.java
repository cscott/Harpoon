package harpoon.ClassFile.Bytecode;

import harpoon.ClassFile.*;

/**
 * <code>Bytecode.Instr</code> is the base type for the specific
 * bytecode instruction classes.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Instr.java,v 1.1 1998-08-03 01:37:27 cananian Exp $
 * @see Code
 */
public abstract class Instr implements HCodeElement {
  String sourcefile;
  int linenumber;
  /** Constructor. */
  protected Instr(String sourcefile, int linenumber) {
    this.sourcefile = sourcefile;
    this.linenumber = linenumber;
  }
  /** Get the original source file name that this bytecode instruction 
   *  is derived from. */
  public String getSourceFile() { return sourcefile; }
  /** Get the line in the original source file that this bytecode 
   * instruction can be traced to. */
  public int getLineNumber() { return linenumber; }
}
