package harpoon.ClassFile.Bytecode;

import harpoon.Util.Util;

/**
 * <code>InCti</code> is used for control-transfer instructions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InCti.java,v 1.2 1998-08-03 07:17:55 cananian Exp $
 * @see Instr
 */
public class InCti extends Instr {
  byte opcode;

  public Instr prev, next[];

  public InCti(String sourcefile, int linenumber,
	       byte opcode /*, ... */) { // FIXME
    super(sourcefile, linenumber);
    this.opcode=opcode;
  }

  /** Return a list of all the <code>Instr</code>s that can precede
   *  this one.  Always an array of length one. */
  public Instr[] prev() { return new Instr[] { prev }; }
  /** Return a list of all the possible <code>Instr</code>s that may
   *  succeed this one. */
  public Instr[] next() { return (Instr[]) Util.copy(next); }
}
